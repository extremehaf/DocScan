package scan.lucas.com.docscan

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_fitros.*
import kotlinx.android.synthetic.main.content_fitros.*
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream


class FitrosActivity : AppCompatActivity() {
    val TAG = "FitrosActivity"
    var matAux: Mat = Mat()
    var matDoc: Mat? = null
    var result: Bitmap? = null
    var caminnho: String = ""
    var blockSize: Int = 15
    var constraintC: Int = 15
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fitros)
        setSupportActionBar(toolbar2)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val intent = intent
        caminnho = intent.getStringExtra("fotoPath")

        var bmpOriginal = BitmapFactory.decodeFile(File(caminnho).absolutePath)

        ivImage.setImageBitmap(bmpOriginal)

        ExibirControlesColorido(false)
        ExibirControlesBinario(true)

        if (File(caminnho.replace("processado", "croped")).exists())
            caminnho = caminnho.replace("processado", "croped")


        //source matrix, unsigned 8-bit 4 channels (RGBA)
        matDoc = Imgcodecs.imread(caminnho)

        //output matrix, grayscale 8-bit 4 channels
        matDoc!!.copyTo(matAux)
        //create bitmap having width (columns) and height(rows) as matrix and in the format of 8 bit/pixel
        result = Bitmap.createBitmap(bmpOriginal.width, bmpOriginal.height, Bitmap.Config.RGB_565)
        Imgproc.cvtColor(matDoc, matDoc, Imgproc.COLOR_RGBA2GRAY)
        Imgproc.cvtColor(matAux, matAux, Imgproc.COLOR_RGBA2GRAY)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                blockSize = progress
                seekBarValue.text = progress.toString()
                AlterarFiltro(blockSize, constraintC)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })
        seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBar2Value.text = progress.toString()
                constraintC = progress
                AlterarFiltro(blockSize, constraintC)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        radioGroup1.setOnCheckedChangeListener { group, checkedId ->
            Log.d(TAG, "checkedId: $checkedId ")
            when (checkedId) {
                R.id.rbBinario -> {
                    ckAutoAjuste.isChecked = false
                    ExibirControlesColorido(false)
                    ExibirControlesBinario(true)
                    Log.d(TAG, "checkedId: $checkedId ")
                }
                R.id.rbPetroEBranco -> {
                    ckAutoAjuste.isChecked = false
                    ExibirControlesColorido(false)
                    ExibirControlesBinario(false)
                    ConverterParaPb()
                }
                R.id.rbColorido -> {
                    ExibirControlesColorido(true)
                    ExibirControlesBinario(false)
                    Log.d(TAG, "checkedId: $checkedId ")
                }

            }
        }

        ckAutoAjuste.setOnClickListener { view ->
            if (ckAutoAjuste.isChecked) {
                AutoAjuste(caminnho)
            } else {
                ivImage.setImageBitmap(BitmapFactory.decodeFile(File(caminnho).absolutePath))
            }
        }

        btnOk.setOnClickListener { view ->
            salvarFoto(caminnho)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        matDoc?.release()
        matAux.release()
        result?.recycle()
    }

    fun ExibirControlesBinario(exibir: Boolean) {
        if (!exibir) {
            controlesFiltro1.visibility = View.GONE
        } else {
            AlterarFiltro(blockSize, constraintC)
            controlesFiltro1.visibility = View.VISIBLE
            controlesFiltroColorido.visibility = View.GONE
        }
    }

    fun ExibirControlesColorido(exibir: Boolean) {
        if (!exibir) {
            controlesFiltroColorido.visibility = View.GONE
        } else {
            ivImage.setImageBitmap(BitmapFactory.decodeFile(File(caminnho).absolutePath))
            controlesFiltroColorido.visibility = View.VISIBLE
            controlesFiltro1.visibility = View.GONE
        }
    }

    fun AlterarFiltro(valChange: Int, c: Int) {
        try {

            Imgproc.adaptiveThreshold(matDoc, matAux, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, valChange, c.toDouble())

            //convert result matrix to bitmap
            Utils.matToBitmap(matAux, result)
            //show bitmap in ImageView
            runOnUiThread {
                ivImage.setImageBitmap(result)
            }
        } catch (e: Exception) {
            Log.e("Filtro", "Erro adaptiveThreshold $valChange : Exception: ${e.message}")
        }

    }

    fun ConverterParaPb() {
        //convert result matrix to bitmap
        Utils.matToBitmap(matDoc, result)
        //show bitmap in ImageView
        runOnUiThread {
            ivImage.setImageBitmap(result)
        }
    }

    fun AutoAjuste(file: String) {
        val src = Imgcodecs.imread(file)
        //Core.normalize(_matDoc, _matDoc)
        val gray = Mat()
        val hsv = Mat()
        val ycrcb: Mat = Mat()
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY) // converter rgb to gray
        Imgproc.cvtColor(src, hsv, Imgproc.COLOR_RGB2HSV) // converter rgb to hsv


        //Imgproc.cvtColor(src, ycrcb, Imgproc.COLOR_BGR2YCrCb)

        val channels = ArrayList<Mat>(3)
        Core.split(hsv, channels)

        Imgproc.equalizeHist(gray, gray)
        Imgproc.equalizeHist(channels[1], channels[1])

        Core.merge(channels, hsv) // merge h,s,v to hsv
        Imgproc.cvtColor(hsv, src, Imgproc.COLOR_HSV2RGB) // converter hsv to rgb


//        val histSize = 256
//        val alpha: Float
//        val beta: Float
//        val minGray = 0.0
//        val maxGray = 0.0
//        val clipHistPercent = 0
//        var mat_gray = Mat()
//
//        if (_matDoc.type() == CvType.CV_8UC1)
//            mat_gray = _matDoc;
//        else if (_matDoc.type() == CvType.CV_8UC3) Imgproc.cvtColor(_matDoc, mat_gray, Imgproc.COLOR_BGR2GRAY);
//        else if (_matDoc.type() == CvType.CV_8UC4) Imgproc.cvtColor(_matDoc, mat_gray, Imgproc.COLOR_BGRA2GRAY);
//        if (clipHistPercent == 0) {
//            // keep full available range
//            Core.minMaxLoc(mat_gray)
//        } else {
//
//            var hist: Mat = Mat()
//            val range = floatArrayOf(0f, 256f)
//            val histRange = range.clone()
//            val uniform = true
//            val accumulate = false
//            Imgproc.calcHist(mat_gray,1, 0, Mat(), hist, 1, histSize, histRange, uniform, accumulate);
//        }
//    }


        Utils.matToBitmap(hsv, this.result)
        //show bitmap in ImageView
        runOnUiThread {
            ivImage.setImageBitmap(this.result)
            src.release()
            gray.release()
            hsv.release()
            ycrcb.release()
        }
    }

    fun salvarFoto(file: String) {

        val draw = ivImage.drawable as BitmapDrawable
        val bitmap = draw.bitmap

        var outStream: FileOutputStream? = null
        val outFile = File(caminnho.replace("croped", "processado"))
        outStream = FileOutputStream(outFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.flush()
        outStream.close()


        val _intent = Intent()
        _intent.putExtra("caminnhoRetorno", caminnho)
        setResult(Activity.RESULT_OK, _intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
