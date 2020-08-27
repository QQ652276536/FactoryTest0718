package com.zistone.factorytest0718.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 读取配置文件用的，如果开发环境改变会读取失败
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public final class MyPropertiesUtil {

    /**
     * （禁止外部实例化）
     */
    private MyPropertiesUtil() {
    }

    public static Properties GetValueProperties(Context context) {
        Properties properties = new Properties();
        InputStream inputStream = context.getClassLoader().getResourceAsStream("assets/config.properties");
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

}
