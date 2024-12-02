import time
from picamera2 import Picamera2, Preview
from libcamera import Transform

picam = Picamera2()

config = picam.create_preview_configuration({"size":(1920,1080)})
picam.configure(config)

picam.start_preview(Preview.QTGL)

picam.start()
time.sleep(2)

try:
    input("Streaming video, Press Any Key to stop")
finally:
    picam.stop()
    picam.stop_preview()