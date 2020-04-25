# Android ScreenShot Detector 截圖偵測器

## 導入方法
編成Library出了點問題以後再處理

## 使用方法
```java
class MainActivity : AppCompatActivity() {

    private lateinit var mScreenShotDetector :ScreenShotDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mScreenShotDetector=ScreenShotDetector(contentResolver,object:ScreenShotDetector.DetectorCallback{
            override fun detectorSuccess(path: String) {
                Toast.makeText(this@MainActivity,"ScreenShot success : $path",Toast.LENGTH_SHORT).show()
            }

            override fun detectorFail(error: String) {
                Toast.makeText(this@MainActivity,"ScreenShot Fail : $error",Toast.LENGTH_SHORT).show()
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
```

## 實作原理


## 注意事項
整個專案clone下來應該就可以Demo了,目前這個demo沒有添加權限的請求,請自己手動開啟不然永遠都會回傳detectorFail。

當回傳一次之後,他就不會再回傳了,如果有需要修改請自己fork修改ONCE_TIME這個參數,但是不設ONCE_TIME這個變數
他成功的時候會回傳兩次不知道為什麼。

checkScreenShot在這個方法裡面你可以自己添加其他的關鍵字,來偵測是否有截圖。



