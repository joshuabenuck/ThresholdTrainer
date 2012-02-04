package com.github.thresholdtrainer;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageFragment extends Fragment {
	File image;
	
	public ImageFragment(File image) {
		this.image = image;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i("ImageFragment", "Creating view for: " + image.getName());
		ImageView view = new ImageView(container.getContext());
		Bitmap bm = BitmapFactory.decodeFile(image.getAbsolutePath());
		view.setImageBitmap(bm);
		return view;
	}
	
}
