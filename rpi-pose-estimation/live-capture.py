import cv2

# Open the camera (0 is usually the default camera)
cap = cv2.VideoCapture(0)

if not cap.isOpened():
    print("Error: Could not open camera.")
    exit()

while True:
    # Capture frame-by-frame
    ret, frame = cap.read()
    
    if not ret:
        print("Error: Failed to capture frame.")
        break
    
    # Display the resulting frame
    cv2.imshow('Camera Feed', frame)
    
    # Wait for key press and exit on 'Esc' key
    if cv2.waitKey(1) & 0xFF == 27:  # 27 is the ASCII code for the 'Esc' key
        break

# Release the camera and close all windows
cap.release()
cv2.destroyAllWindows()