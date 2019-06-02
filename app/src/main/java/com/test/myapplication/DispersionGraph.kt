package com.test.myapplication

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.test.myapplication.model.Mock
import com.test.myapplication.model.Position
import com.test.myapplication.model.Type

class DispersionGraph(
    context: Context, attrs: AttributeSet?) : View(context, attrs),
    ValueAnimator.AnimatorUpdateListener {

    private var data = Mock.dispersionList()

    private var center = PointF()
    private var ovalList: List<Float> = emptyList()
    private var minGraphSize: Int = 0
    private var pathList = emptyList<Path>()
    private var pathListAnimated = emptyList<Path>()
    private lateinit var horizontalAxis: Pair<Double, Double>
    private lateinit var verticalAxis: Pair<Double, Double>
    private lateinit var textTitleListPosition: List<Pair<Type, Point>>
    private val axisBallRadius = 10f
    private var marginTextTitle = 40
    private val circleMarginPercent = 20f
    private lateinit var valueTextList: List<Triple<String, Paint, Point>>
    private var hasError: Boolean = false
    private val noDataFoundText: String = "No data found"

    private var mAnimator: ValueAnimator? = null

    init {
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.DispersionGraph)
        //        val count = typedArray.getInt(R.styleable.DispersionGraph, 0)
        typedArray.recycle()
    }

    private val paintOval = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.argb(50, 50, 50, 255)
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private val paintTitleText = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        isAntiAlias = true
        textSize = 32f
    }

    private val paintValueDiamond1 = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 50, 200, 200)
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 4f
        isAntiAlias = true
        isDither = true
    }

    private val paintValueDiamond2 = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 50, 255, 50)
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 4f
        isAntiAlias = true
        isDither = true
    }

    private val paintLineAxis = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(180, 220, 180)
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }
    private val paintCircleAxis = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(180, 220, 180)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintTextValue1 = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 50, 200, 200)
        style = Paint.Style.STROKE
        isAntiAlias = true
        textSize = 24f
    }
    private val paintTextValue2 = Paint(ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 50, 255, 50)
        style = Paint.Style.STROKE
        isAntiAlias = true
        textSize = 24f
    }

    private val paintBackground = Paint(ANTI_ALIAS_FLAG).apply {
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

    private fun runCalculations() {
        Log.d("maicon", "---runCalculations---")
        minGraphSize = calculateMinimumSize()
        center.x = minGraphSize.center().toFloat()
        center.y = minGraphSize.center().toFloat()
        hasError = false

        if (data.validate()) {
            ovalList = calculateOvalList()
            horizontalAxis = calculateHorizontalAxis()
            verticalAxis = calculateVerticalAxis()
            pathList = calculatePathList()
            textTitleListPosition = calculateTextTitlePositionList()
            paintTitleText.apply { textSize = calculateTextTitleSizes() }
            paintTextValue1.apply { textSize = calculateTextValueSizes() }
            paintTextValue2.apply { textSize = calculateTextValueSizes() }
            marginTextTitle = calculateMarginTextTitle()
            valueTextList = calculateValueTextList()
        } else {
            hasError = true
        }
    }

    private fun calculateValueTextList(): List<Triple<String, Paint, Point>> {

        val list = mutableListOf<Triple<String, Paint, Point>>()

        data.dispersionList.forEachIndexed { dispersionIndex, dispersion ->
            dispersion.valueList.forEach { value ->
                val paint = if (dispersionIndex == 0) paintTextValue1 else paintTextValue2
                val rec = paint.rectOfText(value.value.toString())
                val position = Point()
                val yMargin = dispersionIndex * 6
                val xMargin = minGraphSize.percent(2).toInt()

                when (value.type.position) {
                    Position.RIGHT -> {
                        position.x = horizontalAxis.second.toInt() + xMargin
                        position.y = center.y.toInt() + dispersionIndex * rec.height() + yMargin
                    }
                    Position.LEFT -> {
                        position.x = horizontalAxis.first.toInt() - xMargin - rec.width()
                        position.y = center.y.toInt() + dispersionIndex * rec.height() + yMargin
                    }
                    Position.TOP -> {
                        position.x = center.x.toInt() + xMargin
                        position.y = verticalAxis.first.toInt() + dispersionIndex * rec.height() +
                            yMargin
                    }
                    else -> { //BOT
                        position.x = center.x.toInt() + xMargin
                        position.y = verticalAxis.second.toInt() + dispersionIndex * rec.height() +
                            yMargin
                    }
                }

                list.add(Triple(value.value.toString(), paint, position))
            }
        }

        return list
    }

    private fun calculateMarginTextTitle() = minGraphSize.percent(3).toInt()

    private fun calculateTextTitleSizes(): Float {
        return Math.min(minGraphSize.percent(5).toDouble(), 35.0).toFloat()
    }

    private fun calculateTextValueSizes(): Float {
        return Math.min(minGraphSize.percent(3).toDouble(), 26.0).toFloat()
    }

    private fun getHorizontalAxisPosition(number: Number) =
        getPositionFromAxis(number, horizontalAxis)

    private fun getVerticalAxisPosition(number: Number) = getPositionFromAxis(number, verticalAxis)

    private fun getPositionFromAxis(number: Number, axis: Pair<Double, Double>): Float {
        val maxValue = getMaxDispersionValue()

        val maxSize =
            (axis.second - axis.first).center().minusPercent(circleMarginPercent).toDouble()

        val valuePercent = number.toDouble() * 100 / maxValue.toDouble()

        return (maxSize * valuePercent / 100.0).toFloat()
    }

    private fun getMaxDispersionValue(): Number {
        var max = 0
        data.dispersionList.forEach { dispersion ->
            val currentMax = dispersion.valueList.maxBy { item -> item.value }
            currentMax?.let {
                max = if (it.value > max) it.value else max
            }
        }
        return max
    }

    private fun calculatePathList(): List<Path> {
        val list = mutableListOf<Path>()

        data.dispersionList.forEach { dispersion ->
            val path = Path()

            val valueList = dispersion.valueList.sortedBy { it.type.position.ordinal }

            valueList.forEachIndexed { index, value ->

                value.let {
                    path.fillType = Path.FillType.EVEN_ODD

                    when (index) {
                        0 -> path.moveTo(
                            center.x, center.y - getVerticalAxisPosition(value.value))
                        1 -> path.lineTo(
                            center.x + getHorizontalAxisPosition(value.value), center.y)
                        2 -> path.lineTo(
                            center.x, center.y + getVerticalAxisPosition(value.value))
                        3 -> path.lineTo(
                            center.x - getHorizontalAxisPosition(value.value), center.y)
                    }
                }
            }
            Log.d("maicon", "***CLOSE****")
            path.close()
            list.add(path)

        }

        return list
    }

    private fun calculateOvalList(): List<Float> {
        val circlesPadding = calculateCirclePadding()

        val maxSize = minGraphSize.center().toFloat() - circlesPadding
        val minSize = 0f

        val list = mutableListOf<Float>()
        for (i in 1..data.ovalsAmount) {
            val size = minSize + (maxSize / data.ovalsAmount) * i
            list.add(size)
        }
        return list.toList()
    }

    private fun calculateCirclePadding(): Float {
        return ((minGraphSize / 100.0) * circleMarginPercent).toFloat()
    }

    private fun calculateMinimumSize() = Math.min(measuredHeight, measuredWidth)

    private fun calculateTextTitlePositionList(): List<Pair<Type, Point>> {
        return data.dispersionList.flatMap { d ->
            d.valueList.map {
                calculateTextTitlePosition(it.type)
            }
        }
    }

    private fun calculateTextTitlePosition(type: Type): Pair<Type, Point> {
        val rect = Rect()
        paintTitleText.getTextBounds(type.title, 0, type.title.length, rect)

        val point = when (type.position) {
            Position.TOP -> {
                Point(
                    center.x.toInt() - rect.centerX(),
                    Math.max(verticalAxis.first.toInt() - rect.height(), 0))
            }
            Position.BOT -> {
                Point(
                    center.x.toInt() - rect.centerX(),
                    verticalAxis.second.toInt() + rect.height() + marginTextTitle)
            }
            Position.LEFT -> {
                Point(
                    Math.max(horizontalAxis.first.toInt() - rect.centerX(), 0),
                    center.y.toInt() + rect.height() + marginTextTitle)
            }
            else -> {
                Point(
                    horizontalAxis.second.toInt() - rect.centerX(),
                    center.y.toInt() + rect.height() + marginTextTitle)
            }
        }

        return Pair(type, point)
    }

    private fun calculateHorizontalAxis(): Pair<Double, Double> {
        val left = (measuredWidth - minGraphSize).center().toDouble() + minGraphSize / 100.0 * 12
        val right = (measuredWidth - minGraphSize).center().toDouble() + minGraphSize / 100.0 * 88
        return Pair(left, right)
    }

    private fun calculateVerticalAxis(): Pair<Double, Double> {

        val top = (measuredHeight - minGraphSize) / 2 + minGraphSize / 100.0 * 12
        val bot = (measuredHeight - minGraphSize) / 2 + minGraphSize / 100.0 * 88

        return Pair(top, bot)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {

            //background
            drawRect(0, 0, measuredWidth, measuredHeight, paintBackground)

            if (hasError.not()) {
                //draw all cricles
                ovalList.forEach {
                    drawCircle(center.x, center.y, it, paintOval)
                }

                //draw axis horizontal
                drawLine(
                    horizontalAxis.first, center.y, horizontalAxis.second, center.y, paintLineAxis)

                //draw axis vertical
                drawLine(center.x, verticalAxis.first, center.x, verticalAxis.second, paintLineAxis)

                //draw ball on lines
                drawCircle(center.x, verticalAxis.first, axisBallRadius, paintCircleAxis)
                drawCircle(center.x, verticalAxis.second, axisBallRadius, paintCircleAxis)
                drawCircle(horizontalAxis.first, center.y, axisBallRadius, paintCircleAxis)
                drawCircle(horizontalAxis.second, center.y, axisBallRadius, paintCircleAxis)

                pathList.forEachIndexed { index, path ->
                    val paint = if (index == 0) paintValueDiamond1 else paintValueDiamond2
                    drawPath(path, paint)
                }

                textTitleListPosition.forEach {
                    drawText(
                        it.first.title, it.second.x, it.second.y, paintTitleText)
                }

                //Value text on axis
                valueTextList.forEach {
                    drawText(it.first, it.third.x, it.third.y, it.second)
                }
            } else {
                val rect = paintTitleText.rectOfText(noDataFoundText)
                drawText(
                    noDataFoundText,
                    center.x - rect.centerX(),
                    center.y - rect.centerY(),
                    paintTitleText)
            }
        }
    }

    override fun invalidate() {
        data = Mock.dispersionList()
        super.invalidate()

        runCalculations()
    }

    fun startAnimating() {
        mAnimator = ValueAnimator.ofInt(1, 100)
        mAnimator?.duration = 1000
        mAnimator?.addUpdateListener(this)
        mAnimator?.start()
    }

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        //        pathList.forEach {
        //
        //        }
        //        mPath.reset()
        //        val b = getBounds()
        //        mPath.moveTo(b.left, b.bottom)
        //        mPath.quadTo((b.right - b.left) / 2, animator.getAnimatedValue() as Int, b.right, b.bottom)
        //        invalidateSelf()
    }

    private fun polarToX(theta: Number, r: Number) = r.toDouble() * Math.cos(theta.toDouble())
    private fun polarToY(theta: Number, r: Number) = r.toDouble() * Math.sin(theta.toDouble())
    private fun degreesToRadians(angleInDegrees: Int) = Math.PI * angleInDegrees / 180.0

    companion object {
        val TAG: String = DispersionGraph::class.java.simpleName
    }
}

private fun Number.percent(percent: Number): Number {
    return (this.toDouble() * percent.toDouble()) / 100.0
}

private fun Number.minusPercent(percent: Number): Number {
    if (this.toDouble() > 0.0) {
        return this.toDouble() - this.percent(percent).toDouble()
    }
    return this
}

private fun Number.plusPercent(percent: Number): Number {
    if (this.toDouble() > 0.0) {
        return this.toDouble() + this.percent(percent).toDouble()
    }
    return this
}

private fun Canvas.drawCircle(cx: Number, cy: Number, radius: Number, paint: Paint) {
    return this.drawCircle(cx.toFloat(), cy.toFloat(), radius.toFloat(), paint)
}

private fun Canvas.drawLine(
    startX: Number,
    startY: Number,
    endX: Number,
    endY: Number,
    paint: Paint) {
    return this.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), paint)
}

private fun Canvas.drawText(text: String, x: Number, y: Number, paint: Paint) {
    return this.drawText(text, x.toFloat(), y.toFloat(), paint)
}

private fun Canvas.drawRect(
    left: Number,
    top: Number,
    right: Number,
    bottom: Number,
    paint: Paint) {
    return drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
}

private fun Paint.rectOfText(text: String): Rect {
    val rect = Rect()
    getTextBounds(text, 0, text.length, rect)

    return rect
}

private fun Number.center(): Number = this.toDouble() / 2

class PointP(val theta: Number, val r: Number) {
    override fun toString() = "$theta, $r"
}