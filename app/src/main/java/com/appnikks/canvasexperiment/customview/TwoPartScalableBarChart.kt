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

// TODO LET THE BAR BE CALCULATED BY ITSELF BASED ON DATA POINTS
class TwoPartScalableBarChart : View {

    companion object {
        private const val DEFAULT_SPACING = 20f

        @Dimension(unit = Dimension.SP)
        private const val DEFAULT_TEXT_SIZE = 12f

        private const val DEFAULT_XAXIS_LABELS_PERCENT = 0.04
    }

    var dataSetColors = listOf(ContextCompat.getColor(context, R.color.colorAccent))

    // Dummy data
    var dataSet = listOf(
        ChartUiModel(100f, 20f),
        ChartUiModel(-100f, -10f),
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

    var barLineExtendedWidth = context.dpToPx(2f)

    var barLineType: BarLineType = BarLineType.NORMAL

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
    var valueDrawPosition: ValueDrawPosition = ValueDrawPosition.INSIDE
    var valueTextPadding = context.dpToPx(4f)
    var valueFormatter = BarValueBaseFormatter()
    private val rectTextMeasurements = Rect()

    // X Axis Labels
    var drawXAxisLabels = true
    var xAxisFormatter = XAxisLabelBaseFormatter()

    var isDescriptionEnabled = true
    var upperGraphDescription = "Buy"
    var lowerGraphDescription = "Sell"
    var labelsDescription = "Since"
    private val descriptionLabelsRect = Rect()

    /**
     * The Graph Start Would be calculated
     * TotalWidth Taken By Text
     * Including Margin Left and Right(max)
     */
    var marginUpperGraphDescription = MarginDp(bottom = context.dpToPx(8f))
        set(value) {
            field = value
            invalidate()
        }
    var marginLowerGraphDescription = MarginDp(top = context.dpToPx(8f))
        set(value) {
            field = value
            invalidate()
        }
    var marginXAxisLabelsDescription = MarginDp()
        set(value) {
            field = value
            invalidate()
        }


    @Dimension(unit = Dimension.DP)
    var marginXAxisLabelsDp = -1f
        set(value) {
            field = context.dpToPx(value)
        }

    // Paints
    val mBarPaint = Paint().apply {
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
        textAlign = Paint.Align.CENTER
        color = ContextCompat.getColor(context, android.R.color.black)
        textSize = context.spToPx(DEFAULT_TEXT_SIZE)
    }

    private val mXAxisTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        typeface = Typeface.SANS_SERIF
        textAlign = Paint.Align.CENTER
        color = ContextCompat.getColor(context, android.R.color.black)
        textSize = context.spToPx(DEFAULT_TEXT_SIZE)
    }

    private val mUpperGraphDescriptionTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        typeface = Typeface.SANS_SERIF
        textAlign = Paint.Align.LEFT
        color = ContextCompat.getColor(context, android.R.color.black)
        textSize = context.spToPx(DEFAULT_TEXT_SIZE)
    }

    private val mLowerGraphDescriptionTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        typeface = Typeface.SANS_SERIF
        textAlign = Paint.Align.LEFT
        color = ContextCompat.getColor(context, android.R.color.black)
        textSize = context.spToPx(DEFAULT_TEXT_SIZE)
    }

    private val mXAxisLabelDescriptionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        // Not Initialized
        if (marginXAxisLabelsDp == -1f) {
            marginXAxisLabelsDp = (DEFAULT_XAXIS_LABELS_PERCENT * viewHeight).toFloat()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Pos For View
        val viewLeft = 0f
        val viewBottom = height.toFloat()
        val viewTop = 0
        val viewRight = width.toFloat()

        var lowerDescriptionHeight = 0f

        // Pos For Graph Drawing
        val graphLeft = viewLeft.let {
            var totalLeft = it
            if (drawBarLines && barLineType == BarLineType.Extended) {
                totalLeft += barLineExtendedWidth
            }
            if (isDescriptionEnabled) {
                mUpperGraphDescriptionTextPaint.getTextBounds(
                    upperGraphDescription,
                    0,
                    upperGraphDescription.length,
                    descriptionLabelsRect
                )
                val upperDescriptionWidth =
                    descriptionLabelsRect.width() + marginUpperGraphDescription.left + marginUpperGraphDescription.right


                // Lower Description
                mLowerGraphDescriptionTextPaint.getTextBounds(
                    lowerGraphDescription,
                    0,
                    lowerGraphDescription.length,
                    descriptionLabelsRect
                )
                lowerDescriptionHeight = descriptionLabelsRect.height().toFloat()
                val lowerDescriptionWidth =
                    descriptionLabelsRect.width() + marginLowerGraphDescription.left + marginLowerGraphDescription.right

                // XAxisLabels Description
                mXAxisLabelDescriptionPaint.getTextBounds(
                    labelsDescription,
                    0,
                    labelsDescription.length,
                    descriptionLabelsRect
                )
                val labelsDescriptionWidth =
                    descriptionLabelsRect.width() + marginXAxisLabelsDescription.left + marginXAxisLabelsDescription.right
                totalLeft += maxOf(
                    upperDescriptionWidth,
                    lowerDescriptionWidth,
                    labelsDescriptionWidth
                )
            }
            totalLeft
        }

        val graphRight = if (drawBarLines && barLineType == BarLineType.Extended) {
            viewRight - barLineExtendedWidth
        } else {
            viewRight
        }

        val graphBottom = height - marginXAxisLabelsDp

        // Pos For Upper And Lower Graph
        /**
         * We are leaving space for Zero Line
         * Which will be adjusted from bottom Upper and Lower graph (50-50)
         * Since UpperGraphBottom is already having Required Height - ZeroLineHeight
         * We have to Add Double To LowerGraphTop (UpperGraphBottom+zeroLineHeight)
         */
        val upperGraphTop = viewTop
        val upperGraphBottom =
            ((graphBottom - upperGraphTop) / 2) - if (drawZeroLine) zeroLineHeightDp / 2 else 0f
        val lowerGraphTop = upperGraphBottom + if (drawZeroLine) zeroLineHeightDp else 0f
        val lowerGraphBottom = graphBottom

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
        val columnWidth = (graphRight - graphLeft - totalColumnSpacing) / dataSet.size

        /**
         * These are initial Points For First Bar
         */
        var columnLeft = graphLeft
        var columnRight = columnLeft + columnWidth


        // Drawing Upper Graph Background Drawable
        topGraphBackgroundDrawable?.setBounds(
            viewLeft.toInt(),
            upperGraphTop,
            viewRight.toInt(),
            upperGraphBottom.toInt()
        )
        topGraphBackgroundDrawable?.draw(canvas)

        // Drawing Lower Graph Background Drawable
        bottomGraphBackgroundDrawable?.setBounds(
            viewLeft.toInt(),
            lowerGraphTop.toInt(),
            viewRight.toInt(),
            lowerGraphBottom.toInt()
        )
        bottomGraphBackgroundDrawable?.draw(canvas)


        // Drawing Zero Line
        if (drawZeroLine) {
            canvas.drawLine(
                viewLeft,
                upperGraphBottom + zeroLineHeightDp / 2,
                viewRight,
                upperGraphBottom + zeroLineHeightDp / 2,
                mZeroLinePaint
            )
        }

        if (valueDrawPosition != ValueDrawPosition.TOP) {
            valueTextPadding = 0f
        }

        mBarPaint.color = dataSetColors[0]

        // Drawing Bars
        dataSet.forEachIndexed { index, barValue ->
            rectTextMeasurements.setEmpty()
            val valueString = valueFormatter.getValue(barValue, index)
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
                upperGraphTop + rectTextMeasurements.height() + valueTextPadding + (upperGraphBottom - upperGraphTop - rectTextMeasurements.height() - valueTextPadding) * ((100f - barValue.value.absoluteValue) / 100f)
            } else {
                // Draw in Lower Region
                lowerGraphTop
            }

            val bottom = if (barValue.value > 0) {
                upperGraphBottom
            } else {
                lowerGraphBottom - rectTextMeasurements.height() - valueTextPadding - (lowerGraphBottom - lowerGraphTop - rectTextMeasurements.height() - valueTextPadding) * ((100f - barValue.value.absoluteValue) / 100f)
            }

            canvas.drawRect(columnLeft, top, columnRight, bottom, mBarPaint)

            if (valueDrawPosition == ValueDrawPosition.TOP) {
                canvas.drawText(
                    valueString,
                    columnLeft + (columnRight - columnLeft) / 2,
                    if (barValue.value < 0) bottom + rectTextMeasurements.height() / 2 + valueTextPadding else ((top - valueTextPadding)),
                    mBarValuesTextPaint
                )
            } else if (valueDrawPosition == ValueDrawPosition.INSIDE) {
                canvas.drawText(
                    valueString,
                    columnLeft + (columnRight - columnLeft) / 2,
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
                canvas.drawLine(barXStartPos, barYPos, barXEndPos, barYPos, mBarLinesPaint)
            }

            if (drawXAxisLabels) {
                val labelToDraw = xAxisFormatter.getValue(barValue, index)
                canvas.drawText(
                    labelToDraw,
                    columnLeft + (columnRight - columnLeft) / 2,
                    viewBottom,
                    mXAxisTextPaint
                )
            }

            // Shift over left/right column bounds
            columnLeft = columnRight + totalColumnSpacing / (dataSet.size - 1)
            columnRight = columnLeft + columnWidth
        }

        // Description  Text
        if (isDescriptionEnabled) {
            canvas.drawText(
                upperGraphDescription,
                marginUpperGraphDescription.left,
                upperGraphBottom - marginUpperGraphDescription.bottom,
                mUpperGraphDescriptionTextPaint
            )

            canvas.drawText(
                lowerGraphDescription,
                marginLowerGraphDescription.left,
                lowerGraphTop + lowerDescriptionHeight / 2 + marginLowerGraphDescription.top,
                mLowerGraphDescriptionTextPaint
            )

            canvas.drawText(
                labelsDescription,
                viewLeft,
                viewBottom,
                mXAxisLabelDescriptionPaint
            )
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

    open class BarValueBaseFormatter : ValueFormatter {
        override fun getValue(model: ChartUiModel, position: Int): String {
            return model.value.toString()
        }
    }

    open class XAxisLabelBaseFormatter : ValueFormatter {
        override fun getValue(model: ChartUiModel, position: Int): String {
            return position.toString()
        }
    }

    data class MarginDp(
        val top: Float = 0f,
        val bottom: Float = 0f,
        val left: Float = 0f,
        val right: Float = 0f
    )

    interface ValueFormatter {
        fun getValue(model: ChartUiModel, position: Int): String
    }
}