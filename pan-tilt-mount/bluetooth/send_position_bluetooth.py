import asyncio
from bleak import BleakClient, BleakScanner

# ESP32 BLE information
DEVICE_NAME = "PhysiKneed-BT"  # Name of the BLE server
CHARACTERISTIC_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"  # The UUID of the characteristic

# Discover BLE devices to find the ESP32
async def discover_and_write():
    print("Scanning for BLE devices...")
    devices = await asyncio.sleep(0.1) or await BleakScanner.discover()
    
    esp32_address = None
    for device in devices:
        print(f"Found device: {device.name}, Address: {device.address}")
        if device.name == DEVICE_NAME:
            esp32_address = device.address
            break
    
    if not esp32_address:
        print(f"Device named '{DEVICE_NAME}' not found.")
        return
    
    print(f"Connecting to {DEVICE_NAME} at address {esp32_address}...")
    
    async with BleakClient(esp32_address) as client:
        print(f"Connected: {await client.is_connected()}")
        #for i in ["60,60","40,40","50,50","80,80","90,90","46,60","45,90"]:
        while True:
            print("Enter horizontal position :")
            h_pos = input()
            print("Enter vertical position :")
            v_pos = input()
            
            if h_pos == "exit" or v_pos == "exit":
                break
            message = "{},{}".format(h_pos,v_pos)
            
        
            await client.write_gatt_char(CHARACTERISTIC_UUID, message.encode(),True)
            print(f"Message '{message}' sent to {esp32_address}")
            await asyncio.sleep(0.5)
            

        await client.disconnect()
        print(f"disconnected client")


        


if __name__ == '__main__':
    asyncio.run(discover_and_write())

        
