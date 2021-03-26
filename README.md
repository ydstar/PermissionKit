# PermissionKit
纯Kotlin轻量级权限治理,简洁好用

# LogKit

<img src="https://github.com/ydstar/LogKit/blob/main/preview/show.gif" alt="动图演示效果" width="250px">

## 导入方式

仅支持`AndroidX`
```
dependencies {
     implementation 'com.android.ydkit:permission-kit:1.0.0'
}
```
## 权限组
| 权限组      |权限  |
| :-------- | :--------|
| CALENDA   | READ_CALENDAR  WRITE_CALENDAR  |
| CAMERA    | CAMERA |
| CONTACTS| READ_CONTACTS WRITE_CONTACTS GET_ACCOUNTS|
| LOCATION| ACCESS_FINE_LOCATION ACCESS_COARSE_LOCATION |
| MICROPHONE| RECORD_AUDIO |
| PHONE| READ_PHONE_STATE READ_PHONE_NUMBERS CALL_PHONE ANSWER_PHONE_CALLS READ_CALL_LOG WRITE_CALL_LOG  ADD_VOICEMAIL USE_SIP PROCESS_OUTGOING_CALLS ANSWER_PHONE_CALLS |
| SENSORS| BODY_SENSORS  |
| SMS| SEND_SMS RECEIVE_SMS READ_SMS RECEIVE_WAP_PUSH RECEIVE_MMS|
| STORAGE| READ_EXTERNAL_STORAGE WRITE_EXTERNAL_STORAGE |

这里把你申请的权限中的权限组 转换成数组，检查单个权限是否在manifest中定义  做到权限申请最小化。
比如 你申请了一个Manifest.permission_group.STORAGE权限组，但是manifest却只定义了Manifest.permission.READ_EXTERNAL_STORAGE。
如果拿着权限组去申请，会失败的。所以此时就只去申请Manifest.permission.READ_EXTERNAL_STORAGE

## 使用方法
在 AndroidManifest.xml 中声明所需权限：
```
  <uses-permission android:name="android.permission.CAMERA"/> //拍照
```
代码请求权限
```java
        PermissionUtil.permission(PermissionConstants.CAMERA)
            .callback(object : PermissionUtil.SimpleCallback {
                override fun onGranted() {
                    showToast("获取相机权限成功")
                }
                override fun onDenied() {
                    showToast("你拒绝使用相机权限,扫码功能无法继续使用.")
                }
            })
            //如果被永远拒绝了,弹框解释为什么申请权限的回调
            .rationale(object : PermissionUtil.OnRationaleListener {
                override fun rationale(shouldRequest: PermissionUtil.OnRationaleListener.ShouldRequest?) {
                    //弹框解释,然后调整到设置页面
                    showTipDialog()
                }
            })
            .request()
```
```
    fun showTipDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("权限提示")
        builder.setMessage("设备运行需要您前往设置一些权限,否则软件将不能更好的为您服务")

        builder.setPositiveButton("去设置", { dialog, which ->
            dialog.dismiss()
            //去设置页面设置权限
            PermissionUtil.launchAppDetailsSettings()
        })

        builder.setNegativeButton("不同意", { dialog, which ->
            dialog.dismiss()
        })
        builder.create().show()
    }
```


## License
```text
Copyright [2021] [ydStar]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```