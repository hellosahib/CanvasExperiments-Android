package com.appnikks.canvasexperiment.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.appnikks.canvasexperiment.R

class BarChart : View {

    // TODO Make This Customizable
    private val barColor = ContextCompat.getColor(context, R.color.colorPrimary)
    private val gridColor = ContextCompat.getColor(context, android.R.color.black)
    private val guidelineColor =
        ContextCompat.getColor(context, android.R.color.black)

    private val gridThicknessInPx = 20f
    private val guidelineThicknessInPx = 20f
    private val barSpacing = 20
    private val totalGridLines = 10

    // TODO Change This To Dynamic Data
    private val data =
        arrayOf(1f, 0.8f, 0.6f, 0.40f, 0.20f, 0.0f)


    private val mBarPaint = Paint().apply {
        style = Paint.Style.FILL
        color = barColor
    }

    private val mGridPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = gridColor
        strokeWidth = gridThicknessInPx
    }

    private val mGuidelinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = guidelineColor
        strokeWidth = guidelineThicknessInPx
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val height = height
        val width = width
        val gridLeft = paddingLeft.toFloat()
        val gridBottom = height - paddingBottom.toFloat()
        val gridTop = paddingTop.toFloat()
        val gridRight = width - paddingRight.toFloat()

        // Grid Lines
        canvas.drawLine(gridLeft, gridBottom, gridLeft, gridTop, mGridPaint)
        canvas.drawLine(gridLeft, gridBottom, gridRight, gridBottom, mGridPaint)

        // Grid Lines
        // Draw guide lines
        val guideLineSpacing = (gridBottom - gridTop) / totalGridLines
        for (i in 0..totalGridLines) {
            val y = gridTop + i * guideLineSpacing
            canvas.drawLine(
                gridLeft, y, gridRight, y,
                mGuidelinePaint
            )
        }

        // Bars
        /**
         * Spacing Will Always Be Count + 1
         * _|_|_|_
         * Here _ represent Spacing
         * Count = 3
         */
        val totalColumnSpacing = barSpacing * (data.size + 1).toFloat()

        /**
         * GridRight-GridLeft is the total width.
         * - Total Column Spacing
         * / Data Count Gives Width For Each Bar
         */
        val columnWidth = (gridRight - gridLeft - totalColumnSpacing) / data.size

        /**
         * These are initial Points For First Bar
         */
        var columnLeft = gridLeft + barSpacing
        var columnRight = columnLeft + columnWidth
        // Drawing Bars
        for (percentage in data) {

            // Calculate top of column based on percentage.
            val top = gridTop + (gridBottom - gridTop) * (1f - percentage)
            canvas.drawRect(columnLeft, top, columnRight, gridBottom, mBarPaint)

            // Shift over left/right column bounds
            columnLeft = columnRight + totalColumnSpacing / (data.size + 1)
            columnRight = columnLeft + columnWidth
        }
    }
}