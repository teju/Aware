package com.szabh.smable3.component

import com.szabh.smable3.BleKey
import com.szabh.smable3.BleKeyFlag

//abstract class AbsBleCache {
//
//    abstract fun putBoolean(bleKey: BleKey, value: Boolean, keyFlag: BleKeyFlag? = null)
//
//    abstract fun getBoolean(bleKey: BleKey, def: Boolean = false, keyFlag: BleKeyFlag? = null): Boolean
//
//    abstract fun putInt(bleKey: BleKey, value: Int, keyFlag: BleKeyFlag? = null)
//
//    abstract fun getInt(bleKey: BleKey, def: Int = 0, keyFlag: BleKeyFlag? = null): Int
//
//    abstract fun putLong(bleKey: BleKey, value: Long, keyFlag: BleKeyFlag? = null)
//
//    abstract fun getLong(bleKey: BleKey, def: Long = 0L, keyFlag: BleKeyFlag? = null): Long
//
//    abstract fun putString(bleKey: BleKey, value: String, keyFlag: BleKeyFlag? = null)
//
//    abstract fun getString(bleKey: BleKey, def: String = "", keyFlag: BleKeyFlag? = null): String
//
//    abstract fun <T> putObject(bleKey: BleKey, t: T?, keyFlag: BleKeyFlag? = null)
//
//    abstract fun <T> getObject(bleKey: BleKey, clazz: Class<T>, keyFlag: BleKeyFlag? = null): T?
//
//    abstract fun <T> getObjectNotNull(bleKey: BleKey, clazz: Class<T>, def: T? = null, keyFlag: BleKeyFlag? = null): T
//
//    abstract fun <T> putList(bleKey: BleKey, list: List<T>?, keyFlag: BleKeyFlag? = null)
//
//    abstract fun <T> getList(bleKey: BleKey, clazz: Class<T>, keyFlag: BleKeyFlag? = null): MutableList<T>
//
//    abstract fun remove(bleKey: BleKey, keyFlag: BleKeyFlag? = null)
//}