package com.szabh.smable3.entity

import com.bestmafen.baseble.data.BleReadable

data class BleActivity(
    var mTime: Int = 0,     //距离当地2000/1/1 00:00:00的秒数
    var mMode: Int = 0,     //运动模式，可参考下方 companion object 中的定义
    var mState: Int = 0,    //运动状态，可参考下方 companion object 中的定义
    var mStep: Int = 0,     //步数，例如值为10，即代表走了10步
    var mCalorie: Int = 0,  // 1/10000千卡，例如接收到的数据为56045，则代表 5.6045 Kcal 约等于 5.6 Kcal
    var mDistance: Int = 0  // 1/10000米，例如接收到的数据为56045，则代表移动距离 5.6045 米 约等于 5.6 米
) : BleReadable() {

    override fun decode() {
        super.decode()
        mTime = readInt32()
        mMode = readIntN(5).toInt()
        mState = readIntN(3).toInt()
        mStep = readInt24()
        mCalorie = readInt32()
        mDistance = readInt32()
    }

    companion object {
        const val ITEM_LENGTH = 16

        //运动模式 以下三种为自动识别的模式，没有开始、暂停、结束等状态
        const val AUTO_NONE = 1
        const val AUTO_WALK = 2
        const val AUTO_RUN = 3

        //运动模式 以下为手动锻炼模式 对应 mMode
        const val RUNNING = 7     // 跑步
        const val INDOOR = 8      // 室内运动，跑步机
        const val OUTDOOR = 9     // 户外运动
        const val CYCLING = 10    // 骑行
        const val SWIMMING = 11   // 游泳
        const val WALKING = 12    // 步行，健走
        const val CLIMBING = 13   // 爬山
        const val YOGA = 14       // 瑜伽
        const val SPINNING = 15   // 动感单车
        const val BASKETBALL = 16 // 篮球
        const val FOOTBALL = 17   // 足球
        const val BADMINTON = 18  // 羽毛球
        const val MARATHON = 19  // 马拉松
        const val INDOOR_WALKING = 20  // 室内步行
        const val FREE_EXERCISE = 21  // 自由锻炼
        const val AEROBIC_EXERCISE = 22  // 有氧运动

        //运动状态 手动锻炼模式下的状态 对应 mState
        const val BEGIN = 0 // 开始
        const val ONGOING = 1 // 进行中
        const val PAUSE = 2 // 暂停
        const val RESUME = 3 // 继续
        const val END = 4 // 结束
    }
}
