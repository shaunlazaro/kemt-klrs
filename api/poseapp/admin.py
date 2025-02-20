from django.contrib import admin
from .models import Pose, Routine, TrackingDetail, ExerciseDetail, RoutineConfig, RoutineExercise

# Customizing Pose admin view
@admin.register(Pose)
class PoseAdmin(admin.ModelAdmin):
    list_display = ('id', 'created_at', 'pose_landmarks_count', 'render_pose')  # Added render_pose for inline visualization
    search_fields = ('id',)
    list_filter = ('created_at',)
    readonly_fields = ('render_pose',)  # Add render_pose as a readonly field

    def pose_landmarks_count(self, obj):
        # Assuming landmarks is a JSON field, this counts the number of landmarks
        return len(obj.landmarks) if obj.landmarks else 0
    pose_landmarks_count.short_description = 'Number of Landmarks'

# Customizing Routine admin view
@admin.register(Routine)
class RoutineAdmin(admin.ModelAdmin):
    list_display = ('name', 'description', 'created_at', 'pose_count')  # Show routine name, description, and pose count
    search_fields = ('name',)
    list_filter = ('created_at',)

    def pose_count(self, obj):
        # This counts the number of poses related to a routine
        return obj.poses.count()
    pose_count.short_description = 'Number of Poses'

admin.site.register(TrackingDetail)
admin.site.register(ExerciseDetail)
admin.site.register(RoutineConfig)
admin.site.register(RoutineExercise)