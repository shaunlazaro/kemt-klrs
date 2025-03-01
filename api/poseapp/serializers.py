from rest_framework import serializers
from .models import Pose, Routine, TrackingDetail, ExerciseDetail, RoutineConfig, RoutineExercise

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

class RoutineExerciseSerializer(serializers.ModelSerializer):
    exercise = ExerciseDetailSerializer(read_only=True)
    custom_tracking_details = TrackingDetailSerializer(many=True, required=False)

    class Meta:
        model = RoutineExercise
        fields = ['exercise', 'reps', 'custom_tracking_details']

class RoutineConfigSerializer(serializers.ModelSerializer):
    exercises = RoutineExerciseSerializer(source='routineexercise_set', many=True)

    class Meta:
        model = RoutineConfig
        fields = ['id', 'name', 'exercises']

    def get_exercises(self, obj):
        routine_exercises = RoutineExercise.objects.filter(routine=obj).select_related('exercise').prefetch_related('custom_tracking_details')
        return RoutineExerciseSerializer(routine_exercises, many=True).data
