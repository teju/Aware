package com.szabh.androiddfu.mtk

import com.abupdate.iot_libs.OtaAgentPolicy
import com.abupdate.iot_libs.info.CustomDeviceInfo

object MtkOtaHelper {

    /**
     * 准备MTK固件升级。
     * @param otaMeta 设备固件信息，"mid=xx;mod=xx;oem=xx;pf=xx;p_id=xx;p_sec=xx;ver=xx;d_ty=xx;..."。
     * @param path 固件下载路径。
     *
     * @return 准备成功或失败。
     */
    fun prepare(otaMeta: String, path: String): Boolean {
        try {
            val properties = otaMeta.split(";")
            if (properties.size < 8) return false

            val map = mutableMapOf<String, String>()
            for (property in properties) {
                val keyValue = property.split("=")
                if (keyValue.size == 2) map[keyValue[0]] = keyValue[1]
            }
            val mid = map["mid"] ?: return false
            val mod = map["mod"] ?: return false
            val oem = map["oem"] ?: return false
            val platform = map["pf"] ?: return false
            val productId = map["p_id"] ?: return false
            val productSecret = map["p_sec"] ?: return false
            val version = map["ver"] ?: return false
            val deviceType = map["d_ty"] ?: return false

            val customDeviceInfo = CustomDeviceInfo().apply {
                setMid(mid)
                setModels(mod)
                setOem(oem)
                setPlatform(platform)
                setProductId(productId)
                setProduct_secret(productSecret)
                setVersion(version)
                setDeviceType(deviceType)
            }

            OtaAgentPolicy.getConfig()
                .setUpdatePath(path)
                .setCustomDeviceInfo(customDeviceInfo)
                .commit()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }
}