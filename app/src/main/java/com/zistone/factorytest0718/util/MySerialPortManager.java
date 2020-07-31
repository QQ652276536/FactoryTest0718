package com.zistone.factorytest0718.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

public final class MySerialPortManager {

    private static SerialPort _serialPort;
    private static InputStream _serialPortInputStream;
    private static OutputStream _serialPortOutputStream;

    /**
     * （禁止外部实例化）
     */
    private MySerialPortManager() {
    }

    public static SerialPort NewInstance(String fileName, int baudRate) throws IOException {
        _serialPort = new SerialPort(new File(fileName), baudRate, 0);
        _serialPortOutputStream = _serialPort.getOutputStream();
        _serialPortInputStream = _serialPort.getInputStream();
        return _serialPort;
    }

    public static void Close() {
        try {
            if (null != _serialPortOutputStream)
                _serialPortOutputStream.close();
            if (null != _serialPortInputStream)
                _serialPortInputStream.close();
            if (null != _serialPort)
                _serialPort.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void SendData(byte[] input) {
        try {
            _serialPortOutputStream.write(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] ReadData() {
        byte[] output = null;
        try {
            int size = _serialPortInputStream.available();
            while (size > 0) {
                output = new byte[size];
                _serialPortInputStream.read(output);
                size = _serialPortInputStream.available();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

}