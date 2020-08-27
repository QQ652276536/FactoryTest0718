/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zistone.factorytest0718.util;

import android.util.Log;

import com.zistone.gpio.Gpio;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

/**
 * 誉兴通设备的扫码工具类
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public final class MyScanCodeManager {

    private static final String TAG = "MyScanCodeManager";
    public static final int MAX_SIZE = 2048;

    private static SerialPort _serialPort;
    private static OutputStream _outputStream;
    private static InputStream _inputStream;
    private static ReadThread _readThread = null;
    private static boolean _isReadThreadFlag = false;
    private static ScanCodeListener _scanCodeListener;
    private static Gpio _gpio = Gpio.getInstance();

    public interface ScanCodeListener {
        /**
         * 扫码回调
         *
         * @param data 扫到的数据长度
         * @param len  数据长度
         */
        void onReceived(byte[] data, int len);
    }

    private static class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted() && _isReadThreadFlag) {
                try {
                    int size;
                    byte[] buffer = new byte[MAX_SIZE];
                    if (_inputStream == null) {
                        continue;
                    }
                    size = _inputStream.read(buffer);
                    if (size > 0) {
                        if (size > MAX_SIZE)
                            size = MAX_SIZE;
                        _scanCodeListener.onReceived(buffer, size);
                        _gpio.set_gpio(0, 99);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                    //用interrupt结束线程的话会触发InterruptedException异常，捕获后break即可结束线程
                    break;
                }
            }
        }
    }

    /**
     * （禁止外部实例化）
     */
    private void ScanTestSerialPortManager() {
    }

    public static void Init(ScanCodeListener listener) {
        _scanCodeListener = listener;
    }

    /**
     * 打开扫码设备
     *
     * @param device   设备节点
     * @param baudrate 波特率
     * @param flag     文件操作标志
     * @throws IOException
     */
    public static void OpenSerialPort(File device, int baudrate, int flag) throws IOException {
        if (_serialPort == null) {
            _serialPort = new SerialPort(device, baudrate, flag);
            if (_serialPort != null) {
                _outputStream = _serialPort.getOutputStream();
                _inputStream = _serialPort.getInputStream();
                Log.i(TAG, "打开扫描串口");
            }
        }
    }

    /**
     * 开启读取串口数据的线程
     */
    public static void StartReadThread() {
        _isReadThreadFlag = true;
        _readThread = new ReadThread();
        _readThread.start();
        _gpio.set_gpio(1, 99);
        Log.i(TAG, "开启读取扫描串口数据的线程");
    }

    /**
     * 停止读取数据的线程
     */
    public static void StopReadThread() {
        _isReadThreadFlag = false;
        if (null != _readThread)
            _readThread.interrupt();
        _readThread = null;
        _gpio.set_gpio(0, 99);
        Log.i(TAG, "停止读取数据的线程");
    }

    public static int Write(byte[] data) {
        if (_outputStream != null && data != null) {
            try {
                _outputStream.write(data);
                _outputStream.flush();
                return 0;
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        } else
            return -2;
    }

    public static void Close() {
        _isReadThreadFlag = false;
        if (null != _readThread)
            _readThread.interrupt();
        _readThread = null;
        if (null != _serialPort)
            _serialPort.close();
        _serialPort = null;
        Log.i(TAG, "停止读取数据的线程并关闭扫描串口");
    }

}
