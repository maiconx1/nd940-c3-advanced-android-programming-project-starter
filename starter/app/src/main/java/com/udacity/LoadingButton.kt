package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.Typeface.BOLD
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.withStyledAttributes
import kotlinx.android.synthetic.main.content_main.*
import kotlin.properties.Delegates

private const val PROGRESS_PADDING = 30f

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthSize = 0
    private var heightSize = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
    }

    private var horizontalProgressColor = 0
    private var circularProgressColor = 0
    private var textColor = 0
    private var progressRadius = 50f
    private var text = ""
    private var loadingText = ""

    private val textBounds = Rect()

    private var arcProgress = 0f

    private val valueAnimator = ValueAnimator.ofFloat(0f).apply {
        duration = 3000
        addUpdateListener {
            arcProgress = it.animatedValue as Float
            invalidate()
        }
    }

    private var _buttonState: ButtonState by Delegates.observable(ButtonState.Completed) { _, old, new ->
        if (new != old) {
            if (new == ButtonState.Loading) {
                isEnabled = false
            }
            invalidate()
        }
    }

    var buttonState: ButtonState
        set(value) {
            _buttonState = value
        }
        get() = _buttonState

    private var _progress = 0f

    var progress: Float
        get() = _progress
        set(value) {
            _progress = value
            if (progress > 0f) {
                val start = if (value == 100f) {
                    postDelayed({
                        buttonState = ButtonState.Completed
                        _progress = 0f
                        valueAnimator.setFloatValues(0f)
                        isEnabled = true
                    }, 500)
                    valueAnimator.duration = 300
                    valueAnimator.animatedValue as Float
                } else {
                    valueAnimator.duration = 3000
                    0f
                }
                valueAnimator.setFloatValues(start, value)
                if (valueAnimator.isRunning) {
                    valueAnimator.cancel()
                }
                valueAnimator.start()
            }
        }

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            horizontalProgressColor = getColor(R.styleable.LoadingButton_horizontalProgressColor, 0)
            circularProgressColor = getColor(R.styleable.LoadingButton_circularProgressColor, 0)
            textColor = getColor(R.styleable.LoadingButton_textColor, 0)
            progressRadius = getFloat(R.styleable.LoadingButton_progressRadius, 50f)
            text = getString(R.styleable.LoadingButton_text) ?: ""
            loadingText = getString(R.styleable.LoadingButton_loadingText) ?: ""
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val buttonText = when (buttonState) {
            is ButtonState.Clicked, ButtonState.Completed -> text
            else -> loadingText
        }

        if (buttonState == ButtonState.Loading) {
            paint.color = horizontalProgressColor
            canvas.drawRect(0f, 0f, width * arcProgress/100, height.toFloat(), paint)


            paint.getTextBounds(buttonText, 0, buttonText.length, textBounds)
            val textWidth = textBounds.width()
            paint.color = circularProgressColor
            canvas.drawArc(
                width / 2f + textWidth / 2f + PROGRESS_PADDING,
                height / 2f - progressRadius,
                width / 2f + textWidth / 2f + PROGRESS_PADDING + 2 * progressRadius,
                height / 2f + progressRadius,
                180f,
                360f * arcProgress / 100,
                true,
                paint
            )
        }

        paint.color = textColor
        canvas.drawText(
            buttonText, width / 2f, (height / 2f) - ((paint.descent() + paint.ascent()) / 2), paint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

}