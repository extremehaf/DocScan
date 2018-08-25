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

package scan.lucas.com.docscan.Utils

import android.graphics.*
import android.media.Image
import android.support.v4.app.FragmentActivity
import android.util.Base64
import android.util.Log
import android.widget.Toast
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer


/**
 * This file illustrates Kotlin's Extension Functions by extending FragmentActivity.
 */

/**
 * Shows a [Toast] on the UI thread.
 *
 * @param text The message to show
 */
fun FragmentActivity.showToast(text: String) {
    runOnUiThread { Toast.makeText(this, text, Toast.LENGTH_SHORT).show() }
}

fun Bitmap.ToByteArray(): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}

fun ByteArray.ToBase64(): String {
    return Base64.encodeToString(this, Base64.DEFAULT)
}

fun ByteArray.ToBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}

fun String.Base64ToBitMap(): Bitmap {
    val decodedString = Base64.decode(this, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
}

fun Image.ToMat(): Mat {
    var buffer: ByteBuffer
    var rowStride: Int
    var pixelStride: Int
    val width = this.width
    val height = this.height
    var offset = 0

    val planes = this.planes
    var data = ByteArray(this.width * this.height * 3 / 2)// ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8];
    var rowData = ByteArray(planes[0].rowStride)

    for (i in 0 until planes.size) {
        buffer = planes[i].buffer
        rowStride = planes[i].rowStride
        pixelStride = planes[i].pixelStride
        val w = if (i == 0) width else width / 2
        val h = if (i == 0) height else height / 2
        for (row in 0 until h) {
            val bytesPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8
            if (pixelStride === bytesPerPixel) {
                val length = w * bytesPerPixel
                buffer.get(data, offset, length)

                if (h - row != 1) {
                    buffer.position(buffer.position() + rowStride - length)
                }
                offset += length
            } else {


                if (h - row == 1) {
                    buffer.get(rowData, 0, width - pixelStride + 1)
                } else {
                    buffer.get(rowData, 0, rowStride)
                }

                for (col in 0 until w) {
                    data[offset++] = rowData[col * pixelStride]
                }
            }
        }
    }

    val mat = Mat(height + height / 2, width, CvType.CV_8UC1)
    mat.put(0, 0, data)
    return mat
}

//fun Image.ToMat(): Mat {
//    try {
//        val mat = Mat(this.height, this.width, CvType.CV_8UC1)
//
//        val bytes = this.ToByteArray()
//        mat.put(0, 0, bytes)
//        return mat
//    } catch (e: IOException) {
//        Log.e("Image.ToMat IO Erro", e.toString())
//        return Mat()
//    }
//    catch (e: Exception) {
//        Log.e("Extension Image.ToMat", e.toString())
//        return Mat()
//    }
//}
fun Image.ConvertYuvToRgb(): Mat {
    val planes = this.planes

    val imageData = ByteArray(this.width * this.height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8)

    var buffer = planes[0].buffer
    var lastIndex = buffer.remaining()
    buffer.get(imageData, 0, lastIndex)
    val pixelStride = planes[1].pixelStride

    for (i in 1 until planes.size) {
        buffer = planes[i].buffer
        val planeData = ByteArray(buffer.remaining())
        buffer.get(planeData)

        var j = 0
        while (j < planeData.size) {
            imageData[lastIndex++] = planeData[j]
            j += pixelStride
        }
    }

    val yuvMat = Mat(this.height + this.height / 2, this.width, CvType.CV_8UC1)
    yuvMat.put(0, 0, imageData)

    val rgbMat = Mat()
    Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV420p2RGBA)
    yuvMat.release()
    return rgbMat
}

fun Image.convertYuv420888ToMat(): Mat {
    val width = this.width
    val height = this.height

    val yPlane = this.planes[0]
    val ySize = yPlane.buffer.remaining()

    val uPlane = this.planes[1]
    val vPlane = this.planes[2]

    // be aware that this size does not include the padding at the end, if there is any
    // (e.g. if pixel stride is 2 the size is ySize / 2 - 1)
    val uSize = uPlane.buffer.remaining()
    val vSize = vPlane.buffer.remaining()

    val data = ByteArray(ySize + ySize / 2)

    yPlane.buffer.get(data, 0, ySize)

    val ub = uPlane.buffer
    val vb = vPlane.buffer

    val uvPixelStride = uPlane.pixelStride //stride guaranteed to be the same for u and v planes
    if (uvPixelStride == 1) {
        uPlane.buffer.get(data, ySize, uSize)
        vPlane.buffer.get(data, ySize + uSize, vSize)

        val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
        yuvMat.put(0, 0, data)
        val rgbMat = Mat(height, width, CvType.CV_8UC3)
        Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_I420, 3)
        yuvMat.release()
        return rgbMat
    }

    // if pixel stride is 2 there is padding between each pixel
    // converting it to NV21 by filling the gaps of the v plane with the u values
    vb.get(data, ySize, vSize)
    var i = 0
    while (i < uSize) {
        data[ySize + i + 1] = ub.get(i)
        i += 2
    }

    val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
    yuvMat.put(0, 0, data)
    val rgbMat = Mat(height, width, CvType.CV_8UC3)
    Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2RGB_NV21, 3)
    yuvMat.release()
    return rgbMat
}

fun Image.ToBitMap(): Bitmap? {
    try {
        val file: File? = null
        val width = this.width
        val height = this.height

        val buffer = this.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.count())
    } catch (e: IOException) {
        Log.e("Image.ToMat IO Erro", e.toString())
        return null
    } catch (e: Exception) {
        Log.e("Extension Image.ToMat", e.toString())
        return null
    }
}

fun Image.ToByteArray(): ByteArray? {
    var data: ByteArray? = null
    if (this.format === ImageFormat.JPEG) {
        val planes = this.planes
        val buffer = planes[0].buffer
        data = ByteArray(buffer.capacity())
        buffer.get(data)
        return data
    } else if (this.format === ImageFormat.YUV_420_888) {
        data = NV21toJPEG(
                YUV_420_888toNV21(this),
                this.width, this.height)
    }
    return data
}

private fun YUV_420_888toNV21(image: Image): ByteArray {
    val nv21: ByteArray
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    nv21 = ByteArray(ySize + uSize + vSize)

    //U and V are swapped
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    return nv21
}

private fun NV21toJPEG(nv21: ByteArray, width: Int, height: Int): ByteArray {
    val out = ByteArrayOutputStream()
    val yuv = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    yuv.compressToJpeg(Rect(0, 0, width, height), 100, out)
    return out.toByteArray()
}


