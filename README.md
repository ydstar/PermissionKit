# PermissionKit

## 导入方式

仅支持`AndroidX`
```
dependencies {
     implementation 'com.android.ydkit:permission-kit:1.0.0'
}
```

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