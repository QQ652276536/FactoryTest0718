package com.zistone.factorytest0718;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 系统基本信息
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class SystemIntoActivity extends BaseActivity {

    private static final String TAG = "SystemIntoActivity";

    private TextView _txtType, _txtManu, _txtSystem, _txtHardware, _txtVersion, _txtKernel, _txtResolution, _txtDensity, _txtRoot, _txtBuildId, _txtBoardId, _txtTime, _txtCpu;

    /**
     * 判断是否存在su命令并且有执行权限
     *
     * @return
     */
    public boolean IsRoot() {
        File file;
        String[] paths = {"/system/bin/", "/system/xbin/", "/system/sbin/", "/sbin/", "/vendor/bin/", "/su/bin/"};
        try {
            for (String path : paths) {
                file = new File(path + "su");
                if (file.exists() && file.canExecute()) {
                    Log.i(TAG, "SU路径：" + path);
                    return true;
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return false;
    }

    /***
     * 获取内核版本信息
     */
    public String GetKernalInfo() {
        Process process = null;
        String kernel = "";
        try {
            process = Runtime.getRuntime().exec("cat /proc/version");
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream inputStream = process.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String result = "";
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "获取Linux内核版本信息：" + result);
        if (!"".equals(result)) {
            String Keyword = "version ";
            int index = result.indexOf(Keyword);
            line = result.substring(index + Keyword.length());
            index = line.indexOf(" ");
            kernel = line.substring(0, index);
        }
        return kernel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_system_into);
        SetBaseContentView(R.layout.activity_system_into);
        //设备型号
        _txtType = findViewById(R.id.txt_type_systeminfo);
        _txtType.setText(Build.MODEL);
        //设备制造商
        _txtManu = findViewById(R.id.txt_manu_systeminfo);
        _txtManu.setText(Build.MANUFACTURER);
        //系统定制商
        _txtSystem = findViewById(R.id.txt_system_systeminfo);
        _txtSystem.setText(Build.BRAND);
        //硬件制造商
        _txtHardware = findViewById(R.id.txt_hardware_systeminfo);
        _txtHardware.setText(Build.HARDWARE);
        //系统版本
        _txtVersion = findViewById(R.id.txt_version_systeminfo);
        _txtVersion.setText(Build.VERSION.RELEASE);
        //Kernel版本
        _txtKernel = findViewById(R.id.txt_kernel_systeminfo);
        _txtKernel.setText(GetKernalInfo());
        //屏幕分辨率
        _txtResolution = findViewById(R.id.txt_resolution_systeminfo);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        _txtResolution.setText(displayMetrics.widthPixels + "X" + displayMetrics.heightPixels);
        //屏幕密度
        _txtDensity = findViewById(R.id.txt_density_systeminfo);
        int densityDpi = displayMetrics.densityDpi;
        _txtDensity.setText(densityDpi + "dpi");
        //Root状态
        _txtRoot = findViewById(R.id.txt_root_systeminfo);
        _txtRoot.setText(IsRoot() ? "是" : "否");
        //生产编号
        _txtBuildId = findViewById(R.id.txt_buildid_systeminfo);
        _txtBuildId.setText(Build.ID);
        //主板编号
        _txtBoardId = findViewById(R.id.txt_boardid_systeminfo);
        _txtBoardId.setText(Build.BOARD);
        //编译时间
        _txtTime = findViewById(R.id.txt_time_systeminfo);
        long timeLong = Build.TIME;
        Log.i(TAG, "编译时间：" + timeLong);
        Date date = new Date(Build.TIME);
        String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        _txtTime.setText(timeStr);
        _txtCpu = findViewById(R.id.txt_cpu_systeminfo);
        _txtCpu.setText("");
        for (String temp : Build.SUPPORTED_ABIS)
            _txtCpu.append(temp + " ");
    }
}
