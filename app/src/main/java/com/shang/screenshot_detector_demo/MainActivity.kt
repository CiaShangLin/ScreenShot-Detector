package com.shang.screenshot_detector_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.shang.screenshotdetector.ScreenShotDetector

class MainActivity : AppCompatActivity() {

    private lateinit var mScreenShotDetector: ScreenShotDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mScreenShotDetector = ScreenShotDetector(contentResolver, object : ScreenShotDetector.DetectorCallback {
            override fun detectorSuccess(path: String) {
                Toast.makeText(this@MainActivity, "ScreenShot success : $path", Toast.LENGTH_SHORT).show()
            }

            override fun detectorFail(error: String) {
                Toast.makeText(this@MainActivity, "ScreenShot Fail : $error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //註冊
    override fun onResume() {
        super.onResume()
        mScreenShotDetector.register()
    }

    //取消註冊
    override fun onPause() {
        super.onPause()
        mScreenShotDetector.unregister()
    }

}
