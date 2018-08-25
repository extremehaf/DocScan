
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.calib3d;


// C++: class StereoSGBM
//javadoc: StereoSGBM
public class StereoSGBM extends StereoMatcher {

    public static final int
            MODE_SGBM = 0,
            MODE_HH = 1,
            MODE_SGBM_3WAY = 2,
            MODE_HH4 = 3;


    protected StereoSGBM(long addr) {
        super(addr);
    }


    //
    // C++: static Ptr_StereoSGBM create(int minDisparity = 0, int numDisparities = 16, int blockSize = 3, int P1 = 0, int P2 = 0, int disp12MaxDiff = 0, int preFilterCap = 0, int uniquenessRatio = 0, int speckleWindowSize = 0, int speckleRange = 0, int mode = StereoSGBM::MODE_SGBM)
    //

    //javadoc: StereoSGBM::create(minDisparity, numDisparities, blockSize, P1, P2, disp12MaxDiff, preFilterCap, uniquenessRatio, speckleWindowSize, speckleRange, mode)
    public static StereoSGBM create(int minDisparity, int numDisparities, int blockSize, int P1, int P2, int disp12MaxDiff, int preFilterCap, int uniquenessRatio, int speckleWindowSize, int speckleRange, int mode) {

        StereoSGBM retVal = new StereoSGBM(create_0(minDisparity, numDisparities, blockSize, P1, P2, disp12MaxDiff, preFilterCap, uniquenessRatio, speckleWindowSize, speckleRange, mode));

        return retVal;
    }

    //javadoc: StereoSGBM::create()
    public static StereoSGBM create() {

        StereoSGBM retVal = new StereoSGBM(create_1());

        return retVal;
    }


    //
    // C++:  int getMode()
    //

    // C++: static Ptr_StereoSGBM create(int minDisparity = 0, int numDisparities = 16, int blockSize = 3, int P1 = 0, int P2 = 0, int disp12MaxDiff = 0, int preFilterCap = 0, int uniquenessRatio = 0, int speckleWindowSize = 0, int speckleRange = 0, int mode = StereoSGBM::MODE_SGBM)
    private static native long create_0(int minDisparity, int numDisparities, int blockSize, int P1, int P2, int disp12MaxDiff, int preFilterCap, int uniquenessRatio, int speckleWindowSize, int speckleRange, int mode);


    //
    // C++:  int getP1()
    //

    private static native long create_1();


    //
    // C++:  int getP2()
    //

    // C++:  int getMode()
    private static native int getMode_0(long nativeObj);


    //
    // C++:  int getPreFilterCap()
    //

    // C++:  int getP1()
    private static native int getP1_0(long nativeObj);


    //
    // C++:  int getUniquenessRatio()
    //

    // C++:  int getP2()
    private static native int getP2_0(long nativeObj);


    //
    // C++:  void setMode(int mode)
    //

    // C++:  int getPreFilterCap()
    private static native int getPreFilterCap_0(long nativeObj);


    //
    // C++:  void setP1(int P1)
    //

    // C++:  int getUniquenessRatio()
    private static native int getUniquenessRatio_0(long nativeObj);


    //
    // C++:  void setP2(int P2)
    //

    // C++:  void setMode(int mode)
    private static native void setMode_0(long nativeObj, int mode);


    //
    // C++:  void setPreFilterCap(int preFilterCap)
    //

    // C++:  void setP1(int P1)
    private static native void setP1_0(long nativeObj, int P1);


    //
    // C++:  void setUniquenessRatio(int uniquenessRatio)
    //

    // C++:  void setP2(int P2)
    private static native void setP2_0(long nativeObj, int P2);

    // C++:  void setPreFilterCap(int preFilterCap)
    private static native void setPreFilterCap_0(long nativeObj, int preFilterCap);

    // C++:  void setUniquenessRatio(int uniquenessRatio)
    private static native void setUniquenessRatio_0(long nativeObj, int uniquenessRatio);

    // native support for java finalize()
    private static native void delete(long nativeObj);

    //javadoc: StereoSGBM::getMode()
    public int getMode() {

        int retVal = getMode_0(nativeObj);

        return retVal;
    }

    //javadoc: StereoSGBM::setMode(mode)
    public void setMode(int mode) {

        setMode_0(nativeObj, mode);

        return;
    }

    //javadoc: StereoSGBM::getP1()
    public int getP1() {

        int retVal = getP1_0(nativeObj);

        return retVal;
    }

    //javadoc: StereoSGBM::setP1(P1)
    public void setP1(int P1) {

        setP1_0(nativeObj, P1);

        return;
    }

    //javadoc: StereoSGBM::getP2()
    public int getP2() {

        int retVal = getP2_0(nativeObj);

        return retVal;
    }

    //javadoc: StereoSGBM::setP2(P2)
    public void setP2(int P2) {

        setP2_0(nativeObj, P2);

        return;
    }

    //javadoc: StereoSGBM::getPreFilterCap()
    public int getPreFilterCap() {

        int retVal = getPreFilterCap_0(nativeObj);

        return retVal;
    }

    //javadoc: StereoSGBM::setPreFilterCap(preFilterCap)
    public void setPreFilterCap(int preFilterCap) {

        setPreFilterCap_0(nativeObj, preFilterCap);

        return;
    }

    //javadoc: StereoSGBM::getUniquenessRatio()
    public int getUniquenessRatio() {

        int retVal = getUniquenessRatio_0(nativeObj);

        return retVal;
    }

    //javadoc: StereoSGBM::setUniquenessRatio(uniquenessRatio)
    public void setUniquenessRatio(int uniquenessRatio) {

        setUniquenessRatio_0(nativeObj, uniquenessRatio);

        return;
    }

    @Override
    protected void finalize() throws Throwable {
        delete(nativeObj);
    }

}
