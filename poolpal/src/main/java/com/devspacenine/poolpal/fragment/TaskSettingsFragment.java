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

import java.text.ParseException;
import java.util.*;

import androidx.fragment.app.*;
import androidx.loader.app.*;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

public class TaskSettingsFragment extends /*Sherlock*/Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final String REQUEST_CODE = "request_code";
	public static final String POOL = "pool";
	public static final String FRAGMENT_TAG = "fragment_tag";

	public static Bundle createItem(int requestCode, Pool pool, String fragmentTag) {
		Bundle b = new Bundle();
		b.putInt(REQUEST_CODE, requestCode);
		b.putParcelable(POOL, pool);
		if (fragmentTag != null) b.putString(FRAGMENT_TAG, fragmentTag);
		return b;
	}

	private Pool mPool;
	private Task mTask;
	private SharedPreferences mPoolPreferences;
	private androidx.fragment.app.FragmentActivity mCtx;
	private ListView mListView;
	private SettingsSectionAdapter mAdapter;
	private boolean mPopulated;
	private int mRefreshIndex;
	private int mRefreshTop;
	private int mRequestCode;

	public static TaskSettingsFragment newInstance(Bundle args) {

		TaskSettingsFragment frag = new TaskSettingsFragment();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onActivityCreated(Bundle savedState) {

		super.onActivityCreated(savedState);
		mCtx = /*getSherlock*/getActivity();
		mPoolPreferences = mCtx.getSharedPreferences(PoolPal.PREFS_POOL, 0);
		mPool = getArguments().getParcelable(POOL);
		mRequestCode = getArguments().getInt(REQUEST_CODE);
		getLoaderManager()/*.getInstance(this)*/.initLoader(PoolPal.TASK_LOADER, null, this);
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
			LinkedList<Bundle> schedule_data = new LinkedList<>();
			LinkedList<Bundle> repitition_data = new LinkedList<>();
			LinkedList<Bundle> reminder_data = new LinkedList<>();

			schedule_data.add(TaskDataAdapter.createItem(res.getString(R.string.lbl_task_date), res.getString(R.string.details_task_date), TaskDataAdapter.ACTION_EDIT, TaskDataAdapter.SET_DATE));
			schedule_data.add(TaskDataAdapter.createItem(res.getString(R.string.lbl_task_message), res.getString(R.string.details_task_message), TaskDataAdapter.ACTION_NONE, TaskDataAdapter.SET_MESSAGE));

			repitition_data.add(TaskDataAdapter.createItem(res.getString(R.string.lbl_task_repitition), res.getString(R.string.details_task_repitition), TaskDataAdapter.ACTION_EDIT, TaskDataAdapter.SET_REPITITION));
			repitition_data.add(TaskDataAdapter.createItem(res.getString(R.string.lbl_task_next_on_completion), res.getString(R.string.details_task_next_on_completion), TaskDataAdapter.ACTION_TOGGLE, TaskDataAdapter.SET_NEXT_ON_COMPLETION));

			String[] periods = res.getStringArray(R.array.reminder_periods);
			for (Reminder r : mTask.getReminders()) {
				reminder_data.add(TaskDataAdapter.createItem(periods[r.getPeriod()], res.getString(R.string.details_task_reminders), TaskDataAdapter.ACTION_EDIT, TaskDataAdapter.SET_REMINDER));
			}
			reminder_data.add(TaskDataAdapter.createItem(res.getString(R.string.lbl_task_new_reminder), res.getString(R.string.details_task_new_reminder), TaskDataAdapter.ACTION_ADD, TaskDataAdapter.SET_NEW_REMINDER));
			reminder_data.add(TaskDataAdapter.createItem(res.getString(R.string.lbl_task_late_reminders), res.getString(R.string.details_task_late_reminders), TaskDataAdapter.ACTION_TOGGLE, TaskDataAdapter.SET_LATE_REMINDERS));

			mAdapter = new SettingsSectionAdapter(mCtx, mPool);

			mAdapter.addSection(mTask.getTitle(), new TaskDataAdapter(mCtx, mTask, schedule_data, R.layout.edit_settings_item, getTag()));

			mAdapter.addSection(res.getString(R.string.title_repitition), new TaskDataAdapter(mCtx, mTask, repitition_data, R.layout.edit_settings_item, getTag()));

			mAdapter.addSection(res.getString(R.string.title_reminders), new TaskDataAdapter(mCtx, mTask, reminder_data, R.layout.edit_settings_item, getTag()));

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
				return new CursorLoader(mCtx, Uri.withAppendedPath(PoolPalContent.POOLS_CONTENT_URI, Long.toString(mPoolPreferences.getLong(PoolPal.PREFS_ACTIVE_POOL_ID, 0))), PoolTable.columnProjection(), null, null, null);

			case PoolPal.TASK_LOADER:
				String taskType;
				switch (mRequestCode) {
					case PoolDataAdapter.SET_GENERAL_MAINTENANCE_SCHEDULE:
						taskType = Integer.toString(Task.GENERAL_MAINTENANCE);
						break;
					case PoolDataAdapter.SET_WATER_TESTS_SCHEDULE:
						taskType = Integer.toString(Task.WATER_TESTS);
						break;
					case PoolDataAdapter.SET_FILTER_MAINTENANCE_SCHEDULE:
						taskType = Integer.toString(Task.FILTER_MAINTENANCE);
						break;
					default:
						taskType = Integer.toString(Task.CUSTOM);
				}
				return new CursorLoader(mCtx, PoolPalContent.TASKS_CONTENT_URI, TaskTable.columnProjection(), TaskTable.KEY_POOL + "=? and " + TaskTable.KEY_TASK_TYPE + "=?", new String[]{Long.toString(mPool.getId()), taskType}, null);

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
				}
				break;

			case PoolPal.TASK_LOADER:
				cursor.moveToFirst();
				if (!cursor.isAfterLast()) {
					try {
						mTask = new Task(mCtx, cursor);
					} catch (ParseException e) {
						getLoaderManager()/*.getInstance(this)*/.destroyLoader(PoolPal.TASK_LOADER);
						mTask = null;
						e.printStackTrace();
					}
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

			case PoolPal.TASK_LOADER:
				mTask = null;

				break;

			default:
				break;
		}
	}
}
