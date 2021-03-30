package com.example.permissionkit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.permission.kit.PermissionConstants
import com.permission.kit.PermissionKit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_permission -> {
                requestBtnPermissions()
            }
        }
    }

    private fun requestBtnPermissions() {
        PermissionKit.permission(PermissionConstants.CAMERA)
            .callback(object : PermissionKit.SimpleCallback {
                override fun onGranted() {
                    showToast("获取相机权限成功")
                }
                override fun onDenied() {
                    showToast("你拒绝使用相机权限,扫码功能无法继续使用.")
                }
            })
            //如果被永远拒绝了 ，弹框解释为什么申请权限的回调
            .rationale(object : PermissionKit.OnRationaleListener {
                override fun rationale(shouldRequest: PermissionKit.OnRationaleListener.ShouldRequest?) {
                    //弹框解释,然后调整到设置页面
                    showTipDialog()
                }
            })
            .request()
    }

    fun showTipDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("权限提示")
        builder.setMessage("设备运行需要您前往设置一些权限,否则软件将不能更好的为您服务")

        builder.setPositiveButton("去设置", { dialog, which ->
            dialog.dismiss()
            //去设置页面设置权限
            PermissionKit.launchAppDetailsSettings()
        })

        builder.setNegativeButton("不同意", { dialog, which ->
            dialog.dismiss()
        })
        builder.create().show()
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}