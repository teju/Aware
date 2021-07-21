package com.bestmafen.baseble.messenger

import com.bestmafen.baseble.connector.AbsBleConnector

/**
 * 发送端的消息队列，当一条[WriteMessage]的长度超过[mPacketSize]，会被拆分成多条消息包。
 */
abstract class AbsBleMessenger {
    lateinit var mAbsBleConnector: AbsBleConnector

    var mPacketSize: Int = DEFAULT_PACKET_SIZE // 一次传输的字节数，在requestMtu之后会修改该值

    /**
     * 入队一条消息。
     */
    abstract fun enqueueMessage(message: BleMessage)

    /**
     * 出队一条消息。
     */
    abstract fun dequeueMessage()

    /**
     * 出队一个消息包。
     */
    abstract fun dequeueWritePacket()

    /**
     * 重置消息队列，会清空所有消息。
     */
    abstract fun reset()

    companion object {
        /**
         * 默认消息包的长度。
         */
        const val DEFAULT_PACKET_SIZE = 20
    }
}