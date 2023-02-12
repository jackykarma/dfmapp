package com.jacky.app_apk

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.jacky.android.app_aab.featureflavors.FlavorImpl

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val flavor = FlavorImpl()
        Log.d(TAG, "onCreate: flavor:" + flavor.flavor)
        // 考虑到资源的命名的兼容，apk中对assets等文件访问还是得用dfm相同的方式访问。
        // 因为文件名不同了。得根据flavor来判断文件名的选择。

    }
}