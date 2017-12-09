package com.my.targetDemoApp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;

import com.my.targetDemoApp.R;
import com.my.targetDemoApp.fragments.NativeAdFragment;

import java.util.ArrayList;

public class NativeAdActivity extends AdActivity
		implements BottomNavigationView.OnNavigationItemSelectedListener
{
	public static final String TAG = NativeAdActivity.class.getSimpleName();
	private static final String TAG_STATIC_FRAGMENT = "fragment_static";
	private static final String TAG_VIDEO_FRAGMENT = "fragment_video";
	private static final String TAG_SLIDER_FRAGMENT = "fragment_slider";
	private FragmentManager fragmentManager;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_native_ads);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		}

		BottomNavigationView bottomNavigationView = (BottomNavigationView)
				findViewById(R.id.bottom_navigation);

		bottomNavigationView.setOnNavigationItemSelectedListener(this);
		ArrayList<View> touchables = bottomNavigationView.getTouchables();
		for (int i = 0; i < touchables.size(); i++)
		{
			View view = touchables.get(i);
			view.setContentDescription("Bottom_" + i);
		}
		fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		NativeAdFragment nativeAdFragment =
				NativeAdFragment.newInstance(slotId, R.id.action_native_static);
		fragmentTransaction.add(R.id.fragment_container, nativeAdFragment);
		fragmentTransaction.commit();
	}

	@Override
	void reloadAd()
	{
		for (Fragment fragment : getSupportFragmentManager().getFragments())
		{
			if (fragment != null && fragment instanceof NativeAdFragment)
				((NativeAdFragment) fragment).reloadAd();
		}
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item)
	{
		Fragment fragment;
		String tag = null;
		switch (item.getItemId())
		{
			case R.id.action_native_static:
			default:
				tag = TAG_STATIC_FRAGMENT;
				break;
			case R.id.action_native_video:
				tag = TAG_VIDEO_FRAGMENT;
				break;
			case R.id.action_native_slider:
				tag = TAG_SLIDER_FRAGMENT;
				break;
		}
		fragment = fragmentManager.findFragmentByTag(tag);
		if (fragment != null)
		{
			fragmentManager.beginTransaction()
					.replace(R.id.fragment_container, fragment, tag)
					.commit();
		} else
		{
			fragment = NativeAdFragment.newInstance(slotId, item.getItemId());
			fragmentManager.beginTransaction()
					.add(R.id.fragment_container, fragment, tag)
					.commit();
		}
		return true;
	}

	/**
	 * Overridden to intercept any intent requests from third-party libs (such as MyTarget SDK)
	 * to check if we could handle them ourselves directly. Overriding this method since the
	 * default implementation of the other flavors of startActivity[ForResult] is to eventually
	 * end up calling this method.
	 */
	@Override
	public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
		if (handleIntent(intent)) return;
		super.startActivityForResult(intent, requestCode, options);
	}

	private boolean handleIntent(@Nullable final Intent intent) {
		if (intent == null) return false;
		if (intent.getCategories() == null) {
			// Intercept ad related intents and open them inside our browser when possible
			if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getDataString() != null
					&& intent.getComponent() == null) {
				final String url = intent.getDataString();
				if (URLUtil.isNetworkUrl(url)) {
					openInternal(url);
					return true;
				}
			}
			return false;
		}
		boolean browsable = false;
		for (String category : intent.getCategories()) {
			if (Intent.CATEGORY_BROWSABLE.equals(category)) {
				browsable = true;
				break;
			}
		}
		if (browsable
				&& Intent.ACTION_VIEW.equals(intent.getAction())
				&& intent.getDataString() != null
				&& intent.getComponent() == null) {
			final String url = intent.getDataString();
			if (URLUtil.isNetworkUrl(url)) {
				openInternal(url);
				return true;
			}
		}
		return false;
	}

	private void openInternal(final @NonNull String url) {
		// Todo: open with our webview
		Log.i(TAG, "openInternal " + url);
	}
}
