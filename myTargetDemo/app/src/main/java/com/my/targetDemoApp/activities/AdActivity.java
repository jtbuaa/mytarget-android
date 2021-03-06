package com.my.targetDemoApp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.my.targetDemoApp.MainActivity;
import com.my.targetDemoApp.R;
import com.my.targetDemoApp.models.AdvertisingType;

public abstract class AdActivity extends AppCompatActivity
{
	protected int slotId;
	protected int adType;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (getIntent() != null)
		{
			AdvertisingType advertisingType = getIntent().getParcelableExtra(MainActivity.AD_TYPE_TAG);
			if (advertisingType != null)
			{
				slotId = advertisingType.getSlotId();
				adType = advertisingType.getAdType();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.ad_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		if (id == R.id.action_settings)
		{
			reloadAd();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	abstract void reloadAd();

}
