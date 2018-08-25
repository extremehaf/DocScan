#include <jni.h>
#include <string>

#include <iostream>
#include <fstream>

#include <android/native_window.h>
#include <android/native_window_jni.h>

#include "opencv2/opencv.hpp"
#include "opencv2/core.hpp"
#include "opencv2/imgproc.hpp"
#include "opencv2/imgcodecs.hpp"
#include "opencv2/highgui.hpp"
#include "opencv2/imgcodecs/imgcodecs.hpp"
#include "opencv2/imgcodecs/imgcodecs_c.h"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"

//#include "opencv/imgcodecs.hpp"
using namespace cv;
using namespace std;

extern "C"
JNIEXPORT jstring

JNICALL
Java_scan_lucas_com_docscan_MainActivity_stringFromJNI(JNIEnv *env, jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
//JNIEXPORT void
//JNICALL
//Java_scan_lucas_com_docscan_Processamento_getCanny(
//        JNIEnv *env,
//jobject /* this */, cv::Mat *matInput) {
//
//    cv::Mat image  = *(cv::Mat*)matInput;
//
//    cv::Mat gray, dst;
//    cv::Mat *src;
//    cv::Mat edged, detected_edges;
//
//    cv::cvtColor(image,gray, cv::ColorConversionCodes('COLOR_BGR2GRAY'),4);
//    cv::blur(gray, detected_edges, cv::Size(5,5));
//    cv::Canny(detected_edges,detected_edges, 75, 200,3);
//
//    src->copyTo(image, detected_edges);
//
//}
extern "C"
JNIEXPORT void
JNICALL
Java_scan_lucas_com_docscan_Processamento_imageFromJNI(JNIEnv *env, jobject /* this */,
                                                       jlong inputImage, jlong outputImage) {

    cv::Mat &mat = *(cv::Mat *) inputImage;
    cv::Mat &outputMat = *(cv::Mat *) outputImage;
    cv::Mat mat_gray;
    cv::Mat bgr[3]; //h,s,v
    cv::Mat h, s, v;
    cv::cvtColor(mat, mat_gray, COLOR_BGR2HSV);
    cv::split(mat_gray, bgr);

    h = bgr[0];
    s = bgr[1];
    v = bgr[2];
    /*(3)threshold the S channel using adaptive method(`THRESH_OTSU`) or fixed thresh*/
    cv::Mat threshed;
    cv::threshold(s, threshed, 50, 255, THRESH_BINARY_INV);

    vector<int> compression_params;
    compression_params.push_back(IMWRITE_PNG_COMPRESSION);
    compression_params.push_back(9);


    //writeMatToFile("storage/emulated/0/h.png", h, compression_params);
    //writeMatToFile("storage/emulated/0/s.png", s, compression_params);
    //writeMatToFile("storage/emulated/0/v.png", v, compression_params);
    //writeMatToFile("storage/emulated/0/matGray.png", mat_gray, compression_params);
    //writeMatToFile("storage/emulated/0/mat.png", mat, compression_params);
    //(4) find all the external contours on the threshed S

    //vector<vector<Point> > contours0;
    //vector<Vec4i> hierarchy;
    //findContours(threshed,contours0, RETR_TREE, CHAIN_APPROX_SIMPLE);
    //canvas  = img.copy()
    //#cv2.drawContours(canvas, cnts, -1, (0,255,0), 1)

    // sort and choose the largest contour
    /*cnts = sorted(cnts, key = cv2.contourArea)
    cnt = cnts[-1]

    //approx the contour, so the get the corner points
    arclen = cv2.arcLength(cnt, True)
    approx = cv2.approxPolyDP(cnt, 0.02* arclen, True)
    cv2.drawContours(canvas, [cnt], -1, (255,0,0), 1, cv2.LINE_AA)
    cv2.drawContours(canvas, [approx], -1, (0, 0, 255), 1, cv2.LINE_AA)

    // Ok, you can see the result as tag(6)
    cv2.imwrite("detected.png", canvas)*/

}

extern "C"
JNIEXPORT void JNICALL
Java_scan_lucas_com_docscan_CameraActivity_yuvToRgb(JNIEnv *env, jobject /* this */,
                                                    jintArray rgb, jbyteArray yuv420sp, jint width,
                                                    jint height) {
    int *rgbData;
    int rgbDataSize = 0;
    int sz;
    int i;
    int j;
    int Y;
    int Cr = 0;
    int Cb = 0;
    int pixPtr = 0;
    int jDiv2 = 0;
    int R = 0;
    int G = 0;
    int B = 0;
    int cOff;
    int w = width;
    int h = height;
    sz = w * h;

    jboolean isCopy;
    jbyte *yuv = env->GetByteArrayElements(yuv420sp, &isCopy);
    if (rgbDataSize < sz) {
        int tmp[sz];
        rgbData = &tmp[0];
        rgbDataSize = sz;

    }

    for (j = 0; j < h; j++) {
        pixPtr = j * w;
        jDiv2 = j >> 1;
        for (i = 0; i < w; i++) {
            Y = yuv[pixPtr];
            if (Y < 0) Y += 255;
            if ((i & 0x1) != 1) {
                cOff = sz + jDiv2 * w + (i >> 1) * 2;
                Cb = yuv[cOff];
                if (Cb < 0) Cb += 127; else Cb -= 128;
                Cr = yuv[cOff + 1];
                if (Cr < 0) Cr += 127; else Cr -= 128;
            }
            R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);//1.406*~1.403
            if (R < 0) R = 0; else if (R > 255) R = 255;
            G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >> 3) + (Cr >> 4) +
                (Cr >> 5);//
            if (G < 0) G = 0; else if (G > 255) G = 255;
            B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);//1.765~1.770
            if (B < 0) B = 0; else if (B > 255) B = 255;
            rgbData[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
        }
    }
    env->SetIntArrayRegion(rgb, 0, sz, (jint *) &rgbData[0]);

    env->ReleaseByteArrayElements(yuv420sp, yuv, JNI_ABORT);
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_scan_lucas_com_docscan_Camera2BasicFragment_conversor(
        JNIEnv *env, jobject /* this */,
        jint width, jint height,
        jbyteArray YUVFrameData) {
    jbyte *pYUVFrameData = env->GetByteArrayElements(YUVFrameData, 0);


    Mat mNV(height + height / 2, width, CV_8UC1, (unsigned char *) pYUVFrameData);
    Mat mBgr(height, width, CV_8UC3);

    cv::cvtColor(mNV, mBgr, CV_YUV2BGR_I420);

    env->ReleaseByteArrayElements(YUVFrameData, pYUVFrameData, 0);

    return true;
}