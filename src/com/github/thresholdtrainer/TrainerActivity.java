package com.github.thresholdtrainer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.github.thresholdtrainer.HsvRangeFinder.Range;

public class TrainerActivity extends FragmentActivity {
    private static final String TAG             = "Trainer";

    public static final int     VIEW_MODE_BALL  = 0;
    public static final int     VIEW_MODE_ORIGINAL  = 1;
    public static final int     VIEW_MODE_GRAY  = 2;
    public static final int     VIEW_MODE_THRESHOLDED = 3;

    private MenuItem            mItemPreviewBall;
    private MenuItem            mItemPreviewOriginal;
    private MenuItem            mItemPreviewGray;
    private MenuItem            mItemPreviewThresholded;

    public static int           viewMode        = VIEW_MODE_BALL;
    public CameraView mPreview = null;
    ViewPager mPager;
    ImageListAdapter mAdapter;
    final GestureDetector gd = new GestureDetector(new GestureListener());
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private final int SWIPE_MIN_DISTANCE = 120;
        private final int SWIPE_THRESHOLD_VELOCITY = 200;
    
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
           if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
              // Right to left, your code here
        	   onRightLeft();
              return true;
           } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) >  SWIPE_THRESHOLD_VELOCITY) {
              // Left to right, your code here
        	   onLeftRight();
              return true;
           }
           if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
              // Bottom to top, your code here
        	   onBottomTop();
              return true;
           } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
              // Top to bottom, your code here
        	   onTopBottom();
              return true;
           }
           return false;
        }
     }
    
    public void onRightLeft() {
    	Log.i(TAG, "left");
    	showImageAtPosition(1);
    }
    public void onLeftRight() {
    	Log.i(TAG, "right");
    	showImageAtPosition(-1);
    }
    
    public void onBottomTop() {
    }
    
    public void onTopBottom() {
    	toSkip.edit().putBoolean(images.get(currentImage).getName(), true).commit();
    	images.remove(currentImage);
    	showImageAtPosition(0);
    }

    public TrainerActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

	/**
	 * Maximize the application.
	 */
	public void setFullscreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public void setNoTitle() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	/**
	 * Avoid that the screen get's turned off by the system.
	 */
	public void disableScreenTurnOff() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	/**
	 * Set's the orientation to landscape, as this is needed by AndAR.
	 */
	public void setOrientation() {
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	ImageView view;

	List<File> images = new ArrayList<File>();
	public static final String IMAGES_TO_SKIP = "thresholdtrainer.imagestoskip";
	public void loadImageList() {
		for (File image : new File(Environment.getExternalStorageDirectory() + "/" + 
								   Environment.DIRECTORY_DCIM + "/Camera").listFiles())
		{
			if (image.isFile() && image.getName().endsWith(".jpg"))
			{
				if (toSkip.getBoolean(image.getName(), false) == true) continue;
				images.add(image);
			}
		}
	}
	
	int currentImage = 0;
	Mat hsv, rgb;
	double hue, sat, value;
	int x, y, rx1, ry1, rx2, ry2;
	public void showImageAtPosition(int offset)
	{
		currentImage += offset;
		if (currentImage <= 0) currentImage = 0;
		if (currentImage >= images.size()) currentImage = images.size() - 1;
		File image = images.get(currentImage);
		Log.i(TAG, "Showing image: " + image.getName());
		hsv = new Mat();
		rgb = new Mat();
		Mat tmp = new Mat();
		tmp = Highgui.imread(image.getAbsolutePath(), -1);
		Log.i(TAG, "Height: " + tmp.rows() + " Width: " + tmp.cols());
		Imgproc.resize(tmp, hsv, new Size(tmp.cols() / 4, tmp.rows() / 4));
//		Bitmap bm = BitmapFactory.decodeFile(image.getAbsolutePath());
//		Matrix m = new Matrix();
//		m.setRotate(90, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
//		bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
//		Imgproc.cvtColor(mat, tmp, Imgproc.COLOR_RGBA2RGB);
		Imgproc.cvtColor(hsv, hsv, Imgproc.COLOR_BGR2HSV);
//		Mat rot = Imgproc.getRotationMatrix2D(new Point(mat.cols() / 2.0, mat.rows() / 2.0), 270, 1.0);
		Core.transpose(hsv, hsv);
		Core.flip(hsv, hsv, 1);
		Imgproc.cvtColor(hsv, rgb, Imgproc.COLOR_HSV2RGB);
//		Imgproc.warpAffine(mat, tmp, rot, mat.size());
		rf = new HsvRangeFinder();
		updateDisplay();
	}
	
	HsvRangeFinder rf;
	public void updateDisplay() {
		Mat tmp = rgb.clone();
		if (trainingMode && !(x==0 && y==0)) {
			showHsvValues(tmp);
		}
		if (regionMode && !(rx1==0 && ry1== 0)) {
			showHsvValues(tmp);
		}
		if (thresholdMode) {
			Mat tmp2 = new Mat(hsv.size(), CvType.CV_8UC1);
			Mat result = new Mat(hsv.size(), CvType.CV_8UC1);
			Core.inRange(hsv, new Scalar(0, 0, 0), new Scalar(0, 0, 0), result);
			for (Range h : rf.computeRanges(rf.hvalues)) {
//				Core.inRange(hsv, new Scalar(h.min, 0, 0), 
//						new Scalar(h.max, 256, 256), tmp2);
//				Core.bitwise_or(tmp2, result, result);
				for (Range s : rf.computeRanges(rf.svalues)) {
					for (Range v : rf.computeRanges(rf.vvalues)) {
						Core.inRange(hsv, new Scalar(h.min, s.min, v.min), 
								new Scalar(h.max, s.max, v.max), tmp2);
						Core.bitwise_or(tmp2, result, result);
					}
				}
			}
			Imgproc.cvtColor(result, tmp, Imgproc.COLOR_GRAY2RGBA, 4);
		}
		else {
			Imgproc.cvtColor(tmp, tmp, Imgproc.COLOR_RGB2RGBA);
		}
//		Core.mixChannels(Arrays.asList(mat), Arrays.asList(tmp), Arrays.asList(new Integer[] {1, 3}));
		Bitmap bm = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Config.ARGB_8888);
		Utils.matToBitmap(tmp, bm);
		view.setImageBitmap(bm);		
	}
	private Mat showHsvValues(Mat tmp) {
		if (trainingMode) {
			double[] vals = hsv.get(y, x);
			hue = vals[0];
			sat = vals[1];
			value = vals[2];
			rf.record(hue, sat, value);
			Core.putText(tmp, "H: " + hue + " S: " + sat + " V: " + value, 
					new Point(0, 50), 3, 1, new Scalar(255, 0, 0, 255), 2);
			Core.circle(tmp, new Point(x, y), 3, new Scalar(255, 0, 0, 255));
		}
		if (regionMode) {
			Core.putText(tmp, "X1: " + rx1 + " Y1: " + ry1 + " X2: " + rx2 + " Y2: " + ry2, 
					new Point(0, 50), 3, 1, new Scalar(255, 0, 0, 255), 2);			
			Core.rectangle(tmp, new Point(rx1, ry1), new Point(rx2, ry2), new Scalar(255, 0, 0, 255));
		}
		int textY = 100;
		for (Range r : rf.computeRanges(rf.hvalues)) {
			Core.putText(tmp, "Min H: " + r.min + " Max H: " + r.max, 
					new Point(0, textY), 3, 1, new Scalar(255, 0, 0, 255), 2);
			textY+=50;
		}
		for (Range r : rf.computeRanges(rf.svalues)) {
			Core.putText(tmp, "Min S: " + r.min + " Max S: " + r.max, 
					new Point(0, textY), 3, 1, new Scalar(255, 0, 0, 255), 2);
			textY+=50;
		}
		for (Range r : rf.computeRanges(rf.vvalues)) {
			Core.putText(tmp, "Min V: " + r.min + " Max V: " + r.max, 
					new Point(0, textY), 3, 1, new Scalar(255, 0, 0, 255), 2);
			textY+=50;
		}
		return tmp;
	}
	
	boolean trainingMode = false;
	boolean regionMode = false;
	boolean thresholdMode = false;
	ToggleButton region;
	ToggleButton train;
	ToggleButton threshold;
	SharedPreferences toSkip;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    	toSkip = getSharedPreferences(IMAGES_TO_SKIP, 0);
		setFullscreen();
		setOrientation();
		disableScreenTurnOff();
		view = new ImageView(this);
		view.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent e) {
				if (trainingMode && (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_MOVE)) {
		        	x = (int)e.getX(0);
		        	y = (int)e.getY(0);
//		        	Log.i(TAG, "x: " + x + " y: " + y);
		        	updateDisplay();
				}
				if (regionMode) {
					switch(e.getAction()) {
					case MotionEvent.ACTION_DOWN:
						rf = new HsvRangeFinder();
						rx1 = (int) e.getX(0);
						ry1 = (int) e.getY(0);
						rx2 = rx1;
						ry2 = ry1;
						updateDisplay();
						break;
					case MotionEvent.ACTION_MOVE:
						rx2 = (int) e.getX(0);
						ry2 = (int) e.getY(0);
						updateDisplay();
						break;
					case MotionEvent.ACTION_UP:
						for (int i = rx1; i <= rx2; i++) {
							for (int j = ry1; j <= ry2; j++) {
								double[] vals = hsv.get(j, i);
								double h = vals[0];
								double s = vals[1];
								double v = vals[2];
								rf.record(h, s, v);
							}
						}
						updateDisplay();
					}
				}
				if (!trainingMode && !regionMode) gd.onTouchEvent(e);
				return true;
			}
		});
		loadImageList();
		showImageAtPosition(0);

		FrameLayout frame = new FrameLayout(this);

		// Create our Preview view and set it as the content of our activity.
//		mPreview = new CameraView(this); //new CameraView(getApplication(), 640, 480);
//		
//		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
//				LayoutParams.WRAP_CONTENT);
//		params.height = getWindowManager().getDefaultDisplay().getHeight();
//		params.width = (int) (params.height * 4.0 / 2.88);
//
		LinearLayout vidlay = new LinearLayout(getApplication());

		vidlay.setGravity(Gravity.TOP | Gravity.LEFT);
		vidlay.addView(view);
		frame.addView(vidlay);
//
//		// make the glview overlay ontop of video preview
//		mPreview.setZOrderMediaOverlay(false);

//		glview = new GL2CameraViewer(getApplication(), false, 0, 0);
//		glview.setZOrderMediaOverlay(true);

//		LinearLayout gllay = new LinearLayout(getApplication());
//
//		gllay.setGravity(Gravity.CENTER);
//		gllay.addView(glview, params);
//		frame.addView(gllay);

//		ImageButton capture_button = new ImageButton(getApplicationContext());
//		capture_button.setImageDrawable(getResources().getDrawable(
//				android.R.drawable.ic_menu_camera));
//		capture_button.setLayoutParams(new LayoutParams(
//				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//		capture_button.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				captureChess = true;
//
//			}
//		});

		LinearLayout buttons = new LinearLayout(getApplicationContext());
		buttons.setGravity(Gravity.BOTTOM | Gravity.LEFT);
		train = new ToggleButton(getApplicationContext());
		train.setText("Train");
		train.setTextOff("Train");
		train.setTextOn("Stop");
		train.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				trainingMode = isChecked;
				region.setEnabled(!isChecked);
			}
		});
		buttons.addView(train);
		
		region = new ToggleButton(getApplicationContext());
		region.setText("Region");
		region.setTextOff("Region");
		region.setTextOn("Stop");
		region.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				regionMode = isChecked;
				train.setEnabled(!isChecked);
			}
		});
		buttons.addView(region);

		threshold = new ToggleButton(getApplicationContext());
		threshold.setText("Threshold");
		threshold.setTextOff("Threshold");
		threshold.setTextOn("Normal");
		threshold.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				thresholdMode = isChecked;
				updateDisplay();
			}
		});
		buttons.addView(threshold);
//
//		buttons.addView(capture_button);
//
//		Button focus_button = new Button(getApplicationContext());
//		focus_button.setLayoutParams(new LayoutParams(
//				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//		focus_button.setText("Focus");
//		focus_button.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				mPreview.postautofocus(100);
//			}
//		});
//		buttons.addView(focus_button);
//
		frame.addView(buttons);
		setContentView(frame);
//		toasts(DIALOG_OPENING_TUTORIAL);
        
//        setContentView(new CameraView(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu");
        mItemPreviewBall = menu.add("Ball Tracker");
        mItemPreviewOriginal = menu.add("Original");
        mItemPreviewGray = menu.add("Gray");
        mItemPreviewThresholded = menu.add("Thresholded");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "Menu Item selected " + item);
        if (item == mItemPreviewBall)
            viewMode = VIEW_MODE_BALL;
        else if (item == mItemPreviewOriginal)
            viewMode = VIEW_MODE_ORIGINAL;
        else if (item == mItemPreviewGray)
            viewMode = VIEW_MODE_GRAY;
        else if (item == mItemPreviewThresholded)
            viewMode = VIEW_MODE_THRESHOLDED;
        return true;
    }
}
