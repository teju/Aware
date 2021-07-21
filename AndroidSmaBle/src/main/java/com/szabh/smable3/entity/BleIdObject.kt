package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable
import com.szabh.smable3.component.BleConnector
import com.szabh.smable3.component.ID_ALL

/**
 * 特殊的类，放入缓存前必须指定一个0~0xfe之间的，并且与缓存中该类其他实例不同的一id，通过该id可以标识设备和本地缓存上该类的唯一实例。
 * 一般的对象，比如[BleGestureWake]、[BleSedentarinessSettings]和[BleHrMonitoringSettings]等在缓存中最多只有一个实例，
 * 所以无须id，每次修改都是修改该唯一实例。
 * 但是有些对象，比如[BleAlarm]、[BleSchedule]和[BleCoaching]等，缓存的是一个列表。根据协议，修改和删除时必须指定id来确定要操作
 * 是哪个目标实例，该类就是为处理这种情况。
 *
 * id属性是框架内部处理的：
 * 1.当发送创建指令时，调用[BleConnector.sendObject]创建单个对象或[BleConnector.sendList]创建多个对象，框架内部会为每个对象
 *   分配一个0~0xfe之间还未缓存的id，然后发送给设备并将其追加到本地缓存列表末尾。
 * 2.当发送删除指令时，调用[BleConnector.sendInt8]，在把该id发送给设备后，设备会删除该对象，框架内部也会根据id在已缓存列表找到与之
 *   匹配的实例，并将其从缓存中移除，如果id为[ID_ALL]，会清空设备和本地缓存中该类所有实例。
 * 3.当发送修改指令时，调用[BleConnector.sendObject]，在把该对象发送给设备后，设备会根据id修改该对象，框架内部会根据该对象的id在
 *   已缓存列表找到与之匹配的实例，并将其原地替换。
 * 4.当发送读取指令时，调用[BleConnector.sendInt8]，在把该id发送给设备后，设备会根据id查找与之匹配的实例，并回复给手机，如果id为
 *   [ID_ALL]，则设置会查找该类所有实例，并将其返回。
 * 5.当发送重置指令时，框架内部会从0开始依次为其分配一个id，然后先[BleConnector.sendInt8]让设备删除已有的所有实例，接着调用
 *   [BleConnector.sendList]创新新的列表以实现重置的效果，同时本地也会用新的列表覆盖旧的列表。
 */
open class BleIdObject : BleWritable() {
    var mId: Int = 0
        internal set
}