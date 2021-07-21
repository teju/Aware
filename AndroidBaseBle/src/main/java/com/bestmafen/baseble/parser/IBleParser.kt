package com.bestmafen.baseble.parser

/**
 * 消息解析器，用于把设备返回的流数据，拼装成完整的协议层面的消息。
 */
interface IBleParser {

    fun onReceive(data: ByteArray): ByteArray?

    fun reset()
}
