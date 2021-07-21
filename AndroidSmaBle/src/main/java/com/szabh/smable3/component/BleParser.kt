package com.szabh.smable3.component

import com.bestmafen.baseble.data.getInt
import com.bestmafen.baseble.parser.IBleParser
import com.bestmafen.baseble.util.BleLog

object BleParser : IBleParser {
    private const val LOG_HEADER = "BleParser"

    private var mData: ByteArray = ByteArray(0)
    private var mReceived = -1

    override fun onReceive(data: ByteArray): ByteArray? {
        try {
            if (mReceived == -1) {
                // [0xAB, 0x11, 0x00, 0x04, 0x78, 0x50]
                val contentLength = data.getInt(MessageFactory.LENGTH_BEFORE_LENGTH, MessageFactory.LENGTH_PAYLOAD_LENGTH)
                mData = ByteArray(MessageFactory.LENGTH_BEFORE_CMD + contentLength)
                mReceived = 0
            }
            if (mReceived < mData.size) {
                System.arraycopy(data, 0, mData, mReceived, data.size)
                mReceived += data.size
                BleLog.v("$LOG_HEADER -> data length=${mData.size}, received=$mReceived")

                if (mReceived >= mData.size) {
                    mReceived = -1
                    return mData
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            reset()
        }
        return null
    }

    override fun reset() {
        mData = ByteArray(0)
        mReceived = -1
    }
}