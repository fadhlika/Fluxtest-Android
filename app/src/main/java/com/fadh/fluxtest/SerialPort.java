package com.fadh.fluxtest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by fadhlika on 3/11/2017.
 */

public class SerialPort {
    private final String ACTION_USB_PERMISSION = "com.fadh.fluxtest.USB_PERMISSION";
    private final String ACTION_USB_ATTACHED = "com.fadh.fluxtest.USB_DEVICE_ATTACHED";
    private final String ACTION_USB_DEATTACHED = "com.fadh.fluxtest.USB_DEVICE_DEATTACHED";
    public boolean serialConnected = false;
    private UsbManager manager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serial;
    private Context context;
    private Handler handler;
    private int baudrate = 9600;
    private UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] bytes) {
            if (handler != null) handler.obtainMessage(0, bytes).sendToTarget();
        }
    };
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "mUsbReceiver");
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        connection = manager.openDevice(device);
                        new ConnectionThread().run();
                        Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT);
                    } else {
                        Log.d(TAG, "Permission denied for device" + device);
                    }
                }
            } else if (ACTION_USB_ATTACHED.equals(action)) {
                getPort();
                Toast.makeText(context, "Device attached", Toast.LENGTH_SHORT);
            } else if (ACTION_USB_DEATTACHED.equals(action)) {
                if (serialConnected) serial.close();
                Toast.makeText(context, "Device deattached", Toast.LENGTH_SHORT);
            }
        }
    };

    public SerialPort(Context context_, Handler mHandler) {
        this.context = context_;
        this.handler = mHandler;

        manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(mUsbReceiver, filter);

        Log.d(TAG, "SerialPort class initialized");
    }

    public void getPort() {
        HashMap<String, UsbDevice> devices = manager.getDeviceList();
        if (!devices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : devices.entrySet()) {
                device = entry.getValue();

                if (device.getVendorId() != 0x1d6b || (device.getProductId() != 0x0001 || device.getProductId() != 0x0002 || device.getProductId() != 0x0003)) {
                    PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    manager.requestPermission(device, permissionIntent);
                    keep = false;
                } else {
                    device = null;
                    connection = null;
                }
                if (!keep) break;
            }
        }
    }

    public void closeSerial() {
        if (serialConnected) {
            serial.close();
            serialConnected = false;
            Log.d(TAG, "Device disconnected");
        }
    }

    public void changeBaud(int baud) {
        if (!serialConnected) {
            this.baudrate = baud;
            Log.d("ChangeBaud", "" + baud);
        } else Log.d("ChangeBaud", "Serial is still connected");
    }

    public void destroyReceiver() {
        context.unregisterReceiver(mUsbReceiver);
    }

    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            serial = UsbSerialDevice.createUsbSerialDevice(device, connection);
            if (serial != null) {
                if (serial.open()) {
                    serialConnected = true;
                    serial.setBaudRate(baudrate);
                    serial.setDataBits(UsbSerialInterface.DATA_BITS_8);
                    serial.setStopBits(UsbSerialInterface.STOP_BITS_1);
                    serial.setParity(UsbSerialInterface.PARITY_NONE);
                    serial.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                    serial.read(mCallback);
                    Log.d(TAG, "device connected");
                    Toast.makeText(context, "Device Connected", Toast.LENGTH_SHORT);
                } else {
                    Toast.makeText(context, "Device Connection failed", Toast.LENGTH_SHORT);
                }
            } else {
                Toast.makeText(context, "No Device Connected", Toast.LENGTH_SHORT);
            }
        }
    }
}
