#include <jni.h>
#include <string>

#include <iostream>
#include <fstream>

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
    findContours(threshed, , cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
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

void writeMatToFile(const char *filename, cv::Mat &m) {
    ofstream fout(filename);

    if (!fout) {
        cout << "File Not Opened" << endl;
        return;
    }

    for (int i = 0; i < m.rows; i++) {
        for (int j = 0; j < m.cols; j++) {
            fout << m.at<float>(i, j) << "\t";
        }
        fout << endl;
    }

    fout.close();
}

