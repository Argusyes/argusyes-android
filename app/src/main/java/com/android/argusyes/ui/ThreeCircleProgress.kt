package com.android.argusyes.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.android.argusyes.R


class ThreeCircleProgress : View {

    private var paint: Paint = Paint().apply { isAntiAlias = true }
    private var rectF: RectF = RectF()

    //圆弧的宽度
    private var arcWidth = 12.0f
    private var progress = 0f
    private var progressSecond = 0f
    private var progressThree = 0f
    private var max = 100f
    private var myCPProgressColor = Color.GREEN
    private var myCPBackgroundColor = Color.GRAY

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setAttr(context, attrs)

    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr){
        setAttr(context, attrs)
    }

    private fun setAttr(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ThreeCircleProgress)

        arcWidth = typedArray.getDimension(R.styleable.ThreeCircleProgress_myTCPArcWidth, arcWidth)
        progress = typedArray.getFloat(R.styleable.ThreeCircleProgress_myTCPProgress, progress)
        progressSecond = typedArray.getFloat(R.styleable.ThreeCircleProgress_myTCPProgressSecond, progressSecond)
        progressThree = typedArray.getFloat(R.styleable.ThreeCircleProgress_myTCPProgressThree, progressThree)
        max = typedArray.getFloat(R.styleable.ThreeCircleProgress_myTCPMax, max)
        myCPProgressColor = typedArray.getColor(R.styleable.ThreeCircleProgress_myTCPProgressColor, myCPProgressColor)
        myCPBackgroundColor = typedArray.getColor(R.styleable.ThreeCircleProgress_myTCPBackgroundColor, myCPBackgroundColor)

        typedArray.recycle()
    }

    fun setProgress(progress: Float) {
        this.progress = max.coerceAtMost(progress)
        invalidate()
    }


    fun setProgressSecond(progressSecond: Float) {
        this.progressSecond = max.coerceAtMost(progressSecond)
        invalidate()
    }

    fun setProgressThree(progressThree: Float) {
        this.progressThree = max.coerceAtMost(progressThree)
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
        val radius = width / 2

        val diffOne = arcWidth
        val diffTwo = arcWidth * 2 + 4
        val diffThree = arcWidth * 3 + 8

        canvas.drawCircle(radius, radius, radius - diffOne, paint)

        canvas.drawCircle(radius, radius, radius - diffTwo, paint)
        canvas.drawCircle(radius, radius, radius - diffThree, paint)

        //绘制圆弧
        paint.color = myCPProgressColor
        rectF.set(diffOne, diffOne, measuredWidth - diffOne, measuredWidth - diffOne)
        canvas.drawArc(rectF, 90F, (progress * 360 / max), false, paint)

        rectF.set(diffTwo, diffTwo, measuredWidth - diffTwo, measuredWidth - diffTwo)
        canvas.drawArc(rectF, 90F, (progressSecond * 360 / max), false, paint)

        rectF.set(diffThree, diffThree, measuredWidth - diffThree, measuredWidth - diffThree)
        canvas.drawArc(rectF, 90F, (progressThree * 360 / max), false, paint)

    }

}