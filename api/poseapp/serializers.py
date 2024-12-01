from rest_framework import serializers
from .models import Pose, Routine

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