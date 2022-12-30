package com.android.argusyes.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.android.argusyes.R


class RectangleProgress : View {

    private var paint: Paint = Paint().apply { isAntiAlias = true }

    private var progress = 50f
    private var max = 100f
    private var myRPFrontendColor = Color.GREEN
    private var myRPBackgroundColor = Color.GRAY

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setAttr(context, attrs)

    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr){
        setAttr(context, attrs)
    }

    private fun setAttr(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RectangleProgress)

        progress = typedArray.getFloat(R.styleable.RectangleProgress_myRPProgress, progress)
        max = typedArray.getFloat(R.styleable.RectangleProgress_myRPMax, max)
        myRPFrontendColor = typedArray.getColor(R.styleable.RectangleProgress_myRPFrontendColor, myRPFrontendColor)
        myRPBackgroundColor = typedArray.getColor(R.styleable.RectangleProgress_myRPBackgroundColor, myRPBackgroundColor)
        typedArray.recycle()
    }

    fun setProgress(progress: Float) {
        this.progress = max.coerceAtMost(progress)
        invalidate()
    }

    fun setMax(max: Float) {
        this.max = max
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = myRPBackgroundColor
        canvas.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paint)
        paint.color = myRPFrontendColor
        canvas.drawRect(0f, measuredHeight - measuredHeight * progress / max, measuredWidth.toFloat(), measuredHeight.toFloat(), paint)
    }
}