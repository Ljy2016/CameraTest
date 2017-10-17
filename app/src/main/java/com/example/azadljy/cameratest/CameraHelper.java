package com.example.azadljy.cameratest;


import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.widget.Toast;

/**
 * 作者：Ljy on 2017/10/16.
 * 功能：我的——我的资料
 */


public class CameraHelper {
    private Camera camera;


    public static Camera openCamera(int cameraId) {
        Camera camera;
        try {
            if (cameraId < 0) {
                camera = Camera.open();
            } else {
                camera = Camera.open(cameraId);
            }
            return camera;
        } catch (Exception e) {
            Log.e("TAG", "openCamera: " + e.getMessage());
        }
        return null;
    }


    public static void setParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        camera.setParameters(parameters);
    }

    //修正方向
    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }
}
