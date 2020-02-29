package com.epolly.graph

import DataList
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


class RadarGraphView: View, ValueAnimator.AnimatorUpdateListener {

    //region Constructors
    @JvmOverloads
    constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : super(context, attrs, defStyleAttr) {
        initAttrs(attrs)
    }

    @TargetApi(LOLLIPOP)
    constructor(
        context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int
    ) : super(
        context, attrs, defStyleAttr, defStyleRes) {
        initAttrs(attrs)
    }
    // endregion

    var isAnimationEnabled: Boolean = false

    private var paintDataList: List<Paint> = emptyList()

    private var pathDataList = emptyList<MutableList<PointF>>()

    //Data model containing the data used to populate the graph(user input)
    var dataModel = DataList<String>(dataList = emptyList())
    set(value) {
        field = value
        init()
        invalidate()

        if (isAnimationEnabled) {
            startAnimating()
        } else{
            calcPathList()
        }
    }

    // Center of the graph(minGraphSize / 2)
    private var center = PointF()

    // List of background circles
    private var backgroundOvalList: List<Float> = emptyList()

    // It is a square defined by the minimum values between measuredWith and measuredHeight
    private var minGraphSize: Int = 0

    private var circlesAmount: Int = 3

    private var circlesMarginPercent = 30f

    private var axisPointRadiusPercent = 1f

    private var axisLineStrokePercent = .3f

    // Percentage of the margin between each circle
    private val axisMarginPercent = 15f

    // Path used to draw the lines between axis
    val path = Path()

    private lateinit var paintCircles: Paint

    private lateinit var noDataFoundText: CharSequence

    private lateinit var paintAxisTitleText: Paint

    private lateinit var paintLineAxis: Paint

    private lateinit var paintAxisCircle: Paint

    //region paint
    private fun initPaintCircles(paintColor: Int) {
        paintCircles = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = paintColor
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
    }

    private fun initPaintAxisTitleText(paintColor: Int) {
        paintAxisTitleText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = paintColor
            style = Paint.Style.STROKE
            isAntiAlias = true
            textSize = 32f
        }
    }

    private fun initPaintLineAxis(paintColor: Int) {
        paintLineAxis = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = paintColor
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
    }

    private fun initPaintAxisCircle(paintColor: Int) {
        paintAxisCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = paintColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }
    // endregion

    // region initAttrs
    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it,
                R.styleable.RadarGraphView, 0, 0)

            //Oval color
            val circlesColor = typedArray.getColor(
                R.styleable.RadarGraphView_circlesColor, context.parseColor(R.color.defaultOval))

            initPaintCircles(circlesColor)

            // Oval amount
            circlesAmount = typedArray.getInt(R.styleable.RadarGraphView_circlesAmount, circlesAmount)

            //Circle margin percent
            circlesMarginPercent = typedArray.getFloat(R.styleable.RadarGraphView_circlesMarginPercent, circlesMarginPercent)

            // size of the axis point (oval at the end of the axis)
            axisPointRadiusPercent = typedArray.getFloat(R.styleable.RadarGraphView_axisPointRadiusPercent, axisPointRadiusPercent)

            // Line stroke for the axis
            axisLineStrokePercent = typedArray.getFloat(R.styleable.RadarGraphView_axisLineStrokePercent, axisLineStrokePercent)

            // Text displayed when there is no data
            noDataFoundText = typedArray.getText(R.styleable.RadarGraphView_noDataFoundText)

            // Color of the axis title
            initPaintAxisTitleText(
                typedArray.getColor(R.styleable.RadarGraphView_axisTitleTextColor, context.parseColor(R.color.grey))
            )

            // Color of the axis
            initPaintLineAxis(
                typedArray.getColor(R.styleable.RadarGraphView_axisLineColor, context.parseColor(R.color.defaultLineAxis))
            )

            // Color of the circle of the axis
            initPaintAxisCircle(
                typedArray.getColor(R.styleable.RadarGraphView_axisCircleColor, context.parseColor(R.color.defaultCircleAxis))
            )

            isAnimationEnabled = typedArray.getBoolean(R.styleable.RadarGraphView_isAnimationEnabled, true)

            typedArray.recycle()
        }
    }
    // endregion

    private fun init() {
        pathDataList = dataModel.dataList.map { mutableListOf<PointF>() }

        // create a path for each vertex
        dataModel.dataList.forEachIndexed { i, dataModel ->
            dataModel.vertexList.forEachIndexed { index, _ ->
                pathDataList[i].add(index, PointF(0f, 0f))
            }
        }

        //Internal fun
        fun createRadarPaint(@ColorRes resColor: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = context.parseColor(resColor)
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = 4f
            isAntiAlias = true
            isDither = true
        }

        paintDataList = dataModel.dataList.map { createRadarPaint(it.color) }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d("RadarGraphView", "onMeasure")
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
        paintAxisTitleText.apply { textSize = calculateTextTitleSizes() }
    }

    override fun onDraw(pCanvas: Canvas?) {
        super.onDraw(pCanvas)
        Log.d("RadarGraphView", "onDraw")
        pCanvas?.apply {

            // If the list of data is empty
            if (dataModel.dataList.isEmpty()) {
                val rect = paintAxisTitleText.rectOfText(noDataFoundText)
                drawText(noDataFoundText.toString(), center.x - rect.centerX(), center.y - rect.centerY(), paintAxisTitleText)
                return
            }

            // divide the circle by the numbers of vertex
            val angle = calcAngle()

            val radius = calculateAxisSize()
            val ovalRadius = minGraphSize.percent(axisPointRadiusPercent)

            // drawn background circles
            backgroundOvalList.forEach {
                drawCircle(center.x, center.y, it, paintCircles)
            }

            dataModel.typeList.forEachIndexed { vertexTypeIndex, type ->
                val theta = degreesToRadians(angle * vertexTypeIndex)

                val xEndVertex = polarToX(theta, radius) + center.x
                val yEndVertex = polarToY(theta, radius) + center.y

                drawCircle(
                    xEndVertex, yEndVertex, ovalRadius, paintAxisCircle)
                drawLine(center.x, center.y, xEndVertex, yEndVertex, paintLineAxis)

                // region draw titles
                var xTitle = xEndVertex - paintAxisTitleText.rectOfText(type.label).centerX()
                val titleRectSize = paintAxisTitleText.rectOfText(type.label)
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
                drawText(type.label, xTitle, yTitle, paintAxisTitleText)
                // end region Draw titles
            }

            // drawn paths
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

                drawPath(path, paintDataList[i])
            }
        }
    }

    private fun calcPathList() {
        Log.d("RadarGraphView", "calcPathList")
        val angle = calcAngle()

        val radius = calculateAxisSize()

        dataModel.typeList.forEachIndexed { vertexTypeIndex, type ->
            val theta = degreesToRadians(angle * vertexTypeIndex)
            dataModel.dataList.forEachIndexed { i, data ->
                val vertexIndex = data.vertexList.indexOfFirst { it.type == type }

                if (vertexTypeIndex != -1) {
                    val vertex = data.vertexList[vertexIndex]
                    val vertexPoint = pathDataList[i][vertexIndex]

                    val value = vertex.asNumber()
                    val percent = value.getPercentFrom(getMaxVertexValue())
                    val drawableRadius = radius.minusPercent(20f)
                    val valueRadius = drawableRadius - drawableRadius.minusPercent(percent)

                    vertexPoint.x = polarToX(theta, valueRadius).toFloat() + center.x
                    vertexPoint.y = polarToY(theta, valueRadius).toFloat() + center.y
                }
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

    private fun calcAngle() = 360 / dataModel.typeList.size

    private fun calculateTextTitleSizes() = min(minGraphSize.percent(5).toDouble(), 35.0).toFloat()

    private fun calculateMinAxisStrokeWidth() = max(minGraphSize.percent(axisLineStrokePercent), 2)

    private fun calculateOvalList(): List<Float> {
        val circlesPadding = minGraphSize.center().percent(circlesMarginPercent)
        val maxSize = minGraphSize.center().toFloat() - circlesPadding.toFloat()

        val list = mutableListOf<Float>()
        for (i in 1..circlesAmount) {
            val size = maxSize / circlesAmount * i
            list.add(size)
        }
        return list.toList()
    }

    private fun min(number: Number, number2: Number) =
        number.toDouble().coerceAtMost(number2.toDouble())

    private fun calculateAxisSize() = minGraphSize.center().minusPercent(axisMarginPercent)

    private fun calcMaxSquareSize() = measuredHeight.coerceAtMost(measuredWidth)

    private fun polarToX(theta: Number, r: Number) = r.toDouble() * cos(theta.toDouble())
    private fun polarToY(theta: Number, r: Number) = r.toDouble() * sin(theta.toDouble())
    private fun degreesToRadians(angleInDegrees: Int) = Math.PI * angleInDegrees / 180.0

    private fun max(number1: Number, number2: Number) = number1.toDouble().coerceAtLeast(number2.toDouble())

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        val angle = calcAngle()
        val radius = calculateAxisSize()

        animation?.let {
            dataModel.dataList.forEachIndexed { outerIndex, list ->
                list.vertexList.forEachIndexed { index, vertex ->
                    val theta = degreesToRadians(angle * index)

                    val value = vertex.asNumber()
                    val percent = value.getPercentFrom(getMaxVertexValue())
                    val drawableRadius = radius.minusPercent(20f)
                    val valueRadius = (drawableRadius - drawableRadius.minusPercent(percent)).minusPercent(animation.animatedValue.toFloat())

                    pathDataList[outerIndex][index] = PointF(
                        polarToX(theta, valueRadius).toFloat() + center.x,
                        polarToY(theta, valueRadius).toFloat() + center.y
                    )
                    invalidate()
                }
            }
        }
    }

    private var mAnimator: ValueAnimator? = null
    fun startAnimating() {
        mAnimator = ValueAnimator.ofInt(100, 1).apply {
            duration = 500
            interpolator = CustomTimeInterpolator(1, .5)
            addUpdateListener(this@RadarGraphView)
            start()
        }
    }

    /**
     * Custom Interpolator
     */
    inner class CustomTimeInterpolator(private val bounces: Int, private val energy: Double) : TimeInterpolator {
        override fun getInterpolation(x: Float): Float = (1.0 + (-abs(cos(x * 10 * bounces / Math.PI)) * getCurveAdjustment(x))).toFloat()
        private fun getCurveAdjustment(x: Float) : Double = -(2 * (1 - x) * x * energy + x * x) + 1
    }
}

private fun Any.toFloat(): Float {
    return toString().toFloatOrNull() ?: 0f
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

fun Paint.rectOfText(text: CharSequence): Rect {
    val rect = Rect()
    getTextBounds(text.toString(), 0, text.length, rect)

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
