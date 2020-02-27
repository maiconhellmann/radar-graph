package com.epolly.radargraph

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.epolly.radargraph.model.DummyData

class RadarGraphView: View {

    @JvmOverloads
    constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    @TargetApi(LOLLIPOP)
    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(
        context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    fun init(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it,
                R.styleable.RadarGraphView, 0, 0)

            val circleColor = typedArray.getColor(
                R.styleable.RadarGraphView_circleColor, context.parseColor(R.color.defaultOval))

            initPaintOval(circleColor)

            typedArray.recycle()
        }
    }

    //Data model containing the data used to populate the graph(user input)
    //var dataModel = DataList<String>(emptyList())
    var dataModel = DummyData.createDataList()

    // Center of the graph(minGraphSize / 2)
    private var center = PointF()

    // List of background circles
    private var backgroundOvalList: List<Float> = emptyList()

    // It is a square defined by the minimum values between measuredWith and measuredHeight
    private var minGraphSize: Int = 0

    // Amount of background circles TODO accept it as a parameter
    private val backgroundOvalAmount = 3

    // Margin between the background circles and the outer view TODO accept it as a parameter
    private val circleMarginPercent = 30f

    // Percentage of minGraphSize to define the size of the background circles TODO accept it as a parameter
    private val ovalSizePercent = 1f

    // Percentage of the minGraphSize to define the stroke of the axis lines TODO accept it as a parameter
    private val axisLineStrokePercent = .3f

    // Percentage of the margin between each circle
    private val axisMarginPercent = 15f

    // Path used to draw the lines between axis
    val path = Path()

    private val pathDataList = dataModel.dataList.map { mutableListOf<PointF>() }

    init {
        // create a path for each vertex TODO what does it really do?
        dataModel.dataList.forEachIndexed { i, dataModel ->
            dataModel.vertexList.forEachIndexed { index, _ ->
                pathDataList[i].add(index, PointF(0f, 0f))
            }
        }
    }

    private lateinit var paintOval: Paint

    private fun initPaintOval(paintColor: Int) {
        paintOval = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = paintColor
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
    }

    // TODO make it dinamic
    private val paintTitleText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        isAntiAlias = true
        textSize = 32f
    }

    // TODO make it dinamic
    private val paintValueDiamond1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 50, 200, 200)
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 4f
        isAntiAlias = true
        isDither = true
    }

    // TODO make it dinamic
    private val paintValueDiamond2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 50, 255, 50)
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 4f
        isAntiAlias = true
        isDither = true
    }

    // TODO make it dinamic
    private val paintLineAxis = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(180, 220, 180)
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    // TODO make it dinamic
    private val paintCircleAxis = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(180, 220, 180)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d("RadarGraphView", "widthMeasureSpec")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        runCalculations()
    }
    
    private fun runCalculations() {
        Log.d("RadarGraphView", "-runCalculations")
        minGraphSize = calcMaxSquareSize()
        center.x = measuredWidth.center().toFloat()
        center.y = measuredHeight.center().toFloat()
        backgroundOvalList = calculateOvalList()
        paintLineAxis.apply { strokeWidth = calculateMinAxisStrokeWidth().toFloat() }
        paintTitleText.apply { textSize = calculateTextTitleSizes() }
    }

    override fun onDraw(pCanvas: Canvas?) {
        super.onDraw(pCanvas)
        Log.d("RadarGraphView", "onDraw")
        pCanvas?.apply {

            // If the list of data is empty
            if (dataModel.dataList.isEmpty()) {
                val text = "No data found" // TODO should be dynamic
                val rect = paintTitleText.rectOfText(text) // TODO paint should be dynamic
                drawText(text, center.x - rect.centerX(), center.y - rect.centerY(), paintTitleText)
                return
            }

            // Get all vertexTypes distinctly
            val vertexTypes =
                dataModel.dataList.flatMap { dataModel -> dataModel.vertexList.map { it.type } }.distinct()

            // divide the circle by the numbers of vertex
            val angle = 360 / vertexTypes.size

            val radius = calculateAxisSize()
            val ovalRadius = minGraphSize.percent(ovalSizePercent)

            // drawn background circles
            backgroundOvalList.forEach {
                drawCircle(center.x, center.y, it, paintOval)
            }

            vertexTypes.forEachIndexed { vertexTypeIndex, type ->
                val theta = degreesToRadians(angle * vertexTypeIndex)

                val xEndVertex = polarToX(theta, radius) + center.x
                val yEndVertex = polarToY(theta, radius) + center.y

                drawCircle(
                    xEndVertex, yEndVertex, ovalRadius, paintCircleAxis)
                drawLine(center.x, center.y, xEndVertex, yEndVertex, paintLineAxis)

                // region draw titles
                var xTitle = xEndVertex - paintTitleText.rectOfText(type.label).centerX()
                val titleRectSize = paintTitleText.rectOfText(type.label)
                val yTitle = when {
                    20 + yEndVertex > center.y -> yEndVertex + titleRectSize.height() + 20
                    else -> yEndVertex - titleRectSize.height()
                }
                // avoid drawing out of screen
                if (xTitle + titleRectSize.width() >= measuredWidth) {
                    xTitle = measuredWidth - titleRectSize.width() - 20.0
                }
                if (xTitle <= 0) {
                    xTitle = 10.0
                }
                drawText(type.label, xTitle, yTitle, paintTitleText)
                // end region Draw titles

                // Draw paths between each value of a list of vertex
                dataModel.dataList.forEachIndexed { i, data ->
                    val vertexIndex = data.vertexList.indexOfFirst { it.type == type }

                    if (vertexTypeIndex != -1) {
                        val vertexList = data.vertexList[vertexIndex]
                        val vertexPoint = pathDataList[i][vertexIndex]

                        val value = vertexList.asNumber()
                        val percent = value.getPercentFrom(getMaxVertexValue())
                        val drawableRadius = radius.minusPercent(20f)
                        val valueRadius = drawableRadius - drawableRadius.minusPercent(percent)

                        vertexPoint.x = polarToX(theta, valueRadius).toFloat() + center.x
                        vertexPoint.y = polarToY(theta, valueRadius).toFloat() + center.y
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

    private fun getMaxVertexValue(): Number {
        var max = 0.0
        dataModel.dataList.forEach { vertexList ->
            val currentMax = vertexList.vertexList.maxBy { item -> item.asNumber().toDouble() }
            currentMax?.let {
                max = if (it.asNumber().toDouble() > max) it.asNumber().toDouble() else max
            }
        }
        return max
    }

    private fun calculateTextTitleSizes() = min(minGraphSize.percent(5).toDouble(), 35.0).toFloat()

    private fun calculateMinAxisStrokeWidth() = max(minGraphSize.percent(axisLineStrokePercent), 2)

    private fun calculateOvalList(): List<Float> {
        val circlesPadding = minGraphSize.center().percent(circleMarginPercent)
        val maxSize = minGraphSize.center().toFloat() - circlesPadding.toFloat()

        val list = mutableListOf<Float>()
        for (i in 1..backgroundOvalAmount) {
            val size = maxSize / backgroundOvalAmount * i
            list.add(size)
        }
        return list.toList()
    }

    private fun min(number: Number, number2: Number) =
        number.toDouble().coerceAtMost(number2.toDouble())

    private fun calculateAxisSize() = minGraphSize.center().minusPercent(axisMarginPercent)

    private fun calcMaxSquareSize() = measuredHeight.coerceAtMost(measuredWidth)

    private fun polarToX(theta: Number, r: Number) = r.toDouble() * Math.cos(theta.toDouble())
    private fun polarToY(theta: Number, r: Number) = r.toDouble() * Math.sin(theta.toDouble())
    private fun degreesToRadians(angleInDegrees: Int) = Math.PI * angleInDegrees / 180.0

    fun max(number1: Number, number2: Number) = number1.toDouble().coerceAtLeast(number2.toDouble())
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

fun Context.parseColor(@ColorRes color: Int) = ContextCompat.getColor(this, color)
