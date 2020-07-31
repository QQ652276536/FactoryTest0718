package com.zistone.factorytest0718.util;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
