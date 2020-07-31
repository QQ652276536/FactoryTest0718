package com.zistone.factorytest0718.util;

public interface DialogFragmentListener {
    void OnDismiss(String tag);

    void OnComfirm(String tag, String str);

    void OnComfirm(String tag, Object[] objectArray);
}
