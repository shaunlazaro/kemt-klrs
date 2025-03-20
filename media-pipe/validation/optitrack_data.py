import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

# Load the CSV file, skipping unnecessary rows
csv_path = "media-pipe/validation/optitrack_data/squat_001.csv"  # Update with your actual file path
df = pd.read_csv(csv_path, skiprows=3)  # Skip the first 3 rows

df = df.drop(index=range(0, 3)).reset_index(drop=True)
print(df.head(5))
# df = df.drop(1,0)

# Rename columns for clarity (keeping only needed ones)
df = df.rename(columns={
    "S1:RTHI": "Thigh_X", "S1:RTHI.1": "Thigh_Y", "S1:RTHI.2": "Thigh_Z",
    "S1:RASI": "Hip_X", "S1:RASI.1": "Hip_Y", "S1:RASI.2": "Hip_Z",
    "S1:RKNE": "Knee_X", "S1:RKNE.1": "Knee_Y", "S1:RKNE.2": "Knee_Z",
    "S1:RANK": "Ankle_X", "S1:RANK.1": "Ankle_Y", "S1:RANK.2": "Ankle_Z",
    "S1:RHEE": "Heel_X", "S1:RHEE.1": "Heel_Y", "S1:RHEE.2": "Heel_Z"
})

# Keep only the necessary columns
df = df[["Thigh_X", "Thigh_Y", "Thigh_Z", "Hip_X", "Hip_Y", "Hip_Z", "Knee_X", "Knee_Y", "Knee_Z", "Ankle_X", "Ankle_Y", "Ankle_Z", "Heel_X", "Heel_Y", "Heel_Z"]]

# Function to calculate knee angle
def calculate_angle(a, b, c):
    """Calculate angle at point b given three 3D points."""
    a = np.array(a).astype(float)
    b = np.array(b).astype(float)
    c = np.array(c).astype(float)

    ba = a - b
    bc = c - b

    cosine_angle = np.dot(ba, bc) / (np.linalg.norm(ba) * np.linalg.norm(bc))
    angle = np.degrees(np.arccos(np.clip(cosine_angle, -1.0, 1.0)))

    return angle

# Compute knee angles for each frame
opt_knee_angles_1 = []
for _, row in df.iterrows():
    hip = (row["Hip_X"], row["Hip_Y"], row["Hip_Z"])
    knee = (row["Knee_X"], row["Knee_Y"], row["Knee_Z"])
    ankle = (row["Ankle_X"], row["Ankle_Y"], row["Ankle_Z"])
    
    knee_angle = calculate_angle(hip, knee, ankle)
    opt_knee_angles_1.append(knee_angle)

# Convert to NumPy array for analysis
opt_knee_angles_1 = np.array(opt_knee_angles_1)

# Display the first few knee angles
print("First few OptiTrack knee angles:", opt_knee_angles_1[:10])

# Compute knee angles for each frame
opt_knee_angles_2 = []
for _, row in df.iterrows():
    hip = (row["Thigh_X"], row["Thigh_Y"], row["Thigh_Z"])
    knee = (row["Knee_X"], row["Knee_Y"], row["Knee_Z"])
    ankle = (row["Ankle_X"], row["Ankle_Y"], row["Ankle_Z"])
    
    knee_angle = calculate_angle(hip, knee, ankle)
    opt_knee_angles_2.append(knee_angle)

# Convert to NumPy array for analysis
opt_knee_angles_2 = np.array(opt_knee_angles_2)

# Display the first few knee angles
print("First few OptiTrack knee angles:", opt_knee_angles_2[:10])

# Compute knee angles for each frame
opt_knee_angles_3 = []
for _, row in df.iterrows():
    hip = (row["Thigh_X"], row["Thigh_Y"], row["Thigh_Z"])
    knee = (row["Knee_X"], row["Knee_Y"], row["Knee_Z"])
    ankle = (row["Heel_X"], row["Heel_Y"], row["Heel_Z"])
    
    knee_angle = calculate_angle(hip, knee, ankle)
    opt_knee_angles_3.append(knee_angle)

# Convert to NumPy array for analysis
opt_knee_angles_3 = np.array(opt_knee_angles_3)

# Display the first few knee angles
print("First few OptiTrack knee angles:", opt_knee_angles_3[:10])


# Create a figure and plot the knee angles
plt.figure(figsize=(10, 5))
plt.plot(opt_knee_angles_1, linestyle='-', color='b', label="Knee Angle (ASI)")
plt.plot(opt_knee_angles_2, linestyle='--', color='r', label="Knee Angle (THI)")
plt.plot(opt_knee_angles_3, linestyle='--', color='g', label="Knee Angle (HEE)")

# Label the axes
plt.xlabel("Frame")
plt.ylabel("Knee Angle (degrees)")
plt.title("Knee Angle Over Time (OptiTrack)")

# Add grid and legend
plt.grid(True, linestyle='--', alpha=0.6)
plt.legend()

# Show the plot
plt.show()



# Optional: Save the extracted angles to a new CSV
# df_out = pd.DataFrame({"Frame": np.arange(len(opt_knee_angles_1)), "OptiTrack_Knee_Angle": opt_knee_angles_1})
# df_out.to_csv("optitrack_knee_angles.csv", index=False)

print("Processed knee angles saved to 'optitrack_knee_angles.csv'")
