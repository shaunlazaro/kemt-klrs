import numpy as np
import cv2

def draw_keypoints(frame, keypoints, confidence_threshold):
    y, x, c = frame.shape
    shaped = np.squeeze(np.multiply(keypoints, [y,x,1]))
    
    for kp in shaped:
        ky, kx, kp_conf = kp
        if kp_conf > confidence_threshold:
            cv2.circle(frame, (int(kx), int(ky)), 4, (255,255,255), -1)
            # Display coordinates for every keypoint
            # cv2.putText(frame, f'pt: {int(kx), int(ky)}', (int(kx), int(ky)-10), 
            #     cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 2)
            
def draw_connections(frame, keypoints, edges, confidence_threshold):
    y, x, c = frame.shape
    shaped = np.squeeze(np.multiply(keypoints, [y,x,1]))
    
    for edge, color in edges.items():
        p1, p2 = edge
        y1, x1, c1 = shaped[p1]
        y2, x2, c2 = shaped[p2]
        
        if (c1 > confidence_threshold) & (c2 > confidence_threshold):      
            cv2.line(frame, (int(x1), int(y1)), (int(x2), int(y2)), (255,255,255), 2)

# Function to calculate the angle between three points
def calculate_angle(point1, point2, point3):
    # Create vectors
    vector1 = np.array(point1) - np.array(point2)
    vector2 = np.array(point3) - np.array(point2)

    # Calculate the angle
    cosine_angle = np.dot(vector1, vector2) / (np.linalg.norm(vector1) * np.linalg.norm(vector2))
    angle = np.degrees(np.arccos(cosine_angle))
    return angle
  
def draw_angle_marker(img, knee, hip, ankle, angle):
    # Define the radius of the angle marker
    radius = 30  # Adjust based on visualization preference
    
    # Calculate vectors
    vec1 = np.array([hip[1] - knee[1], hip[0] - knee[0]])  # vec from knee to hip
    vec2 = np.array([ankle[1] - knee[1], ankle[0] - knee[0]])  # vec from knee to ankle
    
    # Normalize vectors to get directions
    vec1_norm = vec1 / np.linalg.norm(vec1)
    vec2_norm = vec2 / np.linalg.norm(vec2)
    
    # Define the start and end points for the arc
    start_point = (int(knee[1] + vec1_norm[0] * radius), int(knee[0] + vec1_norm[1] * radius))
    end_point = (int(knee[1] + vec2_norm[0] * radius), int(knee[0] + vec2_norm[1] * radius))
    
    # Convert angle from degrees to radians
    angle_radians = np.radians(angle)
    
    # Calculate intermediate points for the arc
    num_points = 50  # Number of points to create a smooth arc
    arc_points = []
    for i in range(num_points + 1):
        t = i / num_points  # Interpolation factor
        # Interpolate angle between start and end vectors
        theta = angle_radians * t
        rot_vec = np.array([
            np.cos(theta) * vec1_norm[0] - np.sin(theta) * vec1_norm[1],
            np.sin(theta) * vec1_norm[0] + np.cos(theta) * vec1_norm[1]
        ])
        arc_points.append((int(knee[1] + rot_vec[0] * radius), int(knee[0] + rot_vec[1] * radius)))
    
    # Draw the arc
    for i in range(len(arc_points) - 1):
        cv2.line(img, arc_points[i], arc_points[i + 1], (255, 255, 0), 2)
            
    # Display the angle at the knee position
    cv2.putText(img, f'{int(angle)}°', (int(knee[1]), int(knee[0]) - 40), cv2.FONT_HERSHEY_SIMPLEX, 1.5, (255, 255, 255), 2)

    return img