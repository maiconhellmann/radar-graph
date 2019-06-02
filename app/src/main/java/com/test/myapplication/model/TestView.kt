package com.test.myapplication.model

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup

class TestView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    val data = listOf(DataModel(listOf( //
        Vertices("Monitoramento", "235", { it }, { it.toInt() }),
        Vertices("Horas", "500", { it }, { it.toInt() }),
        Vertices("Cambio", "290", { it }, { it.toInt() }),
        Vertices("Aviação", "100", { it }, { it.toInt() }))), DataModel(listOf( //
        Vertices("Monitoramento", "135", { it }, { it.toInt() }),
        Vertices("Horas", "200", { it }, { it.toInt() }),
        Vertices("Cambio", "590", { it }, { it.toInt() }),
        Vertices("Aviação", "200", { it }, { it.toInt() }))))

    private var hasError = false
    private var center = PointF()
    private var ovalList: List<Float> = emptyList()
    private var minGraphSize: Int = 0
    private val ovalsAmount = 3
    private val circleMarginPercent = 30f
    private val ovalSizePercent = 1f
    private val axisLineStrokePercent = .3f
    private val axisMarginPercent = 15f

    val path = Path()

    private val pathDataList = data.map { mutableListOf<PointF>() }

    init {
        data.forEachIndexed { i, dataModel ->
            dataModel.vertices.forEachIndexed { index, _ ->
                pathDataList[i].add(index, PointF(0f, 0f))
            }
        }
    }

    private val paintOval = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(50, 50, 50, 255)
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val paintTitleText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        isAntiAlias = true
        textSize = 32f
    }

    private val paintValueDiamond1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 50, 200, 200)
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 4f
        isAntiAlias = true
        isDither = true
    }

    private val paintValueDiamond2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 50, 255, 50)
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 4f
        isAntiAlias = true
        isDither = true
    }

    private val paintLineAxis = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(180, 220, 180)
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    private val paintCircleAxis = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(180, 220, 180)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintTextValue1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 50, 200, 200)
        style = Paint.Style.STROKE
        isAntiAlias = true
        textSize = 24f
    }
    private val paintTextValue2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 50, 255, 50)
        style = Paint.Style.STROKE
        isAntiAlias = true
        textSize = 24f
    }

    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val minHeight =
            if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) 400 else layoutParams.height

        setMeasuredDimension(
            getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            getDefaultSize(minHeight, heightMeasureSpec))

        runCalculations()
    }

    override fun onDraw(pCanvas: Canvas?) {
        super.onDraw(pCanvas)
        Log.d("maicon", "onDraw")
        pCanvas?.apply {
            //background
            drawRect(0, 0, measuredWidth, measuredHeight, paintBackground)

            val verticesTypes =
                data.flatMap { dataModel -> dataModel.vertices.map { it.type } }.distinct()
            val angle = 360 / verticesTypes.size
            val radius = calculateAxisSize()
            val ovalRadius = minGraphSize.percent(ovalSizePercent)

            ovalList.forEach {
                drawCircle(center.x, center.y, it, paintOval)
            }

            verticesTypes.forEachIndexed { verticeTypeIndex, title ->
                val theta = degreesToRadians(angle * verticeTypeIndex)

                val xEndVertices = polarToX(theta, radius) + center.x
                val yEndVertices = polarToY(theta, radius) + center.y

                drawCircle(
                    xEndVertices, yEndVertices, ovalRadius, paintCircleAxis)
                drawLine(center.x, center.y, xEndVertices, yEndVertices, paintLineAxis)

                //draw titles
                var xTitle = xEndVertices - paintTitleText.rectOfText(title).centerX()
                val titleRectSize = paintTitleText.rectOfText(title)
                val yTitle = when {
                    20 + yEndVertices > center.y -> yEndVertices + titleRectSize.height() + 20
                    else -> yEndVertices - titleRectSize.height()
                }
                //avoid draw out of screen
                if (xTitle + titleRectSize.width() >= measuredWidth) {
                    xTitle = measuredWidth - titleRectSize.width() - 20.0
                }
                if (xTitle <= 0) {
                    xTitle = 10.0
                }
                drawText(title, xTitle, yTitle, paintTitleText)
                //fim Draw titles

                //**Draw paths
                //array bidimensional: [data][typeList] exemplo: [2018]["monitoramento":100, ...]
                data.forEachIndexed { i, data ->
                    val verticesIndex = data.vertices.indexOfFirst { it.type == title }

                    if (verticeTypeIndex != -1) {
                        val vertices = data.vertices[verticesIndex]
                        val verticesPoint = pathDataList[i][verticesIndex]

                        val value = vertices.asNumber()
                        val percent = value.getPercentFrom(getMaxVerticeValue())
                        val drawableRadius = radius.minusPercent(20f)
                        val valueRadius = drawableRadius - drawableRadius.minusPercent(percent)

                        verticesPoint.x = polarToX(theta, valueRadius).toFloat() + center.x
                        verticesPoint.y = polarToY(theta, valueRadius).toFloat() + center.y
                    }
                }
            }

            pathDataList.forEachIndexed { i, it ->
                path.reset()
                it.forEachIndexed { index, point ->
                    if (index == 0) {
                        path.moveTo(point.x, point.y)
                    } else if (index < it.size) {
                        path.lineTo(point.x, point.y)
                    }
                }
                path.close()

                drawPath(path, if (i == 0) paintValueDiamond1 else paintValueDiamond2)
            }
        }
    }

    private fun getMaxVerticeValue(): Number {
        var max = 0.0
        data.forEach { vertices ->
            val currentMax = vertices.vertices.maxBy { item -> item.asNumber().toDouble() }
            currentMax?.let {
                max = if (it.asNumber().toDouble() > max) it.asNumber().toDouble() else max
            }
        }
        return max
    }

    private fun runCalculations() {
        Log.d("maicon", "---runCalculations---")
        minGraphSize = calculateMinimumSize()
        center.x =measuredWidth.center().toFloat()
        center.y =measuredHeight.center().toFloat()
        hasError = false
        ovalList = calculateOvalList()
        paintLineAxis.apply { strokeWidth = calculatMinAxisStrokeWidth().toFloat() }
        paintTitleText.apply { textSize = calculateTextTitleSizes() }
    }

    private fun calculateTextTitleSizes() = min(minGraphSize.percent(5).toDouble(), 35.0).toFloat()

    private fun calculatMinAxisStrokeWidth() = max(minGraphSize.percent(axisLineStrokePercent), 2)

    private fun calculateOvalList(): List<Float> {
        val circlesPadding = minGraphSize.center().percent(circleMarginPercent)
        val maxSize = minGraphSize.center().toFloat() - circlesPadding.toFloat()

        val list = mutableListOf<Float>()
        for (i in 1..ovalsAmount) {
            val size = maxSize / ovalsAmount * i
            list.add(size)
        }
        return list.toList()
    }

    private fun min(number: Number, number2: Number) =
        Math.min(number.toDouble(), number2.toDouble())

    private fun calculateAxisSize() = minGraphSize.center().minusPercent(axisMarginPercent)

    private fun calculateMinimumSize() = Math.min(measuredHeight, measuredWidth)

    private fun polarToX(theta: Number, r: Number) = r.toDouble() * Math.cos(theta.toDouble())
    private fun polarToY(theta: Number, r: Number) = r.toDouble() * Math.sin(theta.toDouble())
    private fun degreesToRadians(angleInDegrees: Int) = Math.PI * angleInDegrees / 180.0

    fun max(number1: Number, number2: Number) = Math.max(number1.toDouble(), number2.toDouble())
}

private operator fun Number.minus(number: Number): Number = this.toDouble() - number.toDouble()

private fun Number.getPercentFrom(maxValue: Number): Number {
    return this.toDouble() * 100.0 / maxValue.toDouble()
}

fun Number.percent(percent: Number): Number {
    return (this.toDouble() * percent.toDouble()) / 100.0
}

fun Number.minusPercent(percent: Number): Number {
    if (this.toDouble() > 0.0) {
        return this.toDouble() - this.percent(percent).toDouble()
    }
    return this
}

fun Number.plusPercent(percent: Number): Number {
    if (this.toDouble() > 0.0) {
        return this.toDouble() + this.percent(percent).toDouble()
    }
    return this
}

fun Canvas.drawCircle(cx: Number, cy: Number, radius: Number, paint: Paint) {
    return this.drawCircle(cx.toFloat(), cy.toFloat(), radius.toFloat(), paint)
}

fun Canvas.drawLine(startX: Number, startY: Number, endX: Number, endY: Number, paint: Paint) {
    return this.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), paint)
}

fun Canvas.drawText(text: String, x: Number, y: Number, paint: Paint) {
    return this.drawText(text, x.toFloat(), y.toFloat(), paint)
}

fun Canvas.drawRect(left: Number, top: Number, right: Number, bottom: Number, paint: Paint) {
    return drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
}

fun Paint.rectOfText(text: String): Rect {
    val rect = Rect()
    getTextBounds(text, 0, text.length, rect)

    return rect
}

fun PointF.setX(number: Number) {
    this.x = number.toFloat()
}

fun PointF.setY(number: Number) {
    this.y = number.toFloat()
}

fun PointF.set(x: Number, y: Number) {
    this.x = x.toFloat()
    this.y = y.toFloat()
}

fun Number.center(): Number = this.toDouble() / 2
/**
 * ********************* DATA ******************
 */

//test
class DataModel<T>(val vertices: List<Vertices<T>>)

class Vertices<T>(
    val type: String,
    val value: T,
    private val asString: (T) -> String,
    private val asNumber: (T) -> Number) {
    override fun toString() = asString.invoke(value)
    fun asNumber() = asNumber.invoke(value)
}
