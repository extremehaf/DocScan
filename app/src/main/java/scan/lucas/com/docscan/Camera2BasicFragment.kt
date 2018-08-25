/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package scan.lucas.com.docscan

//import android.content.res.Configuration;

//import android.hardware.camera2.CameraMetadata;
//import android.hardware.camera2.CaptureResult;
//import android.hardware.camera2.DngCreator;
//import android.hardware.camera2.params.InputConfiguration;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;

//import java.io.IOException;
//import java.io.OutputStream;
//import java.nio.ByteBuffer;
//import java.text.SimpleDateFormat;
//import java.util.Arrays;
//import java.util.Locale;
//import java.util.Map;
//import java.util.TreeMap;
//import java.util.concurrent.LinkedBlockingQueue;
import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.content.pm.PackageManager
import android.ex.camera2.blocking.BlockingCameraManager
import android.ex.camera2.blocking.BlockingCameraManager.BlockingOpenException
import android.ex.camera2.blocking.BlockingSessionCallback
import android.ex.camera2.blocking.BlockingStateCallback
import android.ex.camera2.exceptions.TimeoutRuntimeException
import android.graphics.*
import android.hardware.SensorManager
import android.hardware.camera2.*
import android.media.AudioManager
import android.media.ImageReader
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.Toast
import scan.lucas.com.docscan.Enum.TipoProcessamento
import scan.lucas.com.docscan.Utils.ConvertYuvToRgb
import scan.lucas.com.docscan.models.ImageMessage
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.collections.ArrayList

//import java.util.Date;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;

class Camera2BasicFragment : Fragment(), View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private var mCameraSize = Size(640, 480)

    private var mSurfaceView: scan.lucas.com.docscan.AutoFitTextureView? = null
    private var mSurface: Surface? = null
    private val mStateLock = Any()
    private val mCameraOpenCloseLock = Semaphore(1)

    private var mCameraId: String? = null
    private var mCaptureSession: CameraCaptureSession? = null
    private var mCameraDevice: CameraDevice? = null

    private var mCharacteristics: CameraCharacteristics? = null
    private var mBackgroundHandler: Handler? = null

    private var mImageReader: RefCountedAutoCloseable<ImageReader>? = null

    private var mTakePicture = 0

    private var mBackgroundThread: HandlerThread? = null
    //    private final int mImageFormat = ImageFormat.RAW_SENSOR;

    private var mOrientationListener: OrientationEventListener? = null
    private val mMessageHandler: Handler

    private val commThread: Thread? = null
    private var mProcessamento: Processamento? = null
    private var mFileName: String? = null

    private val mSurfaceTextureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            Log.d(TAG, "onSurfaceTextureAvailable")
            configureTransform(width, height)
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
            Log.d(TAG, "bob onSurfaceTextureSizeChanged")
            configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {}

    }

    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {
        //@Override
        override fun onCaptureStarted(session: CameraCaptureSession, request: CaptureRequest,
                                      timestamp: Long, frameNumber: Long) {
            //            Log.d(TAG, "bob onCaptureStarted");

        }

        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest,
                                        result: TotalCaptureResult) {
            // bob
            //            Log.d(TAG, "bob onCaptureCompleted");
        }

        override fun onCaptureFailed(session: CameraCaptureSession, request: CaptureRequest,
                                     failure: CaptureFailure) {
            // bob
            Log.d(TAG, "bob onCaptureFailed")
            showToast("Capture failed!")
        }
    }

    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        var _image = reader.acquireLatestImage()
//        val bytes = _image.ToByteArray()
//        val f: File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath, "frameO.jpeg");
//        //here,maybe null, so trycatch
//        try {
//                f.createNewFile();
//                //Convert bitmap to byte array
//                //write the bytes in file
//                val fos: FileOutputStream = FileOutputStream(f)
//                fos.write(bytes);
//                fos.flush();
//                fos.close();
//
//        } catch (e: Exception) {
//            Log.i("Exception e", "ImageFormat.JPEG,,,,,,,,Exception eException e");
//            e.getStackTrace();
//        }
//        finally {
//            _image.close()
//            _image = reader.acquireLatestImage()
//        }

        try {

            val result = ""

            if (_image == null) {
                return@OnImageAvailableListener
            }
            if (mTakePicture == 1) {
                //result = JNIUtils.detectLane(image, mSurface, mFileName, mTakePicture);
                mTakePicture = 0
            } else {
                if (!processando) {
                    processando = true

                    val Y = _image.planes[0]
                    val U = _image.planes[1]
                    val V = _image.planes[2]

                    val Yb = Y.buffer.remaining()
                    val Ub = U.buffer.remaining()
                    val Vb = V.buffer.remaining()

                    val data = ByteArray(Yb + Ub + Vb)

                    Y.buffer.get(data, 0, Yb)
                    U.buffer.get(data, Yb, Ub)
                    V.buffer.get(data, Yb + Ub, Vb)


                    //conversor(_image.width,_image.height, data)
                    val mat = _image.ConvertYuvToRgb()
                    // val mat_y = Mat(org.opencv.core.Size(_image.width.toDouble(), _image.height.toDouble()), CvType.CV_8UC1)
                    //mat_y.put(0,0,data);
                    org.opencv.imgcodecs.Imgcodecs.imwrite(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/original_y.jpg", mat)

                    //val rgbMat = Mat(_image.width,_image.height, CvType.CV_8UC3)
                    //Imgproc.cvtColor(mat_y, rgbMat, Imgproc.COLOR_YUV2RGB_NV21, 3)

                    // Convert YUV matrix to BGR matrix
                    //Imgproc.cvtColor(mat, mat, Imgproc.COLOR_YUV2BGRA_NV21);
                    // Flip width and height then mirror vertically
                    //Core.transpose(mat, mat);
                    //Core.rotate(rgbMat, rgbMat, Core.ROTATE_90_CLOCKWISE)
                    //org.opencv.imgcodecs.Imgcodecs.imwrite(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + "/_original.jpg", rgbMat)                    //Core.flip(mat, mat,  Core.ROTATE_90_CLOCKWISE)
//                    mat_y.put(0, 0, bytes);

//                    Imgproc.cvtColor(mat_y, mat, Imgproc.COLOR_YUV2BGR);
//
//                    mat_y.release()
                    // enviarRequest(TipoProcessamento.PREVIEW, mat)

                }
                //result = JNIUtils.detectLane(image, mSurface, mFileName, mTakePicture);
            }

            //comm.send_lane(result);
            //JNIUtils.blitraw(image, mSurface);
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Too many images queued for saving, dropping image for request: ")
            //                        entry.getKey());
            //                pendingQueue.remove(entry.getKey());
            return@OnImageAvailableListener
        } finally {
            _image!!.close()
        }

    }

    /* Checks if external storage is available for read and write */
    val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    /* Checks if external storage is available to at least read */
    val isExternalStorageReadable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state || Environment.MEDIA_MOUNTED_READ_ONLY == state
        }

    init {
        mMessageHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                val activity = activity
                if (activity != null) {
                    Toast.makeText(activity, msg.obj as String, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createCameraPreviewSession() {
        val mPreviewRequestBuilder: CaptureRequest.Builder
        try {
            val texture = mSurfaceView!!.surfaceTexture
            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)

            // This is the output Surface we need to start preview.
            mSurface = Surface(texture)

            Log.d(TAG, "create CameraPreviewSession")

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewRequestBuilder.addTarget(mSurface!!)
            mPreviewRequestBuilder.addTarget(mImageReader!!.get()!!.surface)

            val sessionCallback = BlockingSessionCallback()

            val outputSurfaces = ArrayList<Surface>()
            outputSurfaces.add(mImageReader!!.get()!!.surface)
            outputSurfaces.add(mSurface!!)

            mCameraDevice!!.createCaptureSession(outputSurfaces, sessionCallback, mBackgroundHandler)

            try {
                Log.d(TAG, "waiting on session.")
                mCaptureSession = sessionCallback.waitAndGetSession(SESSION_WAIT_TIMEOUT_MS.toLong())
                try {
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)

                    // If there is an auto-magical flash control mode available, use it, otherwise default to
                    // the "on" mode, which is guaranteed to always be available.
                    if (contains(mCharacteristics!!.get(
                                    CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES),
                                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)) {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                    } else {
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                CaptureRequest.CONTROL_AE_MODE_ON)
                    }

                    // If there is an auto-magical white balance control mode available, use it.
                    if (contains(mCharacteristics!!.get(
                                    CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES),
                                    CaptureRequest.CONTROL_AWB_MODE_AUTO)) {
                        // Allow AWB to run auto-magically if this device supports this
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE,
                                CaptureRequest.CONTROL_AWB_MODE_AUTO)
                    }
                    // If there is an auto-magical white balance control mode available, use it.
                    if (contains(mCharacteristics!!.get(
                                    CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES),
                                    CaptureRequest.CONTROL_AWB_MODE_AUTO)) {
                        // Allow AWB to run auto-magically if this device supports this
                        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE,
                                CaptureRequest.CONTROL_AWB_MODE_AUTO)
                    }
                    //                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                    // Comment out the above and uncomment this to disable continuous autofocus and
                    // instead set it to a fixed value of 20 diopters. This should make the picture
                    // nice and blurry for denoised edge detection.
                    // mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    //		   CaptureRequest.CONTROL_AF_MODE_OFF);
                    // mPreviewRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, 20.0f);
                    // Finally, we start displaying the camera preview.

                    Log.d(TAG, "setting repeating request")

                    mCaptureSession!!.setRepeatingRequest(mPreviewRequestBuilder.build(),
                            mCaptureCallback, mBackgroundHandler)
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }

            } catch (e: TimeoutRuntimeException) {
                showToast("Failed to configure capture session.")
            }

        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_camera2_basic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<View>(R.id.picture).setOnClickListener(this)

        mSurfaceView = view.findViewById<View>(R.id.texture) as scan.lucas.com.docscan.AutoFitTextureView
        //		mSurfaceView.setAspectRatio(DESIRED_IMAGE_READER_SIZE.getWidth(),
        //                DESIRED_IMAGE_READER_SIZE.getHeight());
        // This must be called here, before the initial buffer creation.
        // Putting this inside surfaceCreated() is insufficient.

        mOrientationListener = object : OrientationEventListener(activity,
                SensorManager.SENSOR_DELAY_NORMAL) {
            override fun onOrientationChanged(orientation: Int) {
                if (mSurfaceView != null && mSurfaceView!!.isAvailable) {
                    Log.d(TAG, "OnViewCreated")
                    configureTransform(mSurfaceView!!.width, mSurfaceView!!.height)
                }
            }
        }

        //comm.start();

        val act = activity
        //File f = act.getApplicationContext().getFilesDir();
        //  File f = getActivity().getExternalFilesDir(null);
        if (isExternalStorageWritable) {
            Log.d(TAG, "bob writable")
        } else {
            Log.d(TAG, "bob NOT writable")
        }

        //        File fp = new File(act.getExternalFilesDir(null), "DemoFile.jpg");

        //        mFile = new File(getActivity().getExternalFilesDir(null), "pic.jpg");

        /*val file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);//.getAbs??olutePath();
        var fp = File(file, "DemoFile2.dat");

        var fList = file.listFiles();
        for (f in fList) {
            if (f.isFile()) {
                Log.d(TAG, "bob file: " + f.toString());
            } else if (file.isDirectory()) {
                Log.d(TAG, "bob dir: " + f.getAbsolutePath());
            }
        }
        mFileName = file.toString();


        var str = fp.toString();

        var data = byteArrayOf(100)
        try {
            val os = FileOutputStream(fp);
            var j = 0;
            for (i in data) {
                data[j] = 0x33;
                j++;
            }
            os.write(data);
            os.close();
            Log.d(TAG, "bob file dir: " + str);
        } catch (e: Exception) {
            // Unable to create file, likely because external storage is
            // not currently mounted.
            Log.w("ExternalStorage", "Error writing " + fp, e);
        }*/
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()

        // Make the SurfaceView VISIBLE so that on resume, surfaceCreated() is called,
        // and on pause, surfaceDestroyed() is called.

        //        openCamera(mSurfaceView.getWidth(), mSurfaceView.getHeight());
        openCamera()

        if (mSurfaceView!!.isAvailable) {
            Log.d(TAG, "onResume")
            configureTransform(mSurfaceView!!.width, mSurfaceView!!.height)
        } else {
            mSurfaceView!!.surfaceTextureListener = mSurfaceTextureListener
        }

        if (mOrientationListener != null && mOrientationListener!!.canDetectOrientation()) {
            mOrientationListener!!.enable()
        }
        if (mProcessamento == null) {
            mProcessamento = Processamento(mBackgroundThread!!.looper, Handler(), this.context as Context)
        }
    }

    override fun onPause() {
        if (mOrientationListener != null) {
            mOrientationListener!!.disable()
        }
        closeCamera()
        stopBackgroundThread()

        super.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSIONS) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    showMissingPermissionError()
                    return
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.picture -> tirarFoto()
        }
    }

    private fun tirarFoto() {
        Log.d(TAG, "click")
        mTakePicture = 1
    }

    private fun enviarRequest(t: TipoProcessamento, obj: Any) {
        val msg = mProcessamento!!.obtainMessage()
        var _object = ImageMessage(t, obj, mCameraSize.width, mCameraSize.height)
        msg.obj = _object
        mProcessamento!!.sendMessage(msg)
    }

    private fun showMissingPermissionError() {
        val activity = activity
        if (activity != null) {
            Toast.makeText(activity, R.string.request_permission, Toast.LENGTH_SHORT).show()
            activity.finish()
        }
    }


    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread!!.start()
        synchronized(mStateLock) {
            mBackgroundHandler = Handler(mBackgroundThread!!.looper)
        }
    }

    private fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            synchronized(mStateLock) {
                mBackgroundHandler = null
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    private fun setUpCameraOutputs(): Boolean {
        val activity = activity
        val manager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            // Find a CameraDevice that supports RAW captures, and configure state.
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // We only use a camera that supports RAW in this sample.
                //if (!contains(characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES),
                //		CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)) {
                //	continue;
                //}

                val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        ?: throw AssertionError()

                val of = map.outputFormats
                for (i in of.indices) {
                    Log.d(TAG, "output:" + i + " " + of[i])
                }
                val sizes = map.getOutputSizes(mImageFormat).toMutableList()
                Collections.sort(sizes) { lhs, rhs -> java.lang.Double.valueOf(rhs.width.toDouble() * rhs.height.toDouble()).compareTo(lhs.width.toDouble() * lhs.height.toDouble()) }
                sizes.removeAt(0)
                for (i in sizes.indices) {
                    Log.d(TAG, "OutputSizes:" + i + " " + sizes[i].width + " x " + sizes[i].height)
                }


                val largestSizeCamera = Collections.max(sizes, scan.lucas.com.docscan.CompareSizesByArea())
                //Size largest = Collections.max(Arrays.asList(map.getOutputSizes(mImageFormat)),new CompareSizesByArea());
                //Size largest = new Size(1080,1440);
                mCameraSize = largestSizeCamera

                synchronized(mStateLock) {
                    // Set up ImageReaders for JPEG and RAW outputs.  Place these in a reference
                    // counted wrapper to ensure they are only closed when all background tasks
                    // using them are finished.

                    Log.d(TAG, "Image size:" + mCameraSize.width + "x" + mCameraSize.height)
                    if (mImageReader == null || mImageReader!!.andRetain == null) {
                        mImageReader = RefCountedAutoCloseable(
                                ImageReader.newInstance(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, mImageFormat, 2))
                    }
                    mImageReader!!.get()!!.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler)
                    mCharacteristics = characteristics

                    mCameraId = cameraId
                }
                return true
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        // If we found no suitable cameras for capturing RAW, warn the user.
        //ErrorDialog.buildErrorDialog("This device doesn't support capturing RAW photos").
        //		show(getFragmentManager(), "dialog");
        return false
    }

    /**
     * Configures the necessary [Matrix] transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val activity = activity
        synchronized(mStateLock) {
            if (null == mSurfaceView || null == activity) {
                return
            }

            val map = mCharacteristics!!.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            // For still image captures, we always use the largest available size.
            //            Size largestJpeg = Collections.max(Arrays.asList(map.getOutputSizes(mImageFormat)), new CompareSizesByArea());

            // Find the rotation of the device relative to the native device orientation.
            val deviceRotation = activity.windowManager.defaultDisplay.rotation
            val displaySize = Point()
            activity.windowManager.defaultDisplay.getSize(displaySize)

            // Find the rotation of the device relative to the camera sensor's orientation.
            val totalRotation = sensorToDeviceRotation(mCharacteristics!!, deviceRotation)

            // Swap the view dimensions for calculation as needed if they are rotated relative to
            // the sensor.
            Log.d(TAG, "rotation:" + deviceRotation + " x " + totalRotation + " size:" + viewWidth + "x" + viewHeight + " disp:" + displaySize.x + "x" + displaySize.y)
            val swappedDimensions = totalRotation == 90 || totalRotation == 270
            val rotatedVW: Int
            val rotatedVH: Int
            var maxPreviewW: Int
            var maxPreviewH: Int

            if (swappedDimensions) {
                rotatedVW = viewHeight
                rotatedVH = viewWidth
                maxPreviewW = displaySize.y
                maxPreviewH = displaySize.x
            } else {
                rotatedVW = viewWidth
                rotatedVH = viewHeight
                maxPreviewW = displaySize.x
                maxPreviewH = displaySize.y

            }

            // Preview should not be larger than display size and 1080p.
            if (maxPreviewW > MAX_PREVIEW_WIDTH) {
                maxPreviewW = MAX_PREVIEW_WIDTH
            }

            if (maxPreviewH > MAX_PREVIEW_HEIGHT) {
                maxPreviewH = MAX_PREVIEW_HEIGHT
            }

            // Find the best preview size for these view dimensions and configured JPEG size.
            assert(map != null)
            val previewSize = chooseOptimalSize(map!!.getOutputSizes(SurfaceTexture::class.java),
                    rotatedVW, rotatedVH, maxPreviewW, maxPreviewH,
                    mCameraSize)

            if (swappedDimensions) {
                mSurfaceView!!.setAspectRatio(previewSize.height, previewSize.width)
            } else {
                mSurfaceView!!.setAspectRatio(previewSize.width, previewSize.height)
            }

            // Find rotation of device in degrees (reverse device orientation for front-facing
            // cameras).
            val cameraFacing: Int?
            assert(mCharacteristics != null)
            cameraFacing = mCharacteristics!!.get(CameraCharacteristics.LENS_FACING)
            assert(cameraFacing != null)

            val rotation = if (cameraFacing == CameraCharacteristics.LENS_FACING_FRONT)
                (360 + ORIENTATIONS.get(deviceRotation)) % 360
            else
                (360 - ORIENTATIONS.get(deviceRotation)) % 360

            val matrix = Matrix()
            val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
            val bufferRect = RectF(0f, 0f, previewSize.height.toFloat(), previewSize.width.toFloat())
            val centerX = viewRect.centerX()
            val centerY = viewRect.centerY()

            // Initially, output stream images from the Camera2 API will be rotated to the native
            // device orientation from the sensor's orientation, and the TextureView will default to
            // scaling these buffers to fill it's view bounds.  If the aspect ratios and relative
            //     in the native device orientation) to the TextureView's dimension.
            //   - Apply a scale-to-fill from the output buffer's rotated dimensions
            //     (i.e. its dimensions in the current device orientation) to the TextureView's
            //     dimensions.
            //   - Apply the rotation from the native device orientation to the current device
            //     rotation.
            if (Surface.ROTATION_90 == deviceRotation || Surface.ROTATION_270 == deviceRotation) {
                bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
                matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
                val scale = Math.max(
                        viewHeight.toFloat() / previewSize.height,
                        viewWidth.toFloat() / previewSize.width)
                matrix.postScale(scale, scale, centerX, centerY)

            }
            matrix.postRotate(rotation.toFloat(), centerX, centerY)
            Log.d(TAG, "rotation:$rotation centerX:$centerX centerY:$centerY")
            mSurfaceView!!.setTransform(matrix)

            // Start or restart the active capture session if the preview was initialized or
            // if its aspect ratio changed significantly.
            if (mPreviewSize == null || !checkAspectsEqual(previewSize, mPreviewSize!!)) {
                mPreviewSize = previewSize
                //                if (mState != STATE_CLOSED) {
                createCameraPreviewSession()
                //                }
            }
            Log.d(TAG, "rotation previewSize:" + mPreviewSize!!.width + " " + mPreviewSize!!.height)
        }
    }

    private fun openCamera() {
        if (!setUpCameraOutputs()) {
            return
        }
        if (!hasAllPermissionsGranted()) {
            //requestCameraPermissions();
            return
        }

        val activity = activity
        val manager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val blockingManager = BlockingCameraManager(manager)
        try {
            val mDeviceCallback = object : BlockingStateCallback() {

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    mCameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    mCameraDevice = null
                    val activity = getActivity()
                    activity?.finish()
                }
            }

            mCameraDevice = blockingManager.openCamera(mCameraId, mDeviceCallback, mBackgroundHandler)

        } catch (e: BlockingOpenException) {
            showToast("Timed out opening camera.")
        } catch (e: TimeoutRuntimeException) {
            showToast("Timed out opening camera.")
        } catch (e: CameraAccessException) {
            showToast("Failed to open camera.") // failed immediately.
        }

    }


    private fun closeCamera() {
        try {
            mCameraOpenCloseLock.acquire()
            synchronized(mStateLock) {

                // Reset state and clean up resources used by the camera.
                // Note: After calling this, the ImageReaders will be closed after any background
                // tasks saving Images from these readers have been completed.
                if (null != mCaptureSession) {
                    mCaptureSession!!.close()
                    mCaptureSession = null
                }
                if (null != mCameraDevice) {
                    mCameraDevice!!.close()
                    mCameraDevice = null
                }
                if (null != mImageReader) {
                    mImageReader!!.close()
                    mImageReader = null
                }
            }
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            mCameraOpenCloseLock.release()
        }
    }

    class ErrorDialog : DialogFragment() {

        private var mErrorMessage: String? = null

        init {
            mErrorMessage = "Unknown error occurred!"
        }

        override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
            val activity = activity
            return AlertDialog.Builder(activity)
                    .setMessage(mErrorMessage)
                    .setPositiveButton(android.R.string.ok) { dialogInterface, i -> activity.finish() }
                    .create()
        }

        companion object {

            // Build a dialog with a custom message (Fragments require default constructor).
            fun buildErrorDialog(errorMessage: String): ErrorDialog {
                val dialog = ErrorDialog()
                dialog.mErrorMessage = errorMessage
                return dialog
            }
        }
    }

    class RefCountedAutoCloseable<T : AutoCloseable>
    /**
     * Wrap the given object.
     *
     * @param object an object to wrap.
     */
    (private var mObject: T?) : AutoCloseable {
        private var mRefCount: Long = 0

        val andRetain: T?
            @Synchronized get() {
                if (mRefCount < 0) {
                    return null
                }
                mRefCount++
                return mObject
            }

        init {
            if (mObject == null) throw NullPointerException()
        }

        /**
         * Return the wrapped object.
         *
         * @return the wrapped object, or null if the object has been released.
         */
        @Synchronized
        fun get(): T? {
            return mObject
        }

        /**
         * Decrement the reference count and release the wrapped object if there are no other
         * users retaining this object.
         */
        @Synchronized
        override fun close() {
            if (mRefCount >= 0) {
                mRefCount--
                if (mRefCount < 0) {
                    try {
                        mObject!!.close()
                    } catch (e: Exception) {
                        throw RuntimeException(e)
                    } finally {
                        mObject = null
                    }
                }
            }
        }
    }

    /**
     * Rotation need to transform from the camera sensor orientation to the device's current
     * orientation.
     *
     * @param c                 the [CameraCharacteristics] to query for the camera sensor
     * orientation.
     * @param deviceOrientation the current device orientation relative to the native device
     * orientation.
     * @return the total rotation from the sensor orientation to the current device orientation.
     */
    private fun sensorToDeviceRotation(c: CameraCharacteristics, deviceOrientation: Int): Int {
        var deviceOrientation = deviceOrientation
        assert(c != null)
        val sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

        val deviceOrt = ORIENTATIONS.get(deviceOrientation)

        deviceOrientation = deviceOrt
        // Reverse device orientation for front-facing cameras
        val facing = c.get(CameraCharacteristics.LENS_FACING)!!
        if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
            deviceOrientation = -deviceOrt
        }

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        return (sensorOrientation + deviceOrientation + 360) % 360
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun requestCameraPermissions() {
        if (shouldShowRationale()) {
            //PermissionConfirmationDialog.newInstance().show(getChildFragmentManager(), "dialog");
        } else {
            requestPermissions(CAMERA_PERMISSIONS, REQUEST_CAMERA_PERMISSIONS)
        }
    }

    private fun hasAllPermissionsGranted(): Boolean {
        for (permission in CAMERA_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(activity!!, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * Gets whether you should show UI with rationale for requesting the permissions.
     *
     * @return True if the UI should be shown.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun shouldShowRationale(): Boolean {
        for (permission in CAMERA_PERMISSIONS) {
            if (activity!!.shouldShowRequestPermissionRationale(permission)) {
                return true
            }
        }
        return false
    }

    private fun showToast(text: String) {
        // We show a Toast by sending request message to mMessageHandler. This makes sure that the
        // Toast is shown on the UI thread.
        val message = Message.obtain()
        message.obj = text
        mMessageHandler.sendMessage(message)
    }

    private fun shootSound() {
        val meng = activity!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        val volume = meng!!.getStreamVolume(AudioManager.STREAM_NOTIFICATION)

        if (volume != 0) {
            val som = MediaPlayer.create(this.context, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"))
            som.start()
        }
    }

    external fun yuvToRgb(rgb: Array<Int>, yuv: ByteArray, width: Int, height: Int)
    external fun conversor(width: Int, height: Int, yuv: ByteArray)

    companion object {
        var processando: Boolean = false
        var mPreviewSize: Size? = null
        private val TAG = "DOCSCAN"
        private val SESSION_WAIT_TIMEOUT_MS = 2500
        private val REQUEST_CAMERA_PERMISSIONS = 1

        private val CAMERA_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        private val MAX_PREVIEW_WIDTH = 1920
        private val MAX_PREVIEW_HEIGHT = 1080
        private val ASPECT_RATIO_TOLERANCE = 0.005
        //    private Size mCameraSize = new Size(800,600);
        //    private Size mCameraSize = new Size(1280,960);

        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 0)
            ORIENTATIONS.append(Surface.ROTATION_90, 90)
            ORIENTATIONS.append(Surface.ROTATION_180, 180)
            ORIENTATIONS.append(Surface.ROTATION_270, 270)
        }

        //    private final int mImageFormat = ImageFormat.FLEX_RGB_888;
        private val mImageFormat = ImageFormat.YUV_420_888

        fun newInstance(): Camera2BasicFragment {
            return Camera2BasicFragment()
        }


        /**
         * Given `choices` of `Size`s supported by a camera, choose the smallest one that
         * is at least as large as the respective texture view size, and that is at most as large as the
         * respective max size, and whose aspect ratio matches with the specified value. If such size
         * doesn't exist, choose the largest one that is at most as large as the respective max size,
         * and whose aspect ratio matches with the specified value.
         *
         * @param choices           The list of sizes that the camera supports for the intended output
         * class
         * @param textureViewWidth  The width of the texture view relative to sensor coordinate
         * @param textureViewHeight The height of the texture view relative to sensor coordinate
         * @param maxWidth          The maximum width that can be chosen
         * @param maxHeight         The maximum height that can be chosen
         * @param aspectRatio       The aspect ratio
         * @return The optimal `Size`, or an arbitrary one if none were big enough
         */
        private fun chooseOptimalSize(choices: Array<Size>, textureViewWidth: Int,
                                      textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: Size): Size {
            // Collect the supported resolutions that are at least as big as the preview Surface
            val bigEnough = ArrayList<Size>()
            // Collect the supported resolutions that are smaller than the preview Surface
            val notBigEnough = ArrayList<Size>()
            val w = aspectRatio.width
            val h = aspectRatio.height
            for (option in choices) {
                if (option.width <= maxWidth && option.height <= maxHeight &&
                        option.height == option.width * h / w) {
                    if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                        bigEnough.add(option)
                    } else {
                        notBigEnough.add(option)
                    }
                }
            }

            // Pick the smallest of those big enough. If there is no one big enough, pick the
            // largest of those not big enough.
            if (bigEnough.size > 0) {
                return Collections.min(bigEnough, CompareSizesByArea())
            } else if (notBigEnough.size > 0) {
                return Collections.max(notBigEnough, CompareSizesByArea())
            } else {
                Log.e(TAG, "Couldn't find any suitable preview size")
                return choices[0]
            }
        }

        /**
         * Return true if the given array contains the given integer.
         *
         * @param modes array to check.
         * @param mode  integer to get for.
         * @return true if the array contains the given integer, otherwise false.
         */
        private fun contains(modes: IntArray?, mode: Int): Boolean {
            if (modes == null) {
                return false
            }
            for (i in modes) {
                if (i == mode) {
                    return true
                }
            }
            return false
        }

        /**
         * Return true if the two given [Size]s have the same aspect ratio.
         *
         * @param a first [Size] to compare.
         * @param b second [Size] to compare.
         * @return true if the sizes have the same aspect ratio, otherwise false.
         */
        private fun checkAspectsEqual(a: Size, b: Size): Boolean {
            val aAspect = a.width / a.height.toDouble()
            val bAspect = b.width / b.height.toDouble()
            return Math.abs(aAspect - bAspect) <= ASPECT_RATIO_TOLERANCE
        }
    }
}
