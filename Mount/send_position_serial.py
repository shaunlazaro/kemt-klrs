import serial
import time

if __name__ == '__main__':
    ser = serial.Serial('/dev/ttyUSB0', 115200)
    ser.setRTS(False)
    ser.setDTR(False)
    
    while True:
        print("Enter horizontal position :")
        h_pos = input()
        print("Enter vertical position :")
        v_pos = input()
        
        if h_pos == "exit" or v_pos == "exit":
            ser.close()
            break
        msg = "{},{}".format(h_pos,v_pos)
        print("sending hpos,vpos: {} to serial".format(msg))
        ser.write(msg.encode('utf-8'))
        line = ser.readline().decode('utf-8').rstrip()
        print(line)
        time.sleep(1)
        
        
