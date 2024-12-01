from django.db import models

class Pose(models.Model):
    # Store the entire list of landmarks as a JSON field
    landmarks = models.JSONField(default=list)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"Pose {self.id} with {len(self.landmarks)} landmarks"


class Routine(models.Model):
    name = models.CharField(max_length=100)
    description = models.TextField(blank=True, null=True)
    poses = models.ManyToManyField(Pose, related_name="routines")
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return self.name