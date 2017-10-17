package com.example.azadljy.cameratest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;


import java.io.IOException;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    private TextureView textureView;
    private Camera camera;
    private static final int RC_CAMERA_PERM = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textureView = (TextureView) findViewById(R.id.textureview);
        textureView.setSurfaceTextureListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCamera();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        startPreview(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_CAMERA_PERM)
    public void getCamera() {
        if (hasCameraPermission()) {
            camera = CameraHelper.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            CameraHelper.setParameters(camera);
            CameraHelper.setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, camera);
            SurfaceTexture texture = textureView.getSurfaceTexture();
            if (null != texture) {
                startPreview(texture);
            }
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_camera),
                    RC_CAMERA_PERM,
                    Manifest.permission.CAMERA);
        }
    }

    private boolean hasCameraPermission() {
        return EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA);
    }

    public void startPreview(SurfaceTexture surface) {
        if (camera == null) {
            return;
        }
        try {
            byte[] bufferByte = new byte[textureView.getWidth() * textureView.getHeight() * 3 / 2];
            camera.addCallbackBuffer(bufferByte);
            camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    //// TODO: 2017/10/17 处理数据
                    camera.addCallbackBuffer(data);
                }
            });


            camera.setPreviewTexture(surface);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
