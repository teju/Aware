package com.szabh.smable3.entity

object BleRepeat {
    const val MONDAY = 1
    const val TUESDAY = 1 shl 1
    const val WEDNESDAY = 1 shl 2
    const val THURSDAY = 1 shl 3
    const val FRIDAY = 1 shl 4
    const val SATURDAY = 1 shl 5
    const val SUNDAY = 1 shl 6
    const val ONCE = 0
    const val WORKDAY = MONDAY or TUESDAY or WEDNESDAY or THURSDAY or FRIDAY
    const val WEEKEND = SATURDAY or SUNDAY
    const val EVERYDAY = MONDAY or TUESDAY or WEDNESDAY or THURSDAY or FRIDAY or SATURDAY or SUNDAY

    private val WEEKDAYS = listOf(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)

    /**
     * 把Int值的重复转换成"周一 周二 周三"，或者"Mon Tues Thur"，具体依赖transfer的返回值
     */
    fun toWeekdayText(repeat: Int, transfer: (Int) -> String): String {
        val builder = StringBuilder()
        for (weekday in WEEKDAYS) {
            if ((repeat and EVERYDAY) and weekday > 0) {
                builder.append(transfer.invoke(weekday))
            }
        }
        return builder.toString()
    }

    /**
     * 把Int值的重复转换成索引集合，比如 MONDAY | TUESDAY 转换成 (0 ,1), 具体依赖transfer的返回值，默认:
     * MONDAY索引为0
     * TUESDAY索引为1
     *   ..
     *   ..
     * SATURDAY索引为5
     * SUNDAY索引为6
     */
    fun toIndices(repeat: Int, transfer: (Int) -> Int = { WEEKDAYS.indexOf(it) }): Set<Int> {
        val indices = mutableSetOf<Int>()
        for (weekday in WEEKDAYS) {
            if ((repeat and EVERYDAY) and weekday > 0) {
                indices.add(transfer.invoke(weekday))
            }
        }
        return indices
    }

    /**
     * 把索引集合转换成Int值的重复，比如 (0 ,1) 转换成 MONDAY | TUESDAY, 具体依赖transfer的返回值，默认:
     * 0为MONDAY
     * 1为TUESDAY
     *   ..
     *   ..
     * 5为SATURDAY
     * 6为SUNDAY
     */
    fun indicesToRepeat(indices: Set<Int>, transfer: (Int) -> Int = { WEEKDAYS[it] }): Int {
        var repeat = 0
        for (index in indices) {
            repeat = repeat or transfer.invoke(index)
        }
        return repeat
    }
}