package com.devspacenine.poolpal.fragment;

import android.app.*;
import android.content.*;
import android.database.*;
import android.net.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.devspacenine.poolpal.*;
import com.devspacenine.poolpal.contentprovider.*;
import com.devspacenine.poolpal.database.*;
import com.devspacenine.poolpal.object.*;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.*;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

public abstract class InputDialogFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	public static final String REQUEST_CODE = "request_code";
	public static final String TITLE = "title";
	public static final String DETAILS = "details";
	public static final String LAYOUT = "layout";
	static final String FRAGMENT_TAG = "fragment_tag";
	private static final String VALUES = "values";

	public static Bundle createItem(int requestCode, String title, String details, int layout, Bundle values, String fragmentTag) {
		Bundle args = new Bundle();
		args.putInt(REQUEST_CODE, requestCode);
		args.putString(TITLE, title);
		args.putString(DETAILS, details);
		args.putInt(LAYOUT, layout);
		args.putBundle(VALUES, values);
		if (fragmentTag != null) {
			args.putString(FRAGMENT_TAG, fragmentTag);
		}
		return args;
	}

	OnDecisionListener mListener;
	Bundle mValues;
	protected FragmentActivity mCtx;
	protected Pool mPool;
	private SharedPreferences mPoolPreferences;

	// View references
	ViewGroup mInputView;
	TextView mPrompt;
	TextView mCancelButton;
	TextView mConfirmButton;
	int mRequestCode;

	public static Object newInstance(Class<?> clss, FragmentActivity context, Bundle args) {

		Fragment frag = Fragment.instantiate(context, clss.getName(), null);
		frag.setArguments(args);

		return clss.cast(frag);
	}

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);

		// Make sure the calling fragment or activity implements OnDecisionMadeListener
		if (getArguments().containsKey(FRAGMENT_TAG)) {
			Fragment frag = ((FragmentActivity) activity).getSupportFragmentManager().findFragmentByTag(getArguments().getString(FRAGMENT_TAG));
			try {
				mListener = (OnDecisionListener) frag;
			} catch (ClassCastException e) {
				throw new ClassCastException(frag + " must implement the OnDecisionMadeListener interface.");
			}
		} else {
			try {
				mListener = (OnDecisionListener) activity;
			} catch (ClassCastException e) {
				throw new ClassCastException(activity + " must implement the OnDecisionMadeListener interface.");
			}
		}
	}

	@Override
	public void onActivityCreated(Bundle savedState) {

		super.onActivityCreated(savedState);
		mCtx = getActivity();
		mPoolPreferences = mCtx.getSharedPreferences(PoolPal.PREFS_POOL, 0);

		getLoaderManager()/*.getInstance(this)*/.initLoader(PoolPal.POOL_LOADER, null, this);
	}

	@Override
	public void onCreate(Bundle savedState) {

		super.onCreate(savedState);

		setStyle(STYLE_NO_FRAME, R.style.Theme_PoolPal_Transparent);
		setCancelable(true);

		mCtx = getActivity();

		mRequestCode = getArguments().getInt(REQUEST_CODE);
		mValues = getArguments().getBundle(VALUES);
	}

	@Override
	public void onCancel(DialogInterface dialog) {

		super.onCancel(dialog);
		negativeDecision();
	}

	protected void negativeDecision() {

		mListener.onNegativeDecision(mRequestCode);
		dismiss();
	}

	protected void positiveDecision() {

		mListener.onPositiveDecision(mRequestCode, mValues);
		dismiss();
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
				} else {
					Log.e("DSN", "Could not load pool from database.");
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
