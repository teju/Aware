package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleWritable

// 阶段
enum class Stage(val stage: Int) {
    WARM_UP(0), GO_FOR(1), RECOVERY(2), REST_FOR(3), COOL_DOWN(4), OTHER(0xFF)
}

// 完成条件
enum class CompletionCondition(val condition: Int) {
    DURATION(0), MANUAL(1), DURATION_IN_HR_ZONE(2), HR_ABOVE(3), HR_BELOW(4)
}

// 心率区间
enum class HrZone(val zone: Int) {
    LOW(1), NORMAL(2), MODERATE(3), HARD(4), MAXIMUM(5)
}

enum class SegmentActivity(val activity: Int) {
    TIMER(0), RUN(1), JUMP_JACKS(2), PUSH_UP(3), DISTANCE(4), RUN_FAST(5),
    WALK(6), SWIM(7), BICYCLE(8), WORKOUT(9), REST(10), STRETCH(11),
    SPINNING(12), SIT_UP(15), WARM_UP(16), COOL_DOWN(17),
}

data class BleCoachingSegment(
    var mCompletionCondition: Int = 0,
    var mName: String = "", // 名称
    var mActivity: Int = 0, // 运动
    var mStage: Int = 0,

    // CompletionCondition为DURATION -> 秒数
    // CompletionCondition为MANUAL -> 重复次数
    // CompletionCondition为DURATION_IN_HR_ZONE -> 秒数
    // CompletionCondition为HR_ABOVE或HR_BELOW -> 心率值
    var mCompletionValue: Int = 0,

    // 只有在CompletionCondition为DURATION_IN_HR_ZONE时有意义
    var mHrZone: Int = 0
) : BleWritable() {
    override val mLengthToWrite: Int
        get() = ITEM_LENGTH

    override fun encode() {
        super.encode()
        writeInt8(mStage)
        writeStringWithFix(mName, LENGTH_NAME)
        writeInt8(mActivity)
        writeInt8(mCompletionCondition)
        writeInt16(mCompletionValue)
        writeInt8(mHrZone)
    }

    override fun decode() {
        super.decode()
        mStage = readInt8().toInt()
        mName = readString(LENGTH_NAME)
        mActivity = readInt8().toInt()
        mCompletionCondition = readInt8().toInt()
        mCompletionValue = readInt16().toInt()
        mHrZone = readInt8().toInt()
    }

    override fun toString(): String {
        return "BleCoachingSegment(mCompletionCondition=$mCompletionCondition, mName='$mName', " +
            "mActivity=$mActivity, mStage=$mStage, mCompletionValue=$mCompletionValue, mHrZone=$mHrZone)"
    }

    companion object {
        const val ITEM_LENGTH = 21

        private const val LENGTH_NAME = 15
    }
}
