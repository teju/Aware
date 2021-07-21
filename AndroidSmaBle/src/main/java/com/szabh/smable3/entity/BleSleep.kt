package com.szabh.smable3.entity

import android.util.SparseIntArray
import com.bestmafen.baseble.data.BleReadable
import kotlin.math.ceil

data class BleSleep(
    var mTime: Int = 0, // 距离当地2000/1/1 00:00:00的秒数
    var mMode: Int = 0, // 睡眠状态
    var mSoft: Int = 0, // 轻动，即睡眠过程中检测到相对轻微的动作
    var mStrong: Int = 0 // 重动，即睡眠过程中检测到相对剧烈的运动
) : BleReadable() {

    override fun decode() {
        super.decode()
        mTime = readInt32()
        mMode = readUInt8().toInt()
        mSoft = readUInt8().toInt()
        mStrong = readUInt8().toInt()
    }

    companion object {
        const val ITEM_LENGTH = 7

        //睡眠状态
        const val DEEP = 1  //深睡
        const val LIGHT = 2 //浅睡
        const val AWAKE = 3 //清醒
        const val START = 17 //睡眠开始
        const val END = 34  //睡眠结束-当前无有效定义和使用
        const val TOTAL_LENGTH = 4 //睡眠总时长
        const val DEEP_LENGTH = 5 //深睡总时长
        const val LIGHT_LENGTH = 6 //浅睡总时长
        const val AWAKE_LENGTH = 7 //清醒总时长

        private const val PERIOD = 900 // 秒数
        private const val OFFSET = 60 // 数据偏差范围60秒,之所以添加此数值,是因为数据记录的时间可能存在偏差
        private const val ERROR_DATA = 14400 // 如果任意两条睡眠数据之间存在4小时的空白期,则判定此次睡眠数据无效

        /**
         * 处理原始睡眠数据，数据必须是按时间升序排列的
         */
        fun analyseSleep(origin: List<BleSleep>,algorithmType:Int = 0): List<BleSleep> {
            if (origin.size < 2) return emptyList()

            var hasFallenAsleep = false // 是否有入睡点的数据
            val result = mutableListOf<BleSleep>()
            for (index in 0..origin.size - 2) {
                if ((origin[index + 1].mTime - origin[index].mTime) >= ERROR_DATA) {
                    return emptyList()
                }
            }

            if (algorithmType == 0){
                origin.forEachIndexed { index, sleep ->
                    when (sleep.mMode) {
                        START -> {
                            hasFallenAsleep = true
                            // 有多个入睡点，只保留最后一段，所以把之前的全部晴空
                            result.clear()
                            // 把入睡拆分成前15分钟浅睡，后面深睡
                            sleep.mMode = LIGHT
                            result.add(sleep)
                            if (origin.size > index + 1) {
                                // 如果跟下个点的时间相差15分钟以上，插入一个深睡点
                                if (origin[index + 1].mTime - sleep.mTime > PERIOD) {
                                    result.add(BleSleep(sleep.mTime + PERIOD, DEEP))
                                }
                            }
                        }
                        DEEP -> {
                            if (sleep.mStrong > 2) { // 如果重动>2, 则为清醒
                                if (result.isEmpty() || sleep.mTime - result.last().mTime > (PERIOD + OFFSET)) {
                                    // 如果跟上个点的时间相差15+1分钟以上，插入一个清醒点
                                    result.add(sleep.copy(mTime = sleep.mTime - PERIOD, mMode = AWAKE))
                                } else if (result.isNotEmpty() && (sleep.mTime - result.last().mTime) <= (PERIOD - OFFSET)) {
                                    // 如果跟上个点的时间相差15-1分钟以内，直接将上个点修改为清醒
                                    result.last().mMode = AWAKE
                                } else {
                                    result.add(sleep.copy(mMode = AWAKE))
                                }
                            } else {
                                if (sleep.mSoft > 2) { // 重动<=2, 轻动>2, 则为浅睡
                                    if (result.isEmpty() || sleep.mTime - result.last().mTime > PERIOD) {
                                        // 如果跟上个点的时间相差15分钟以上，插入一个浅睡点
                                        result.add(
                                            sleep.copy(
                                                mTime = sleep.mTime - PERIOD,
                                                mMode = LIGHT
                                            )
                                        )
                                    } else if (result.isNotEmpty() && sleep.mTime - result.last().mTime <= PERIOD) {
                                        // 如果跟上个点的时间相差15分钟以内，直接将上个点修改为浅睡
                                        result.last().mMode = LIGHT
                                    } else {
                                        result.add(sleep.copy(mMode = LIGHT))
                                    }
                                } else {
                                    if (result.isNotEmpty() && result.last().mMode == sleep.mMode && (sleep.mTime - result.last().mTime) > (PERIOD - OFFSET)) {
                                        //当相近点都为深睡,且与上一个点记录间隔超过15-1分钟,则将当前点设置为浅睡点并记录
                                        result.add(
                                            sleep.copy(
                                                mTime = sleep.mTime,
                                                mMode = LIGHT
                                            )
                                        )
                                    } else if (result.size > 2 && result.last().mMode == LIGHT
                                        && result[result.size - 2].mMode == DEEP
                                        && (sleep.mTime - result.last().mTime) > (PERIOD - OFFSET)
                                    ) {
                                        //如果出现: A点深睡,B点浅睡,且C点与B点相隔在15-1分钟内,而C点传过来的为深睡,那么C点也要强行归类到浅睡
                                        result.add(
                                            sleep.copy(
                                                mTime = sleep.mTime,
                                                mMode = LIGHT
                                            )
                                        )
                                    } else if (result.isEmpty() || result.last().mMode != sleep.mMode) {
                                        result.add(sleep)
                                    }

                                }
                            }
                        }
                        LIGHT -> {
                            if (sleep.mStrong > 2 || sleep.mSoft > 25) {
                                if (result.isEmpty() || sleep.mTime - result.last().mTime > PERIOD) {
                                    // 如果跟上个点的时间相差15分钟以上，插入一个清醒点
                                    result.add(sleep.copy(mTime = sleep.mTime - PERIOD, mMode = AWAKE))
                                } else if (result.isNotEmpty() && sleep.mTime - result.last().mTime <= PERIOD) {
                                    // 如果跟上个点的时间相差15分钟以内，直接将上个点修改为清醒
                                    result.last().mMode = AWAKE
                                }
                            } else {
                                if (result.isEmpty() || result.last().mMode != sleep.mMode) {
                                    result.add(sleep)
                                }
                            }
                        }
                        AWAKE -> {
                            if (result.isEmpty() || result.last().mMode != sleep.mMode) {
                                result.add(sleep)
                            }
                        }

                    }
                }
                if (!hasFallenAsleep && result.isNotEmpty()) {
                    result[0].mMode = LIGHT
                    if (result.size > 1) {
                        // 如果跟下个点的时间相差15分钟以上，插入一个深睡点
                        if (result[1].mTime - result[0].mTime > PERIOD) {
                            result.add(1, BleSleep(result[0].mTime + PERIOD, DEEP))
                        }
                    }
                }
                return result
            }else{
                origin.forEachIndexed { _, sleep ->
                    when (sleep.mMode) {
                        START -> {
                            hasFallenAsleep = true
                            // 有多个入睡点，只保留最后一段，所以把之前的全部清空
                            result.clear()
                            //入睡后10秒会产生第二状态，所以入睡时的状态数据可以抛弃
                        }
                        DEEP -> {
                            result.add(sleep)
                        }
                        LIGHT -> {
                            result.add(sleep)
                        }
                        AWAKE -> {
                            result.add(sleep)
                        }
                        END ->{
                            result.add(sleep.copy(mMode = result.last().mMode))

                        }
                    }
                }
                return result
            }
        }

        /**
         * 获取各个状态的时间，分钟数，数据必须是按时间升序排列的
         */
        fun getSleepStatusDuration(sleeps: List<BleSleep>): SparseIntArray {
            val result = SparseIntArray()
            sleeps.forEachIndexed { index, sleep ->
                if (index < sleeps.lastIndex) {
                    val key = sleep.mMode
                    val duration = (sleeps[index + 1].mTime - sleep.mTime)
                    result.put(key, result[key] + duration)
                }
            }
            //转换为分钟
            result.put(LIGHT, ceil(result[LIGHT] / 60.0).toInt())
            result.put(DEEP, ceil(result[DEEP] / 60.0).toInt())
            result.put(AWAKE, ceil(result[AWAKE] / 60.0).toInt())
            return result
        }

        /**
         * 如果是新睡眠算法，睡眠总时长、深睡总时长、浅睡总时长、清醒总时长都是设备计算好，
         * 从设备返回的原始数据中获取mode为[TOTAL_LENGTH] [DEEP_LENGTH] [LIGHT_LENGTH] [AWAKE_LENGTH]的数据
         * 时长(分钟)为 mSoft << 8 + mStrong
         */
        fun getSleepStatusDurationNew(sleeps: List<BleSleep>): SparseIntArray {
            val result = SparseIntArray()
            sleeps.forEach {
                when(it.mMode){
                    TOTAL_LENGTH -> result.put(TOTAL_LENGTH, (it.mSoft shl 8) + it.mStrong) //总睡时长 (分钟)
                    DEEP_LENGTH -> result.put(DEEP, (it.mSoft shl 8) + it.mStrong)  //深睡时长 (分钟)
                    LIGHT_LENGTH -> result.put(LIGHT, (it.mSoft shl 8) + it.mStrong)  //浅睡时长 (分钟)
                    AWAKE_LENGTH -> result.put(AWAKE, (it.mSoft shl 8) + it.mStrong)  //清醒时长 (分钟)
                }
            }
            return result
        }
    }
}
