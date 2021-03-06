package com.my.targetDemoApp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.my.target.ads.InterstitialAd;
import com.my.target.ads.InterstitialAd.InterstitialAdListener;
import com.my.target.ads.instream.InstreamAd;
import com.my.targetDemoApp.fragments.PlusDialogFragment;
import com.my.targetDemoApp.models.AdvertisingType;
import com.my.targetDemoApp.utils.MaterialColors;
import com.my.targetDemoApp.utils.Tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.my.targetDemoApp.AdTypes.AD_TYPE_FULLSCREEN;

public class MainActivity extends AppCompatActivity implements PlusDialogFragment.SaveTypeListener
{
	public static final String AD_TYPE_TAG = "adtype";
	private static final String KEY_STRING_SET = "saved_ads_set";
	private RecyclerView recyclerView;
	private SharedPreferences sharedPreferences;
	private MainAdapter adapter;
	private View loadingView;
	private ArrayList<AdvertisingType> typeList;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		InstreamAd.setDebugMode(true);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		recyclerView = (RecyclerView) findViewById(R.id.main_grid);
		loadingView = findViewById(R.id.loading_view_main);
		initGrid();
	}

	private void initGrid()
	{

		AdvertisingType defaultAd = new AdvertisingType(AdTypes.AD_TYPE_320X50, 0);
		defaultAd.setName(getString(R.string.standard_banners));
		defaultAd.setImageResource(R.drawable.img_banners);
		defaultAd.setDescription(getString(R.string.standard_banners_desc));

		AdvertisingType interstitialAd = new AdvertisingType(AD_TYPE_FULLSCREEN, 0);
		interstitialAd.setName(getString(R.string.interstitial_ads));
		interstitialAd.setImageResource(R.drawable.img_interstitials);
		interstitialAd.setDescription(getString(R.string.interstitial_ads_desc));

		AdvertisingType nativeAd = new AdvertisingType(AdTypes.AD_TYPE_NATIVE, 0);
		nativeAd.setName(getString(R.string.native_ads));
		nativeAd.setImageResource(R.drawable.img_native);
		nativeAd.setDescription(getString(R.string.native_ads_desc));

		AdvertisingType instreamAd = new AdvertisingType(AdTypes.AD_TYPE_INSTREAM,
				DefaultSlots.MIDROLL);
		instreamAd.setName(getString(R.string.instream_ads));
		instreamAd.setImageResource(R.drawable.img_instream);
		instreamAd.setDescription(getString(R.string.instream_ads_desc));

		typeList = new ArrayList<>();
		typeList.add(defaultAd);
		typeList.add(interstitialAd);
		typeList.add(nativeAd);
		typeList.add(instreamAd);

		typeList.addAll(getSavedTypes());

		RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
		itemAnimator.setAddDuration(300);
		itemAnimator.setRemoveDuration(300);
		recyclerView.setItemAnimator(itemAnimator);

		GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),
				getResources().getInteger(R.integer.grid_coloumns));
		recyclerView.setLayoutManager(gridLayoutManager);


		adapter = new MainAdapter(typeList);
		recyclerView.setAdapter(adapter);
	}

	private ArrayList<AdvertisingType> getSavedTypes()
	{
		ArrayList<AdvertisingType> advertisingTypes = new ArrayList<>();

		Set<String> set = sharedPreferences.getStringSet(KEY_STRING_SET, new HashSet<String>());

		for (String s : set)
		{
			String[] sar = TextUtils.split(s, ":");
			AdvertisingType advertisingType = new AdvertisingType(sar[0], sar[1]);
			advertisingTypes.add(advertisingType);
		}
		return advertisingTypes;
	}

	@Override
	public void onSaveType(int adType, int slotId)
	{
		Set<String> set = sharedPreferences.getStringSet(KEY_STRING_SET, new HashSet<String>());
		set.add(adType + ":" + slotId);
		sharedPreferences.edit().remove(KEY_STRING_SET).apply();
		sharedPreferences.edit().putStringSet(KEY_STRING_SET, set).apply();
		AdvertisingType newType = new AdvertisingType(adType, slotId);
		typeList.add(newType);
		adapter.notifyItemInserted(typeList.size() - 1);
	}

	private void removeType(AdvertisingType type)
	{
		if (recyclerView.isAnimating()) return;
		int pos = typeList.indexOf(type);
		typeList.remove(pos);
		adapter.notifyItemRemoved(pos);
		Set<String> set = sharedPreferences.getStringSet(KEY_STRING_SET, new HashSet<String>());
		set.remove(type.getAdType() + ":" + type.getSlotId());
		sharedPreferences.edit().remove(KEY_STRING_SET).apply();
		sharedPreferences.edit().putStringSet(KEY_STRING_SET, set).apply();
	}

	private class MainAdapter extends
			RecyclerView.Adapter<MainAdapter.ViewHolder>
	{
		private final MaterialColors materialColors;
		private List<AdvertisingType> advertisingTypes;

		private MainAdapter(List<AdvertisingType> advertisingTypes)
		{
			this.advertisingTypes = advertisingTypes;
			materialColors = new MaterialColors();
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			View v = LayoutInflater.from(parent.getContext()).inflate(
					R.layout.adtype_card, parent, false);
			return new ViewHolder(v);
		}

		@Override
		public int getItemViewType(int position)
		{
			if (position == getItemCount() - 1)
				return 1;
			else return 0;
		}

		@Override
		public void onBindViewHolder(ViewHolder viewHolder, int position)
		{
			if (position == getItemCount() - 1)
			{
				viewHolder.nameLabel.setText(getString(R.string.custom_title));
				viewHolder.imageView.setImageResource(R.drawable.plus);
				viewHolder.descriptionLabel.setText(getString(R.string.custom_description));
				viewHolder.cardFrame.setBackgroundColor(Color.TRANSPARENT);
			} else
			{
				final AdvertisingType type = advertisingTypes.get(position);
				viewHolder.nameLabel.setText(type.getName());

				int res = type.getImageResource();
				if (res != 0)
				{
					viewHolder.imageView.setVisibility(View.VISIBLE);
					viewHolder.imageView.setImageResource(type.getImageResource());
					viewHolder.slotId.setVisibility(View.GONE);
					viewHolder.removeButton.setVisibility(View.GONE);
					viewHolder.cardFrame.setBackgroundColor(materialColors.get(position));
					viewHolder.descriptionLabel.setText(type.getDescription());
				} else
				{
					viewHolder.slotId.setVisibility(View.VISIBLE);
					viewHolder.imageView.setVisibility(View.GONE);
					viewHolder.nameLabel.setText(AdTypes.getAdTypeName(type.getAdType()));
					viewHolder.slotId.setText(String.valueOf(type.getSlotId()));
					viewHolder.removeButton.setVisibility(View.VISIBLE);
					viewHolder.cardFrame.setBackgroundColor(Color.LTGRAY);
					viewHolder.descriptionLabel.setText(getString(R.string.custom_ad));
					viewHolder.removeButton.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							removeType(type);
						}
					});
				}
			}
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public int getItemCount()
		{
			return advertisingTypes.size() + 1;
		}

		class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
		{
			final ImageView imageView;
			final TextView nameLabel;
			final TextView descriptionLabel;
			final FrameLayout cardFrame;
			final TextView slotId;
			final ImageView removeButton;

			public ViewHolder(View itemView)
			{
				super(itemView);
				imageView = (ImageView) itemView.findViewById(R.id.iv_card_image);
				nameLabel = (TextView) itemView.findViewById(R.id.tv_card_title);
				descriptionLabel = (TextView) itemView.findViewById(R.id.tv_card_description);
				cardFrame = (FrameLayout) itemView.findViewById(R.id.card_frame);
				slotId = (TextView) itemView.findViewById(R.id.tv_slotid);
				removeButton = (ImageView) itemView.findViewById(R.id.iv_remove);

				itemView.setOnClickListener(this);
			}

			@Override
			public void onClick(View v)
			{
				int position = getLayoutPosition();
				if (position == getItemCount() - 1)
				{
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
					if (prev != null)
					{
						ft.remove(prev);
					}
					ft.addToBackStack(null);

					PlusDialogFragment newFragment = new PlusDialogFragment();
					newFragment.setSaveTypeListener(MainActivity.this);
					newFragment.show(ft, "dialog");
				} else
				{
					if (advertisingTypes.size() > position && advertisingTypes.get(position) != null)
					{
						AdvertisingType advertisingType = advertisingTypes.get(position);
						if (advertisingType.getAdType() == AD_TYPE_FULLSCREEN && advertisingType.getSlotId() != 0)
						{
							loadAndShowCustomInterstitial(advertisingType);
						}
						else
						{
							Intent intent = new Intent(MainActivity.this, AdTypes.getActivityByType(advertisingType.getAdType()));
							intent.putExtra(AD_TYPE_TAG, advertisingType);
							startActivity(intent);
						}
					}
				}
			}
		}
	}

	private void loadAndShowCustomInterstitial(final AdvertisingType advertisingType)
	{
		if (loadingView != null)
		{
			loadingView.setVisibility(View.VISIBLE);
		}
		final InterstitialAd interstitialAd = new InterstitialAd(advertisingType.getSlotId(), this);
		Tools.fillCustomParamsUserData(interstitialAd.getCustomParams());
		interstitialAd.setListener(new InterstitialAdListener()
		{
			@Override
			public void onLoad(final InterstitialAd ad)
			{
				if (loadingView != null)
				{
					loadingView.setVisibility(View.GONE);
				}
				interstitialAd.show();
			}

			@Override
			public void onNoAd(final String s, final InterstitialAd ad)
			{
				if (loadingView != null)
				{
					loadingView.setVisibility(View.GONE);
				}
				Snackbar.make(recyclerView, getString(R.string.no_ad), Snackbar.LENGTH_LONG).show();
			}

			@Override
			public void onClick(final InterstitialAd ad)
			{

			}

			@Override
			public void onDismiss(final InterstitialAd ad)
			{

			}

			@Override
			public void onVideoCompleted(final InterstitialAd ad)
			{

			}

			@Override
			public void onDisplay(final InterstitialAd ad)
			{

			}
		});
		interstitialAd.load();
	}
}
