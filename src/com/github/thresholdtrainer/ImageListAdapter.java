package com.github.thresholdtrainer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class ImageListAdapter extends PagerAdapter {
	public static String TAG="Images";
	private final FragmentManager mFragmentManager;
	private FragmentTransaction mCurTransaction = null;
	Map<Integer, Fragment> fragments = new HashMap<Integer, Fragment>();
	List<File> images = new ArrayList<File>();
	public ImageListAdapter(FragmentManager fm) {
		mFragmentManager = fm;
		for (File image : new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Camera").listFiles())
		{
			if (image.isFile() && image.getName().endsWith(".jpg"))
			{
				images.add(image);
			}
		}
	}

	@Override
	public int getCount() {
		return images.size();
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		Log.i(TAG, "Destroying image: " + images.get(position).getName());
		if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }
		Fragment f = fragments.remove(position);
		mCurTransaction.remove(f);
	}

	@Override
	public void finishUpdate(ViewGroup container) {
		super.finishUpdate(container);
	}

	@Override
	public Object instantiateItem(View container, int position) {
		Log.i(TAG, "Creating image: " + images.get(position).getName());
		if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }		ImageFragment fragment = new ImageFragment(images.get(position));
		fragments.put(position, fragment);
		mCurTransaction.add(container.getId(), fragment);
		return fragment;
	}

	@Override
	public void startUpdate(ViewGroup container) {
		super.startUpdate(container);
	}

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		return ((Fragment)obj).getView() == view;
	}
	
    @Override
    public void finishUpdate(View container) {
        if (mCurTransaction != null) {
            mCurTransaction.commit();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }
}
