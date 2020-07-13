package com.appnikks.canvasexperiment.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.Dimension
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import com.appnikks.canvasexperiment.R
import kotlin.math.absoluteValue

class TwoPartScalableBarChart : View {

    companion object {
        private const val DEFAULT_SPACING = 20f
    }

    var dataSetColors = listOf(ContextCompat.getColor(context, R.color.colorAccent))

    // Dummy data
    var data = listOf(
        ChartUiModel(listOf(20f)),
        ChartUiModel(listOf(-30f)),
        ChartUiModel(listOf(10f))
    )
        set(value) {
            field = value
        }

    var barSpacing: Float = context.dpToPx(DEFAULT_SPACING)

    var drawZeroLine = true
    var zeroLineHeightDp = context.dpToPx(2f)
        set(value) {
            field = context.dpToPx(value)
            mZeroLinePaint.strokeWidth = field
        }
    var zeroLineColorRes = ContextCompat.getColor(context, android.R.color.black)
        set(value) {
            field = ContextCompat.getColor(context, value)
            mZeroLinePaint.color = field
        }


    // Paints
    private val mBarPaint = Paint().apply {
        style = Paint.Style.FILL
        // Default Color
        color = ContextCompat.getColor(context, R.color.colorPrimary)
    }

    private val mZeroLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = zeroLineColorRes
        strokeWidth = zeroLineHeightDp
    }

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

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val gridLeft = paddingLeft.toFloat()
        val gridBottom = height - paddingBottom.toFloat()
        val gridTop = paddingTop.toFloat()
        val gridRight = width - paddingRight.toFloat()

        val upperGraphTop = paddingTop.toFloat()
        val upperGraphBottom =
            ((height - paddingBottom.toFloat() - paddingTop.toFloat()) / 2) - if (drawZeroLine) zeroLineHeightDp / 2 else 0f

        val lowerGraphTop = upperGraphBottom + if (drawZeroLine) zeroLineHeightDp / 2 else 0f
        val lowerGraphBottom = height - upperGraphTop

        // Bars
        /**
         * Spacing Will Always Be Count + 1
         * |_|_|
         * Here _ represent Spacing
         * Count = 2
         */
        val totalColumnSpacing = barSpacing * (data.size - 1).toFloat()

        /**
         * GridRight-GridLeft is the total width.
         * - Total Column Spacing
         * / Data Count
         * Gives Width For Each Bar
         */
        val columnWidth = (gridRight - gridLeft - totalColumnSpacing) / data.size

        /**
         * These are initial Points For First Bar
         */
        var columnLeft = gridLeft
        var columnRight = columnLeft + columnWidth

        mBarPaint.color = dataSetColors[0]
        // Drawing Bars
        for (barValue in data) {
            // Calculate top of column based on barValue.

            val top = if (barValue.value[0] > 0) {
                // Draw in Upper Region
                upperGraphTop + (upperGraphBottom - upperGraphTop) * ((100f - barValue.value[0].absoluteValue) / 100f)
            } else {
                // Draw in Lower Region
                lowerGraphTop
            }

            val bottom = if (barValue.value[0] > 0) {
                upperGraphBottom
            } else {
                lowerGraphBottom * ((100f - barValue.value[0].absoluteValue) / 100f)
            }

            canvas?.drawRect(columnLeft, top, columnRight, bottom, mBarPaint)

            // Shift over left/right column bounds
            columnLeft = columnRight + totalColumnSpacing / (data.size - 1)
            columnRight = columnLeft + columnWidth
        }

        // Drawing Zero Line
        if (drawZeroLine) {
            canvas?.drawLine(
                gridLeft,
                upperGraphBottom,
                gridRight,
                upperGraphBottom,
                mZeroLinePaint
            )
        }
    }

    // Value should be between 0 - 100f
    data class ChartUiModel(val value: List<Float>)

    @Px
    private fun Context.dpToPx(@Dimension(unit = Dimension.DP) dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }
}