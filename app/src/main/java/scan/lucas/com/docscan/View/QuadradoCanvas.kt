package scan.lucas.com.docscan.View

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.shapes.Shape
import android.util.AttributeSet
import android.view.View
import java.util.*

class QuadradoCanvas @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val shapes = ArrayList<Canvashape>()

    inner class Canvashape {
        val shape: Shape
        private val mPaint: Paint
        private val mBorder: Paint?

        constructor(shape: Shape, paint: Paint) {
            this.shape = shape
            mPaint = paint
            mBorder = null
        }

        constructor(shape: Shape, paint: Paint, border: Paint) {
            this.shape = shape
            mPaint = paint
            mBorder = border
            mBorder.style = Paint.Style.STROKE
        }

        fun draw(canvas: Canvas) {
            shape.draw(canvas, mPaint)

            if (mBorder != null) {
                shape.draw(canvas, mBorder)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        for (s in shapes) {
            s.shape.resize(contentWidth.toFloat(), contentHeight.toFloat())
            s.draw(canvas)
        }

    }

    fun add(shape: Shape, paint: Paint): Canvashape {
        val hudShape = Canvashape(shape, paint)
        shapes.add(hudShape)
        return hudShape
    }

    fun add(shape: Shape, paint: Paint, border: Paint): Canvashape {
        val hudShape = Canvashape(shape, paint, border)
        shapes.add(hudShape)
        return hudShape
    }

    fun remove(shape: Canvashape) {
        shapes.remove(shape)
    }

    fun remove(index: Int) {
        shapes.removeAt(index)
    }

    fun clear() {
        shapes.clear()
    }
}