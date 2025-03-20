from django.contrib import admin
from .models import Pose, Routine, TrackingDetail, ExerciseDetail, RoutineConfig, RoutineExercise, RepData, RoutineComponentData, RoutineData, Patient

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

@admin.register(RepData)
class RepDataAdmin(admin.ModelAdmin):
    list_display = ('id', 'rep_number', 'max_score', 'goal_flexion_met', 'goal_extension_met')
    search_fields = ('rep_number',)

@admin.register(RoutineComponentData)
class RoutineComponentDataAdmin(admin.ModelAdmin):
    list_display = ('id', 'exercise_detail',)
    filter_horizontal = ('rep_data',)

@admin.register(RoutineData)
class RoutineDataAdmin(admin.ModelAdmin):
    list_display = ('id', 'routine_config', 'created_at')
    filter_horizontal = ('routine_component_data',)

admin.site.register(Patient)