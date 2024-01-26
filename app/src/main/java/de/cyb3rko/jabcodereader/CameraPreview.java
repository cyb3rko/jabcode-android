package de.cyb3rko.jabcodereader;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    byte[] bytes;
    private Camera camera;
    private SurfaceHolder holder;
    PreviewReadyCallback mPreviewReadyCallback;
    int orientation;
    List<Camera.Size> possibleSizes;

    public interface PreviewReadyCallback {
        void onPreviewFrame(Bitmap bitmap, byte[] bArr, int i);
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
    }

    public void setOnPreviewReady(PreviewReadyCallback previewReadyCallback) {
        this.mPreviewReadyCallback = previewReadyCallback;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public CameraPreview(Context context) {
        super(context);
        this.camera = null;
        this.holder = null;
        this.mPreviewReadyCallback = null;
        this.camera = openCamera();
        this.holder = getHolder();
        this.holder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            configureCamera();
            this.camera.setPreviewDisplay(this.holder);
            this.camera.startPreview();
            takePicture();
        } catch (IOException e) {
            Log.d("ContentValues", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void takePicture() {
        this.camera.setPreviewCallback(new Camera.PreviewCallback() {
            public void onPreviewFrame(byte[] bArr, Camera camera) {
                int i = camera.getParameters().getPreviewSize().width;
                int i2 = camera.getParameters().getPreviewSize().height;
                RenderScript create = RenderScript.create(CameraPreview.this.getContext());
                ScriptIntrinsicYuvToRGB create2 = ScriptIntrinsicYuvToRGB.create(create, Element.U8_4(create));
                Allocation createTyped = Allocation.createTyped(create, new Type.Builder(create, Element.U8(create)).setX(bArr.length).create(), 1);
                Allocation createTyped2 = Allocation.createTyped(create, new Type.Builder(create, Element.RGBA_8888(create)).setX(i).setY(i2).create(), 1);
                createTyped.copyFrom(bArr);
                create2.setInput(createTyped);
                create2.forEach(createTyped2);
                Bitmap createBitmap = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
                createTyped2.copyTo(createBitmap);
                ByteBuffer allocate = ByteBuffer.allocate(createBitmap.getByteCount());
                createBitmap.copyPixelsToBuffer(allocate);
                allocate.rewind();
                CameraPreview.this.bytes = allocate.array();
                CameraPreview.this.mPreviewReadyCallback.onPreviewFrame(createBitmap, CameraPreview.this.bytes, CameraPreview.this.orientation);
            }
        });
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        releaseCamera();
    }

    private void releaseCamera() {
        if (this.camera != null) {
            this.holder.removeCallback(this);
            this.camera.stopPreview();
            this.camera.setPreviewCallback((Camera.PreviewCallback) null);
            this.camera.release();
            this.camera = null;
        }
    }

    private Camera openCamera() {
        try {
            return Camera.open();
        } catch (Exception unused) {
            return null;
        }
    }

    public void configureCamera() {
        Camera.Parameters parameters = this.camera.getParameters();
        parameters.getSupportedPreviewFormats();
        this.possibleSizes = this.camera.getParameters().getSupportedPreviewSizes();
        parameters.setFocusMode("continuous-picture");
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(0, cameraInfo);
        this.orientation = cameraInfo.orientation;
        parameters.setRotation(this.orientation);
        this.camera.setDisplayOrientation(this.orientation);
        this.camera.setParameters(parameters);
    }
}
