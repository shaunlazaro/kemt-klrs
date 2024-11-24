import unittest
import numpy as np
import cv2
from pose_estimation.main import calculate_angle, draw_keypoints, draw_connections, movenet, EDGES

class TestPoseEstimation(unittest.TestCase):

    def test_calculate_angle(self):
        point1 = [1, 0]
        point2 = [0, 0]
        point3 = [0, 1]
        angle = calculate_angle(point1, point2, point3)
        self.assertAlmostEqual(angle, 90, places=1)

    def test_draw_keypoints(self):
        frame = np.zeros((100, 100, 3), dtype=np.uint8)
        keypoints = np.array([[[0.5, 0.5, 0.9]]])
        confidence_threshold = 0.5
        draw_keypoints(frame, keypoints, confidence_threshold)
        self.assertTrue(np.any(frame))

    def test_draw_connections(self):
        frame = np.zeros((100, 100, 3), dtype=np.uint8)
        keypoints = np.array([[[0.5, 0.5, 0.9], [0.6, 0.6, 0.9]]])
        confidence_threshold = 0.5
        draw_connections(frame, keypoints, EDGES, confidence_threshold)
        self.assertTrue(np.any(frame))

    def test_movenet(self):
        input_image = np.zeros((1, 256, 256, 3), dtype=np.uint8)
        keypoints = movenet(input_image)
        self.assertEqual(keypoints.shape, (1, 1, 17, 3))

    def test_count_reps(self):
        # Simulate keypoints for single leg extensions
        keypoints = np.array([[[0.5, 0.5, 0.9], [0.6, 0.6, 0.9], [0.7, 0.7, 0.9]]])
        threshold = 0.5
        reps = 0
        previous_angle = None

        for _ in range(10):  # Simulate 10 frames
            left_hip = keypoints[0, 0, :2]
            left_knee = keypoints[0, 1, :2]
            left_ankle = keypoints[0, 2, :2]

            if keypoints[0, 0, 2] > threshold and keypoints[0, 1, 2] > threshold and keypoints[0, 2, 2] > threshold:
                current_angle = calculate_angle(left_hip, left_knee, left_ankle)
                if previous_angle is not None and current_angle < 90 and previous_angle >= 90:
                    reps += 1
                previous_angle = current_angle

        self.assertEqual(reps, 1)

if __name__ == '__main__':
    unittest.main()