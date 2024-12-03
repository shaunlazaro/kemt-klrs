import mediapipe as mp

mp_pose = mp.solutions.pose

# Returns a json serializable version of a pose, suitable for import into database.
# Pass processing results (e.g. results = pose.process(image); )
def result_to_pose_json(results):
    if results.pose_landmarks:
        # Convert landmarks to a list of simple dicts
        landmarks = [
            {
                "landmark_index": i,
                "x": lm.x,
                "y": lm.y,
                "z": lm.z,
                "visibility": lm.visibility
            }
            for i, lm in enumerate(results.pose_landmarks.landmark)
        ]
        return landmarks
    return None

# Just for reference
def get_landmark_names():
    return {landmark.value: landmark.name for landmark in mp_pose.PoseLandmark}

# Just for reference
def get_connections():
    return list(mp_pose.POSE_CONNECTIONS)

if __name__ == "__main__":
    print(get_landmark_names())
    print(get_connections())