package scan.lucas.com.docscan


import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import kotlinx.android.synthetic.main.activity_scan.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import scan.lucas.com.docscan.Enum.TipoProcessamento
import scan.lucas.com.docscan.Helpers.PreferenceHelper
import scan.lucas.com.docscan.Helpers.PreferenceHelper.get
import scan.lucas.com.docscan.models.ImageMessage
import java.io.File
import java.util.*


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ScanActivity : AppCompatActivity(), SurfaceHolder.Callback, Camera.PictureCallback, Camera.PreviewCallback {
    private var currentApiVersion: Int = 0
    val permissoes: kotlin.Array<String> = arrayOf(Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var mSurfaceView: SurfaceView? = null
    private var mSurfaceHolder: SurfaceHolder? = null
    private var mCamera: Camera? = null
    private var mAutoFocus: Boolean = false
    private var mCameraFlash: Boolean = false
    var mPrefs: SharedPreferences? = null
    private var mProcessamento: Processamento? = null
    private var mBackgroundThread: HandlerThread? = null
    private var lockFoto: Boolean = false
    private var nFotos: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_scan)

        mSurfaceView = texture


        btnCancelar.setOnClickListener({
            AlertDialog.Builder(this@ScanActivity)
                    .setMessage("Deseja cancelar a digitalização?")
                    .setTitle("Sair")
                    .setCancelable(true)
                    .setPositiveButton("Não", DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->

                    })
                    .setPositiveButton("Sim", DialogInterface.OnClickListener { dialog, which ->
                        val intent = Intent(this@ScanActivity, MainActivity::class.java)
                        intent.putExtra("uploadFile", false)
                        startActivity(intent)
                    })
                    .create()
                    .show()
        })
        btnflash.setOnClickListener(View.OnClickListener {
            mCameraFlash = AtivarFlash(!mCameraFlash)
            if (mCameraFlash) {
                this.runOnUiThread {
                    btnflash.setBackgroundResource(R.drawable.ic_flash_on)
                }
            } else
                this.runOnUiThread {
                    btnflash.setBackgroundResource(R.drawable.ic_flash_off)
                }

        })
        btnFoto.setOnClickListener(View.OnClickListener {
            if (!lockFoto) {
                lockFoto = true
                mCamera!!.takePicture(null, null, this@ScanActivity)

            }
        })
        imagePreview.setOnClickListener(View.OnClickListener {
            var intent = Intent(this.applicationContext, EdicaoActivity::class.java)
            startActivity(intent)
        })
    }

    private fun limparDiretorio() {
        val file = File(Processamento.caminhoPadrao)
        if (file.isDirectory) {
            var children = file.list()
            for (i in 0 until children.size) {
                File(file, children[i]).delete()
            }
        }
    }

    public override fun onResume() {
        super.onResume()

        currentApiVersion = android.os.Build.VERSION.SDK_INT

        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // This work only for android 4.4+
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {

            window.decorView.systemUiVisibility = flags

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            val decorView = window.decorView
            decorView.setOnSystemUiVisibilityChangeListener(object : View.OnSystemUiVisibilityChangeListener {

                override fun onSystemUiVisibilityChange(visibility: Int) {
                    if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN === 0) {
                        decorView.systemUiVisibility = flags
                    }
                }
            })
        }

        Log.d(TAG, "resuming")
        verificarPermissoes()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }

        mPrefs = PreferenceHelper.defaultPrefs(this)

        if (mBackgroundThread == null) {
            mBackgroundThread = HandlerThread("Worker Thread")
            mBackgroundThread!!.start()
        }

        if (mProcessamento == null) {
            mProcessamento = Processamento(mBackgroundThread!!.looper, Handler(), this)
        }
        mProcessamento!!.processando = false
        limparDiretorio()
    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onDestroy() {
        super.onDestroy()
        // TODO: check disableView()
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {

        val pictureSize = camera!!.parameters.previewSize
        if (!mProcessamento!!.processando && !lockFoto) {
            mProcessamento!!.processando = true

            val yuv = Mat(Size(pictureSize.width.toDouble(), pictureSize.height * 1.5), CvType.CV_8UC1)
            yuv.put(0, 0, data)
            //Core.transpose(mat, mat);
            // Core.rotate(yuv, yuv, Core.ROTATE_90_CLOCKWISE)
            val mat = Mat(Size(pictureSize.width.toDouble(), pictureSize.height.toDouble()), CvType.CV_8UC4)
            Imgproc.cvtColor(yuv, mat, Imgproc.COLOR_YUV2RGBA_NV21, 4)
            Core.rotate(mat, mat, Core.ROTATE_90_CLOCKWISE)
            yuv.release()

            enviarRequest(TipoProcessamento.PREVIEW, mat)
        }

    }

    private fun enviarRequest(t: TipoProcessamento, obj: Any) {
        val msg = mProcessamento!!.obtainMessage()
        var _object = ImageMessage(t, obj, 0, 0)
        msg.obj = _object
        mProcessamento!!.sendMessage(msg)
    }

    override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
        val pictureSize = camera!!.parameters.pictureSize
        Log.d(TAG, "Foto tirada - " + pictureSize.width + "x" + pictureSize.height)

        val meng = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION)

        if (volume != 0) {
            var sound = MediaPlayer.create(this, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"))
            if (sound != null) {
                sound.start()
            }
        }


        val mat = Mat(Size(pictureSize.width.toDouble(), pictureSize.height.toDouble()), CvType.CV_8U)
        mat.put(0, 0, data)
        val img = Imgcodecs.imdecode(mat, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED)
        Core.rotate(img, img, Core.ROTATE_90_CLOCKWISE)

        lockFoto = false
        mProcessamento!!.processando = true
        enviarRequest(TipoProcessamento.PICTURE, img)
        mCamera!!.startPreview()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            val cameraId = MelhorCamera()
            mCamera = Camera.open(cameraId)
        } catch (e: RuntimeException) {
            System.err.println(e)
            return
        }
        val cameraParametros = mCamera!!.parameters

        val pSize = MelhorResolucaoFrame(mCamera as Camera)
        cameraParametros.setPreviewSize(pSize.width, pSize.height)

        val display = windowManager.defaultDisplay
        val size = android.graphics.Point()
        display.getRealSize(size)

        val displayWidth = Math.min(size.y, size.x)
        val displayHeight = Math.max(size.y, size.x)

        val displayRatio = displayHeight.toFloat() / displayWidth.toFloat()

        val previewRatio = pSize.width.toFloat() / pSize.height
        var previewHeight = displayHeight

        if (displayRatio > previewRatio) {
            val surfaceParams = mSurfaceView!!.layoutParams
            previewHeight = (size.y.toFloat() / displayRatio * previewRatio).toInt()
            surfaceParams.height = previewHeight
            mSurfaceView!!.layoutParams = surfaceParams

            canvasCustom.layoutParams.height = previewHeight
        }
        val maxRes = MaiorResolucaoFoto(previewRatio, mCamera as Camera)
        if (maxRes != null) {
            cameraParametros.setPictureSize(maxRes.width, maxRes.height)
            Log.d(TAG, "max supported picture resolution: " + maxRes.width + "x" + maxRes.height)
        }

        val pm = packageManager
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
            cameraParametros.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            Log.d(TAG, "ativando o autofocus")
        } else {
            mAutoFocus = true
            Log.d(TAG, "autofocus não disponivel")
        }
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            cameraParametros.flashMode = if (mCameraFlash) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF
        }

        mCamera!!.parameters = cameraParametros


        if (mPrefs!!["ROTACIONAR", false] as Boolean) {
            mCamera!!.setDisplayOrientation(270)
        } else {
            mCamera!!.setDisplayOrientation(90)
        }

        try {
            mCamera!!.setAutoFocusMoveCallback(Camera.AutoFocusMoveCallback { start, camera ->
                mAutoFocus = !start
                Log.d(TAG, "focusMoving: $mAutoFocus")
            })
        } catch (e: Exception) {
            Log.d(TAG, "failed setting AutoFocusMoveCallback")
        }
        mAutoFocus = true

    }

    private fun AtualizarCamera() {
        try {
            mCamera!!.stopPreview()
        } catch (e: Exception) {
            Log.d(TAG, "${e.message}")
        }

        try {
            mCamera!!.setPreviewDisplay(mSurfaceHolder)
            mCamera!!.startPreview()
            mCamera!!.setPreviewCallback(this)
        } catch (e: Exception) {
            Log.d(TAG, "${e.message}")
        }

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        AtualizarCamera()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mCamera!!.stopPreview()
        mCamera!!.setPreviewCallback(null)
        mCamera!!.release()
        mCamera = null
    }

    private fun MelhorCamera(): Int {
        var cameraId = -1

        val nCamera = Camera.getNumberOfCameras()
        //for every camera check
        for (i in 0 until nCamera) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i
                break
            }
            cameraId = i
        }
        return cameraId
    }

    private fun MelhorResolucaoFrame(camera: Camera): Camera.Size {
        camera.lock()
        var maxWidth = 0
        var lisTamanhos = camera.parameters.supportedPreviewSizes.toMutableList()
        lisTamanhos.removeAt(0)
        return Collections.max(lisTamanhos, scan.lucas.com.docscan.CompareCameraSizesByArea())
    }

    private fun MaiorResolucaoFoto(pvRatio: Float, camera: Camera): Camera.Size {
        var maxPixels = 0
        var ratioMaxPixels = 0
        var currentMaxRes: Camera.Size? = null
        var ratioCurrentMaxRes: Camera.Size? = null
        for (r in camera.parameters.supportedPictureSizes) {
            val pictureRatio = r.width.toFloat() / r.height
            Log.d(TAG, "Resolucao suportada: " + r.width + "x" + r.height + " ratio: " + pictureRatio)
            val resolutionPixels = r.width * r.height

            if (resolutionPixels > ratioMaxPixels && pictureRatio == pvRatio) {
                ratioMaxPixels = resolutionPixels
                ratioCurrentMaxRes = r
            }

            if (resolutionPixels > maxPixels) {
                maxPixels = resolutionPixels
                currentMaxRes = r
            }
        }

        val mAspect = mPrefs!!["mAspect", true] as Boolean
        if (ratioCurrentMaxRes != null && mAspect) {

            Log.d(TAG, "Max supported picture resolution with preview aspect ratio: "
                    + ratioCurrentMaxRes.width + "x" + ratioCurrentMaxRes.height)
            return ratioCurrentMaxRes

        }

        return currentMaxRes as Camera.Size
    }

    fun onNavigationItemSelected(item: MenuItem): Boolean {
        return false
    }

    fun EnableCamera() {

        mSurfaceHolder = mSurfaceView!!.holder

        mSurfaceHolder!!.addCallback(this)
        mSurfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        mSurfaceView!!.visibility = (SurfaceView.VISIBLE)
    }

    fun EnableCameraView() {
        EnableCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CREATE_PERMISSIONS_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    EnableCamera()
                }
            }

            RESUME_PERMISSIONS_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    EnableCameraView()
                }
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }

    private fun checkResumePermissions() {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.CAMERA),
                    RESUME_PERMISSIONS_REQUEST_CAMERA)

        } else {
            EnableCameraView()
        }
    }

    private fun verificarPermissoes() {

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_WRITE)

        }

    }

    //Carrega a openCV assyncrono
    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    checkResumePermissions()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    fun AtivarFlash(flashAtivo: Boolean): Boolean {
        val pm = packageManager
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            val parametros = mCamera!!.parameters
            parametros.flashMode = if (flashAtivo) Camera.Parameters.FLASH_MODE_TORCH else Camera.Parameters.FLASH_MODE_OFF
            mCamera!!.parameters = parametros
            Log.d(TAG, "flash: $flashAtivo")
            return flashAtivo
        }
        return false
    }

    fun salvar(matOriginal: Mat, matCrop: Mat, matProcess: Mat) {
        val file = File(Processamento.caminhoPadrao)
        if (!file.exists())
            file.mkdirs()

        Imgcodecs.imwrite(file.absolutePath + "/croped" + nFotos + ".jpg", matCrop)
        Imgcodecs.imwrite(file.absolutePath + "/processado" + nFotos + ".jpg", matProcess)
        Imgcodecs.imwrite(file.absolutePath + "/original" + nFotos + ".jpg", matOriginal)

        var imageBitmap = ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(file.absolutePath + "/processado" + nFotos + ".jpg"), 50, 50)
        this.runOnUiThread {
            if (image_layout.visibility == android.view.View.GONE) {
                image_layout.visibility = View.VISIBLE
                imagePreview.visibility = View.VISIBLE
            }
            nFotos++
            qntImage.text = nFotos.toString()
            imagePreview.setImageBitmap(imageBitmap)

        }

    }

    companion object {

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300

        private val CREATE_PERMISSIONS_REQUEST_CAMERA = 1
        private val PERMISSIONS_REQUEST_WRITE = 3

        private val RESUME_PERMISSIONS_REQUEST_CAMERA = 11

        private val TAG = "ScanActivity"

    }

}
