package com.zistone.factorytest0718.util;

import android.bluetooth.le.ScanResult;

/**
 * 低功耗蓝牙相关的接口
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public interface BleListener {

    /**
     * 扫描到的设备
     *
     * @param result
     */
    void OnScanLeResult(ScanResult result);

    /**
     * 连接成功
     */
    void OnConnected();

    /**
     * 正在连接
     */
    void OnConnecting();

    /**
     * 断开连接
     */
    void OnDisConnected();

    /**
     * 数据发送成功
     *
     * @param byteArray
     */
    void OnWriteSuccess(byte[] byteArray);

    /**
     * 成功收到数据
     *
     * @param byteArray
     */
    void OnReadSuccess(byte[] byteArray);

}
