package de.cyb3rko.jabcodelib.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.cyb3rko.jabcodelib.JabCodeLib
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Instrumented test, which will generate image files
 */
@RunWith(AndroidJUnit4::class)
internal class JabCodeGenerateTest {
    @Test
    fun generateBasic() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val inputFile = File(appContext.cacheDir, "data.txt")
        inputFile.delete()
        FileOutputStream(inputFile).use {
            it.write("Testing".encodeToByteArray())
        }
        val outputFile = File(appContext.cacheDir, "result.png")
        outputFile.delete()
        val generationResult = JabCodeLib().generate(
            inputFile.absolutePath,
            outputFile.absolutePath
        )
        Assert.assertEquals(generationResult, 0)
        Assert.assertEquals(outputFile.exists(), true)
        Assert.assertEquals(outputFile.length(), 6206)
    }

    @Test
    fun generateEmpty() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val inputFile = File(appContext.cacheDir, "data.txt")
        inputFile.delete()
        inputFile.createNewFile()
        val outputFile = File(appContext.cacheDir, "result.png")
        outputFile.delete()
        val generationResult = JabCodeLib().generate(
            inputFile.absolutePath,
            outputFile.absolutePath
        )
        Assert.assertEquals(generationResult, 1)
        Assert.assertEquals(outputFile.exists(), false)
    }
}
