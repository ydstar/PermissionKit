package com.permission.kit

import android.Manifest
import android.os.Build
import androidx.annotation.StringDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Author: 信仰年轻
 * Date: 2021-03-26 11:20
 * Email: hydznsqk@163.com
 * Des:申请权限时从这里指定权限名的名称
 */
object PermissionConstants {
    const val CALENDAR = Manifest.permission_group.CALENDAR
    const val CAMERA = Manifest.permission_group.CAMERA
    const val CONTACTS = Manifest.permission_group.CONTACTS
    const val LOCATION = Manifest.permission_group.LOCATION
    const val MICROPHONE = Manifest.permission_group.MICROPHONE
    const val PHONE = Manifest.permission_group.PHONE
    const val SENSORS = Manifest.permission_group.SENSORS
    const val SMS = Manifest.permission_group.SMS
    const val STORAGE = Manifest.permission_group.STORAGE

    //日历组
    private val GROUP_CALENDAR = arrayOf(
        Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR
    )
    //相机组
    private val GROUP_CAMERA = arrayOf(
        Manifest.permission.CAMERA
    )
    //通讯录组
    private val GROUP_CONTACTS = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.GET_ACCOUNTS
    )
    //定位组
    private val GROUP_LOCATION = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    //麦克风组
    private val GROUP_MICROPHONE = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )
    //打电话组
    private val GROUP_PHONE = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG,
        Manifest.permission.ADD_VOICEMAIL,
        Manifest.permission.USE_SIP,
        Manifest.permission.PROCESS_OUTGOING_CALLS,
        Manifest.permission.ANSWER_PHONE_CALLS
    )
    //8.0以下打电话组 SDK_INT < 26
    private val GROUP_PHONE_BELOW_O = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG,
        Manifest.permission.ADD_VOICEMAIL,
        Manifest.permission.USE_SIP,
        Manifest.permission.PROCESS_OUTGOING_CALLS
    )

    //传感器组
    private val GROUP_SENSORS = arrayOf(
        Manifest.permission.BODY_SENSORS
    )
    //短信组
    private val GROUP_SMS = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_WAP_PUSH,
        Manifest.permission.RECEIVE_MMS
    )
    //存储组
    private val GROUP_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    fun getPermissions(@Permission permission: String): Array<String> {
        when (permission) {
            CALENDAR -> return GROUP_CALENDAR
            CAMERA -> return GROUP_CAMERA
            CONTACTS -> return GROUP_CONTACTS
            LOCATION -> return GROUP_LOCATION
            MICROPHONE -> return GROUP_MICROPHONE
            PHONE -> return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                GROUP_PHONE_BELOW_O
            } else {
                GROUP_PHONE
            }
            SENSORS -> return GROUP_SENSORS
            SMS -> return GROUP_SMS
            STORAGE -> return GROUP_STORAGE
        }
        return arrayOf(permission)
    }

    @StringDef(
        CALENDAR,
        CAMERA,
        CONTACTS,
        LOCATION,
        MICROPHONE,
        PHONE,
        SENSORS,
        SMS,
        STORAGE
    )
    @Retention(RetentionPolicy.SOURCE)
    annotation class Permission
}