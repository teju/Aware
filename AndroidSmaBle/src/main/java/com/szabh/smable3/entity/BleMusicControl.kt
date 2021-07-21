package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable

data class BleMusicControl(val mMusicEntity: MusicEntity, val mMusicAttr: MusicAttr, val mContent: String)
    : BleWritable() {

    constructor(musicEntity: MusicEntity, musicAttr: MusicAttr, contents: List<String>)
        : this(musicEntity, musicAttr, contents.joinToString(","))

    override val mLengthToWrite: Int
        get() = 2 + mContent.toByteArray().size

    override fun encode() {
        super.encode()
        writeInt8(mMusicEntity.mEntity.toInt())
        writeInt8(mMusicAttr.mAttr.toInt())
        writeString(mContent)
    }

    override fun toString(): String {
        return "BleMusicControl(mMusicEntity=$mMusicEntity, mMusicAttr=$mMusicAttr, mContent='$mContent')"
    }
}

enum class MusicEntity(val mEntity: Byte) {
    PLAYER(0x00), // 播放器
    QUEUE(0x01), // 播放队列
    TRACK(0x02), // 当前播放的曲目
    UNKNOWN(-1);

    companion object {

        fun of(entity: Byte): MusicEntity {
            val musicEntity = values().find { it.mEntity == entity }
            return musicEntity ?: UNKNOWN
        }
    }
}

enum class MusicAttr(val mAttr: Byte) {
    PLAYER_NAME(0x00), // 播放器的app名字
    PLAYER_PLAYBACK_INFO(0x01), // 播放器状态（暂停，播放，快退，快进），播放器速率，进度（已播放的秒数）
    PLAYER_VOLUME(0x02), // 音量（0f ～ 1f）

    QUEUE_INDEX(0x00), // 第几首
    QUEUE_COUNT(0x01), // 总共多少首
    QUEUE_SHUFFLE_MODE(0x02),
    QUEUE_REPEAT_MODE(0x03),

    TRACK_ARTIST(0x00), // 艺术家
    TRACK_ALBUM(0x01), // 专辑名
    TRACK_TITLE(0x02), // 歌曲名
    TRACK_DURATION(0x03), // 时长，浮点数

    UNKNOWN(-1);

    companion object {

        fun of(entity: MusicEntity, attr: Byte): MusicAttr {
            return when (entity) {
                MusicEntity.PLAYER -> when (attr) {
                    0.toByte() -> PLAYER_NAME
                    1.toByte() -> PLAYER_PLAYBACK_INFO
                    2.toByte() -> PLAYER_VOLUME
                    else -> UNKNOWN
                }
                MusicEntity.QUEUE -> when (attr) {
                    0.toByte() -> QUEUE_INDEX
                    1.toByte() -> QUEUE_COUNT
                    2.toByte() -> QUEUE_SHUFFLE_MODE
                    3.toByte() -> QUEUE_REPEAT_MODE
                    else -> UNKNOWN
                }
                MusicEntity.TRACK -> when (attr) {
                    0.toByte() -> TRACK_ARTIST
                    1.toByte() -> TRACK_ALBUM
                    2.toByte() -> TRACK_TITLE
                    3.toByte() -> TRACK_DURATION
                    else -> UNKNOWN
                }
                else -> UNKNOWN
            }
        }

        fun of(entity: Byte, attr: Byte): MusicAttr {
            return of(MusicEntity.of(entity), attr)
        }
    }
}

enum class PlaybackState(val mState: Byte) {
    PAUSED(0x00), PLAYING(0x01), REWINDING(0x02), FAST_FORWARDING(0x03), UNKNOWN(-1)
}

/**
 * 设备触发的命令
 */
enum class MusicCommand(val mCommand: Byte) {
    PLAY(0x00),
    PAUSE(0x01),
    TOGGLE(0x02),
    NEXT(0x03),
    PRE(0x04),
    VOLUME_UP(0x05),
    VOLUME_DOWN(0x06),
    UNKNOWN(-1);

    companion object {

        fun of(command: Byte): MusicCommand {
            val musicCommand = values().find { it.mCommand == command }
            return musicCommand ?: UNKNOWN
        }
    }
}
