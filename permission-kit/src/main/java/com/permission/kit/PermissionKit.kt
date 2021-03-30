package com.permission.kit

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.permission.kit.MainHandler.postDelay

import java.util.*

/**
 * Author: 信仰年轻
 * Date: 2021-03-26 12:28
 * Email: hydznsqk@163.com
 * Des:权限治理工具类
 */
class PermissionKit private constructor(vararg permissions: String) {
    private var mOnRationaleListener: OnRationaleListener? = null
    private var mSimpleCallback: SimpleCallback? = null
    private var mFullCallback: FullCallback? = null

    private val mPermissions = LinkedHashSet<String>()
    private var mPermissionsRequest: MutableList<String> = mutableListOf()
    private var mPermissionsGranted: MutableList<String> = mutableListOf()
    private var mPermissionsDenied: MutableList<String> = mutableListOf()
    private var mPermissionsDeniedForever: MutableList<String> = mutableListOf()

    init {
        for (@PermissionConstants.Permission permission in permissions) {
            //这里把你申请的权限中的权限组 转换成数组，检查单个权限是否在manifest中定义  做到权限申请最小化。
            //比如 你申请了一个Manifest.permission_group.STORAGE权限组，但是manifest却只定义了Manifest.permission.READ_EXTERNAL_STORAGE。
            // 如果拿着权限组去申请，会失败的。所以此时就只去申请Manifest.permission.READ_EXTERNAL_STORAGE
            for (singlePermission in PermissionConstants.getPermissions(permission)) {
                //你申请的权限 必须在manifest 里面 已经定义了的才行
                if (MANIFEST_PERMISSIONS.contains(singlePermission)) {
                    mPermissions.add(singlePermission)
                }
            }
        }
        sInstance = this
    }

    /**
     * 简单回调，只会告诉你已授权 还是未授权，不区分那个权限
     */
    fun callback(callback: SimpleCallback?): PermissionKit {
        mSimpleCallback = callback
        return this
    }

    /**
     * 这个会回调 那些已授权，那些未授权，跟上面那个SimpleCallback看场景使用
     */
    fun callback(callback: FullCallback?): PermissionKit {
        mFullCallback = callback
        return this
    }

    /**
     * 如果被永远拒绝了,弹框解释为什么申请权限的回调
     */
    fun rationale(listener: OnRationaleListener?): PermissionKit {
        mOnRationaleListener = listener
        return this
    }

    /**
     * 开始申请权限
     */
    fun request() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mPermissionsGranted.addAll(mPermissions)
            requestCallback()
        } else {
            for (permission in mPermissions) {
                if (isGranted(permission)) {
                    mPermissionsGranted.add(permission)
                } else {
                    mPermissionsRequest.add(permission)
                }
            }
            if (mPermissionsRequest.isEmpty()) {
                requestCallback()
            } else {
                startPermissionActivity()
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun startPermissionActivity() {
        mPermissionsDenied = ArrayList()
        mPermissionsDeniedForever = ArrayList()
        PermissionActivity.start(
            sApplication.applicationContext,
            PermissionActivity.TYPE_RUNTIME
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun rationale(activity: Activity): Boolean {
        var isRationale = false
        if (mOnRationaleListener != null) {
            for (permission in mPermissionsRequest) {
                if (shouldShowRequestPermissionRationale(activity, permission)) {
                    getPermissionsStatus(activity)
                    mOnRationaleListener!!.rationale(object : OnRationaleListener.ShouldRequest {

                        override fun again(again: Boolean) {
                            activity.finish()
                            if (again) {
                                startPermissionActivity()
                            } else {
                                requestCallback()
                            }
                        }
                    })
                    isRationale = true
                    break
                }
            }
            mOnRationaleListener = null
        }
        return isRationale
    }

    private fun getPermissionsStatus(activity: Activity) {
        for (permission in mPermissionsRequest) {
            if (isGranted(permission)) {
                mPermissionsGranted.add(permission)
            } else {
                mPermissionsDenied.add(permission)
                if (!shouldShowRequestPermissionRationale(activity, permission)) {
                    mPermissionsDeniedForever.add(permission)
                }
            }
        }
    }

    private fun requestCallback() {
        if (mSimpleCallback != null) {
            //只有当所有权限都被授权的时候才会回调onGranted，否则只会把被拒绝的权限回调到onDenied
            //你也可以改成 把本次授权的回调到onGranted，被拒绝的回调到onDenied
            if (mPermissionsRequest.size == 0 || mPermissions.size == mPermissionsGranted.size
            ) {
                mSimpleCallback!!.onGranted()
            } else {
                if (mPermissionsDenied.isNotEmpty()) {
                    mSimpleCallback!!.onDenied()
                }
            }
            mSimpleCallback = null
        }

        if (mFullCallback != null) {
            //只有当所有权限都被授权的时候才会回调onGranted，否则只会把被拒绝的权限回调到onDenied
            //你也可以改成 把本次授权的回调到onGranted，被拒绝的回调到onDenied
            //但我们认为现在这种比较好，可以做到一次权限申请最小化，不要在首次启动一次性申请那么多，按需申请
            if (mPermissionsRequest.size == 0 || mPermissions.size == mPermissionsGranted.size
            ) {
                mFullCallback!!.onGranted(mPermissionsGranted)
            } else {
                if (mPermissionsDenied.isNotEmpty()) {
                    mFullCallback!!.onDenied(mPermissionsDeniedForever, mPermissionsDenied)
                }
            }
            mFullCallback = null
        }
        mOnRationaleListener = null
    }

    private fun onRequestPermissionsResult(activity: Activity) {
        getPermissionsStatus(activity)
        requestCallback()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    class PermissionActivity : Activity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            //不拦截点击事件,接受out_side事件，其余down/move/up不接受
            window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
            val byteExtra = intent.getIntExtra(
                TYPE,
                TYPE_RUNTIME
            )
            if (byteExtra == TYPE_RUNTIME) {
                if (sInstance == null) {
                    Log.e("PermissionKit", "request permissions failed")
                    finish()
                    return
                }
                //申请权限之前 如果需要需要弹窗说明,则这里暂停
                if (sInstance!!.rationale(this)) {
                    return
                }
                if (sInstance!!.mPermissionsRequest != null) {
                    val size = sInstance!!.mPermissionsRequest.size
                    if (size <= 0) {
                        finish()
                        return
                    }
                    requestPermissions(sInstance!!.mPermissionsRequest.toTypedArray(), 1)
                }
            } else if (byteExtra == TYPE_WRITE_SETTINGS) {
                startWriteSettingsActivity(
                    this,
                    TYPE_WRITE_SETTINGS
                )
            } else if (byteExtra == TYPE_DRAW_OVERLAYS) {
                startOverlayPermissionActivity(
                    this,
                    TYPE_DRAW_OVERLAYS
                )
            }
        }

        override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
        ) {
            if (sInstance != null) {
                sInstance!!.onRequestPermissionsResult(this)
            }
            finish()
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == TYPE_WRITE_SETTINGS) {
                if (sSimpleCallback4WriteSettings == null) return
                if (isGrantedWriteSettings) {
                    sSimpleCallback4WriteSettings!!.onGranted()
                } else {
                    sSimpleCallback4WriteSettings!!.onDenied()
                }
                sSimpleCallback4WriteSettings = null
            } else if (requestCode == TYPE_DRAW_OVERLAYS) {
                if (sSimpleCallback4DrawOverlays == null) return

                postDelay(100, Runnable {
                    if (isGrantedDrawOverlays) {
                        sSimpleCallback4DrawOverlays!!.onGranted()
                    } else {
                        sSimpleCallback4DrawOverlays!!.onDenied()
                    }
                    sSimpleCallback4DrawOverlays = null
                })
            }
            finish()
        }

        companion object {
            private const val TYPE = "TYPE"
            const val TYPE_RUNTIME = 0x01
            const val TYPE_WRITE_SETTINGS = 0x02
            const val TYPE_DRAW_OVERLAYS = 0x03
            fun start(context: Context, type: Int) {
                val starter =
                    Intent(context, PermissionActivity::class.java)
                starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                starter.putExtra(TYPE, type)
                context.startActivity(starter)
            }
        }
    }


    //////////////////////interface/////////////////////////////////////////////////////
    interface OnRationaleListener {
        fun rationale(shouldRequest: ShouldRequest?)
        interface ShouldRequest {
            fun again(again: Boolean)
        }
    }

    interface SimpleCallback {
        fun onGranted()
        fun onDenied()
    }

    interface FullCallback {
        fun onGranted(permissionsGranted: List<String>?)
        fun onDenied(
            permissionsDeniedForever: List<String>?,
            permissionsDenied: List<String>?
        )
    }
    //////////////////////interface/////////////////////////////////////////////////////


    companion object {
        private var sInstance: PermissionKit? = null
        private val sApplication = AppGlobals.get()!!
        private val MANIFEST_PERMISSIONS = getPermissions(sApplication.packageName)

        private var sSimpleCallback4WriteSettings: SimpleCallback? = null
        private var sSimpleCallback4DrawOverlays: SimpleCallback? = null

        private fun isGranted(permission: String): Boolean {
            return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                    || PackageManager.PERMISSION_GRANTED
                    == ContextCompat.checkSelfPermission(
                sApplication,
                permission
            ))
        }

        @TargetApi(Build.VERSION_CODES.M)
        private fun startWriteSettingsActivity(
            activity: Activity,
            requestCode: Int
        ) {
            val intent =
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + sApplication!!.packageName)
            if (!isIntentAvailable(intent)) {
                launchAppDetailsSettings()
                return
            }
            activity.startActivityForResult(intent, requestCode)
        }

        @TargetApi(Build.VERSION_CODES.M)
        private fun startOverlayPermissionActivity(
            activity: Activity,
            requestCode: Int
        ) {
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:" + sApplication!!.packageName)
            if (!isIntentAvailable(intent)) {
                launchAppDetailsSettings()
                return
            }
            activity.startActivityForResult(intent, requestCode)
        }

        /**
         * APP 是否可以 添加悬浮view
         * api 官网解释如下
         * Checks if the specified context can draw on top of other apps. As of API
         * * level 23, an app cannot draw on top of other apps unless it declares the
         * * [android.Manifest.permission.SYSTEM_ALERT_WINDOW] permission in its
         * * manifest.
         */
        @get:RequiresApi(api = Build.VERSION_CODES.M)
        val isGrantedDrawOverlays: Boolean
            get() = Settings.canDrawOverlays(sApplication)

        @RequiresApi(api = Build.VERSION_CODES.M)
        fun requestDrawOverlays(callback: SimpleCallback?) {
            if (isGrantedDrawOverlays) {
                callback?.onGranted()
                return
            }
            sSimpleCallback4DrawOverlays = callback
            PermissionActivity.start(
                sApplication.applicationContext,
                PermissionActivity.TYPE_DRAW_OVERLAYS
            )
        }

        /**
         * 设置需要请求的权限数组
         */
        fun permission(vararg permissions: String): PermissionKit {
            return PermissionKit(*permissions)
        }

        /**
         * 启动app设置页面
         */
        fun launchAppDetailsSettings() {
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + sApplication.packageName)
            if (!isIntentAvailable(intent)) return
            sApplication.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        private fun isIntentAvailable(intent: Intent): Boolean {
            return sApplication
                .packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                .size > 0
        }

        /**
         * 获取安装包在manifest中声明的权限集合--此时你可以在APP中 开发一个隐私权限页，告诉用户那些已授权，是噶什么用的
         */
        fun getPermissions(packageName: String): List<String> {
            val pm =
                sApplication.packageManager
            return try {
                val permissions = pm.getPackageInfo(
                    packageName,
                    PackageManager.GET_PERMISSIONS
                ).requestedPermissions
                    ?: return emptyList()
                Arrays.asList(*permissions)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                emptyList()
            }
        }

        /**
         * 是否已经授权过了
         */
        fun isGranted(vararg permissions: String): Boolean {
            for (permission in permissions) {
                if (!isGranted(permission)) {
                    return false
                }
            }
            return true
        }

        /**
         * 检查APP是否获取修改系统设置的权限
         */
        @get:RequiresApi(api = Build.VERSION_CODES.M)
        val isGrantedWriteSettings: Boolean
            get() = Settings.System.canWrite(sApplication)

        @RequiresApi(api = Build.VERSION_CODES.M)
        fun requestWriteSettings(callback: SimpleCallback?) {
            if (isGrantedWriteSettings) {
                callback?.onGranted()
                return
            }
            sSimpleCallback4WriteSettings = callback
            PermissionActivity.start(
                sApplication!!.applicationContext,
                PermissionActivity.TYPE_WRITE_SETTINGS
            )
        }

    }
}