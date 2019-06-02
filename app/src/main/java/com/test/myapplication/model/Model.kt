package com.test.myapplication.model

import android.graphics.Color
import android.util.Log
import java.util.*

class Data<T>(val dispersionList: List<Dispersion<T>>, val ovalsAmount: Int) {
    fun validate(): Boolean {
        val tag = Data::class.java.simpleName
        return if (ovalsAmount == 0) {
            Log.e(tag, "$tag ovalsAmount can't be 0")
            false
        } else if (ovalsAmount > 10) {
            Log.e(tag, "$tag ovalsAmount can't more than 10")
            false
        } else dispersionList.find { it.valueList.validate().not() } == null
    }
}

class Dispersion<T>(val title: String, val color: Int, val valueList: List<Value<T>>)

class Value<T>(val type: Type, val value: T)

class Type(val title: String, val textColor: Int, val position: Position)

class Mock() {
    companion object {
        fun dispersionList() = Data(
            listOf(dispersion(), dispersion()), Mock.random(3,3))

        fun dispersion() = Dispersion("dispersion title", Color.GRAY, valueList())

        fun valueList() = typeList().map { Value(it, Mock.random(0, 350)) }

        fun typeList() = listOf(
            Type("Monitoramento", Color.RED, Position.TOP),
            Type("Consumo", Color.BLUE, Position.LEFT),
            Type("Comercial", Color.GREEN, Position.RIGHT),
            Type("Não Comercial", Color.BLACK, Position.BOT))

        fun random(min: Int, max: Int): Int {
            val r = Random()
            return r.nextInt(max - min + 1) + min
        }
    }
}

enum class Position {
    TOP, RIGHT, BOT, LEFT
}

fun List<Value<*>>.validate(): Boolean {
    val tag = Value::class.java.simpleName
    return when {
        this.isEmpty() -> {
            val msg = "$tag list is empty"
            Log.e(tag, msg)
            return false
        }
        this.size != 4 -> {
            val msg = "$tag list doesn't contain all positions(TOP,LEFT,RIGHT and BOT)"
            Log.e(tag, msg)
            return false
        }
        this.firstOrNull { it.type.position == Position.BOT } == null -> {
            val msg = "$tag doesn't contain BOT position"
            Log.e(tag, msg)
            return false
        }
        this.firstOrNull { it.type.position == Position.LEFT } == null -> {
            val msg = "$tag doesn't contain LEFT position"
            Log.e(tag, msg)
            return false
        }
        this.firstOrNull { it.type.position == Position.RIGHT } == null -> {
            val msg = "$tag doesn't contain RIGHT position"
            Log.e(tag, msg)
            return false
        }
        this.firstOrNull { it.type.position == Position.TOP } == null -> {
            Log.e(
                tag, "$tag doesn't contain TOP position")
            return false
        }
        else -> true
    }
}