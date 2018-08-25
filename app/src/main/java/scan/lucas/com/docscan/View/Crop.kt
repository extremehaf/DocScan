package scan.lucas.com.docscan.View

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import scan.lucas.com.docscan.R
import java.util.*

class CropBox : FrameLayout {
    private var mContext: Context? = null
    private var paint: Paint? = null
    private var pointer1: ImageView? = null
    private var pointer2: ImageView? = null
    private var pointer3: ImageView? = null
    private var pointer4: ImageView? = null
    private var midPointer13: ImageView? = null
    private var midPointer12: ImageView? = null
    private var midPointer34: ImageView? = null
    private var midPointer24: ImageView? = null
    private var polygonView: CropBox? = null

    constructor(context: Context?) :
            super(context) {
        mContext = context
        iniciar()

    }

    constructor(context: Context?, attributeSet: AttributeSet) :
            super(context, attributeSet) {
        mContext = context
        iniciar()

    }

    constructor(context: Context?, attributeSet: AttributeSet, defStyleAttr: Int) :
            super(context, attributeSet, defStyleAttr) {
        mContext = context
        iniciar()

    }

    private fun iniciar() {
        polygonView = this
        pointer1 = getImageView(0, 0)
        pointer2 = getImageView(width, 0)
        pointer3 = getImageView(0, height)
        pointer4 = getImageView(width, height)
        midPointer13 = getImageView(0, height / 2)
        (midPointer13 as ImageView).setOnTouchListener(MidPointTouchListenerImpl(pointer1!!, pointer3!!))

        midPointer12 = getImageView(0, width / 2)
        (midPointer12 as ImageView).setOnTouchListener(MidPointTouchListenerImpl(pointer1!!, pointer2!!))

        midPointer34 = getImageView(0, height / 2)
        (midPointer34 as ImageView).setOnTouchListener(MidPointTouchListenerImpl(pointer3!!, pointer4!!))

        midPointer24 = getImageView(0, height / 2)
        (midPointer24 as ImageView).setOnTouchListener(MidPointTouchListenerImpl(pointer2!!, pointer4!!))

        addView(pointer1)
        addView(pointer2)
        addView(midPointer13)
        addView(midPointer12)
        addView(midPointer34)
        addView(midPointer24)
        addView(pointer3)
        addView(pointer4)
        initPaint()
    }

    private fun initPaint() {
        paint = Paint()
        paint!!.color = resources.getColor(R.color.blue)
        paint!!.strokeWidth = 2.0f
        paint!!.isAntiAlias = true
    }

    fun getPoints(): HashMap<Int, PointF> {

        val points = ArrayList<PointF>()
        points.add(PointF(pointer1!!.x, pointer1!!.y))
        points.add(PointF(pointer2!!.x, pointer2!!.y))
        points.add(PointF(pointer3!!.x, pointer3!!.y))
        points.add(PointF(pointer4!!.x, pointer4!!.y))

        return getOrderedPoints(points)
    }

    fun getOrderedPoints(points: ArrayList<PointF>): HashMap<Int, PointF> {

        val centerPoint = PointF()
        val size = points.size
        for (pointF in points) {
            centerPoint.x += pointF.x / size
            centerPoint.y += pointF.y / size
        }
        val orderedPoints = HashMap<Int, PointF>()
        for (pointF in points) {
            var index = -1
            if (pointF.x < centerPoint.x && pointF.y < centerPoint.y) {
                index = 0
            } else if (pointF.x > centerPoint.x && pointF.y < centerPoint.y) {
                index = 1
            } else if (pointF.x < centerPoint.x && pointF.y > centerPoint.y) {
                index = 2
            } else if (pointF.x > centerPoint.x && pointF.y > centerPoint.y) {
                index = 3
            }
            orderedPoints[index] = pointF
        }
        return orderedPoints
    }

    fun setPoints(pointFMap: HashMap<Int, PointF>) {
        if (pointFMap.size == 4) {
            setPointsCoordinates(pointFMap)
        }
    }

    private fun setPointsCoordinates(pointFMap: HashMap<Int, PointF>) {
        pointer1!!.x = (pointFMap.get(0)!!.x)
        pointer1!!.y = (pointFMap.get(0)!!.y)

        pointer2!!.x = (pointFMap.get(1)!!.x)
        pointer2!!.y = (pointFMap.get(1)!!.y)

        pointer3!!.x = (pointFMap.get(2)!!.x)
        pointer3!!.y = (pointFMap.get(2)!!.y)

        pointer4!!.x = (pointFMap.get(3)!!.x)
        pointer4!!.y = (pointFMap.get(3)!!.y)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawLine(pointer1!!.x + pointer1!!.width / 2, pointer1!!.y + pointer1!!.height / 2, pointer3!!.x + pointer3!!.width / 2, pointer3!!.y + pointer3!!.height / 2, paint)
        canvas.drawLine(pointer1!!.x + pointer1!!.width / 2, pointer1!!.y + pointer1!!.height / 2, pointer2!!.x + pointer2!!.width / 2, pointer2!!.y + pointer2!!.height / 2, paint)
        canvas.drawLine(pointer2!!.x + pointer2!!.width / 2, pointer2!!.y + pointer2!!.height / 2, pointer4!!.x + pointer4!!.width / 2, pointer4!!.y + pointer4!!.height / 2, paint)
        canvas.drawLine(pointer3!!.x + pointer3!!.width / 2, pointer3!!.y + pointer3!!.height / 2, pointer4!!.x + pointer4!!.width / 2, pointer4!!.y + pointer4!!.height / 2, paint)
        midPointer13!!.x = (pointer3!!.x - (pointer3!!.x - pointer1!!.x) / 2)
        midPointer13!!.y = (pointer3!!.y - (pointer3!!.y - pointer1!!.y) / 2)
        midPointer24!!.x = (pointer4!!.x - (pointer4!!.x - pointer2!!.x) / 2)
        midPointer24!!.y = (pointer4!!.y - (pointer4!!.y - pointer2!!.y) / 2)
        midPointer34!!.x = (pointer4!!.x - (pointer4!!.x - pointer3!!.x) / 2)
        midPointer34!!.y = (pointer4!!.y - (pointer4!!.y - pointer3!!.y) / 2)
        midPointer12!!.x = (pointer2!!.x - (pointer2!!.x - pointer1!!.x) / 2)
        midPointer12!!.y = (pointer2!!.y - (pointer2!!.y - pointer1!!.y) / 2)
    }


    private fun getImageView(x: Int, y: Int): ImageView {

        val imageView = ImageView(context)
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        imageView.layoutParams = layoutParams
        imageView.setImageResource(R.drawable.select_circle)
        imageView.x = x.toFloat()
        imageView.y = y.toFloat()
        imageView.setOnTouchListener(TouchListenerImpl())
        return imageView
    }

    fun isValidShape(pointFMap: HashMap<Int, PointF>): Boolean {
        return pointFMap.size == 4
    }


    private inner class MidPointTouchListenerImpl(private val mainPointer1: ImageView, private val mainPointer2: ImageView) : View.OnTouchListener {

        internal var DownPT = PointF() // Record Mouse Position When Pressed Down
        internal var StartPT = PointF() // Record Start Position of 'img'

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val eid = event.action
            when (eid) {
                MotionEvent.ACTION_MOVE -> {
                    val mv = PointF(event.x - DownPT.x, event.y - DownPT.y)

                    if (Math.abs(mainPointer1.x - mainPointer2.x) > Math.abs(mainPointer1.y - mainPointer2.y)) {
                        if (mainPointer2.y + mv.y + v.height.toFloat() < polygonView!!.height && mainPointer2.y + mv.y > 0) {
                            v.x = (StartPT.y + mv.y).toInt().toFloat()
                            StartPT = PointF(v.x, v.y)
                            mainPointer2.y = (mainPointer2.y + mv.y).toInt().toFloat()
                        }
                        if (mainPointer1.y + mv.y + v.height.toFloat() < polygonView!!.height && mainPointer1.y + mv.y > 0) {
                            v.x = (StartPT.y + mv.y).toInt().toFloat()
                            StartPT = PointF(v.x, v.y)
                            mainPointer1.y = (mainPointer1.y + mv.y).toInt().toFloat()
                        }
                    } else {
                        if (mainPointer2.x + mv.x + v.width.toFloat() < polygonView!!.width && mainPointer2.x + mv.x > 0) {
                            v.x = (StartPT.x + mv.x).toInt().toFloat()
                            StartPT = PointF(v.x, v.y)
                            mainPointer2.x = (mainPointer2.x + mv.x).toInt().toFloat()
                        }
                        if (mainPointer1.x + mv.x + v.width.toFloat() < polygonView!!.width && mainPointer1.x + mv.x > 0) {
                            v.x = (StartPT.x + mv.x).toInt().toFloat()
                            StartPT = PointF(v.x, v.y)
                            mainPointer1.x = (mainPointer1.x + mv.x).toInt().toFloat()
                        }
                    }
                }
                MotionEvent.ACTION_DOWN -> {
                    DownPT.x = event.x
                    DownPT.y = event.y
                    StartPT = PointF(v.x, v.y)
                }
                MotionEvent.ACTION_UP -> {
                    var color = 0
                    if (isValidShape(getPoints())) {
                        color = resources.getColor(R.color.blue)
                    } else {
                        color = resources.getColor(R.color.orange)
                    }
                    paint!!.color = color
                }
                else -> {
                }
            }
            polygonView!!.invalidate()
            return true
        }
    }

    private inner class TouchListenerImpl : View.OnTouchListener {

        internal var DownPT = PointF() // Record Mouse Position When Pressed Down
        internal var StartPT = PointF() // Record Start Position of 'img'

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val eid = event.action
            when (eid) {
                MotionEvent.ACTION_MOVE -> {
                    val mv = PointF(event.x - DownPT.x, event.y - DownPT.y)
                    if (StartPT.x + mv.x + v.width.toFloat() < polygonView!!.width && StartPT.y + mv.y + v.height.toFloat() < polygonView!!.height && StartPT.x + mv.x > 0 && StartPT.y + mv.y > 0) {
                        v.x = (StartPT.x + mv.x).toInt().toFloat()
                        v.y = (StartPT.y + mv.y).toInt().toFloat()
                        StartPT = PointF(v.x, v.y)
                    }
                }
                MotionEvent.ACTION_DOWN -> {
                    DownPT.x = event.x
                    DownPT.y = event.y
                    StartPT = PointF(v.x, v.y)
                }
                MotionEvent.ACTION_UP -> {
                    var color = 0
                    if (isValidShape(getPoints())) {
                        color = resources.getColor(R.color.blue)
                    } else {
                        color = resources.getColor(R.color.orange)
                    }
                    paint!!.color = color
                }
                else -> {
                }
            }
            polygonView!!.invalidate()
            return true
        }

    }

}