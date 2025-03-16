from rest_framework import serializers
from .models import Pose, Routine, TrackingDetail, ExerciseDetail, RoutineConfig, RoutineExercise, RepData, RoutineComponentData, RoutineData

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

class RepDataSerializer(serializers.ModelSerializer):
    class Meta:
        model = RepData
        fields = '__all__'

class RoutineComponentDataSerializer(serializers.ModelSerializer):
    rep_data = RepDataSerializer(many=True)

    class Meta:
        model = RoutineComponentData
        fields = '__all__'

class RoutineDataSerializer(serializers.ModelSerializer):
    routine_component_data = RoutineComponentDataSerializer(many=True)

    class Meta:
        model = RoutineData
        fields = '__all__'