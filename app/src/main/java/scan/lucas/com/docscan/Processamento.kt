package scan.lucas.com.docscan


import android.app.Activity
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.shapes.PathShape
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import kotlinx.android.synthetic.main.fragment_camera2_basic.*
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.imgproc.Imgproc.contourArea
import org.opencv.imgproc.Imgproc.isContourConvex
import scan.lucas.com.docscan.Enum.TipoProcessamento
import scan.lucas.com.docscan.models.ImageMessage
import java.util.*
import kotlin.collections.ArrayList


class Processamento(looper: Looper, handler: Handler, context: android.content.Context) : Handler(looper) {
    var processando: Boolean = false
    var aguardFocus: Boolean = false
    val mContext: android.content.Context = context

    init {
        // val sharedPref = PreferenceManager.getDefaultSharedPreferences(mMainActivity)

    }

    override fun handleMessage(msg: Message) {
        if (msg.obj.javaClass != ImageMessage::class.java)
            return

        var result = msg.obj as ImageMessage
        when (result.Tipo) {

            TipoProcessamento.PICTURE -> {
                val matResult = result.Obj as Mat

                var doc = Mat()
                matResult.copyTo(doc)

                val pt = DetectDoc(matResult)
                if (pt != null && pt.count() == 4) {

                    doc = Pespectiva(matResult, pt)

                }
                var docNoThresed = Mat()
                doc.copyTo(docNoThresed)
                //val aux = Mat (doc.size(), CvType.CV_8UC4);
                Imgproc.cvtColor(doc, doc, Imgproc.COLOR_RGBA2GRAY)
                Imgproc.adaptiveThreshold(doc, doc, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 15.0)

                //passa a matris original, a recortade sem o processamento e recortada com o processamento
                (mContext as ScanActivity).salvar(matResult, docNoThresed, doc)


                matResult.release()
                doc.release()
                Camera2BasicFragment.processando = false
                val canvasCustom = (mContext as Activity).canvasCustom
                canvasCustom.clear()
                (mContext as Activity).runOnUiThread {

                    canvasCustom.invalidate()
                }
                aguardFocus = false
                processando = false


            }
            TipoProcessamento.PREVIEW -> {
                val outputImage = Mat()

                //imageFromJNI((result.Obj as Mat).getNativeObjAddr(), outputImage.getNativeObjAddr())
                //org.opencv.imgcodecs.Imgcodecs.imwrite(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/cannedImageCCP.jpg", outputImage)

                val pt = DetectDoc(result.Obj as Mat)
                if (pt != null && pt.count() == 4) {
                    var s = (result.Obj as Mat).size()

                    val ratio = (s!!.height.toDouble() / 500.toDouble())
                    var pontosNovos = ArrayList<Point>()
                    for (p in pt) {
                        val x = java.lang.Double.valueOf(p.x * ratio).toInt()
                        val y = java.lang.Double.valueOf(p.y * ratio).toInt()
                        pontosNovos.add(Point(x.toDouble(), y.toDouble()))
                    }
                    Desenhar(android.util.Size(s.width.toInt(), s.height.toInt()), pontosNovos)
                }
                processando = false

            }
            TipoProcessamento.COLOR -> {

            }
            TipoProcessamento.FILTER -> {
            }

        }
        Camera2BasicFragment.processando = false
    }

    private fun Preview(frame: Mat) {

    }

    private fun DetectDoc(frame: Mat): List<Point>? {
        val ratio = frame.size().height / 500
        val height = (frame.size().height.toDouble() / ratio.toDouble()).toInt()
        val width = (frame.size().width.toDouble() / ratio.toDouble()).toInt()
        val size = Size(width.toDouble(), height.toDouble())

        var resizedImage = Mat(size, CvType.CV_8UC4)
        var resizedImage2 = Mat(size, CvType.CV_8UC4)
        var grayImage = Mat(size, CvType.CV_8UC4)
        var threshedImage = Mat(size, CvType.CV_8UC4)
        var cannedImage = Mat(size, CvType.CV_8UC1)
        var cannedImageT = Mat(size, CvType.CV_8UC1)


        Imgproc.resize(frame, resizedImage, size)
        org.opencv.imgcodecs.Imgcodecs.imwrite(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/resizedImage.jpg", resizedImage)
        resizedImage.copyTo(resizedImage2)

        Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_RGBA2GRAY, 4)
        org.opencv.imgcodecs.Imgcodecs.imwrite(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/grayImage.jpg", grayImage)

        Imgproc.adaptiveThreshold(grayImage, threshedImage, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 15, 15.0)
        org.opencv.imgcodecs.Imgcodecs.imwrite(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/threshed.jpg", threshedImage)

        //Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_BGR2HSV)
        //val lab_list = ArrayList<Mat>(3)
        //Core.split(grayImage, lab_list)

        //org.opencv.imgcodecs.Imgcodecs.imwrite(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/c1.jpg", lab_list[0])
        //org.opencv.imgcodecs.Imgcodecs.imwrite(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/c2.jpg", lab_list[1])
        //org.opencv.imgcodecs.Imgcodecs.imwrite(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/c3.jpg", lab_list[2])

        Imgproc.medianBlur(grayImage, grayImage, 9)
        //Imgproc.GaussianBlur(grayImage, grayImage, Size(5.0, 5.0), 0.0)
        //org.opencv.imgcodecs.Imgcodecs.imwrite(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/GaussianBlur.jpg", grayImage)

        Imgproc.Canny(grayImage, cannedImage, 75.0, 200.0)
        Imgproc.dilate(cannedImage, cannedImage, Mat(), Point(-1.0, -1.0), 1)
        org.opencv.imgcodecs.Imgcodecs.imwrite(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/cannedImage.jpg", cannedImage)

        Imgproc.Canny(threshedImage, cannedImageT, 75.0, 200.0)
        org.opencv.imgcodecs.Imgcodecs.imwrite(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/cannedImageTr.jpg", cannedImageT)
        //val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "canny.jpg")

        val lines = Mat() // will hold the results of the detection
        Imgproc.HoughLines(cannedImage, lines, 1.0, Math.PI / 180, 200) // runs the actual detection
        //! [hough_lines]
        //! [draw_lines]
        // Draw the lines
        for (x in 0 until lines.rows()) {
            val rho = lines.get(x, 0)[0]
            val theta = lines.get(x, 0)[1]

            val a = Math.cos(theta)
            val b = Math.sin(theta)
            val x0 = a * rho
            val y0 = b * rho
            val pt1 = Point(Math.round(x0 + 1000 * -b).toDouble(), Math.round(y0 + 1000 * a).toDouble())
            val pt2 = Point(Math.round(x0 - 1000 * -b).toDouble(), Math.round(y0 - 1000 * a).toDouble())
            Imgproc.line(resizedImage, pt1, pt2, Scalar(0.0, 0.0, 255.0), 3, Imgproc.LINE_AA, 0)
        }
        //! [draw_lines]

        //! [hough_lines_p]
        // Probabilistic Line Transform
        val linesP = Mat() // will hold the results of the detection
        Imgproc.HoughLinesP(cannedImage, linesP, 1.0, Math.PI / 180, 50, 50.0, 10.0) // runs the actual detection
        //! [hough_lines_p]
        //! [draw_lines_p]
        // Draw the lines
        for (x in 0 until linesP.rows()) {
            val l = linesP.get(x, 0)
            Imgproc.line(resizedImage2, Point(l[0], l[1]), Point(l[2], l[3]), Scalar(0.0, 0.0, 255.0), 3, Imgproc.LINE_AA, 0)
        }
        org.opencv.imgcodecs.Imgcodecs.imwrite(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/linhas.jpg", resizedImage)
        org.opencv.imgcodecs.Imgcodecs.imwrite(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/linhasp.jpg", resizedImage2)
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()

        Imgproc.findContours(cannedImage, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

        hierarchy.release()

        Collections.sort(contours) { lhs, rhs -> java.lang.Double.valueOf(Imgproc.contourArea(rhs)).compareTo(Imgproc.contourArea(lhs)) }

        resizedImage.release()
        grayImage.release()
        cannedImage.release()
        cannedImageT.release()
        threshedImage.release()
        resizedImage2.release()

        var i = 0
        for (c in contours) {

            val c2f = MatOfPoint2f(*c.toArray())
            val peri = Imgproc.arcLength(c2f, true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true)

            val points = approx.toList()

            val app = MatOfPoint()
            approx.convertTo(app, CvType.CV_32S)
            // select biggest 4 angles polygon
            if (points.size == 4 &&
                    Math.abs(contourArea(contours.get(i))) > 3000 &&
                    isContourConvex(app)) {
                var maxCosine = 0.0

                val point = BestPoints(points)
                //if(insideArea(point, size)){
                return point
                //}
                //else
                //    return emptyList()


            }
        }
        return ArrayList<Point>()
    }

    private fun Desenhar(size: android.util.Size?, points: List<Point>) {
        var path = Path()
        val previewWidth = size!!.width.toFloat()
        val previewHeight = size.height.toFloat()

        var p1x = points[0].x.toFloat()
        var p1y = points[0].y.toFloat()

        var p2x = points[1].x.toFloat()
        var p2y = points[1].y.toFloat()

        var p3x = points[3].x.toFloat()
        var p3y = points[3].y.toFloat()

        var p4x = points[2].x.toFloat()
        var p4y = points[2].y.toFloat()
        path.moveTo(p1x, p1y)
        path.lineTo(p2x, p2y)
        path.lineTo(p4x, p4y)
        path.lineTo(p3x, p3y)
        path.close()

        val newBox = PathShape(path, previewWidth, previewHeight)

        val paint = Paint()
        paint.color = Color.argb(64, 0, 255, 0)

        val border = Paint()
        border.color = Color.rgb(0, 255, 0)
        border.strokeWidth = 5f

        val canvasCustom = (mContext as Activity).canvasCustom
        canvasCustom.clear()
        canvasCustom.add(newBox, paint, border)

        mContext.runOnUiThread {

            canvasCustom.invalidate()
        }
    }

    external fun getCanny(mat: Mat)
    external fun imageFromJNI(inputImage: Long, outputImage: Long)


    fun angle(pt1: Point, pt2: Point, pt0: Point): Double {
        val dx1 = pt1.x - pt0.x
        val dy1 = pt1.y - pt0.y
        val dx2 = pt2.x - pt0.x
        val dy2 = pt2.y - pt0.y
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10)
    }


    companion object {
        fun DetectDocPt(mat: Mat): List<Point> {
            val ratio = mat.size().height / 500
            val height = (mat.size().height.toDouble() / ratio.toDouble()).toInt()
            val width = (mat.size().width.toDouble() / ratio.toDouble()).toInt()
            val size = Size(width.toDouble(), height.toDouble())

            var resizedImage = Mat(size, CvType.CV_8UC4)
            var grayImage = Mat(size, CvType.CV_8UC4)
            var cannedImage = Mat(size, CvType.CV_8UC1)

            Imgproc.resize(mat, resizedImage, size)
            Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_RGBA2GRAY, 4)
            Imgproc.GaussianBlur(grayImage, grayImage, Size(5.0, 5.0), 0.0)
            Imgproc.Canny(grayImage, cannedImage, 75.0, 200.0)

            val contours = ArrayList<MatOfPoint>()
            val hierarchy = Mat()

            Imgproc.findContours(cannedImage, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

            hierarchy.release()

            Collections.sort(contours) { lhs, rhs -> java.lang.Double.valueOf(Imgproc.contourArea(rhs)).compareTo(Imgproc.contourArea(lhs)) }

            resizedImage.release()
            grayImage.release()
            cannedImage.release()
            mat.release()

            var i = 0
            for (c in contours) {

                val c2f = MatOfPoint2f(*c.toArray())
                val peri = Imgproc.arcLength(c2f, true)
                val approx = MatOfPoint2f()
                Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true)

                val points = approx.toList()

                val app = MatOfPoint()
                approx.convertTo(app, CvType.CV_32S)
                // select biggest 4 angles polygon
                if (points.size == 4 &&
                        Math.abs(contourArea(contours.get(i))) > 3000 &&
                        isContourConvex(app)) {
                    var maxCosine = 0.0

                    val point = BestPoints(points)
                    return point


                }
            }
            return ArrayList<Point>()

        }

        fun Pespectiva(ori: Mat, pts: List<Point>, ratio: Float = 500.0f): Mat {
            val ratio = ori.size().height / ratio

            val topL = pts.get(0)
            val topR = pts.get(1)
            val botR = pts.get(2)
            val botL = pts.get(3)

            //calculamos a distancia euclidiana entre os 4 pontos para obter a maior distancia
            val widthA = Math.sqrt(Math.pow(botR.x - botL.x, 2.0) + Math.pow(botR.y - botL.y, 2.0))
            val widthB = Math.sqrt(Math.pow(topR.x - topL.x, 2.0) + Math.pow(topR.y - topL.y, 2.0))
            var maiorWidth = (Math.max(widthA, widthB) * ratio)

            val heightA = Math.sqrt(Math.pow(topR.x - botR.x, 2.0) + Math.pow(topR.y - botR.y, 2.0))
            val heightB = Math.sqrt(Math.pow(topL.x - botL.x, 2.0) + Math.pow(topL.y - botL.y, 2.0))

            var maiorHeight = (Math.max(heightA, heightB) * ratio)

            val origem = Mat(4, 1, CvType.CV_32FC2)
            val destino = Mat(4, 1, CvType.CV_32FC2)

            origem.put(0, 0,
                    topL.x * ratio,
                    topL.y * ratio,
                    topR.x * ratio,
                    topR.y * ratio,
                    botR.x * ratio,
                    botR.y * ratio,
                    botL.x * ratio,
                    botL.y * ratio)
            destino.put(0, 0, 0.0, 0.0, maiorWidth, 0.0, maiorWidth, maiorHeight, 0.0, maiorHeight)
            val docFinal = Mat(maiorHeight.toInt(), maiorWidth.toInt(), CvType.CV_8UC4)

            val m = Imgproc.getPerspectiveTransform(origem, destino)

            Imgproc.warpPerspective(ori, docFinal, m, docFinal.size())

            return docFinal
        }

        private fun BestPoints(src: List<Point>): List<Point> {

            val srcPoints = src.toMutableList()

            val result = ArrayList<Point>(4)

            val sumComparator = Comparator<Point> { lhs, rhs -> java.lang.Double.valueOf(lhs.y + lhs.x).compareTo(rhs.y + rhs.x) }

            val diffComparator = Comparator<Point> { lhs, rhs -> java.lang.Double.valueOf(lhs.y - lhs.x).compareTo(rhs.y - rhs.x) }

// top-left corner = minimal sum
            result.add(Collections.min(srcPoints, sumComparator))
// top-right corner = minimal diference
            result.add(Collections.min(srcPoints, diffComparator))
// bottom-right corner = maximal sum
            result.add(Collections.max(srcPoints, sumComparator))
// bottom-left corner = maximal diference
            result.add(Collections.max(srcPoints, diffComparator))

            return result
        }

        // Used to load the 'native-lib' library on application startup.
        val caminhoPadrao = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/Scan"

        init {
            System.loadLibrary("native-lib")
        }
    }
}
