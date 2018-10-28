package com.devspacenine.poolpal.fragment;

import android.content.*;
import android.content.res.*;
import android.database.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import com.devspacenine.poolpal.*;
import com.devspacenine.poolpal.contentprovider.*;
import com.devspacenine.poolpal.database.*;
import com.devspacenine.poolpal.object.*;
import com.devspacenine.poolpal.widget.*;

import java.util.*;

import androidx.fragment.app.*;
import androidx.loader.app.*;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

public class PoolTasksFragment extends /*Sherlock*/Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private Pool mPool;
	private SharedPreferences mPoolPreferences;
	private ListView mListView;
	private SettingsSectionAdapter mAdapter;
	private boolean mPopulated;
	private int mRefreshIndex;
	private int mRefreshTop;
	private FragmentActivity mCtx;

	static PoolTasksFragment newInstance() {
		PoolTasksFragment frag = new PoolTasksFragment();

		Bundle args = new Bundle();
		frag.setArguments(args);

		return frag;
	}

	@Override
	public void onActivityCreated(Bundle savedState) {

		super.onActivityCreated(savedState);
		mCtx = /*getSherlock*/getActivity();
		mPoolPreferences = mCtx.getSharedPreferences(PoolPal.PREFS_POOL, 0);
		getLoaderManager()/*.getInstance(this)*/.initLoader(PoolPal.POOL_LOADER, null, this);
	}

	@Override
	public void onCreate(Bundle savedState) {

		super.onCreate(savedState);
		mPopulated = false;
		mRefreshIndex = 0;
		mRefreshTop = 0;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mPopulated = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {

		View v = inflater.inflate(R.layout.edit_pool_info, container, false);
		mListView = (ListView) v;
		return v;
	}

	public void populate() {

		Resources res = getResources();

		if (!mPopulated) {
			LinkedList<Bundle> tasks_data = new LinkedList<>();
			LinkedList<Bundle> notifications_data = new LinkedList<>();

			tasks_data.add(PoolDataAdapter.createItem(res.getString(R.string.lbl_general_maintenance), res.getString(R.string.details_general_maintenance), PoolDataAdapter.ACTION_OPEN, PoolDataAdapter.SET_GENERAL_MAINTENANCE_SCHEDULE));
			tasks_data.add(PoolDataAdapter.createItem(res.getString(R.string.lbl_water_tests), res.getString(R.string.details_water_tests), PoolDataAdapter.ACTION_OPEN, PoolDataAdapter.SET_WATER_TESTS_SCHEDULE));
			tasks_data.add(PoolDataAdapter.createItem(res.getString(R.string.lbl_filter_maintenance), res.getString(R.string.details_filter_maintenance), PoolDataAdapter.ACTION_OPEN, PoolDataAdapter.SET_FILTER_MAINTENANCE_SCHEDULE));
			tasks_data.add(PoolDataAdapter.createItem(res.getString(R.string.lbl_custom_tasks), res.getString(R.string.details_custom_tasks), PoolDataAdapter.ACTION_OPEN, PoolDataAdapter.SET_CUSTOM_TASK_SETTINGS));

			notifications_data.add(PoolDataAdapter.createItem(res.getString(R.string.lbl_weather_notifications), res.getString(R.string.details_weather_notification), PoolDataAdapter.ACTION_TOGGLE, PoolDataAdapter.SET_WEATHER_NOTIFICATIONS));
			notifications_data.add(PoolDataAdapter.createItem(res.getString(R.string.lbl_water_test_reminders), res.getString(R.string.details_water_test_reminder), PoolDataAdapter.ACTION_TOGGLE, PoolDataAdapter.SET_WATER_TEST_REMINDERS));
			notifications_data.add(PoolDataAdapter.createItem(res.getString(R.string.lbl_maintenance_reminders), res.getString(R.string.details_maintenance_reminder), PoolDataAdapter.ACTION_TOGGLE, PoolDataAdapter.SET_MAINTENANCE_REMINDERS));
			notifications_data.add(PoolDataAdapter.createItem(res.getString(R.string.lbl_filter_reminders), res.getString(R.string.details_filter_reminder), PoolDataAdapter.ACTION_TOGGLE, PoolDataAdapter.SET_FILTER_REMINDERS));
			notifications_data.add(PoolDataAdapter.createItem(res.getString(R.string.lbl_safety_notifications), res.getString(R.string.details_safety_notification), PoolDataAdapter.ACTION_TOGGLE, PoolDataAdapter.SET_SAFETY_NOTIFICATIONS));
			notifications_data.add(PoolDataAdapter.createItem(res.getString(R.string.lbl_custom_reminders), res.getString(R.string.details_custom_reminder), PoolDataAdapter.ACTION_TOGGLE, PoolDataAdapter.SET_CUSTOM_NOTIFICATIONS));
			notifications_data.add(PoolDataAdapter.createItem(res.getString(R.string.lbl_coupon_notifications), res.getString(R.string.details_coupon_notification), PoolDataAdapter.ACTION_TOGGLE, PoolDataAdapter.SET_COUPON_NOTIFICATIONS));

			mAdapter = new SettingsSectionAdapter(mCtx, mPool);

			mAdapter.addSection(res.getString(R.string.title_tasks), new PoolDataAdapter(mCtx, mPool, tasks_data, R.layout.edit_settings_item, getTag()));

			mAdapter.addSection(res.getString(R.string.title_notifications), new PoolDataAdapter(mCtx, mPool, notifications_data, R.layout.toggle_setting_item, getTag()));

			mListView.setAdapter(mAdapter);
			mPopulated = true;

			if (mRefreshIndex > 0 || mRefreshTop > 0) {
				mListView.setSelectionFromTop(mRefreshIndex, mRefreshTop);
				mRefreshIndex = 0;
				mRefreshTop = 0;
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (outState.isEmpty()) {
			outState.putBoolean("bug:fix", true);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		switch (id) {
			case PoolPal.POOL_LOADER:
				CursorLoader loader = new CursorLoader(mCtx, Uri.withAppendedPath(PoolPalContent.POOLS_CONTENT_URI, Long.toString(mPoolPreferences.getLong(PoolPal.PREFS_ACTIVE_POOL_ID, 0))), PoolTable.columnProjection(), null, null, null);
				return loader;

			default:
				return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		switch (loader.getId()) {
			case PoolPal.POOL_LOADER:
				cursor.moveToFirst();
				if (!cursor.isAfterLast()) {
					mPool = new Pool(mCtx, cursor);
					populate();
				}
				break;

			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

		switch (loader.getId()) {
			case PoolPal.POOL_LOADER:
				mPool = null;

				break;

			default:
				break;
		}
	}
}
