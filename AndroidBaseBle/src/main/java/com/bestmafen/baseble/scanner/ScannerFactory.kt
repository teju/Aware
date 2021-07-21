package com.bestmafen.baseble.scanner

import android.os.Build
import java.util.*

/**
 * AbsBleScanner的工厂类，根据不同的API，返回不同的AbsBleScanner
 */
object ScannerFactory {

    fun newInstance(serviceUuids: Array<UUID>? = null, scanMode: ScanMode = ScanMode.BALANCED): AbsBleScanner {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            BleScanner18(serviceUuids)
        } else {
            BleScanner21(serviceUuids, scanMode)
        }
    }
}
