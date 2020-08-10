package com.zistone.factorytest0718.face.util;

import com.arcsoft.face.FaceInfo;

import java.util.List;

public class TrackUtil {

    public static boolean IsSameFace(FaceInfo faceInfo1, FaceInfo faceInfo2) {
        return faceInfo1.getFaceId() == faceInfo2.getFaceId();
    }

    public static void KeepMaxFace(List<FaceInfo> ftFaceList) {
        if (ftFaceList == null || ftFaceList.size() <= 1) {
            return;
        }
        FaceInfo maxFaceInfo = ftFaceList.get(0);
        for (FaceInfo faceInfo : ftFaceList) {
            if (faceInfo.getRect().width() > maxFaceInfo.getRect().width()) {
                maxFaceInfo = faceInfo;
            }
        }
        ftFaceList.clear();
        ftFaceList.add(maxFaceInfo);
    }

}
