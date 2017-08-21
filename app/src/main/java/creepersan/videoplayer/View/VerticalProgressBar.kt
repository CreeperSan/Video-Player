package creepersan.videoplayer.View

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import creepersan.videoplayer.Activity.R

class VerticalProgressBar(context:Context,attributeSet: AttributeSet?,defStyleAttr:Int,defStyleRes:Int) : View(context,attributeSet,defStyleAttr,defStyleRes) {
    private var lineWidth = 1
    private var lineColor = Color.WHITE
    private var barColor = Color.parseColor("#66ccff")
    private var lineBarTap = 10
    private var progress = 50
    private var progressMax = 100

    private val mPaint = Paint()

    constructor(context: Context) : this(context,null)
    constructor(context:Context,attributeSet: AttributeSet?) : this(context,attributeSet,0)
    constructor(context:Context,attributeSet: AttributeSet?,defStyleAttr:Int) : this(context,attributeSet,defStyleAttr,0)

    init {
        mPaint.strokeWidth = lineWidth.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val height = canvas.height
        val width = canvas.width
        //先绘制边框
        mPaint.style = Paint.Style.STROKE
        mPaint.color = lineColor
        canvas.drawRect(Rect(lineWidth/2+1,lineWidth/2+1,width-lineWidth/2,height-lineWidth/2-1),mPaint)
        //后绘制进度
        mPaint.style = Paint.Style.FILL
        mPaint.color = barColor
        canvas.drawRect(Rect(
                lineWidth/2 + 1 + lineBarTap,
                (lineWidth/2 + 1 + lineBarTap) + (((height - (lineWidth/2) - lineBarTap)-(lineWidth/2 + 1 + lineBarTap))*((progressMax-progress).toFloat()/progressMax.toFloat())).toInt(),
                width - lineWidth/2 - lineBarTap,
                height - (lineWidth/2) - lineBarTap -1
        ),mPaint)
    }

    fun setProgress(progress:Int){
        this.progress = progress
        invalidate()
    }
    fun setProgressMax(max:Int){
        this.progressMax = max
        invalidate()
    }
    fun getProgress():Int = progress
    fun getProgressMax():Int = progressMax

}