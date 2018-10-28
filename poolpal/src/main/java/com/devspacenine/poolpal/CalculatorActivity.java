package com.devspacenine.poolpal;

import android.content.*;
import android.database.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;

import com.devspacenine.poolpal.contentprovider.*;
import com.devspacenine.poolpal.database.*;
import com.devspacenine.poolpal.fragment.*;
import com.devspacenine.poolpal.object.*;

import java.io.*;

import androidx.appcompat.app.*;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

public class CalculatorActivity extends /*Sherlock*/AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnClickListener {

	// Cursor for pools
	private Pool mPool;
	private SharedPreferences mPoolPreferences;
	private ImageView mPoolImage;
	private Bitmap mPoolImageBitmap;

	@Override
	public void onAttachedToWindow() {

		super.onAttachedToWindow();
		Window window = getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedState) {

		super.onCreate(savedState);

		mPoolPreferences = getSharedPreferences(PoolPal.PREFS_POOL, 0);

		ActionBar ab = /*getSupport*/getSupportActionBar();
		ab.setHomeButtonEnabled(true);

		setContentView(R.layout.calculator);
		// Setup cursor loader and adapter for the pool spinner
		getSupportLoaderManager()/*.getInstance(this)*/.initLoader(PoolPal.POOL_LOADER, null, this);

		// Load the calculator fragment
		CalculatorFragment frag = CalculatorFragment.newInstance();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.calculator, frag);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();

	}

	@Override
	public void onPause() {

		getContentResolver().update(Uri.withAppendedPath(PoolPalContent.POOLS_CONTENT_URI, Long.toString(mPool.getId())), mPool.getContentValues(), null, null);
		super.onPause();
	}

	@Override
	public void onRestart() {

		getSupportLoaderManager()/*.getInstance(this)*/.initLoader(PoolPal.POOL_LOADER, null, this);
		super.onRestart();
	}

	@Override
	public void onClick(View v) {

		Intent intent = new Intent(this, EditPoolActivity.class);
		intent.putExtra(PoolPal.EXTRA_PAGE, R.layout.edit_pool);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		super.onCreateOptionsMenu(menu);

		// Inflate the menu from XML
		MenuInflater inflater = /*getSupport*/getMenuInflater();
		inflater.inflate(R.menu.calculator_menu, menu);

		MenuItem alerts = menu.getItem(0);
		alerts.setVisible(true);
		alerts.setEnabled(true);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void populateInfo() {

		// Set references to pool image
		mPoolImage = findViewById(R.id.photo);
		if (mPoolImage != null) {
			mPoolImageBitmap = ((BitmapDrawable) mPoolImage.getDrawable()).getBitmap();
			if (mPool.hasImage()) {
				try {
					InputStream stream = getContentResolver().openInputStream(Uri.parse(mPool.getImage()));
					mPoolImageBitmap = BitmapFactory.decodeStream(stream);
					stream.close();
					mPoolImage.setImageBitmap(mPoolImageBitmap);
					mPoolImage.setTag(R.id.key_uri, mPool.getImage());
					mPoolImage.setTag(R.id.key_set, Boolean.toString(true));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e2) {
					e2.printStackTrace();
				}

			}
		}

		TextView name = findViewById(R.id.nickname);
		if (name != null) {
			name.setText(mPool.getName());
		}
		TextView location = findViewById(R.id.location);
		if (location != null) {
			location.setText(mPool.getLocationText());
		}
		TextView volume = findViewById(R.id.volume);
		if (volume != null) {
			volume.setText(mPool.getVolumeString());
		}

		findViewById(R.id.pool).setOnClickListener(this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		switch (id) {
			case PoolPal.POOL_LOADER:
				CursorLoader loader = new CursorLoader(this, Uri.withAppendedPath(PoolPalContent.POOLS_CONTENT_URI, Long.toString(mPoolPreferences.getLong(PoolPal.PREFS_ACTIVE_POOL_ID, 0))), PoolTable.columnProjection(), null, null, null);
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
					mPool = new Pool(this, cursor);
					// Set the details of the PoolView
					populateInfo();
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
