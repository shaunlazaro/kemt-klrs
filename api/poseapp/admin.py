from django.contrib import admin
from django.utils.html import format_html
from .models import Pose, Routine
from .constants import LANDMARK_CONNECTIONS
import json

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

    def render_pose(self, obj):
        """Renders the pose landmarks and connections as an HTML canvas."""
        landmarks = obj.landmarks
        if not landmarks:
            return "No landmarks available"

        # Prepare connections data for JavaScript
        connections_json = json.dumps(LANDMARK_CONNECTIONS)
        landmarks_json = json.dumps(landmarks)

        html = f"""
        <div>
            <canvas id="poseCanvas-{obj.id}" width="300" height="300" 
                style="border:1px solid black;"></canvas>
            <script>
                (function() {{
                    const canvas = document.getElementById("poseCanvas-{obj.id}");
                    const ctx = canvas.getContext("2d");
                    const landmarks = {landmarks_json};
                    const connections = {connections_json};

                    // Scale factor for rendering
                    const canvasWidth = canvas.width;
                    const canvasHeight = canvas.height;

                    // Draw connections
                    ctx.strokeStyle = "blue";
                    ctx.lineWidth = 2;
                    connections.forEach(([startIdx, endIdx]) => {{
                        const start = landmarks[startIdx];
                        const end = landmarks[endIdx];
                        if (start && end) {{
                            ctx.beginPath();
                            ctx.moveTo(start.x * canvasWidth, start.y * canvasHeight);
                            ctx.lineTo(end.x * canvasWidth, end.y * canvasHeight);
                            ctx.stroke();
                        }}
                    }});

                    // Draw landmarks
                    ctx.fillStyle = "red";
                    landmarks.forEach(l => {{
                        const x = l.x * canvasWidth;
                        const y = l.y * canvasHeight;
                        ctx.beginPath();
                        ctx.arc(x, y, 3, 0, 2 * Math.PI);
                        ctx.fill();
                    }});
                }})();
            </script>
        </div>
        """
        return format_html(html)

    render_pose.short_description = "Pose Visual"

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