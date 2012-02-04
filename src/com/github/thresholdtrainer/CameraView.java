package com.github.thresholdtrainer;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.SurfaceHolder;

class CameraView extends CvViewBase {
    private Mat mRgba;
    private Mat mGray;
    private Mat mOriginal;
    private Mat mIntermediateMat;
    private Mat mThresholded;

    public CameraView(Context context) {
        super(context);
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        super.surfaceChanged(_holder, format, width, height);

        synchronized (this) {
            // initialize Mats before usage
        	mOriginal = new Mat();
            mGray = new Mat();
            mRgba = new Mat();
            mIntermediateMat = new Mat();
            mThresholded = new Mat();
        }
    }

    @Override
    protected Bitmap processFrame(VideoCapture capture) {
    	Mat display = new Mat();
        capture.retrieve(mOriginal, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
        Imgproc.cvtColor(mOriginal, mIntermediateMat, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_RGB2HSV);
//        Core.inRange(mRgba, new Scalar(230, 50, 170, 0), new Scalar(330, 180, 256, 0), mThresholded);
        Core.inRange(mRgba, new Scalar(150, 0, 0, 0), new Scalar(330, 256, 256, 256), mThresholded);
        
//        capture.retrieve(mGray, Highgui.CV_CAP_ANDROID_GREY_FRAME);
//        Imgproc.Canny(mGray, mOriginal, 80, 100);
        
//        Imgproc.cvtColor(mOriginal, mGray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(mThresholded, mThresholded, new Size(9, 9), 9);
        
//        Imgproc.HoughCircles(mGray, mIntermediateMat, Imgproc.CV_HOUGH_GRADIENT, 2, mGray.width() / 18, 200, 300,0,0);
        
        Imgproc.HoughCircles(mThresholded, mIntermediateMat, Imgproc.CV_HOUGH_GRADIENT, 2, mThresholded.height()/4, 100, 40, 20, 200);
        
        switch (TrainerActivity.viewMode) {
        case TrainerActivity.VIEW_MODE_BALL:
//            Imgproc.cvtColor(mRgba, display, Imgproc.COLOR_GRAY2RGBA, 4);
            Imgproc.cvtColor(mThresholded, display, Imgproc.COLOR_GRAY2RGBA, 4);
            Core.putText(display, "" + mIntermediateMat.rows(), new Point(100, 100), 3, 2, new Scalar(255, 0, 0, 255), 3);
            if (mIntermediateMat.rows() > 0)
            {
            	double[] data = mIntermediateMat.get(0, 0);
                Core.circle(display, new Point(data[0], data[1]), (int)data[2], new Scalar(255, 0, 255), 3, 8, 0);
//                Core.putText(mRgba, data[0] + "," + data[1] + "," + data[2], new Point(100, 200), 3, 2, new Scalar(255, 0, 0, 255), 3);            	
            }            
            break;
        case TrainerActivity.VIEW_MODE_GRAY:
            Imgproc.cvtColor(mOriginal, mGray, Imgproc.COLOR_RGBA2GRAY, 4);
            Imgproc.cvtColor(mGray, display, Imgproc.COLOR_GRAY2RGBA, 4);
            break;
        case TrainerActivity.VIEW_MODE_ORIGINAL:
        	display.release();
        	display = mOriginal;
            break;
        case TrainerActivity.VIEW_MODE_THRESHOLDED:
            Imgproc.cvtColor(mThresholded, display, Imgproc.COLOR_GRAY2RGBA, 4);        	
            break;
        }

        Bitmap bmp = Bitmap.createBitmap(display.cols(), display.rows(), Bitmap.Config.ARGB_8888);

        try {
	        if (Utils.matToBitmap(display, bmp))
	            return bmp;
	
	        bmp.recycle();
        }
        finally {
        	display.release();
        }
        display = null;
        return null;
    }

    @Override
    public void run() {
        super.run();

        synchronized (this) {
            // Explicitly deallocate Mats
            if (mOriginal != null)
                mOriginal.release();
            if (mRgba != null)
                mRgba.release();
            if (mGray != null)
                mGray.release();
            if (mIntermediateMat != null)
                mIntermediateMat.release();

            mOriginal = null;
            mRgba = null;
            mGray = null;
            mIntermediateMat = null;
        }
    }
}
