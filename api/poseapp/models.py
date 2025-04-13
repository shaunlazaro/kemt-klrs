from django.db import models
from django.utils.safestring import mark_safe
from django.db.models.signals import post_save, post_delete
from django.dispatch import receiver
from django.core.cache import cache
# ----
# Workout Result Data  (DEPRECATED)
# ----
# Not deprecated
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

# DEPRECATED
class Routine(models.Model):
    name = models.CharField(max_length=100)
    description = models.TextField(blank=True, null=True)
    poses = models.ManyToManyField(Pose, related_name="routines")
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return self.name

# ----
# Workout Result Data (New as of 3/11/25)
# ----
class RepData(models.Model):
    rep_number = models.IntegerField()
    max_flexion = models.FloatField()
    max_extension = models.FloatField()
    concentric_time = models.FloatField()
    eccentric_time = models.FloatField()
    total_time = models.FloatField()
    goal_flexion_met = models.BooleanField()
    goal_extension_met = models.BooleanField()
    max_score = models.FloatField()
    alerts = models.JSONField(default=list)  # List of alert messages
    poses = models.ManyToManyField('Pose', blank=True)  # Link to Pose model

    def __str__(self):
        return f"{self.id} - Rep {self.rep_number}: Score {self.max_score}"

class RoutineComponentData(models.Model):
    # routine_component = models.ForeignKey('RoutineExercise', on_delete=models.CASCADE)
    # AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAHHHHHHHHHHHHHHHHHHHHHHHHHH
    exercise_detail = models.ForeignKey('ExerciseDetail', on_delete=models.CASCADE, null=True) # Don't null this...
    rep_data = models.ManyToManyField(RepData)

    def __str__(self):
        return f"{self.id} - Routine Component {self.exercise_detail}"

class RoutineData(models.Model):
    routine_config = models.ForeignKey('RoutineConfig', on_delete=models.CASCADE)
    routine_component_data = models.ManyToManyField(RoutineComponentData)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"{self.id} - Routine Data for {self.routine_config.name}"

# Invalidate cache when a RoutineData object is saved
@receiver(post_save, sender=RoutineData)
def invalidate_routine_data_cache(sender, instance, **kwargs):
    cache_key = f"routine_data:{instance.id}"  # Assuming routine_data is cached by instance.id
    cache.delete(cache_key)

# Invalidate cache when a RoutineData object is deleted
@receiver(post_delete, sender=RoutineData)
def invalidate_routine_data_cache_delete(sender, instance, **kwargs):
    cache_key = f"routine_data:{instance.id}"
    cache.delete(cache_key)

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
    symmetric = models.BooleanField(default=True)
    dimensionality = models.CharField(max_length=50, default="2D", blank=True)
    goal_flexion = models.FloatField(null=True, blank=True)
    goal_extension = models.FloatField(null=True, blank=True)
    show_alert_if_above = models.FloatField(null=True, blank=True)
    show_alert_if_below = models.FloatField(null=True, blank=True)
    alert_message = models.TextField(null=True, blank=True)

    def __str__(self):
        return f"{self.tracking_type} - {self.alert_message or 'No Alert'}"

class ExerciseDetail(models.Model):
    rep_tracking = models.ForeignKey(TrackingDetail, on_delete=models.CASCADE, null=True, blank=True)
    rep_keypoints = models.JSONField()
    start_angle = models.FloatField(default=0)
    min_rep_time = models.FloatField(default=0)
    threshold_flexion = models.FloatField()
    threshold_extension = models.FloatField()
    display_name = models.CharField(max_length=100)
    start_in_flexion = models.BooleanField(default=False)
    body_alignment = models.CharField(max_length=50, default="TODO")
    default_tracking_details = models.ManyToManyField(TrackingDetail, related_name="exercise_details")
    instruction = models.CharField(max_length=900, default="Enter some instructions to show the user...")

    def __str__(self):
        return self.display_name

class RoutineConfig(models.Model):
    name = models.CharField(max_length=100, default="Unnamed Routine")
    injury = models.CharField(max_length=100, default="No Injury Specified")
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

class Patient(models.Model):
    SEX_CHOICES = [
        ('M', 'Male'),
        ('F', 'Female'),
        ('O', 'Other'),
    ]

    # user_id = models.CharField(max_length=255)  # External user identifier
    first_name = models.CharField(max_length=255)
    last_name = models.CharField(max_length=255)
    email = models.EmailField(unique=True)
    date_of_birth = models.DateField()
    sex = models.CharField(max_length=1, choices=SEX_CHOICES)
    condition = models.TextField()
    exercises = models.ForeignKey(RoutineConfig, on_delete=models.SET_NULL, null=True, blank=True)

    def __str__(self):
        return f"{self.first_name} {self.last_name}"
