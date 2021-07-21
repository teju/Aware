package com.szabh.smable3.entity

import com.bestmafen.baseble.util.BleLog
import java.util.*

object Languages {
    const val DEFAULT_CODE = 0x01
    const val INVALID_CODE = 0x1F // R5语言包刷错时，code会变成该值
    const val DEFAULT_LANGUAGE = "en"

    private val LANGUAGES = mapOf(
        "zh" to 0x00, // 中文(简体)
        "en" to 0x01, // 英语
        "tr" to 0x02, // 土耳其语
        "ru" to 0x04, // 俄语
        "es" to 0x05, // 西班牙语
        "it" to 0x06, // 意大利语
        "ko" to 0x07, // 韩语
        "pt" to 0x08, // 葡萄牙语
        "de" to 0x09, // 德语
        "fr" to 0x0A, // 法语
        "nl" to 0x0B, // 荷兰语
        "pl" to 0x0C, // 波兰语
        "cs" to 0x0D, // 捷克语
        "hu" to 0x0E, // 匈牙利语
        "sk" to 0x0F, // 斯洛伐克语
        "ja" to 0x10, // 日语
        "da" to 0x11, // 丹麦
        "fi" to 0x12, // 芬兰
        "no" to 0x13, // 挪威
        "sv" to 0x14, // 瑞典
        "sr" to 0x15, // 塞尔维亚
        "th" to 0x16, // 泰语
        "in" to 0x17, // 印地语
        "el" to 0x18, // 希腊语
        "Hant" to 0x19, // 中文繁体
        "lt" to 0x1A,  // 立陶宛
        "vi" to 0x1B,  // 越南
        "ar" to 0x1C  // 阿拉伯语
//        "invalid" to 0x1F  // 1F不能再使用，R5语言包刷错时，code会变成该值
    )

    // 将语言转换成协议对应的Int值
    fun languageToCode(language: String = Locale.getDefault().language, default: Int = DEFAULT_CODE): Int {
        var tmpLanguage = language
        if ("zh" == language){
            if ("Hant" == Locale.getDefault().script){
                //繁体中文
                tmpLanguage = "Hant"
            }
        }
        val code = LANGUAGES[tmpLanguage] ?: default
        BleLog.v("Languages languageToCode -> language=${tmpLanguage}, code=${String.format("0x%02X", code)}")
        return code
    }

    // 将协议对应的Int值转换成语言
    fun codeToLanguage(code: Int, default: String = DEFAULT_LANGUAGE): Locale? {
        var language: String? = null
        for ((key, value) in LANGUAGES) {
            if (value == code) {
                language = key
                break
            }
        }
        language = language ?: default
        BleLog.v("Languages codeToLanguage -> code=${String.format("0x%02X", code)}, language=${language}}")
        return when (language) {
            "Hant" -> {
                //繁体中文
                Locale.Builder().setLanguage("zh").setScript("Hant").build()
            }
            "zh" -> {
                //简体中文
                Locale.Builder().setLanguage("zh").setScript("Hans").build()
            }
            else -> {
                Locale(language)
            }
        }
    }
}