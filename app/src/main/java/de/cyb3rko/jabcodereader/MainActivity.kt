package de.cyb3rko.jabcodereader

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.AsyncTask
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import de.cyb3rko.jabcodereader.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset

class MainActivity : AppCompatActivity(), CameraPreview.PreviewReadyCallback {
    private lateinit var binding: ActivityMainBinding

    private var camera: Camera? = null
    private var cameraOrientation = 0
    private var cameraPreview: CameraPreview? = null
    private var detecting = false

    external fun detect(): Int

    companion object {
        init {
            System.loadLibrary("jabcodelib")
        }
        private const val BEEP_DURATION = 800
        private const val VIBRATE_DURATION = 200L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (checkCameraHardware(baseContext)) {
            checkPermission()
        }
    }

    private fun checkCameraHardware(context: Context): Boolean {
        return context.packageManager.hasSystemFeature("android.hardware.camera")
    }

    private fun loadApplication() {
        cameraPreview = CameraPreview(this)
        camera = cameraPreview!!.camera
        cameraPreview!!.setOnPreviewReady(this)
        (binding.container as ViewGroup).addView(cameraPreview, 0)
    }

    private fun checkPermission() {
        if (checkSelfPermission("android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED) {
            Log.v("TAG", "Camera Permission is granted")
        } else {
            Log.v("TAG", "Camera Permission is revoked")
            ActivityCompat.requestPermissions(this, arrayOf("android.permission.CAMERA"), 1)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }

    override fun onPreviewFrame(bitmap: Bitmap?, bArr: ByteArray?, i: Int) {
        if (!detecting) {
            bitmap?.let {
                val detectionTaskParams = DetectionTaskParams(bitmap, bArr!!)
                DetectionTask().execute(*arrayOf(detectionTaskParams))
            }
        }
        cameraOrientation = i
    }

    override fun onResume() {
        super.onResume()
        if (camera == null) {
            loadApplication()
        }
    }

    override fun onStop() {
        super.onStop()
        camera = null
    }

    private class DetectionTaskParams(
        var bitmap: Bitmap,
        var bitmapBytes: ByteArray
    )

    private inner class DetectionTask : AsyncTask<DetectionTaskParams?, Int?, ByteArray>() {
        var toast: Toast? = null

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: DetectionTaskParams?): ByteArray {
            params[0]?.bitmap?.let { bitmap ->
//                return detect(bitmap.convertToByteArray())
                val file = File(applicationContext.cacheDir, "feed.png")
                FileOutputStream(file).use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                val returnCode = detect()
                if (returnCode == 0) {
                    toast?.cancel()
                    runOnUiThread {
                        toast = Toast.makeText(applicationContext, "JAB found", Toast.LENGTH_SHORT)
                        toast!!.show()
                        playBeepSound()
                        (getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(VIBRATE_DURATION)
                    }
                }
                return byteArrayOf()
            }
            return byteArrayOf()
        }

        @Deprecated("Deprecated in Java")
        public override fun onPreExecute() {
            super.onPreExecute()
            this@MainActivity.detecting = true
        }

        @Deprecated("Deprecated in Java")
        override fun onProgressUpdate(vararg numArr: Int?) {
            super.onProgressUpdate(*numArr)
        }

        @Deprecated("Deprecated in Java")
        public override fun onPostExecute(result: ByteArray?) {
            detecting = false
//            println("Returned, ${result?.size}")
//            if (returnObject != null) {
//                onDetectionTaskComplete(returnObject)
//            }
        }
    }

    fun playBeepSound() {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100).startTone(
            ToneGenerator.TONE_PROP_BEEP,
            BEEP_DURATION
        )
    }

    private fun onDetectionTaskComplete(returnObject: ReturnObject) {
        val str: String
        val status = returnObject.status
        str = if (status != 1) {
            if (status != 3) BuildConfig.BUILD_TYPE else String(
                returnObject.data,
                Charset.forName("ISO-8859-15")
            )
        } else {
            BuildConfig.BUILD_TYPE
        }
        if (str.isNotEmpty()) {
            camera!!.stopPreview()
            camera!!.setPreviewCallback(null as PreviewCallback?)
            val intent = Intent(applicationContext, Result::class.java)
            intent.putExtra("result", str)
            startActivity(intent)
        } else if (camera != null) {
            cameraPreview!!.takePicture()
        }
    }

    // Thanks to https://stackoverflow.com/a/47755479
    private fun Bitmap.convertToByteArray(): ByteArray {
        //minimum number of bytes that can be used to store this bitmap's pixels
        val size = this.byteCount
        //allocate new instances which will hold bitmap
        val buffer = ByteBuffer.allocate(size)
        val bytes = ByteArray(size)
        //copy the bitmap's pixels into the specified buffer
        this.copyPixelsToBuffer(buffer)
        //rewinds buffer (buffer position is set to zero and the mark is discarded)
        buffer.rewind()
        //transfer bytes from buffer into the given destination array
        buffer.get(bytes)
        //return bitmap's pixels
        return bytes
    }
}
