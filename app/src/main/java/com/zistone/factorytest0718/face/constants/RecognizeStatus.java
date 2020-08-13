package com.zistone.factorytest0718.face.constants;

/**
 * 人证比对的状态
 */
public enum RecognizeStatus {
    /**
     * 等待输入证件照
     */
    STATUS_WAITING_CARD,
    /**
     * 等待画面中的人脸
     */
    STATUS_WAITING_FACE,
    /**
     * 比对通过
     */
    STATUS_COMPARE_FINISHED_CHECK_SUCCESS,
    /**
     * 比对失败
     */
    STATUS_COMPARE_FINISHED_CHECK_FAIL
}