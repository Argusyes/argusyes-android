package com.android.argusyes.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.android.argusyes.R


class CircleProgress : View {

    private var paint: Paint = Paint().apply { isAntiAlias = true }
    private var rectF: RectF = RectF()
    private var rect: Rect = Rect()

    //圆弧的宽度
    private var arcWidth = 16.0f
    private var progress = 0f
    private var progressSecond = 0f
    private var max = 100f
    private var myCPProgressColor = Color.GREEN
    private var myCPProgressSecondColor = Color.WHITE
    private var myCPBackgroundColor = Color.GRAY
    private var myCPTextSize = 16f
    private var myCPTextColor = Color.GRAY

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setAttr(context, attrs)

    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr){
        setAttr(context, attrs)
    }

    private fun setAttr(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgress)

        arcWidth = typedArray.getDimension(R.styleable.CircleProgress_arcWidth, arcWidth)
        progress = typedArray.getFloat(R.styleable.CircleProgress_progress, progress)
        progressSecond = typedArray.getFloat(R.styleable.CircleProgress_progressSecond, progressSecond)
        max = typedArray.getFloat(R.styleable.CircleProgress_max, max)
        myCPProgressColor = typedArray.getColor(R.styleable.CircleProgress_myCPProgressColor, myCPProgressColor)
        myCPProgressSecondColor = typedArray.getColor(R.styleable.CircleProgress_myCPProgressSecondColor, myCPProgressSecondColor)
        myCPBackgroundColor = typedArray.getColor(R.styleable.CircleProgress_myCPBackgroundColor, myCPBackgroundColor)
        myCPTextSize = typedArray.getDimension(R.styleable.CircleProgress_myCPTextSize, myCPTextSize)
        myCPTextColor = typedArray.getColor(R.styleable.CircleProgress_myCPTextColor, myCPTextColor)
        typedArray.recycle()
    }

    fun setProgress(progress: Float) {
        this.progress = max.coerceAtMost(progress)
        invalidate()
    }


    fun setProgressSecond(progressSecond: Float) {
        this.progressSecond = (max - progress).coerceAtMost(progressSecond)
        invalidate()
    }

    fun setMax(max: Float) {
        this.max = max
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //绘制圆形
        //设置为空心圆
        paint.style = Paint.Style.STROKE
        //设置圆弧的宽度（圆环的宽度）
        paint.strokeWidth = arcWidth
        paint.color = myCPBackgroundColor

        //绘制圆
        val width = measuredWidth.toFloat()
        //大圆的半径
        val bigCircleRadius = width / 2
        //小圆的半径
        val smallCircleRadius = bigCircleRadius - arcWidth
        canvas.drawCircle(bigCircleRadius, bigCircleRadius, smallCircleRadius, paint)

        //绘制圆弧
        paint.color = myCPProgressColor
        rectF.set(arcWidth, arcWidth, measuredWidth - arcWidth, measuredWidth - arcWidth)
        canvas.drawArc(rectF, 90F, (progress * 360 / max), false, paint)

        paint.color =  myCPProgressSecondColor
        canvas.drawArc(rectF, 90F + (progress * 360 / max), ((progressSecond) * 360 / max), false, paint)

        val txt = "${(progress * 100 / max).toInt()}%"
        paint.textSize = myCPTextSize
        paint.color = myCPTextColor
        paint.style = Paint.Style.FILL
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.getTextBounds(txt, 0, txt.length, rect)
        canvas.drawText(txt,
            (bigCircleRadius - rect.width() / 2),
            (bigCircleRadius + rect.height() / 2), paint)
    }

}