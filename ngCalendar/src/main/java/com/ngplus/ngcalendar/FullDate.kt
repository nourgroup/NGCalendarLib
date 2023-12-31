package com.ngplus.ngcalendar

class FullDate(
    val year: Int = 0,
    val month: Int = 0,
    val day: Int = 0,
    val hours: List<String> = listOf()
) : Comparable<FullDate> {
    override fun equals(other: Any?): Boolean {
        val day = other as FullDate
        return day.day == this.day && day.month == this.month && day.year == this.year
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return "$year/$month/$day"
    }

    override fun compareTo(other: FullDate): Int {
        return if (this.year > other.year) {
            1
        } else if (this.year == other.year) {
            if (this.month > other.month) {
                1
            } else if (this.month == other.month) {
                if (this.day > other.day) {
                    1
                } else if (this.day == other.day) {
                    0
                } else {
                    -1
                }
            } else {
                -1
            }
        } else {
            -1
        }
    }
}