package com.maydaycode.izup

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.opencv.imgproc.Imgproc
import org.opencv.core.Core
import org.opencv.core.MatOfDouble
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import android.provider.MediaStore
import android.content.Intent
import android.widget.ImageView
import java.io.File
import android.view.View
import android.widget.TextView
import java.math.BigDecimal
import android.graphics.BitmapFactory
import android.support.v4.content.FileProvider.getUriForFile
import org.opencv.core.CvType

class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_TAKE_PHOTO = 1

    private val tag = MainActivity::class.java!!.name
    private var imageFile: File? = null

    fun Double.roundTo2DecimalPlaces() = BigDecimal(this).setScale(2, BigDecimal.ROUND_HALF_UP).toDouble()

    init {
        System.loadLibrary("opencv_java3")
    }

    //base algorithm
    fun blurLevel1(path: String?): Double {
        val image = Imgcodecs.imread(path, Imgcodecs.CV_LOAD_IMAGE_COLOR)
        if (image.empty()) {
            Log.e(tag, "CANNOT OPEN IMAGE!")
            return 0.0
        } else {

            Log.d(tag, "Image: $image")
            val destination = Mat()
            val matGray = Mat()

            Imgproc.cvtColor(image, matGray, Imgproc.COLOR_BGR2GRAY)
            Imgproc.Laplacian(matGray, destination, 3)
            val median = MatOfDouble()
            val std = MatOfDouble()
            Core.meanStdDev(destination, median, std)

            return Math.pow(std.get(0, 0)[0], 2.0)
        }
    }

    //very similar to blurLevel1
    fun blurLevel2(path: String?): Double {
        val image = Imgcodecs.imread(path, Imgcodecs.CV_LOAD_IMAGE_COLOR)
        if (image.empty()) {
            Log.e(tag, "CANNOT OPEN IMAGE!")
            return 0.0
        } else {
            val destination = Mat()
            val matGray = Mat()
            val kernel = object : Mat(3, 3, CvType.CV_32F) {
                init {
                    put(0, 0, 0.0)
                    put(0, 1, -1.0)
                    put(0, 2, 0.0)

                    put(1, 0, -1.0)
                    put(1, 1, 4.0)
                    put(1, 2, -1.0)

                    put(2, 0, 0.0)
                    put(2, 1, -1.0)
                    put(2, 2, 0.0)
                }
            }
            Imgproc.cvtColor(image, matGray, Imgproc.COLOR_BGR2GRAY)
            Imgproc.filter2D(matGray, destination, -1, kernel)
            val median = MatOfDouble()
            val std = MatOfDouble()
            Core.meanStdDev(destination, median, std)

            return Math.pow(std.get(0, 0)[0], 2.0)
        }
    }

    //standard deviation based algorithm
    fun blurLevel3(path: String?): Double {
        val image = Imgcodecs.imread(path, Imgcodecs.CV_LOAD_IMAGE_COLOR)
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY)
        val mu = MatOfDouble() // mean
        val sigma = MatOfDouble() // standard deviation
        Core.meanStdDev(image, mu, sigma)
        return Math.pow(mu.get(0, 0)[0], 2.0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imf = imageFile
            if ((imf != null) && imf.exists()) {
                val bitmap = BitmapFactory.decodeFile(imf.absolutePath)
                val imageView = findViewById<ImageView>(R.id.imageView)
                imageView.setImageBitmap(bitmap)

                //01
                val blurLevelTextView1: TextView = findViewById(R.id.blurLevelTextView1)
                blurLevelTextView1.text = blurLevel1(imf.absolutePath).roundTo2DecimalPlaces().toString()

                //02
                val blurLevelTextView2: TextView = findViewById(R.id.blurLevelTextView2)
                blurLevelTextView2.text = blurLevel2(imf.absolutePath).roundTo2DecimalPlaces().toString()

                //03
                val blurLevelTextView3: TextView = findViewById(R.id.blurLevelTextView3)
                blurLevelTextView3.text = blurLevel3(imf.absolutePath).roundTo2DecimalPlaces().toString()

            } else {
                Log.e(tag, "Image file is not exists!")
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {

            val imagePath = File(filesDir, "data-store")
            if (!imagePath.exists()) {
                imagePath.mkdir()
            }

            imageFile = File(imagePath, "current-photo.jpg")
            val contentUri = getUriForFile(this, "com.maydaycode.izup.fileprovider", imageFile)

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri)
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
        }
    }

    fun mainButtonClicked(v: View) {
        Log.d(tag, "mainButtonClicked(): $v")
        dispatchTakePictureIntent()
    }

    fun settingsButtonClicked(v: View) {
        Log.d(tag, "settingsButtonClicked(): $v")
    }
}