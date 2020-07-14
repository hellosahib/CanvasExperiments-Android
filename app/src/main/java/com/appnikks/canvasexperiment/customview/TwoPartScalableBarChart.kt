package com.appnikks.canvasexperiment.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.Dimension
import androidx.annotation.FloatRange
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import com.appnikks.canvasexperiment.R
import kotlin.math.absoluteValue

class TwoPartScalableBarChart : View {

    companion object {
        private const val DEFAULT_SPACING = 20f

        @Dimension(unit = Dimension.SP)
        private const val DEFAULT_TEXT_SIZE = 12f
    }

    var dataSetColors = listOf(ContextCompat.getColor(context, R.color.colorAccent))

    // Dummy data
    var dataSet = listOf(
        ChartUiModel(20f, 20f),
        ChartUiModel(-30f, -10f),
        ChartUiModel(10f, 0f),
        ChartUiModel(50f, 20f)
    )
        set(value) {
            field = value
            // TODO Sanity Data For BarLineValue SHOULD BE INSIDE BOUNDS
            invalidate()
        }

    var barSpacing: Float = context.dpToPx(DEFAULT_SPACING)

    // TODO DEFAULT FALSE
    var drawBarLines: Boolean = true
    var drawBarLineHeightDp: Float = context.dpToPx(2f)
        set(value) {
            field = context.dpToPx(value)
            mBarLinesPaint.strokeWidth = field
        }
    var drawBarLineColorRes: Int = ContextCompat.getColor(context, android.R.color.holo_orange_dark)
        set(value) {
            field = ContextCompat.getColor(context, value)
            mBarLinesPaint.color = field
        }

    // TODO CHANGE TO 0DP
    var barLineExtendedWidth = context.dpToPx(2f)

    // TODO CHANGE TO NORMAL
    var barLineType: BarLineType = BarLineType.Contracted

    // TODO DEFAULT FALSE
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

    var topGraphBackgroundDrawable: Drawable? = null

    var bottomGraphBackgroundDrawable: Drawable? = null

    // Value Text Properties
    var valueDrawPosition: ValueDrawPosition = ValueDrawPosition.TOP
    var valueTextPadding = context.dpToPx(4f)
    var valueFormatter = BarValueBaseFormatter()
    private val rectTextMeasurements = Rect()

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

    private val mBarLinesPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = drawBarLineColorRes
        strokeWidth = drawBarLineHeightDp
    }

    private val mBarValuesTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        typeface = Typeface.SANS_SERIF
        textAlign = Paint.Align.LEFT
        color = ContextCompat.getColor(context, android.R.color.black)
        textSize = context.spToPx(DEFAULT_TEXT_SIZE)
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

        val gridLeft = if (drawBarLines && barLineType == BarLineType.Extended) {
            paddingLeft.toFloat() + barLineExtendedWidth
        } else {
            paddingLeft.toFloat()
        }

        val gridRight = if (drawBarLines && barLineType == BarLineType.Extended) {
            width - paddingRight.toFloat() - barLineExtendedWidth
        } else {
            width - paddingRight.toFloat()
        }

        val gridBottom = height - paddingBottom.toFloat()
        val gridTop = paddingTop.toFloat()

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
        val totalColumnSpacing = barSpacing * (dataSet.size - 1).toFloat()

        /**
         * GridRight-GridLeft is the total width.
         * - Total Column Spacing
         * / Data Count
         * Gives Width For Each Bar
         */
        val columnWidth = (gridRight - gridLeft - totalColumnSpacing) / dataSet.size

        /**
         * These are initial Points For First Bar
         */
        var columnLeft = gridLeft
        var columnRight = columnLeft + columnWidth


        // Drawing Upper Graph Background Drawable
        topGraphBackgroundDrawable?.setBounds(
            gridLeft.toInt(),
            upperGraphTop.toInt(),
            gridRight.toInt(),
            upperGraphBottom.toInt()
        )
        topGraphBackgroundDrawable?.draw(canvas!!)

        // Drawing Lower Graph Background Drawable
        bottomGraphBackgroundDrawable?.setBounds(
            gridLeft.toInt(),
            lowerGraphTop.toInt(),
            gridRight.toInt(),
            lowerGraphBottom.toInt()
        )
        bottomGraphBackgroundDrawable?.draw(canvas!!)


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

        if (valueDrawPosition != ValueDrawPosition.TOP) {
            valueTextPadding = 0f
        }

        mBarPaint.color = dataSetColors[0]

        // Drawing Bars
        for (barValue in dataSet) {
            rectTextMeasurements.setEmpty()
            val valueString = valueFormatter.getValue(barValue)
            if (valueDrawPosition == ValueDrawPosition.TOP) {
                mBarValuesTextPaint.getTextBounds(
                    valueString,
                    0,
                    valueString.length,
                    rectTextMeasurements
                )
            }

            // Calculate top of column based on barValue.
            val top = if (barValue.value > 0) {
                // Draw in Upper Region
                upperGraphTop + (upperGraphBottom - upperGraphTop - rectTextMeasurements.height() / 2 - valueTextPadding) * ((100f - barValue.value.absoluteValue) / 100f)
            } else {
                // Draw in Lower Region
                lowerGraphTop
            }

            val bottom = if (barValue.value > 0) {
                upperGraphBottom
            } else {
                lowerGraphBottom - (lowerGraphBottom - lowerGraphTop) * ((100f - barValue.value.absoluteValue) / 100f)
            }

            canvas?.drawRect(columnLeft, top, columnRight, bottom, mBarPaint)

            if (valueDrawPosition == ValueDrawPosition.TOP) {
                canvas?.drawText(
                    valueString,
                    (columnLeft + rectTextMeasurements.left),
                    if (barValue.value < 0) bottom + rectTextMeasurements.height() / 2 + valueTextPadding else ((top - valueTextPadding)),
                    mBarValuesTextPaint
                )
            } else if (valueDrawPosition == ValueDrawPosition.INSIDE) {
                canvas?.drawText(
                    valueString,
                    (columnLeft + rectTextMeasurements.left),
                    bottom - ((bottom - top) / 2),
                    mBarValuesTextPaint
                )
            }


            // Drawing Bar Lines
            if (drawBarLines) {
                val barYPos = if (barValue.barLineValue >= 0) {
                    upperGraphTop + (upperGraphBottom - upperGraphTop) * ((100f - barValue.barLineValue.absoluteValue) / 100f)
                } else {
                    lowerGraphBottom - (lowerGraphBottom - lowerGraphTop) * ((100f - barValue.barLineValue.absoluteValue) / 100f)
                }
                val barXStartPos = when (barLineType) {
                    BarLineType.NORMAL -> columnLeft
                    BarLineType.Contracted -> columnLeft + barLineExtendedWidth
                    BarLineType.Extended -> columnLeft - barLineExtendedWidth
                }
                val barXEndPos = when (barLineType) {
                    BarLineType.NORMAL -> columnRight
                    BarLineType.Contracted -> columnRight - barLineExtendedWidth
                    BarLineType.Extended -> columnRight + barLineExtendedWidth
                }
                canvas?.drawLine(barXStartPos, barYPos, barXEndPos, barYPos, mBarLinesPaint)
            }

            // Shift over left/right column bounds
            columnLeft = columnRight + totalColumnSpacing / (dataSet.size - 1)
            columnRight = columnLeft + columnWidth
        }
    }

    // Value should be between -100f to 100f
    data class ChartUiModel(
        @FloatRange(from = -100.0, to = 100.0)
        val value: Float,
        val barLineValue: Float = -1f,
        val data: Any? = null
    )

    enum class ValueDrawPosition {
        TOP, INSIDE, NONE
    }

    enum class BarLineType {
        /**
         * Same The Size Of Bar
         */
        NORMAL,

        /**
         * Bar Will Be More Than Size of Bar
         * @see barLineExtendedWidth
         */
        Extended,

        /**
         * Bar Will Be Less Than Size of Bar
         * @see barLineExtendedWidth
         */
        Contracted
    }

    @Px
    private fun Context.dpToPx(@Dimension(unit = Dimension.DP) dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }

    @Px
    private fun Context.spToPx(@Dimension(unit = Dimension.SP) dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp, resources.displayMetrics)
    }

    open class BarValueBaseFormatter {
        open fun getValue(model: ChartUiModel): String {
            return model.value.toString()
        }
    }
}