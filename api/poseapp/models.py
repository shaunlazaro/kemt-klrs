from django.db import models
from django.utils.safestring import mark_safe

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