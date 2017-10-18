package com.example.azadljy.cameratest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;


import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvClearMemStorage;
import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_ROUGH_SEARCH;
import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_FIND_BIGGEST_OBJECT;
import static org.bytedeco.javacpp.opencv_objdetect.cvHaarDetectObjects;


public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    private TextureView textureView;
    private ImageView iv_bitmap_show;
    private Camera camera;
    private static final int RC_CAMERA_PERM = 123;
    private boolean isStart;
    public static final String TAG = "test";
    public static final int SUBSAMPLING_FACTOR = 4;
    private opencv_core.IplImage grayImage;
    private opencv_core.CvMemStorage storage;
    private opencv_core.CvSeq faces;
    private opencv_objdetect.CvHaarClassifierCascade classifier;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        textureView = (TextureView) findViewById(R.id.textureview);
        iv_bitmap_show = (ImageView) findViewById(R.id.iv_bitmap_show);
        textureView.setSurfaceTextureListener(this);
        storage = opencv_core.CvMemStorage.create();
        File classifierFile = null;
        try {
            classifierFile = Loader.extractResource(getClass(),
                    "/org/bytedeco/javacv/facepreview/haarcascade_frontalface_alt.xml",
                    context.getCacheDir(), "classifier", ".xml");
        } catch (IOException e) {
            e.printStackTrace();
        }
//        if (classifierFile == null || classifierFile.length() <= 0) {
//            throw new IOException("Could not extract the classifier file from Java resource.");
//        }

        // Preload the opencv_objdetect module to work around a known bug.
        Loader.load(opencv_objdetect.class);
        classifier = new opencv_objdetect.CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
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
            final byte[] bufferByte = new byte[textureView.getWidth() * textureView.getHeight() * 10];
            Log.e(TAG, "startPreview: 字节数组的长度" + bufferByte.length);
            camera.addCallbackBuffer(bufferByte);
            camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    if (isStart) {
                        Log.e("TAG", "onPreviewFrame:开始 ");
                        isStart = false;
                        long startTime = System.currentTimeMillis();
                        Camera.Size size = camera.getParameters().getPreviewSize();
//                        Bitmap bmp = CameraHelper.rawByteArray2RGBABitmap(data, size.width, size.height);
//                        iv_bitmap_show.setImageBitmap(bmp);
//                        long endTime = System.currentTimeMillis();
//                        Log.e(TAG, "onPreviewFrame: 耗时" + (endTime - startTime));
                        Log.e(TAG, "onPreviewFrame: " + size.height + "----" + size.width);
                        Log.e(TAG, "onPreviewFrame: " + textureView.getWidth() + "----" + textureView.getHeight());
                        try {
                            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                            if (image != null) {
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, stream);
                                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
                                iv_bitmap_show.setImageBitmap(bmp);
                                //TODO：此处可以对位图进行处理，如显示，保存等
                                long endTime = System.currentTimeMillis();
                                Log.e(TAG, "onPreviewFrame: 耗时" + (endTime - startTime));
                                stream.close();
                            }
                        } catch (Exception ex) {
                            Log.e("Sys", "Error:" + ex.getMessage());
                        }
                    }
                    camera.addCallbackBuffer(data);
                }
            });
            camera.setPreviewTexture(surface);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void go(View view) {
        isStart = true;
        Log.e(TAG, "go: 点击了");
    }

    protected void processImage(byte[] data, int width, int height) {
        // First, downsample our image and convert it into a grayscale IplImage
        int f = SUBSAMPLING_FACTOR;
        if (grayImage == null || grayImage.width() != width / f || grayImage.height() != height / f) {
            grayImage = opencv_core.IplImage.create(width / f, height / f, IPL_DEPTH_8U, 1);
        }
        int imageWidth = grayImage.width();
        int imageHeight = grayImage.height();
        int dataStride = f * width;
        int imageStride = grayImage.widthStep();
        ByteBuffer imageBuffer = grayImage.getByteBuffer();
        for (int y = 0; y < imageHeight; y++) {
            int dataLine = y * dataStride;
            int imageLine = y * imageStride;
            for (int x = 0; x < imageWidth; x++) {
                imageBuffer.put(imageLine + x, data[dataLine + f * x]);
            }
        }

        cvClearMemStorage(storage);
        faces = cvHaarDetectObjects(grayImage, classifier, storage, 1.1, 3,
                CV_HAAR_FIND_BIGGEST_OBJECT | CV_HAAR_DO_ROUGH_SEARCH);
        // TODO: 2017/10/18      绘制边框

    }


}
