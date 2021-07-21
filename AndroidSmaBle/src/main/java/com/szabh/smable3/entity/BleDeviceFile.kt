package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable

data class BleDeviceFile(
    var mFileType: Int = 0,     //文件类型，0-音频文件
    var mTime: Int = 0,         //距离当地2000/1/1 00:00:00的秒数
    var mIndex: Int = 0,        //包索引
    var mFileFormat: Int = 0,     //文件格式，可参考下方 companion object 中的定义 ，例如 1 = wav格式
    var mFileSize: Int = 0,     //文件大小
    var mOffsetValue: Int = 0,  //文件偏移地址-即该包在整个文件中的位置
    var mFileContent: ByteArray = byteArrayOf()  //文件内容
) : BleReadable() {

    override fun decode() {
        super.decode()
        mFileType = readInt8().toInt()
        mTime = readInt32()
        mIndex = readInt32()
        mFileFormat = readInt8().toInt()
        mFileSize = readInt32()
        mOffsetValue = readInt32()
        if (mBytes!!.size > 18){
            mFileContent = readBytes(mBytes!!.size - 18)
        }
    }

    companion object {

        const val AUDIO = 0

        const val WAV = 1
    }
}