import serial
import serial.tools.list_ports
import time

def find_esp32_serial_port(device_name="RePose-BT"):
    """Find the serial port associated with the ESP32 Bluetooth device."""
    print("Scanning available serial ports...")
    ports = serial.tools.list_ports.comports()

    for port in ports:
        if device_name in port.description or device_name in port.name:
            print(f"Found {device_name} on {port.device}")
            return port.device

    print(f"No matching serial port found for {device_name}.")
    return None

def connect_bluetooth_serial(device_name="RePose-BT", baudrate=115200, timeout=2):
    """Automatically find and connect to the ESP32 Bluetooth serial port."""
    port = find_esp32_serial_port(device_name)
    if not port:
        return

    try:
        ser = serial.Serial(port, baudrate, timeout=timeout)
        print(f"Connected to {device_name} on {port} at {baudrate} baud")

        # Example: Send and receive data
        ser.write(b'Hello ESP32!\n')
        time.sleep(1)

        if ser.in_waiting > 0:
            received = ser.readline().decode('utf-8').strip()
            print(f"Received: {received}")

        ser.close()
    except serial.SerialException as e:
        print(f"Failed to connect: {e}")

if __name__ == "__main__":
    connect_bluetooth_serial()
