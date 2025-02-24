import cv2
import mediapipe as mp
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from collections import deque
from scipy.interpolate import interp1d
from scipy.signal import correlate
from scipy.signal import find_peaks



# Initialize MediaPipe Pose
mp_pose = mp.solutions.pose
mp_drawing = mp.solutions.drawing_utils

# Define buffer size for smoothing
BUFFER_SIZE = 7


def smooth_point(buffer, new_point):
    """Applies moving average smoothing."""
    buffer.append(new_point)
    return np.mean(buffer, axis=0)


def calculate_angle(a, b, c):
    """Calculate angle at point b given three 3D points."""
    a, b, c = np.array(a), np.array(b), np.array(c)
    ba, bc = a - b, c - b
    cosine_angle = np.dot(ba, bc) / (np.linalg.norm(ba) * np.linalg.norm(bc))
    return np.degrees(np.arccos(np.clip(cosine_angle, -1.0, 1.0)))
  
def calculate_angle_optitrack(a, b, c):
    """Calculate angle at point b given three 3D points."""
    a = np.array(a).astype(float)
    b = np.array(b).astype(float)
    c = np.array(c).astype(float)

    ba = a - b
    bc = c - b

    cosine_angle = np.dot(ba, bc) / (np.linalg.norm(ba) * np.linalg.norm(bc))
    angle = np.degrees(np.arccos(np.clip(cosine_angle, -1.0, 1.0)))
    
    return angle



def process_video(video_path):
    """Extracts knee angles from a video using MediaPipe Pose."""
    cap = cv2.VideoCapture(video_path)
    frame_width, frame_height = int(cap.get(3)), int(cap.get(4))
    fps = int(cap.get(cv2.CAP_PROP_FPS))

    # Buffers for smoothing
    hip_buffer, knee_buffer, ankle_buffer = deque(maxlen=BUFFER_SIZE), deque(maxlen=BUFFER_SIZE), deque(maxlen=BUFFER_SIZE)
    knee_angles_3d, knee_angles_2d = [], []

    with mp_pose.Pose(static_image_mode=False, model_complexity=1, min_detection_confidence=0.5, min_tracking_confidence=0.5) as pose:
        while cap.isOpened():
            ret, frame = cap.read()
            if not ret:
                break
            frame_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            results = pose.process(frame_rgb)

            if results.pose_landmarks:
                landmarks = results.pose_landmarks.landmark
                hip_3d = np.array([landmarks[mp_pose.PoseLandmark.RIGHT_HIP].x,
                                   landmarks[mp_pose.PoseLandmark.RIGHT_HIP].y,
                                   landmarks[mp_pose.PoseLandmark.RIGHT_HIP].z])
                knee_3d = np.array([landmarks[mp_pose.PoseLandmark.RIGHT_KNEE].x,
                                    landmarks[mp_pose.PoseLandmark.RIGHT_KNEE].y,
                                    landmarks[mp_pose.PoseLandmark.RIGHT_KNEE].z])
                ankle_3d = np.array([landmarks[mp_pose.PoseLandmark.RIGHT_ANKLE].x,
                                     landmarks[mp_pose.PoseLandmark.RIGHT_ANKLE].y,
                                     landmarks[mp_pose.PoseLandmark.RIGHT_ANKLE].z])
                
                # Apply smoothing
                hip_3d = smooth_point(hip_buffer, hip_3d)
                knee_3d = smooth_point(knee_buffer, knee_3d)
                ankle_3d = smooth_point(ankle_buffer, ankle_3d)

                # Normalize 3D Z-coordinates
                hip_3d[2] = 0
                knee_3d[2] -= hip_3d[2]
                ankle_3d[2] -= hip_3d[2]
                
                hip_2d = (int(landmarks[mp_pose.PoseLandmark.RIGHT_HIP].x * frame_width), 
                      int(landmarks[mp_pose.PoseLandmark.RIGHT_HIP].y * frame_height))
                knee_2d = (int(landmarks[mp_pose.PoseLandmark.RIGHT_KNEE].x * frame_width), 
                          int(landmarks[mp_pose.PoseLandmark.RIGHT_KNEE].y * frame_height))
                ankle_2d = (int(landmarks[mp_pose.PoseLandmark.RIGHT_ANKLE].x * frame_width), 
                            int(landmarks[mp_pose.PoseLandmark.RIGHT_ANKLE].y * frame_height))

                # Convert 2D points to pixel coordinates (mp provides normalized values (0-1))
                # We need to multiply by the frame dimensions in order to get pixel coordinates
                
                # knee_pos = (int(knee_2d[0] * frame_width), int(knee_2d[1] * frame_height))
                # hip_pos = (int(hip_2d[0] * frame_width), int(hip_2d[1] * frame_height))
                # ankle_pos = (int(ankle_2d[0] * frame_width), int(ankle_2d[1] * frame_height))
                
                
                # Compute knee angles
                knee_angle_3d = calculate_angle(hip_3d, knee_3d, ankle_3d)
                knee_angle_2d = calculate_angle(hip_2d, knee_2d, ankle_2d)

                # Store angles
                knee_angles_3d.append(knee_angle_3d)
                knee_angles_2d.append(knee_angle_2d)

    cap.release()
    return np.array(knee_angles_3d), np.array(knee_angles_2d), fps


def process_optitrack(csv_path):
    """Loads OptiTrack data and computes knee angles."""
    df = pd.read_csv(csv_path, skiprows=3)  # Skip the first 3 rows

    df = df.drop(index=range(0, 3)).reset_index(drop=True)
    
    # Rename columns for clarity (keeping only needed ones)
    df = df.rename(columns={
        "S1:RTHI": "Hip_X", "S1:RTHI.1": "Hip_Y", "S1:RTHI.2": "Hip_Z",
        "S1:RKNE": "Knee_X", "S1:RKNE.1": "Knee_Y", "S1:RKNE.2": "Knee_Z",
        "S1:RHEE": "Ankle_X", "S1:RHEE.1": "Ankle_Y", "S1:RHEE.2": "Ankle_Z"
    })

    # Keep only the necessary columns
    df = df[["Hip_X", "Hip_Y", "Hip_Z", "Knee_X", "Knee_Y", "Knee_Z", "Ankle_X", "Ankle_Y", "Ankle_Z"]]
    
    opt_knee_angles = []
    for _, row in df.iterrows():
        hip = (row["Hip_X"], row["Hip_Y"], row["Hip_Z"])
        knee = (row["Knee_X"], row["Knee_Y"], row["Knee_Z"])
        ankle = (row["Ankle_X"], row["Ankle_Y"], row["Ankle_Z"])
        
        knee_angle = calculate_angle_optitrack(hip, knee, ankle)
        opt_knee_angles.append(knee_angle)

    # Convert to NumPy array
    opt_knee_angles = np.array(opt_knee_angles)

    return opt_knee_angles



def compute_time_shift(mp_knee_angles, opt_knee_angles, fps_opt=120, max_shift=0.2, align_method="cross_correlation", peak_prominence=1.0, max_peaks=3):
    """Finds the optimal time shift (restricted to ±0.2s) using normalized cross-correlation."""
    max_lag = int(max_shift * fps_opt)  # Convert seconds to frames

    if align_method == "cross_correlation":
        # Normalize signals (subtract mean, divide by standard deviation)
        mp_knee_norm = (mp_knee_angles - np.mean(mp_knee_angles)) / np.std(mp_knee_angles)
        opt_knee_norm = (opt_knee_angles - np.mean(opt_knee_angles)) / np.std(opt_knee_angles)

        # Compute cross-correlation
        correlation = correlate(mp_knee_norm, opt_knee_norm, mode='full')

        # Create lag indices
        lags = np.arange(-len(mp_knee_angles) + 1, len(opt_knee_angles))

        # Restrict search range to ± max_lag
        valid_lags = (lags >= -max_lag) & (lags <= max_lag)
        restricted_lags = lags[valid_lags]
        restricted_correlation = correlation[valid_lags]

        # Find the lag with the highest correlation
        best_lag = restricted_lags[np.argmax(restricted_correlation)]
    
    elif align_method == "peaks":
        # Detect peaks in both signals with higher prominence and limit the number of peaks
        mp_peaks, _ = find_peaks(mp_knee_angles, prominence=peak_prominence)
        opt_peaks, _ = find_peaks(opt_knee_angles, prominence=peak_prominence)

        # Only keep the top `max_peaks` number of peaks for alignment
        mp_peaks = mp_peaks[:max_peaks]
        opt_peaks = opt_peaks[:max_peaks]

        if len(mp_peaks) > 0 and len(opt_peaks) > 0:
            # Align based on the first peak of each signal
            peak_shifts = opt_peaks[:max_peaks] - mp_peaks[:max_peaks]  # Align based on multiple peaks
            best_lag = np.median(peak_shifts)  # Take the median shift to avoid outliers
        else:
            best_lag = 0  # No peaks found, fall back to no shift

    elif align_method == "valleys":
        # Detect valleys (negative peaks) in both signals with higher prominence
        mp_valleys, _ = find_peaks(-mp_knee_angles, prominence=peak_prominence)
        opt_valleys, _ = find_peaks(-opt_knee_angles, prominence=peak_prominence)

        # Only keep the top `max_peaks` number of valleys for alignment
        mp_valleys = mp_valleys[:max_peaks]
        opt_valleys = opt_valleys[:max_peaks]

        if len(mp_valleys) > 0 and len(opt_valleys) > 0:
            # Align based on the first valley of each signal
            valley_shifts = opt_valleys[:max_peaks] - mp_valleys[:max_peaks]  # Align based on multiple valleys
            best_lag = np.median(valley_shifts)  # Take the median shift to avoid outliers
        else:
            best_lag = 0  # No valleys found, fall back to no shift

    else:
        raise ValueError("Invalid alignment method. Choose from 'cross_correlation', 'peaks', or 'valleys'.")

    # Convert lag to time shift in seconds
    time_shift = best_lag / fps_opt  # Convert frames to seconds
    return time_shift


  
def align_and_plot(mp_knee_angles_3d, mp_knee_angles_2d, fps_mp, opt_knee_angles, view):
    """Aligns MediaPipe and OptiTrack knee angle data using refined cross-correlation and plots results."""
    
    # Generate time axes
    time_opt = np.linspace(0, len(opt_knee_angles) / 120, len(opt_knee_angles))  # OptiTrack at 120 FPS
    time_mp = np.linspace(0, len(mp_knee_angles_3d) / fps_mp, len(mp_knee_angles_3d))  # MediaPipe FPS

    # Compute the optimal time shift (restricted to ±0.2 sec)
    time_shift = compute_time_shift(mp_knee_angles_3d, opt_knee_angles, align_method="cross_correlation")
    print(f"Optimal time shift: {time_shift:.3f} sec")

    # Apply time shift to MediaPipe timestamps
    time_mp_shifted = time_mp + time_shift  

    # Interpolate MediaPipe angles onto OptiTrack time scale
    mp_interp_3d = interp1d(time_mp_shifted, mp_knee_angles_3d, kind='linear', fill_value="extrapolate")
    mp_knee_resampled_3d = mp_interp_3d(time_opt)

    mp_interp_2d = interp1d(time_mp_shifted, mp_knee_angles_2d, kind='linear', fill_value="extrapolate")
    mp_knee_resampled_2d = mp_interp_2d(time_opt)

    # Find the overlap region
    overlap_start = max(time_opt[0], time_mp_shifted[0])
    overlap_end = min(time_opt[-1], time_mp_shifted[-1])

    # Crop the data to the overlapping time region
    overlap_indices_opt = (time_opt >= overlap_start) & (time_opt <= overlap_end)
    overlap_indices_mp = (time_mp_shifted >= overlap_start) & (time_mp_shifted <= overlap_end)
    
    # Shift the time axes so that the overlap start becomes t=0
    time_opt_shifted = time_opt[overlap_indices_opt] - overlap_start
    time_mp_shifted_adjusted = time_mp_shifted[overlap_indices_mp] - overlap_start

    # Plot aligned data for the overlap region
    plt.figure(figsize=(12, 6))
    plt.plot(time_opt_shifted, opt_knee_angles[overlap_indices_opt], label="OptiTrack Knee Angle", linestyle='solid', color='C3')
    plt.plot(time_opt_shifted, mp_knee_resampled_3d[overlap_indices_opt], label="MediaPipe 3D", linestyle='solid', color='C0')
    plt.plot(time_opt_shifted, mp_knee_resampled_2d[overlap_indices_opt], label="MediaPipe 2D", linestyle='dashed', color='C0')
    plt.xlabel("Time (seconds)")
    plt.ylabel("Knee Angle (degrees)")
    plt.legend()
    plt.title(f"Aligned Squat Right Knee Angle Comparison: MediaPipe ({view}) vs OptiTrack")
    plt.grid(True, linestyle="--", alpha=0.6)
    plt.show()


# Main Execution
# Squat Front/Side
# opt_knee_angles = process_optitrack("media-pipe/validation/optitrack_data/squat_001.csv")
# mp_knee_angles_3d_side, mp_knee_angles_2d_side, fps_mp_side = process_video("media-pipe/images/squat_1_side.mp4")
# mp_knee_angles_3d_front, mp_knee_angles_2d_front, fps_mp_front = process_video("media-pipe/images/squat_1_front.MOV")

# Squat 45/45
opt_knee_angles = process_optitrack("media-pipe/validation/optitrack_data/squat_006.csv")
mp_knee_angles_3d_side, mp_knee_angles_2d_side, fps_mp_side = process_video("media-pipe/images/squat_6_45_close_leg.mp4")
mp_knee_angles_3d_front, mp_knee_angles_2d_front, fps_mp_front = process_video("media-pipe/images/squat_6_45_far_leg.MOV")

# # Leg extension Front/Side
# opt_knee_angles = process_optitrack("media-pipe/validation/optitrack_data/leg_extension_001.csv")
# mp_knee_angles_3d_side, mp_knee_angles_2d_side, fps_mp_side = process_video("media-pipe/images/leg_extension_1_side.mp4")
# mp_knee_angles_3d_front, mp_knee_angles_2d_front, fps_mp_front = process_video("media-pipe/images/leg_extension_1_front.MOV")

# # Leg extension 45/45
# opt_knee_angles = process_optitrack("media-pipe/validation/optitrack_data/leg_extension_006.csv")
# mp_knee_angles_3d_side, mp_knee_angles_2d_side, fps_mp_side = process_video("media-pipe/images/leg_extension_6_45_close_leg.mp4")
# mp_knee_angles_3d_front, mp_knee_angles_2d_front, fps_mp_front = process_video("media-pipe/images/leg_extension_6_45_far_leg.MOV")

# Plot results for side and front views
align_and_plot(mp_knee_angles_3d_side, mp_knee_angles_2d_side, fps_mp_side, opt_knee_angles, "Cam A - close 45º-view")
align_and_plot(mp_knee_angles_3d_front, mp_knee_angles_2d_front, fps_mp_front, opt_knee_angles, "Cam B - far 45º-view")
