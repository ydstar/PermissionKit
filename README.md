# PermissionKit

## YdKit通用组件库
YdKit 是一组功能丰富的 Android 通用组件。

* [LogKit](https://github.com/ydstar/LogKit) — 轻量级的 Android 日志系统。
* [RestfulKit](https://github.com/ydstar/RestfulKit) — 简洁但不简单的 Android 网络组件库。
* [CrashKit](https://github.com/ydstar/CrashKit) — 简洁易用的 Android Crash日志捕捉组件。
* [PermissionKit](https://github.com/ydstar/PermissionKit) — 简洁易用的 Android 权限请求组件。
* [RefreshKit](https://github.com/ydstar/RefreshKit) — 简洁易用的 Android 下拉刷新和上拉加载组件。
* [AdapterKit](https://github.com/ydstar/AdapterKit) — 简洁易用的 Android 列表组件。
* [BannerKit](https://github.com/ydstar/BannerKit) — 简洁易用的 Android 无限轮播图组件。
* [TabBottomKit](https://github.com/ydstar/TabBottomKit) — 简洁易用的 Android 底部导航组件。

## 导入方式

仅支持`AndroidX`
```
dependencies {
     implementation 'com.android.ydkit:permission-kit:1.0.1'
}
```

## 使用方法
在 AndroidManifest.xml 中声明所需权限：
```
  <uses-permission android:name="android.permission.CAMERA"/> //拍照
```
代码请求权限
```java
PermissionKit
      .permission(PermissionConstants.CAMERA)
      .callback(object : PermissionKit.SimpleCallback {
           override fun onGranted() {
                showToast("获取相机权限成功")
           }
           override fun onDenied() {
                showToast("你拒绝使用相机权限,扫码功能无法继续使用.")
           }
      })
      //如果被永远拒绝了,弹框解释为什么申请权限的回调
      .rationale(object : PermissionKit.OnRationaleListener {
           override fun rationale(shouldRequest: PermissionKit.OnRationaleListener.ShouldRequest?) {
               //弹框解释,然后调整到设置页面
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
