from rest_framework import serializers
from .models import Pose, Routine, TrackingDetail, ExerciseDetail, RoutineConfig, RoutineExercise, RepData, RoutineComponentData, RoutineData, Patient

class PoseSerializer(serializers.ModelSerializer):
    class Meta:
        model = Pose
        fields = ['id', 'landmarks', 'created_at']


class RoutineSerializer(serializers.ModelSerializer):
    poses = PoseSerializer(many=True)

    class Meta:
        model = Routine
        fields = ['id', 'name', 'description', 'poses', 'created_at']

    def create(self, validated_data):
        poses_data = validated_data.pop('poses')
        routine = Routine.objects.create(**validated_data)
        for pose_data in poses_data:
            pose = Pose.objects.create(**pose_data)
            routine.poses.add(pose)
        return routine

class TrackingDetailSerializer(serializers.ModelSerializer):
    class Meta:
        model = TrackingDetail
        fields = '__all__'

class ExerciseDetailSerializer(serializers.ModelSerializer):
    default_tracking_details = TrackingDetailSerializer(many=True)
    rep_tracking = TrackingDetailSerializer()  # Ensure rep_tracking returns full object

    class Meta:
        model = ExerciseDetail
        fields = '__all__'

    def create(self, validated_data):
        tracking_details_data = validated_data.pop('default_tracking_details')
        rep_tracking_data = validated_data.pop('rep_tracking')
        exercise = ExerciseDetail.objects.create(**validated_data, rep_tracking=TrackingDetail.objects.create(**rep_tracking_data))
        exercise.default_tracking_details.set([
            TrackingDetail.objects.create(**detail) for detail in tracking_details_data
        ])
        return exercise

# Desired READ behaviour 
# class RoutineExerciseSerializer(serializers.ModelSerializer):
#     exercise = ExerciseDetailSerializer(read_only=True)
#     custom_tracking_details = TrackingDetailSerializer(many=True, required=False)

#     class Meta:
#         model = RoutineExercise
#         fields = ['exercise', 'reps', 'custom_tracking_details']

# Desired POST behaviour
# class RoutineExerciseSerializer(serializers.ModelSerializer):
#     exercise = serializers.PrimaryKeyRelatedField(queryset=ExerciseDetail.objects.all())  # Accept exercise ID
#     custom_tracking_details = TrackingDetailSerializer(many=True, required=False)

#     class Meta:
#         model = RoutineExercise
#         fields = ['exercise', 'reps', 'custom_tracking_details']

# class RoutineExerciseSerializer(serializers.ModelSerializer):
#     exercise_id = serializers.PrimaryKeyRelatedField(
#         queryset=ExerciseDetail.objects.all(), write_only=True  # Accept ID for writes
#     )
#     exercise = ExerciseDetailSerializer(read_only=True)  # Send full object on read
#     custom_tracking_details = TrackingDetailSerializer(many=True, required=False)

#     class Meta:
#         model = RoutineExercise
#         fields = ['exercise', 'exercise_id', 'reps', 'custom_tracking_details']

class RoutineExerciseSerializer(serializers.ModelSerializer):
    exercise_id = serializers.PrimaryKeyRelatedField(
        queryset=ExerciseDetail.objects.all(), source="exercise", write_only=True
    )
    exercise = ExerciseDetailSerializer(read_only=True)  # Full object on read
    custom_tracking_details = TrackingDetailSerializer(many=True, required=False)

    class Meta:
        model = RoutineExercise
        fields = ['exercise_id', 'exercise', 'reps', 'custom_tracking_details']

class RoutineConfigSerializer(serializers.ModelSerializer):
    exercises = RoutineExerciseSerializer(source='routineexercise_set', many=True)

    class Meta:
        model = RoutineConfig
        fields = ['id', 'name', 'exercises', 'injury']

    def get_exercises(self, obj):
        routine_exercises = RoutineExercise.objects.filter(routine=obj).select_related('exercise').prefetch_related('custom_tracking_details')
        return RoutineExerciseSerializer(routine_exercises, many=True).data
    
    def create(self, validated_data):
        exercises_data = validated_data.pop('routineexercise_set', [])  # Extract exercises
        routine = RoutineConfig.objects.create(**validated_data)  # Create RoutineConfig

        for exercise_data in exercises_data:
            RoutineExercise.objects.create(routine=routine, **exercise_data)  # Add exercises

        return routine

    def update(self, instance, validated_data):
        exercises_data = validated_data.pop('routineexercise_set', [])  # Extract exercises

        # Update routine fields
        instance.name = validated_data.get('name', instance.name)
        instance.injury = validated_data.get('injury', instance.injury)
        instance.save()

        # Delete old exercises and recreate them (simpler than partial updates)
        instance.routineexercise_set.all().delete()
        for exercise_data in exercises_data:
            RoutineExercise.objects.create(routine=instance, **exercise_data)

        return instance

# class RepDataSerializer(serializers.ModelSerializer):
#     class Meta:
#         model = RepData
#         fields = '__all__'

# class RoutineComponentDataSerializer(serializers.ModelSerializer):
#     rep_data = RepDataSerializer(many=True)

#     class Meta:
#         model = RoutineComponentData
#         fields = '__all__'

# class RoutineDataSerializer(serializers.ModelSerializer):
#     routine_component_data = RoutineComponentDataSerializer(many=True)

#     class Meta:
#         model = RoutineData
#         fields = '__all__'

class PoseSerializer(serializers.ModelSerializer):
    """Serializer for Pose model."""
    class Meta:
        model = Pose
        fields = '__all__'

    

class RepDataSerializer(serializers.ModelSerializer):
    """Serializer for RepData, allowing creation of new RepData with nested poses."""
    poses = PoseSerializer(many=True)  # Expect full Pose objects in the JSON payload

    class Meta:
        model = RepData
        fields = '__all__'

    def create(self, validated_data):
        """Custom create function for RepData with nested poses."""
        pose_data_list = validated_data.pop('poses', [])
        rep_data_instance = RepData.objects.create(**validated_data)  # Create RepData object

        for pose_data in pose_data_list:
            pose_instance = Pose.objects.create(**pose_data)  # Create Pose objects from JSON
            rep_data_instance.poses.add(pose_instance)  # Link Pose objects to RepData instance

        return rep_data_instance
    
    def to_representation(self, instance):
        """Customize serialization to include poses only when a specific RoutineData is requested."""
        data = super().to_representation(instance)
        
        # Get request from context
        request = self.context.get('request', None)
        
        if request and request.parser_context:
            # Check if this is a retrieve action (single instance request)
            view = request.parser_context.get('view', None)
            if view and getattr(view, 'action', None) != 'retrieve':
                data.pop('poses', None)  # Remove poses for list requests

        return data


class RoutineComponentDataSerializer(serializers.ModelSerializer):
    """Serializer for RoutineComponentData with nested RepData."""
    exercise_detail = serializers.PrimaryKeyRelatedField(
        queryset=ExerciseDetail.objects.all()
    )  # Reference ExerciseDetail by ID
    rep_data = RepDataSerializer(many=True)  # Accept full RepData objects

    class Meta:
        model = RoutineComponentData
        fields = '__all__'

    def create(self, validated_data):
        """Custom create function for RoutineComponentData with nested RepData."""
        rep_data_list = validated_data.pop('rep_data', [])
        routine_component_data_instance = RoutineComponentData.objects.create(**validated_data)

        for rep_data in rep_data_list:
            rep_data_instance = RepDataSerializer().create(rep_data)  # Use RepDataSerializer to create objects
            routine_component_data_instance.rep_data.add(rep_data_instance)  # Link RepData to RoutineComponentData

        return routine_component_data_instance

    def to_representation(self, instance):
        """Ensure RepDataSerializer receives context."""
        data = super().to_representation(instance)
        
        data['rep_data'] = RepDataSerializer(
            instance.rep_data.all(), many=True, context=self.context
        ).data
        
        return data

class RoutineDataSerializer(serializers.ModelSerializer):
    """Serializer for RoutineData with full routine_component_data objects."""
    routineConfig_id = serializers.PrimaryKeyRelatedField(
        queryset=RoutineConfig.objects.all(),
        source="routine_config",  # Map to the routine_config field on the model
        write_only=True  # For deserialization only
    )
    routine_config = RoutineConfigSerializer(read_only=True)  # Send full object on read
    routine_component_data = RoutineComponentDataSerializer(many=True)  # Accept full nested objects

    class Meta:
        model = RoutineData
        fields = ['id', 'routine_config', 'routineConfig_id', 'routine_component_data', 'created_at']

    def create(self, validated_data):
        """Custom create function for RoutineData with nested RoutineComponentData."""
        routine_component_data_list = validated_data.pop('routine_component_data')
        routine_data_instance = RoutineData.objects.create(**validated_data)
        
        for component_data in routine_component_data_list:
            component_instance = RoutineComponentDataSerializer().create(component_data)
            routine_data_instance.routine_component_data.add(component_instance)  # Link RoutineComponentData

        return routine_data_instance

    def update(self, instance, validated_data):
        """Custom update to handle nested routine_component_data."""
        routine_component_data = validated_data.pop('routine_component_data', None)
        instance.routine_config = validated_data.get('routine_config', instance.routine_config)
        instance.save()

        if routine_component_data:
            # Clear and recreate nested routine_component_data
            instance.routine_component_data.clear()
            for component_data in routine_component_data:
                rep_data_list = component_data.pop('rep_data')
                routine_component = RoutineComponentData.objects.create(
                    **component_data, routine_data=instance
                )
                for rep_data in rep_data_list:
                    pose_list = rep_data.pop('poses', [])
                    rep = RepData.objects.create(**rep_data)
                    rep.poses.set(pose_list)  # Add poses to the ManyToMany field
                    routine_component.rep_data.add(rep)
                instance.routine_component_data.add(routine_component)

        return instance

    def to_representation(self, instance):
        """Customize the serialized output."""
        representation = super().to_representation(instance)
        representation['routineConfig_id'] = instance.routine_config.id  # Add routineConfig ID
        representation['routine_component_data'] = RoutineComponentDataSerializer(
            instance.routine_component_data.all(), many=True, context=self.context
        ).data  # Serialize full routine_component_data
        return representation


class PatientSerializer(serializers.ModelSerializer):
    exercises = RoutineConfigSerializer(read_only=True)
    exercises_id = serializers.PrimaryKeyRelatedField(
        queryset=RoutineConfig.objects.all(), source="exercises", write_only=True, required=False
    )

    class Meta:
        model = Patient
        fields = ["id", "first_name", "last_name", "email", "date_of_birth", "sex", "condition", "exercises", "exercises_id"]