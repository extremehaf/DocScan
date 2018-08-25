package scan.lucas.com.docscan

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.activity_crop.*
import kotlinx.android.synthetic.main.content_crop.*
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import java.util.HashMap
import kotlin.collections.ArrayList


//https://github.com/jhansireddy/AndroidScannerDemo
class CropActivity : AppCompatActivity() {

    private var caminhoSalvar = ""
    private lateinit var original: Bitmap
    private var scaledBitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        val caminho = this.intent.getStringExtra("fotoPath")
        caminhoSalvar = this.intent.getStringExtra("destino")
        original = BitmapFactory.decodeFile(caminho)
        scaledBitmap = original
        if (original != null)
            setBitmap(original)

        btnCropOk.setOnClickListener {
            retornaImagemCrop(scaledBitmap!!)
        }

    }

    private fun setBitmap(original: Bitmap) {
        val display = windowManager.defaultDisplay
        val size = android.graphics.Point()
        display.getRealSize(size)

        val displayWidth = Math.min(size.y, size.x)
        val displayHeight = Math.max(size.y, size.x)
        scaledBitmap = scaledBitmap(original, displayWidth, displayHeight)
        android_image.setImageBitmap(scaledBitmap)

        val tempBitmap = (android_image.drawable as BitmapDrawable).bitmap


        val pointFs = getEdgePoints(scaledBitmap!!)
        cropBox.setPoints(pointFs)

        cropBox.visibility = View.VISIBLE

        val padding = 16
        val layoutParams = FrameLayout.LayoutParams(tempBitmap.width + 2 * padding, tempBitmap.height + 2 * padding)
        layoutParams.gravity = Gravity.CENTER
        cropBox.layoutParams = layoutParams
    }

    private fun getEdgePoints(tempBitmap: Bitmap): HashMap<Int, PointF> {
        val mat = Mat()
        Utils.bitmapToMat(tempBitmap, mat)
        var points = Processamento.DetectDocPt(mat)
        mat.release()
        var pointFList = ArrayList<PointF>()
        for (p in points) {
            pointFList.add((PointF(p.x.toFloat(), p.y.toFloat())))
        }
        if (pointFList.size == 0)
            return getOutlinePoints(tempBitmap)

        return cropBox.getOrderedPoints(pointFList)
    }

    private fun getOutlinePoints(tempBitmap: Bitmap): HashMap<Int, PointF> {
        val outlinePoints = HashMap<Int, PointF>()
        outlinePoints.put(0, PointF(0.0f, 0.0f))
        outlinePoints.put(1, PointF(tempBitmap.width.toFloat() - 40, 0.0f))
        outlinePoints.put(2, PointF(0.0f, tempBitmap.height.toFloat() - 40))
        outlinePoints.put(3, PointF(tempBitmap.width.toFloat() - 40, tempBitmap.height.toFloat() - 40))
        return outlinePoints
    }


    private fun scaledBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val m = Matrix()
        m.setRectToRect(RectF(0.0f, 0.0f, bitmap.width.toFloat(), bitmap.height.toFloat())
                , RectF(0.0f, 0.0f, width.toFloat(), height.toFloat()), Matrix.ScaleToFit.CENTER)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }

    private fun retornaImagemCrop(tempBitmap: Bitmap) {
        val points = cropBox.getPoints()

        val width = original.width
        val height = original.height
        val xRatio = original.width.toFloat() / android_image.width
        val yRatio = original.height.toFloat() / android_image.height

        val x1 = points[0]?.x!! * xRatio
        val x2 = points[1]?.x!! * xRatio
        val x3 = points[2]?.x!! * xRatio
        val x4 = points[3]?.x!! * xRatio
        val y1 = points[0]?.y!! * yRatio
        val y2 = points[1]?.y!! * yRatio
        val y3 = points[2]?.y!! * yRatio
        val y4 = points[3]?.y!! * yRatio

        //val topL = points.get(0)
        //val topR = points.get(1)
        //val botR = points.get(3)
        //val botL = points.get(2)

        //val listPoints = ArrayList<Point>()
        //for ( p in points){
        //    listPoints.add(Point(p.value.x.toDouble(), p.value.y.toDouble()))
        //}
        var mat = Mat()
        Utils.bitmapToMat(tempBitmap, mat)

        //calculamos a distancia euclidiana entre os 4 pontos para obter a maior distancia
        //val widthA = Math.sqrt(Math.pow((botR!!.x - botL!!.x).toDouble(), 2.0) + Math.pow((botR.y - botL.y).toDouble(), 2.0))
        //val widthB = Math.sqrt(Math.pow((topR!!.x - topL!!.x).toDouble(), 2.0) + Math.pow((topR.y - topL.y).toDouble(), 2.0))
        //var maiorWidth = Math.max(widthA, widthB);

        //val heightA = Math.sqrt(Math.pow((topR.x - botR.x).toDouble(), 2.0) + Math.pow((topR.y - botR.y).toDouble(), 2.0))
        //val heightB = Math.sqrt(Math.pow((topL.x - botL.x).toDouble(), 2.0) + Math.pow((topL.y - botL.y).toDouble(), 2.0))

        //var maiorHeight = (Math.max(heightA, heightB) * yRatio);

        val w1 = Math.sqrt(Math.pow((x4 - x3).toDouble(), 2.0) + Math.pow((x4 - x3).toDouble(), 2.0))
        val w2 = Math.sqrt(Math.pow((x2 - x1).toDouble(), 2.0) + Math.pow((x2 - x1).toDouble(), 2.0))
        val h1 = Math.sqrt(Math.pow((y2 - y4).toDouble(), 2.0) + Math.pow((y2 - y4).toDouble(), 2.0))
        val h2 = Math.sqrt(Math.pow((y1 - y3).toDouble(), 2.0) + Math.pow((y1 - y3).toDouble(), 2.0))

        val maxWidth = if (w1 < w2) w1 else w2
        val maxHeight = if (h1 < h2) h1 else h2

        val origem = Mat(4, 1, CvType.CV_32FC2)
        val destino = Mat(4, 1, CvType.CV_32FC2)

        origem.put(0, 0,
                x1.toDouble(),
                y1.toDouble(),
                x2.toDouble(),
                y2.toDouble(),
                x3.toDouble(),
                y3.toDouble(),
                x4.toDouble(),
                y4.toDouble())
        destino.put(0, 0, 0.0, 0.0, maxWidth, 0.0, 0.0, maxHeight, maxWidth, maxHeight)
        val docFinal = Mat(maxHeight.toInt(), maxWidth.toInt(), CvType.CV_8UC4)

        val m = Imgproc.getPerspectiveTransform(origem, destino)

        Imgproc.warpPerspective(mat, docFinal, m, docFinal.size())

        //Imgcodecs.imwrite(this.applicationInfo.dataDir  +"/files/cropedFoto.jpg",mat)
        Imgcodecs.imwrite((Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/cropedFoto.jpg"), docFinal)

        val intent = Intent()
        intent.data = Uri.fromFile(File((Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/cropedFoto.jpg")))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}
