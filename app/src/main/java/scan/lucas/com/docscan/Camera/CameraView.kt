package scan.lucas.com.docscan.Camera

import android.app.Activity
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.util.Log
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import java.io.IOException


internal class CameraSurfaceTextureListener(private val mActivity: Activity) : SurfaceTextureListener {
    private var mCamera: Camera? = null
    private var mBackCameraInfo: CameraInfo? = null

    private val backCamera: Pair<CameraInfo, Int>?
        get() {
            val cameraInfo = CameraInfo()
            val numberOfCameras = Camera.getNumberOfCameras()

            for (i in 0 until numberOfCameras) {
                Camera.getCameraInfo(i, cameraInfo)
                if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                    return Pair<Camera.CameraInfo, Int>(cameraInfo,
                            Integer.valueOf(i))
                }
            }
            return null
        }

    val isCameraOpen: Boolean
        get() = mCamera != null

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture,
                                             width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        if (mCamera != null) {
            mCamera!!.stopPreview()
            mCamera!!.release()
            mCamera = null
        }
        return true
    }

    override fun onSurfaceTextureAvailable(
            surface: SurfaceTexture,
            width: Int, height: Int) {
        Log.d("!!!!", "onSurfaceTextureAvailable!!!")
        val backCamera = backCamera
        val backCameraId = backCamera!!.second
        mBackCameraInfo = backCamera.first
        mCamera = Camera.open(backCameraId)
        cameraDisplayRotation()

        try {
            mCamera!!.setPreviewTexture(surface)
            mCamera!!.startPreview()
        } catch (ioe: IOException) {
            // Something bad happened
        }

    }

    fun cameraDisplayRotation() {
        val rotation = mActivity.windowManager
                .defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        val displayOrientation = (mBackCameraInfo!!.orientation - degrees + 360) % 360
        mCamera!!.setDisplayOrientation(displayOrientation)
    }
}