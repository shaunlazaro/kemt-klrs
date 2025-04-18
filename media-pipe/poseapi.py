
import requests
import json
import copy
from datetime import datetime

# URL to Django API
API_BASE_URL = "http://129.153.49.30:8000/api"


# Function to send a routine to the backend
# This will perform a POST -> Creates a new routine
def send_routine_to_server(routine_data):
    # Convert routine data to JSON format
    routine_json = json.dumps(routine_data)

    api_url = f"{API_BASE_URL}/routines/"
    # Send POST request to the Django server
    response = requests.post(api_url, data=routine_json, headers={'Content-Type': 'application/json'})

    if response.status_code == 201:
        print('Routine created successfully:', response.json())
    else:
        print('Error:', response.status_code, response.text)


# Quick and dirty way of uploading data for testing.  Just insert poses from cv script over time, then upload when done. 
BASE_ROUTINE = {
    "name": "TEST ROUTINE",
    "description": f"Test data, generated at {datetime.now()}",
    "poses":[],
}
current_routine = copy.deepcopy(BASE_ROUTINE)

def add_pose_to_routine(pose_json):
    global current_routine
    current_routine["poses"].append({"landmarks":pose_json})
    return len(current_routine["poses"])


def upload_current_routine():
    global current_routine
    send_routine_to_server(current_routine)
    current_routine = copy.deepcopy(BASE_ROUTINE)


if __name__ == "__main__":
    # Example data for a routine.  Each element of poses should be: {"landmarks":[pose_to_json()...]}
    example_routine = {
        "name": "Test Routine",
        "description": "Test data, two poses in one routine.",
        "poses": [
            # Pose 1 - List of landmarks for the first pose
            {
                "landmarks": [
                    {
                        "landmark_index": 0,
                        "x": 0.5,
                        "y": 0.3,
                        "z": 0.1,
                        "visibility": 0.9
                    },
                    {
                        "landmark_index": 1,
                        "x": 0.6,
                        "y": 0.4,
                        "z": 0.2,
                        "visibility": 0.8
                    },
                    # Add more landmarks for pose 1 as needed
                ]
            },
            # Pose 2 - List of landmarks for the second pose
            {
                "landmarks": [
                    {
                        "landmark_index": 2,
                        "x": 0.7,
                        "y": 0.5,
                        "z": 0.3,
                        "visibility": 0.7
                    },
                    {
                        "landmark_index": 3,
                        "x": 0.8,
                        "y": 0.6,
                        "z": 0.4,
                        "visibility": 0.6
                    },
                ]
            },
        ]
    }
    # Usage example
    send_routine_to_server(example_routine)