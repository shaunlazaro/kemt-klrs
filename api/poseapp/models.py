from django.db import models
from django.utils.safestring import mark_safe

# ----
# Workout Result Data
# ----
class Pose(models.Model):
    # Store the entire list of landmarks as a JSON field
    landmarks = models.JSONField(default=list)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"Pose {self.id} with {len(self.landmarks)} landmarks"
    
    def render_pose(self):
        # Generate JavaScript to render the pose
        return mark_safe(f"""
        <div>
            <canvas id="poseCanvas-{self.id}" width="500" height="500" style="border:1px solid #000;"></canvas>
            <script>
                (function() {{
                    const canvas = document.getElementById('poseCanvas-{self.id}');
                    const ctx = canvas.getContext('2d');
                    const landmarks = {self.landmarks};

                    // Draw connections
                    const connections = [
                        [11, 12], [11, 13], [13, 15], [12, 14], [14, 16],
                        [23, 24], [11, 23], [12, 24], [23, 25], [25, 27],
                        [27, 29], [29, 31], [24, 26], [26, 28], [28, 30], [30, 32]
                    ];

                    ctx.clearRect(0, 0, canvas.width, canvas.height);

                    // Draw the connections
                    connections.forEach(([start, end]) => {{
                        const startPoint = landmarks[start];
                        const endPoint = landmarks[end];
                        if (startPoint && endPoint) {{
                            ctx.beginPath();
                            ctx.moveTo(startPoint.x * canvas.width, startPoint.y * canvas.height);
                            ctx.lineTo(endPoint.x * canvas.width, endPoint.y * canvas.height);
                            ctx.strokeStyle = "blue";
                            ctx.lineWidth = 2;
                            ctx.stroke();
                        }}
                    }});

                    // Draw the landmarks
                    landmarks.forEach((lm, index) => {{
                        ctx.beginPath();
                        ctx.arc(lm.x * canvas.width, lm.y * canvas.height, 3, 0, 2 * Math.PI);
                        ctx.fillStyle = "red";
                        ctx.fill();
                    }});
                }})();
            </script>
        </div>
        """)

    render_pose.short_description = "Pose Rendering"  # Optional: Display column title

class Routine(models.Model):
    name = models.CharField(max_length=100)
    description = models.TextField(blank=True, null=True)
    poses = models.ManyToManyField(Pose, related_name="routines")
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return self.name

# ----
# Workout Routine/Config Data
# ----
class TrackingType(models.TextChoices):
    ANGLE_OF_THREE_POINTS = "Angle of three points"
    VELOCITY_OF_POINT = "Velocity of point"
    ACCELERATION_OF_POINT = "Acceleration of point"
    DISTANCE_BETWEEN_POINTS = "Distance between points"
    ANGLE_WITH_HORIZONTAL = "Angle with horizontal"
    ANGLE_WITH_VERTICAL = "Angle with vertical"
    ANGLE_WITH_LINE = "Angle with line"

# Each of these represents a single thing that the app should track.  
# E.g. Tracking Detail: lifted heel, keypoints: toe, heel, ankle, show alert if angle < 80deg
class TrackingDetail(models.Model):
    tracking_type = models.CharField(max_length=50, choices=TrackingType.choices) # S.T.C, keep as string for now so we don't have to sync enums across projects
    keypoints = models.JSONField()  # List of keypoints (list<str>)
    show_alert_if_above = models.FloatField(null=True, blank=True)
    show_alert_if_below = models.FloatField(null=True, blank=True)
    alert_message = models.TextField(null=True, blank=True)

    def __str__(self):
        return f"{self.tracking_type} - {self.alert_message or 'No Alert'}"

class ExerciseDetail(models.Model):
    rep_keypoints = models.JSONField()
    threshold_flexion = models.FloatField()
    threshold_extension = models.FloatField()
    display_name = models.CharField(max_length=100)
    default_tracking_details = models.ManyToManyField(TrackingDetail, related_name="exercise_details")

    def __str__(self):
        return self.display_name

class RoutineConfig(models.Model):
    name = models.CharField(max_length=100)
    exercises = models.ManyToManyField(
        ExerciseDetail,
        through='RoutineExercise',
        related_name='routine_configs'
    )

    def __str__(self):
        return self.name

# Part of routine.  This is a join table which links routine (the whole set of exercices) to individual workout configs, adding metadata like reps and tracking details
class RoutineExercise(models.Model):
    # Join these two tables
    routine = models.ForeignKey(RoutineConfig, on_delete=models.CASCADE)
    exercise = models.ForeignKey(ExerciseDetail, on_delete=models.CASCADE)
    # Extra metadata:
    reps = models.FloatField(null=True, blank=True)
    custom_tracking_details = models.ManyToManyField(TrackingDetail, related_name='routine_exercises', blank=True)

    def __str__(self):
        return f"{self.routine} - {self.exercise.display_name}"