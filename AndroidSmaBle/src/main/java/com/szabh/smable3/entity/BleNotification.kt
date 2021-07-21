package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable
import kotlin.math.min

data class BleNotification(
    var mCategory: Int = 0,
    var mTime: Long = 0L, // ms
    var mPackage: String = "",
    var mTitle: String = "",
    var mContent: String = ""
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = 1 + 6 + PACKAGE_LENGTH + TITLE_LENGTH + min(mContent.toByteArray().size, CONTENT_MAX_LENGTH)

    override fun encode() {
        super.encode()
        writeInt8(mCategory)
        writeObject(BleTime.ofLocal(mTime))
        writeStringWithFix(mPackage, PACKAGE_LENGTH)
        writeStringWithFix(mTitle, TITLE_LENGTH)
        writeStringWithLimit(mContent, CONTENT_MAX_LENGTH)
    }

    override fun toString(): String {
        return "BleNotification(mCategory=$mCategory, mTime=$mTime, mPackage='$mPackage'" +
            ", mTitle='$mTitle', mContent='$mContent')"
    }

    companion object {
        const val CATEGORY_INCOMING_CALL = 0x01
        const val CATEGORY_MESSAGE = 0x7F

        const val PACKAGE_MISSED_CALL = "com.android.mobilephone"
        const val PACKAGE_SMS = "com.android.mms"

        const val ANDROID_EMAIL = "com.android.email"

        //以下应用建议默认打开推送开关
        const val SKYPE = "com.skype.raider"
        const val FACEBOOK_MESSENGER = "com.facebook.orca"
        const val FACEBOOK_MESSENGER_LITE = "com.facebook.mlite"
        const val FACEBOOK = "com.facebook.katana"
        const val FACEBOOK_LITE = "com.facebook.lite"
        const val WHATS_APP = "com.whatsapp"
        const val LINE = "jp.naver.line.android"
        const val INSTAGRAM = "com.instagram.android"
        const val KAKAO_TALK = "com.kakao.talk"
        const val GMAIL = "com.google.android.gm"
        const val TWITTER = "com.twitter.android"
        const val LINKED_IN = "com.linkedin.android"
        const val SINA_WEIBO = "com.sina.weibo"
        const val QQ = "com.tencent.mobileqq"
        const val WE_CHAT = "com.tencent.mm"
        const val OUT_LOOK = "com.microsoft.office.outlook"
        const val OUT_LOOK2 = "park.outlook.sign.in.client"
        const val YAHOO_MAIL = "com.yahoo.mobile.client.android.mail"
        const val VIBER = "com.viber.voip"
        const val BAND = "com.nhn.android.band"
        const val TELEGRAM = "org.telegram.messenger"
        const val BETWEEN = "kr.co.vcnc.android.couple"
        const val NAVERCAFE = "com.nhn.android.navercafe"
        const val YOUTUBE = "com.google.android.youtube"
        const val NETFLIX = "com.netflix.mediaclient"
        //以上应用建议默认打开推送开关

        const val HUAWEI_SYSTEM_MANAGER = "com.huawei.systemmanager"

        //电话应用
        const val ANDROID_INCALLUI = "com.android.incallui"
        const val ANDROID_TELECOM = "com.android.server.telecom"
        const val GOOGLE_TELECOM = "com.google.android.dialer"
        const val SAMSUNG_INCALLUI = "com.samsung.android.incallui"
        const val SAMSUNG_TELECOM = "com.samsung.android.dialer"


        //短信应用
        const val ANDROID_MMS = "com.android.mms"
        const val ANDROID_MMS_SERVICE = "com.android.mms.service"
        const val GOOGLE_MMS = "com.google.android.apps.messaging"
        const val SAMSUNG_MMS = "com.samsung.android.messaging"

        private val sEmailPackages = listOf(GMAIL, OUT_LOOK, OUT_LOOK2)

        private const val PACKAGE_LENGTH = 32 // 字节数
        private const val TITLE_LENGTH = 32 // 字节数
        private const val CONTENT_MAX_LENGTH = 250 // 内容最大长度，字节数

        fun isEmail(packageName: String): Boolean {
            return packageName.contains("mail") || sEmailPackages.contains(packageName)
        }
    }
}