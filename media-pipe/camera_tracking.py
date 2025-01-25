import time
import serial

# TODO: Revise the motor control for smoother and more precise movement

# Define frame dimensions and tolerance range
TOLERANCE = 50  # Pixels from the center where no updates are sent
MAX_MOVE_DISTANCE = 25  # Move no more than this many units.
TIME_THRESHOLD = 1.5

class MotorController:
    def __init__(self, port='COM3', baudrate=115200):
        try:
            self.ser = serial.Serial(port, baudrate)
            self.ser.setRTS(False)
            self.ser.setDTR(False)
            print(f"Serial initialized on {port}")
        except Exception as e:
            self.ser = None
            print(f"Error initializing serial:\n{e}\n")
        
        # Initialize motor positions
        self.motor_x = 90
        self.motor_y = 45
        self.last_sent = time.time()

        # Send initial position
        self.update_position()

    def update_position(self):
        if self.ser:
            msg = "{},{}".format(self.motor_x, self.motor_y)
            self.ser.write(msg.encode('utf-8'))
            print(f"Sent motor positions: X={self.motor_x}, Y={self.motor_y}")

    def check_offset_tolerance(self, offset_x, offset_y):
        cur = time.time()
        if self.last_sent and cur - self.last_sent > TIME_THRESHOLD:
            if abs(offset_x) > TOLERANCE or abs(offset_y) > TOLERANCE:
                if offset_x > TOLERANCE and self.motor_x > 0:
                    self.motor_x -= min(round(offset_x / 10), MAX_MOVE_DISTANCE)
                elif offset_x < -TOLERANCE and self.motor_x < 180:
                    self.motor_x -= max(round(offset_x / 10), -MAX_MOVE_DISTANCE)

                if offset_y > TOLERANCE and self.motor_y > 0:
                    self.motor_y -= min(round(offset_y / 10), MAX_MOVE_DISTANCE)
                elif offset_y < -TOLERANCE and self.motor_y < 180:
                    self.motor_y -= max(round(offset_y / 10), -MAX_MOVE_DISTANCE)

                self.update_position()
                self.last_sent = time.time()

    @staticmethod
    def get_offset(all_points, frame_width, frame_height):
        # Calculate the midpoint of all landmarks
        x_coords, y_coords = zip(*all_points)  # Separate x and y coordinates
        midpoint = (sum(x_coords) / len(x_coords), sum(y_coords) / len(y_coords)) # NOTE: (y, x) format

        # Convert midpoint from world coordinates to pixel coordinates
        pixel_midpoint = (int(midpoint[0] * frame_width), int(midpoint[1] * frame_height))

        # Calculate offsets from frame center
        offset_x = pixel_midpoint[0] - frame_width / 2
        offset_y = pixel_midpoint[1] - frame_height / 2

        return pixel_midpoint, offset_x, offset_y
