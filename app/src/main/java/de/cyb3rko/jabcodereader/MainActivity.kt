package de.cyb3rko.jabcodereader

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.common.util.concurrent.ListenableFuture
import de.cyb3rko.jabcodelib.JabCodeLib
import de.cyb3rko.jabcodereader.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private lateinit var camera: Camera
    private val toast: Toast by lazy { Toast.makeText(applicationContext, "JAB found", Toast.LENGTH_SHORT) }
    private var capturingJob: Job? = null
    private val jabCodeLib by lazy { JabCodeLib() }

    companion object {
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

    private fun checkPermission() {
        if (checkSelfPermission("android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED) {
            Log.v("TAG", "Camera Permission is granted")
            initCamera()
        } else {
            Log.v("TAG", "Camera Permission is revoked")
            ActivityCompat.requestPermissions(this, arrayOf("android.permission.CAMERA"), 1)
        }
    }

    private fun initCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)
        camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)
        binding.previewView.previewStreamState.observe(this as LifecycleOwner) { streamState ->
            if (streamState == PreviewView.StreamState.STREAMING) { // if preview visible
                println("Ready...")
                lifecycleScope.launch(Dispatchers.IO) {
                    runCodeDetection { result ->
                        print("Result: $result")
                        if (result.toInt() == 0) {
                            playBeepSound()
                            toast.cancel()
                            toast.show()
                            playBeepSound()
                            (getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(VIBRATE_DURATION)
                        }
                    }
                }
            } else if (streamState == PreviewView.StreamState.IDLE) { // if preview not visible
                println("Not ready...")
            }
        }
    }

    private suspend fun runCodeDetection(onResult: (result: String) -> Unit) {
        while (true) {
            println("New run...")
            if (binding.previewView.previewStreamState.value != PreviewView.StreamState.STREAMING) {
                break
            }
            val file = File(applicationContext.cacheDir, "feed.png")
            val bitmap = withContext(Dispatchers.Main) {
                binding.previewView.bitmap
            } ?: continue
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, it)
            }
            val returnCode = jabCodeLib.detect()
            withContext(Dispatchers.Main) {
                onResult(returnCode.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::cameraProviderFuture.isInitialized && cameraProviderFuture.get() != null) {
            println("Continue capturing job...")
            capturingJob?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        println("Pause capturing job...")
        capturingJob?.cancel()
    }

    private fun playBeepSound() {
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100).startTone(
            ToneGenerator.TONE_PROP_BEEP,
            BEEP_DURATION
        )
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
