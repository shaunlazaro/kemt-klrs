import React, { useEffect, useRef } from "react";
import { Pose } from "../../interfaces/routineData.interface";

interface Landmark {
    landmark_index: number;
    visibility: number;
    x: number;
    y: number;
    z: number;
}

interface PoseForVideo {
    landmarks: Landmark[];
}

interface PoseVideoProps {
    posesRaw: Pose[]; // Array of poses, each containing 33 landmarks
    fps?: number; // Frames per second (default: 30)
}

const PoseVideo: React.FC<PoseVideoProps> = ({ posesRaw, fps = 10 }) => {
    const canvasRef = useRef<HTMLCanvasElement>(null);
    const frameIndexRef = useRef(0);
    const intervalRef = useRef<number | null>(null);

    // Convert posesRaw to PoseForVideo array
    const poses: PoseForVideo[] = posesRaw.map((poseRaw) => ({
        landmarks: poseRaw.landmarks as Landmark[],
    }));

    const VISIBILITY_THRESHOLD = 0

    const connections = [
        [11, 12], [11, 13], [13, 15], [12, 14], [14, 16],
        [23, 24], [11, 23], [12, 24], [23, 25], [25, 27],
        [27, 29], [29, 31], [24, 26], [26, 28], [28, 30], [30, 32]
    ];

    useEffect(() => {
        frameIndexRef.current = 0; // Reset frame index to prevent out-of-bounds access
        const canvas = canvasRef.current;
        if (!canvas || poses.length === 0) return;
        const ctx = canvas.getContext("2d");
        if (!ctx) return;

        const drawPose = (pose: PoseForVideo) => {
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            ctx.strokeStyle = "blue";
            ctx.lineWidth = 2;

            // Draw connections
            connections.forEach(([start, end]) => {
                const startPoint = pose.landmarks.find(lm => lm.landmark_index === start);
                const endPoint = pose.landmarks.find(lm => lm.landmark_index === end);
                if (startPoint && endPoint && startPoint.visibility > VISIBILITY_THRESHOLD && endPoint.visibility > VISIBILITY_THRESHOLD) {
                    ctx.beginPath();
                    ctx.moveTo(startPoint.x * canvas.width, startPoint.y * canvas.height);
                    ctx.lineTo(endPoint.x * canvas.width, endPoint.y * canvas.height);
                    ctx.stroke();
                }
            });

            // Draw landmarks
            pose.landmarks.forEach(lm => {
                if (lm.visibility > VISIBILITY_THRESHOLD) {
                    ctx.beginPath();
                    ctx.arc(lm.x * canvas.width, lm.y * canvas.height, 3, 0, 2 * Math.PI);
                    ctx.fillStyle = "red";
                    ctx.fill();
                }
            });
        };

        const startAnimation = () => {
            if (intervalRef.current) clearInterval(intervalRef.current);
            intervalRef.current = window.setInterval(() => {
                drawPose(poses[frameIndexRef.current]);
                frameIndexRef.current = (frameIndexRef.current + 1) % poses.length;
            }, 1000 / fps);
        };

        startAnimation();

        return () => {
            if (intervalRef.current) clearInterval(intervalRef.current);
        };
    }, [poses, fps]);

    return <canvas ref={canvasRef} width={1000} height={1000} className="w-3/4 aspect-square" style={{ border: "1px solid #000" }} />;
};

export default PoseVideo;
