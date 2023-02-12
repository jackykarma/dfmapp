package com.jacky.dfmapp

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.jacky.dfmapp.MainActivity.FlavorClassLoader.loadResId
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.InvocationTargetException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val flavorName = checkFlavor()
        checkDfmRes(flavorName)
        flavorAssetsLoad(flavorName)
        checkMetadataString()
        checkAllowBackup()
    }

    private fun checkAllowBackup() {
        try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val allowBackup = applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP
            // 11000011001000 1011111101000110 allowBackup为true
            // 11000011001000 0011111101000110 allowBackup为false
            //                1000000000000000
            Log.d(TAG, "checkAllowBackup:" + Integer.toBinaryString(applicationInfo.flags))
            // 1000000000000000
            Log.d(
                TAG,
                "checkAllowBackup:" + Integer.toBinaryString(ApplicationInfo.FLAG_ALLOW_BACKUP)
            )
            Log.d(TAG, "checkAllowBackup: allowBackup:" + (allowBackup > 0))
            // TODO:代码动态设置该属性，应用初始化时设置。manifest中属性替换没有找到合适。
            applicationInfo.flags = applicationInfo.flags or ApplicationInfo.FLAG_ALLOW_BACKUP
            val allowBackupUpdate = applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP
            Log.d(TAG, "checkAllowBackup: newAllowBackup:" + (allowBackupUpdate > 0))
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun checkDfmRes(pluginName: String?) {
        if (pluginName!!.isEmpty()) return
        // TODO:layout测试通过
        val layoutId: Int = loadResId(this, "layout_$pluginName", "layout", pluginName)
        Log.d(TAG, "checkDfmRes: layoutId:$layoutId")
        val rawId: Int = loadResId(this, "raw", "raw", pluginName)
        Log.d(TAG, "checkDfmRes: rawId:$rawId")
        val xmlId: Int = loadResId(this, "widgets", "xml", pluginName)
        Log.d(TAG, "checkDfmRes: xmlId:$xmlId")
        val stringId: Int = loadResId(this, "app_key", "string", pluginName)
        Log.d(TAG, "checkDfmRes: stringId:$stringId")
        Log.d(TAG, "checkDfmRes: app_key:" + getString(stringId))
    }

    private fun flavorAssetsLoad(pluginName: String?) {
        val assetManager = assets
        try {
            val `is` = assetManager.open("config_$pluginName.xml")
            val bufferedReader = BufferedReader(InputStreamReader(`is`))
            while (bufferedReader.read() != -1) {
                val line = bufferedReader.readLine()
                Log.d(TAG, "flavorAssetsLoad: line:$line")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "flavorAssetsLoad: exception:" + e.localizedMessage)
        }
    }

    private fun checkMetadataString() {
        try {
            val applicationInfo =
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val appKey = applicationInfo.metaData["app_key"] as String?
            Log.d(TAG, "checkMetadataString: appKey:$appKey")
            val shortcuts = applicationInfo.metaData["android.app.shortcuts"]
            Log.d(
                TAG,
                "checkMetadataString: shortcuts:$shortcuts"
            )
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun checkFlavor(): String? {
        val clz = classLoader.loadClass("com.jacky.android.app_aab.featureflavors.FlavorImpl")
        if (clz != null) {
            try {
                val obj = clz.newInstance()
                val method = clz.getMethod("getFlavor")
                val flavor = method.invoke(obj) as String
                Log.d(TAG, "checkFlavor: $flavor")
                return flavor
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            }
        }
        return null
    }

    internal object FlavorClassLoader {
        fun <T> loadClass(context: Context, clsName: String?): T? {
            return try {
                val cls = context.classLoader.loadClass(clsName)
                cls.newInstance() as T
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                null
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                null
            } catch (e: InstantiationException) {
                e.printStackTrace()
                null
            }
        }

        fun loadResId(
            context: Context,
            resName: String?,
            resType: String?,
            pluginName: String
        ): Int {
            val pkgName = context.packageName + "." + pluginName
            // TODO:这个理解是错误的
            // pkgName = "com.jacky.android.app_aab.featureflavors.domestic";
            Log.d(TAG, "loadResId: pkgName:$pkgName")
            return context.resources.getIdentifier(resName, resType, pkgName)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}