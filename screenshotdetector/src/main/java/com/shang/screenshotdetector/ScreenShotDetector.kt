package com.shang.screenshotdetector

import android.content.ContentResolver
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat

class ScreenShotDetector(private var mContentResolver: ContentResolver, private var mCallback: DetectorCallback?) {

    //理論上要打開外部權限這個功能才會完整
    //不打開權限的話應該cursor去訪問資料庫時會丟出權限例外
    interface DetectorCallback {
        fun detectorSuccess(path: String)
        fun detectorFail(error: String)
    }

    companion object {
        const val TAG = "ScreenShotDetector"
        const val DETECTOR_SUCCESS = 200
        const val DETECTOR_FAIL = 404
        private var ONCE_TIME = true
    }

    private var mHandler: Handler
    private var mHandlerThread: HandlerThread
    private var mInternalObserver: MediaContentObserver
    private var mExternalObserver: MediaContentObserver

    init {
        mHandlerThread = HandlerThread(TAG)
        mHandlerThread.start()
        mHandler = object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    DETECTOR_SUCCESS -> {
                        mCallback?.detectorSuccess(msg.obj.toString())
                    }
                    DETECTOR_FAIL -> {
                        mCallback?.detectorFail(msg.obj.toString())
                    }
                }
            }
        }

        mInternalObserver =
                MediaContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI, mHandler, mContentResolver)
        mExternalObserver =
                MediaContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, mHandler, mContentResolver)
    }

    fun register() {
        //內部存儲空間
        mContentResolver.registerContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI, true, mInternalObserver)
        //外部存儲空間
        mContentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, mExternalObserver)
    }

    fun unregister() {
        mContentResolver.unregisterContentObserver(mInternalObserver)
        mContentResolver.unregisterContentObserver(mExternalObserver)
    }

    class MediaContentObserver(var uri: Uri, var handler: Handler, var mContentResolver: ContentResolver) :
            ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            handleMediaContentChange(uri)
        }

        private fun handleMediaContentChange(contentUri: Uri) {
            var cursor: Cursor? = null
            try {
                cursor = mContentResolver.query(
                        contentUri,
                        arrayOf(MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DATE_TAKEN),
                        null,
                        null,
                        MediaStore.Images.ImageColumns.DATE_ADDED + " desc limit 1"
                )
                cursor?.moveToFirst()

                val dataIndex = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                val dataTakenIndex = cursor?.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)

                val data: String? = cursor?.getString(dataIndex!!)
                val taken: Long? = cursor?.getLong(dataTakenIndex!!)
                val simple = SimpleDateFormat("YYYY-MM-dd")

                Log.d(TAG, "$data \n $taken \n ${simple.format(taken)}")

                if (checkScreenShot(data.toString())) {
                    sendMessage(data.toString(), DETECTOR_SUCCESS)
                } else {
                    sendMessage("not screen-shot", DETECTOR_FAIL)
                }
            } catch (e: Exception) {
                sendMessage(e.message.toString(), DETECTOR_FAIL)
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }

        //TODO 檢查關鍵字,這裡可以自己添加關鍵字
        private fun checkScreenShot(data: String): Boolean {
            var word = data.toLowerCase()
            var isScreen = false
            val keyWord = arrayOf(
                    "screenshot",
                    "screen_shot",
                    "screen-shot",
                    "screen shot",
                    "screencapture",
                    "screen_capture",
                    "screen-capture",
                    "screen capture",
                    "screencap",
                    "screen_cap",
                    "screen-cap",
                    "screen cap"
            )
            keyWord.forEach {
                if (word.contains(it)) {
                    isScreen = true
                }
            }

            val file = File(data)
            Log.d(TAG, "isScreenShot:$isScreen File Exists:${file.exists()}")

            return isScreen && file.exists()
        }

        @Synchronized
        private fun sendMessage(path: String, what: Int) {
            if (ONCE_TIME) {
                handler.sendMessage(Message().apply {
                    this.what = what
                    this.obj = path
                })
            }
            ONCE_TIME = false
            //TODO 如果不設置ONCE_TIME的話,成功的時候他會回傳兩次,目前不知道原因
        }
    }

}