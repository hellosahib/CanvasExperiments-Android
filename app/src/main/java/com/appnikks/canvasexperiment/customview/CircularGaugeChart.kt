package com.appnikks.canvasexperiment.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.Dimension
import androidx.annotation.FloatRange
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import kotlin.math.min

class CircularGaugeChart : View {

    companion object {
        const val DEFAULT_STROKE_WIDTH = 15f
        const val DEFAULT_SPACING_BETWEEN_DATA = 40f
        const val DEFAULT_BACKGROUND_ITEM_COLOR = android.R.color.darker_gray
        const val DEFAULT_FOREGROUND_ITEM_COLOR = android.R.color.black
        val DEFAULT_DUMMY_DATA = listOf(CircularData(0.8f), CircularData(0.6f), CircularData(0.4f))
        val DEFAULT_DUMMY_SWEEP_POINTS = DEFAULT_DUMMY_DATA.map { 360f * it.value }
    }

    private var strokeWidth = context.dpToPx(DEFAULT_STROKE_WIDTH)

    private var dataPoints = listOf<CircularData>()
        set(value) {
            field = value
            sweepAnglesPoints = value.map {
                360f * it.value
            }
        }

    private var sweepAnglesPoints = listOf<Float>()

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
        strokeWidth = DEFAULT_STROKE_WIDTH
        color = ContextCompat.getColor(context, android.R.color.black)
    }

    private val initialRect = RectF()
    private val requiredRect = RectF()

    private var isEqualSpacingEnabled = true

    private var spacingBetweenItems = DEFAULT_SPACING_BETWEEN_DATA

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val min = min(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(min, min)
        if (isInEditMode) {
            if (isEqualSpacingEnabled) {
                spacingBetweenItems =
                    (measuredHeight - (DEFAULT_DUMMY_DATA.size * DEFAULT_STROKE_WIDTH) - 50f) / DEFAULT_DUMMY_DATA.size
            }
        } else {
            if (isEqualSpacingEnabled) {
                spacingBetweenItems =
                    (measuredHeight - (dataPoints.size * DEFAULT_STROKE_WIDTH) - 50f) / dataPoints.size
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (isInEditMode) {
            drawDataSet(canvas, DEFAULT_DUMMY_DATA, DEFAULT_DUMMY_SWEEP_POINTS)
        } else {
            drawDataSet(canvas, dataPoints, sweepAnglesPoints)
        }
    }

    private fun drawDataSet(
        canvas: Canvas?,
        dataPoints: List<CircularData>,
        sweepAnglesPoints: List<Float>
    ) {
        // Sanity Checks
        if (dataPoints.isEmpty()) return

        // Initial Rect Setup
        initialRect.set(
            strokeWidth / 2,
            strokeWidth / 2,
            width.toFloat() - strokeWidth / 2,
            height.toFloat() - strokeWidth / 2
        )

        // Data Points Draw
        dataPoints.forEachIndexed { index, it ->
            val rect = if (index == 0) {
                initialRect
            } else {
                requiredRect.apply {
                    set(
                        initialRect.left + spacingBetweenItems / 2 + strokeWidth / 2,
                        initialRect.top + spacingBetweenItems / 2 + strokeWidth / 2,
                        initialRect.right - spacingBetweenItems / 2 - strokeWidth / 2,
                        initialRect.bottom - spacingBetweenItems / 2 - strokeWidth / 2
                    )
                    initialRect.set(requiredRect)
                }

            }
            // Background Progress Bar
            arcPaint.color = ContextCompat.getColor(
                context,
                if (it.backgroundColor == -1) DEFAULT_BACKGROUND_ITEM_COLOR else it.backgroundColor
            )
            canvas?.drawArc(
                rect,
                -90f + sweepAnglesPoints[index],
                360f - sweepAnglesPoints[index],
                false,
                arcPaint
            )
            // Foreground Progress Bar
            arcPaint.color = ContextCompat.getColor(
                context,
                if (it.foregroundColor == -1) DEFAULT_FOREGROUND_ITEM_COLOR else it.foregroundColor
            )
            canvas?.drawArc(rect, -90f, sweepAnglesPoints[index], false, arcPaint)
        }
    }

    fun submitData(dataModel: List<CircularData>) {
        dataPoints = dataModel
        invalidate()
    }

    @Px
    private fun Context.dpToPx(@Dimension(unit = Dimension.DP) dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }

    data class CircularData(
        @FloatRange(from = 0.0, to = 1.0, fromInclusive = true, toInclusive = true)
        val value: Float,
        @ColorRes
        val foregroundColor: Int = -1,
        @ColorRes
        val backgroundColor: Int = -1
    )
}