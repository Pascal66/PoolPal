package com.devspacenine.poolpal.fragment;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.pm.PackageManager.*;
import android.content.res.*;
import android.location.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import android.view.inputmethod.*;
import android.widget.*;

import com.devspacenine.poolpal.*;
import com.devspacenine.poolpal.object.*;
import com.devspacenine.poolpal.util.*;
import com.devspacenine.poolpal.widget.*;

import java.io.*;
import java.util.*;

public class AddressInputDialogFragment extends InputDialogFragment implements LocationListener {
	// Request codes
	private static final int ENABLE_GPS = 1;

	// Thread Message Handler Codes
	private static final int MESSAGE_GPS_ADDRESS_SUCCESS = 1;
	private static final int MESSAGE_STOP = 2;
	private static final int MESSAGE_ERROR = 3;

	// Tags
	private static final String TAG_NORMAL = "normal";
	private static final String TAG_GPS = "gps";

	// Location variables
	private LocationManager mLocationManager;
	private boolean waiting_for_gps;
	private LocationListener mLocationListener;
	private HashMap<Integer, GetAddressThread> mGetAddressThreads;

	// View references
	private EditText mLineOne;
	private EditText mCity;
	private EditText mState;
	private EditText mZip;
	private ImageView mGPSButton;
	private ViewGroup mProgressView;
	private ViewGroup mChoiceView;
	private ListView mChoiceList;
	private AddressAdapter mAdapter;
	private Address mInitAddress;
	private boolean mGeocoded;

	private boolean mTest = false;
	private String mLocationProvider;

	public static AddressInputDialogFragment newInstance(Bundle args) {

		AddressInputDialogFragment frag = new AddressInputDialogFragment();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedState) {

		super.onCreate(savedState);

		mLocationListener = this;

		// Get the device's default location manager
		mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

		mGetAddressThreads = new HashMap<>();

		if (PoolAddress.isBlank(mValues.getParcelable(PoolDataAdapter.VALUE_ADDRESS))) {
			Log.d("DSN Debug", "Initial Address is blank");
			mInitAddress = PoolAddress.blankAddress();
		} else {
			mInitAddress = PoolAddress.clone(mValues.getParcelable(PoolDataAdapter.VALUE_ADDRESS));
		}

		try {
			ApplicationInfo ai = mCtx.getPackageManager().getApplicationInfo(mCtx.getPackageName(), PackageManager.GET_META_DATA);
			Bundle b = ai.metaData;
		//	mTest = b.getBoolean("com.devspacenine.poolpal.TEST", true);
		} catch (NameNotFoundException e) {
		//	mTest = true;
			e.printStackTrace();
		}

		if (mTest) {
			mLocationProvider = MockLocationUtil.PROVIDER_NAME;
			MockLocationUtil.createMockLocationProvider(mLocationManager, mLocationProvider);
		} else {
			mLocationProvider = LocationManager.GPS_PROVIDER;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {

		final Resources res = getResources();
		Bundle args = getArguments();

		mRequestCode = args.getInt(REQUEST_CODE);

		View v;
		v = inflater.inflate(R.layout.input_dialog, container, false);

		// Set the layout of the view stub
		ViewStub stub = v.findViewById(R.id.stub);
		stub.setLayoutResource(args.getInt(LAYOUT));
		mInputView = (ViewGroup) stub.inflate();

		final ViewStub progressStub = v.findViewById(R.id.progress_stub);
		final ViewStub choiceStub = v.findViewById(R.id.choice_stub);

		// Set the title
		TextView title = v.findViewById(R.id.title);
		title.setText(args.getString(TITLE));

		// Set the prompt
		mPrompt = v.findViewById(R.id.prompt);
		mPrompt.setVisibility(View.VISIBLE);
		mPrompt.setText(args.getString(DETAILS));

		if (!(mValues.containsKey(PoolDataAdapter.VALUE)) || mValues.getString(PoolDataAdapter.VALUE) == null) {
			mValues.putString(PoolDataAdapter.VALUE, "");
		}

		mCancelButton = v.findViewById(R.id.cancel);
		mConfirmButton = v.findViewById(R.id.confirm);

		// Initialize the type tag of the cancel button
		mCancelButton.setTag(R.id.key_type, TAG_NORMAL);
		mConfirmButton.setTag(R.id.key_type, TAG_NORMAL);

		mLineOne = mInputView.findViewById(R.id.line1);
		mCity = mInputView.findViewById(R.id.city);
		mState = mInputView.findViewById(R.id.state);
		mZip = mInputView.findViewById(R.id.zip);

		Address a = mValues.getParcelable(PoolDataAdapter.VALUE_ADDRESS);
		mLineOne.setText(a.getAddressLine(0));
		mCity.setText(a.getLocality());
		mState.setText(a.getAdminArea());
		mZip.setText(a.getPostalCode());

		if (!mValues.containsKey(PoolDataAdapter.VALUE_GEOCODED)) {
			mValues.putBoolean(PoolDataAdapter.VALUE_GEOCODED, false);
			mGeocoded = false;
		} else {
			mGeocoded = mValues.getBoolean(PoolDataAdapter.VALUE_GEOCODED);
		}

		// Use TextWatchers to update the value if any edittexts change
		mLineOne.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {

				Address a = mValues.getParcelable(PoolDataAdapter.VALUE_ADDRESS);
				String newVal = s.toString().trim().replaceAll(" +", " ");
				if (newVal.equals(a.getAddressLine(0))) {
					return;
				}
				a.setAddressLine(0, newVal);
				a = PoolAddress.setLines(PoolAddress.parseLineOne(a));
				mValues.putParcelable(PoolDataAdapter.VALUE_ADDRESS, a);
				mValues.putString(PoolDataAdapter.VALUE, PoolAddress.toString(a));
				if (!newVal.equals(mInitAddress.getAddressLine(0))) {
					mGeocoded = false;
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});

		mLineOne.setOnEditorActionListener((v17, actionId, event) -> {
			if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) || actionId == EditorInfo.IME_ACTION_NEXT) {
				mCity.requestFocus();
				return true;
			}
			return false;
		});

		mCity.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {

				Address a = mValues.getParcelable(PoolDataAdapter.VALUE_ADDRESS);
				String newVal = s.toString().trim().replaceAll(" +", " ");
				if (newVal.equals(a.getLocality())) {
					return;
				}
				a.setLocality(newVal);
				mValues.putParcelable(PoolDataAdapter.VALUE_ADDRESS, PoolAddress.setLines(a));
				mValues.putString(PoolDataAdapter.VALUE, PoolAddress.toString(a));
				if (!newVal.equals(mInitAddress.getLocality())) {
					mGeocoded = false;
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});

		mCity.setOnEditorActionListener((v16, actionId, event) -> {
			if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) || actionId == EditorInfo.IME_ACTION_NEXT) {
				mState.requestFocus();
				return true;
			}
			return false;
		});

		mState.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {

				Address a = mValues.getParcelable(PoolDataAdapter.VALUE_ADDRESS);
				String newVal = s.toString().trim().replaceAll(" +", " ");
				if (newVal.equals(a.getAdminArea())) {
					return;
				}
				a.setAdminArea(newVal);
				mValues.putParcelable(PoolDataAdapter.VALUE_ADDRESS, PoolAddress.setLines(a));
				mValues.putString(PoolDataAdapter.VALUE, PoolAddress.toString(a));
				if (!newVal.equals(mInitAddress.getAdminArea())) {
					mGeocoded = false;
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});

		mState.setOnEditorActionListener((v15, actionId, event) -> {
			if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) || actionId == EditorInfo.IME_ACTION_NEXT) {
				mZip.requestFocus();
				return true;
			}
			return false;
		});

		mZip.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {

				Address a = mValues.getParcelable(PoolDataAdapter.VALUE_ADDRESS);
				String newVal = s.toString().trim().replaceAll(" +", " ");
				if (newVal.equals(a.getPostalCode())) {
					return;
				}
				a.setPostalCode(newVal);
				mValues.putParcelable(PoolDataAdapter.VALUE_ADDRESS, PoolAddress.setLines(a));
				mValues.putString(PoolDataAdapter.VALUE, PoolAddress.toString(a));
				if (!newVal.equals(mInitAddress.getPostalCode())) {
					mGeocoded = false;
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});

		mZip.setOnEditorActionListener((v14, actionId, event) -> {
			if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) || actionId == EditorInfo.IME_ACTION_DONE) {
				saveAddress(mValues.getParcelable(PoolDataAdapter.VALUE_ADDRESS));
				return true;
			}
			return false;
		});

		mGPSButton = mInputView.findViewById(R.id.gps);
		mGPSButton.setOnClickListener(v13 -> {
			// Make sure the location provider is enabled
			if (mLocationManager.isProviderEnabled(mLocationProvider)) {
				// Create the progress view if it hasn't been already
				if (mProgressView == null) {
					progressStub.setLayoutResource(R.layout.progress_stub);
					mProgressView = (ViewGroup) progressStub.inflate();
					((TextView) mProgressView.findViewById(R.id.progress_text)).setText(res.getString(R.string.waiting_for_gps));
					ImageView graphic = mProgressView.findViewById(R.id.progress_graphic);
					Animation rotateAnim = AnimationUtils.loadAnimation(mCtx, R.anim.progress_indeterminate_animation);
					graphic.startAnimation(rotateAnim);
				}

				// Create the choice view if it hasn't been already
				if (mChoiceView == null) {
					choiceStub.setLayoutResource(R.layout.spinner_input);
					mChoiceView = (ViewGroup) choiceStub.inflate();
					mChoiceList = mChoiceView.findViewById(R.id.list);
					mChoiceView.setVisibility(View.GONE);

					// Set the choices adapter as a blank list of addresses
					mAdapter = new AddressAdapter(mCtx);
					mChoiceList.setAdapter(mAdapter);
					// Set the onItemClick listener for the choices list
					mChoiceList.setOnItemClickListener((parent, view, position, id) -> {
						Address a12 = (Address) mAdapter.getItem(position);

						// Update the values
						mValues.putParcelable(PoolDataAdapter.VALUE_ADDRESS, a12);
						mValues.putBoolean(PoolDataAdapter.VALUE_GEOCODED, true);
						mValues.putString(PoolDataAdapter.VALUE, PoolAddress.toString(a12));

						// Update the text inputs
						mLineOne.setText(a12.getAddressLine(0));
						mCity.setText(a12.getLocality());
						mZip.setText(a12.getPostalCode());
						mState.setText(a12.getAdminArea());

						// Stop listening for location updates
						mHandler.removeCallbacks(mLocationUpdatesTimer);
						mLocationManager.removeUpdates(mLocationListener);
						waiting_for_gps = false;

						// Return to the starting layout
						mInputView.setVisibility(View.VISIBLE);
						mChoiceView.setVisibility(View.GONE);
						mProgressView.setVisibility(View.GONE);
						mPrompt.setText(R.string.details_address);
						mCancelButton.setTag(R.id.key_type, TAG_NORMAL);
						mConfirmButton.setTag(R.id.key_type, TAG_NORMAL);
						mConfirmButton.setEnabled(true);
						mConfirmButton.setText(android.R.string.ok);
					});
				}
				startGPSSearch();
			} else {
				// GPS is disabled, send out an intent to turn it on
				Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivityForResult(intent, ENABLE_GPS);
			}
		});

		// make the confirm button save the address
		mConfirmButton.setOnClickListener(v12 -> {
			if (v12.isEnabled()) {
				if (v12.getTag(R.id.key_type).toString().equals(TAG_GPS)) {
					startGPSSearch();
				} else {
					Address a1 = mValues.getParcelable(PoolDataAdapter.VALUE_ADDRESS);
					if (!PoolAddress.equals(a1, mInitAddress)) {
						mValues.putBoolean(PoolDataAdapter.VALUE_GEOCODED, mGeocoded);
					}
					saveAddress(a1);
				}
			}
		});

		// Set click listeners for the cancel button
		mCancelButton.setOnClickListener(v1 -> {
			if (v1.getTag(R.id.key_type).toString().equals(TAG_GPS)) {
				mHandler.removeCallbacks(mLocationUpdatesTimer);
				mLocationManager.removeUpdates(mLocationListener);
				stopGetAddressThreads();
				waiting_for_gps = false;
				mProgressView.setVisibility(View.GONE);
				mChoiceView.setVisibility(View.GONE);
				mInputView.setVisibility(View.VISIBLE);
				mCancelButton.setTag(R.id.key_type, TAG_NORMAL);
				mConfirmButton.setTag(R.id.key_type, TAG_NORMAL);
				mConfirmButton.setEnabled(true);
				mConfirmButton.setText(android.R.string.ok);
			} else {
				negativeDecision();
			}
		});

		return v;
	}

	private void startGPSSearch() {
		// Make sure the location provider is enabled
		if (mLocationManager.isProviderEnabled(mLocationProvider)) {
			// Start getting the current location in a asynchronous thread
			if (mTest) {
				MockLocationUtil.publishMockLocation(32.72395, -97.41373, mCtx, mLocationManager, mLocationListener, mLocationProvider);
			} else {
				try {
					mLocationManager.requestLocationUpdates(mLocationProvider, 100, 0, mLocationListener);
				} catch (SecurityException ignore) {
				}
			}
			waiting_for_gps = true;

			// Hide the text input view
			mInputView.setVisibility(View.GONE);

			mProgressView.setVisibility(View.VISIBLE);

			// Disable the confirm button and make the cancel button just cancel
			// the gps feature
			mConfirmButton.setTag(R.id.key_type, TAG_GPS);
			mConfirmButton.setEnabled(false);
			mConfirmButton.setText(R.string.search_again);
			mCancelButton.setTag(R.id.key_type, TAG_GPS);

			// Stop the location updates after 60 seconds
			mHandler.postDelayed(mLocationUpdatesTimer, 1000 * 60);

			// Try to get the last read location
			Location loc = null;
			try {
				loc = mLocationManager.getLastKnownLocation(mLocationProvider);
			} catch (SecurityException ignore) {
			}
			if (loc != null) {
				startGetAddressThread(loc);
			}
		} else {
			// GPS is disabled, send out an intent to turn it on
			Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(intent, ENABLE_GPS);
		}
	}

	private void saveAddress(Address a) {

		Address geocodedAddress = null;

		// Skip this step if this address is equal to the initial address
		if (PoolAddress.equals(a, mInitAddress)) {
			Log.d("DSN Debug", "New address same as initial address.\nNew: (" + a + ")\nInit: (" + mInitAddress + ")");
			negativeDecision();
			return;
		}

		// Get coordinates for this address if it isn't geocoded
		if (!mValues.getBoolean(PoolDataAdapter.VALUE_GEOCODED, false)) {
			Log.d("DSN Debug", "not geocoded");
			Geocoder gc = new Geocoder(mCtx);
			if (Geocoder.isPresent()) {
				List<Address> locs = new ArrayList<>();
				try {
					locs = gc.getFromLocationName(a.getAddressLine(0) + " " + a.getAddressLine(1) + " " + a.getAddressLine(2), 5);
				} catch (IOException e) {
					e.printStackTrace();
					Log.d("DSN Debug", "Geocoder IOException");
					negativeDecision();
					return;
				}

				for (Iterator<Address> iter = locs.iterator(); iter.hasNext(); ) {
					Address _a = iter.next();
					if (PoolAddress.isGeocoded(_a)) {
						Log.d("DSN Debug", "geocoded valid address");
						geocodedAddress = PoolAddress.setLines(_a);
						break;
					}
					if (!iter.hasNext()) {
						Log.d("DSN Debug", "Could not geocode a complete address");
						if (a.getFeatureName().equals("") || a.getThoroughfare().equals("")) {
							a = PoolAddress.parseLineOne(a);
						}
						a = PoolAddress.setLines(a);
					}
				}
			} else {
				Log.d("DSN Debug", "No Geocoder service available. Continue without coordinates");
				a.clearLatitude();
				a.clearLongitude();
				mValues.putParcelable(PoolDataAdapter.VALUE_ADDRESS, PoolAddress.setLines(a));
				mValues.putString(PoolDataAdapter.VALUE, PoolAddress.toString(a));
				positiveDecision();
				return;
			}
		}

		if (PoolAddress.isComplete(geocodedAddress)) {
			mValues.putParcelable(PoolDataAdapter.VALUE_ADDRESS, geocodedAddress);
			mValues.putString(PoolDataAdapter.VALUE, PoolAddress.toString(a));
			positiveDecision();
		} else {
			Log.d("DSN Debug", "Could not save address because it is incomplete.");
			negativeDecision();
		}
	}

	@Override
	public void negativeDecision() {

		mLocationManager.removeUpdates(mLocationListener);
		stopGetAddressThreads();
		mValues.putParcelable(PoolDataAdapter.VALUE_ADDRESS, mInitAddress);
		mValues.putBoolean(PoolDataAdapter.VALUE_GEOCODED, PoolAddress.isGeocoded(mInitAddress));
		mValues.putString(PoolDataAdapter.VALUE, PoolAddress.toString(mInitAddress));
		super.negativeDecision();
	}

	@Override
	public void positiveDecision() {

		mLocationManager.removeUpdates(mLocationListener);
		stopGetAddressThreads();
		super.positiveDecision();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
			case ENABLE_GPS:
				if (resultCode == Activity.RESULT_OK) {
					if (mLocationManager.isProviderEnabled(mLocationProvider)) {

					} else {
						Toast.makeText(getActivity(), "GPS Provider failed to start.", Toast.LENGTH_SHORT).show();
					}
				}
				break;

			default:
				super.onActivityResult(requestCode, resultCode, data);
				break;
		}
	}

	private void startGetAddressThread(Location location) {
		int index = mGetAddressThreads.size();
		GetAddressThread new_thread = new GetAddressThread(location.getLatitude(), location.getLongitude(), 10, mHandler, index);
		new_thread.startThread();
		mGetAddressThreads.put(index, new_thread);
	}

	private void stopGetAddressThreads() {
		for (int key : mGetAddressThreads.keySet()) {
			mGetAddressThreads.get(key).stopThread();
		}
		mGetAddressThreads.clear();
	}

	/**
	 * Called when the gps location has been updated
	 */
	public void onLocationChanged(Location location) {
		startGetAddressThread(location);
	}

	/**
	 * Called when the status of the provider changes
	 */
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (status == LocationProvider.OUT_OF_SERVICE) {
			mHandler.removeCallbacks(mLocationUpdatesTimer);
			mLocationManager.removeUpdates(mLocationListener);
			stopGetAddressThreads();
			waiting_for_gps = false;
			Toast.makeText(getActivity(), "Could not get an accurate GPS location", Toast.LENGTH_SHORT).show();
			mProgressView.setVisibility(View.GONE);
			mChoiceView.setVisibility(View.GONE);
			mInputView.setVisibility(View.VISIBLE);
			mCancelButton.setTag(R.id.key_type, TAG_NORMAL);
			mConfirmButton.setTag(R.id.key_type, TAG_NORMAL);
			mConfirmButton.setEnabled(true);
			mConfirmButton.setText(android.R.string.ok);
		}
	}

	/**
	 * Called when the provider is enabled
	 */
	public void onProviderEnabled(String provider) {}

	/**
	 * Called when the provider is disabled
	 */
	public void onProviderDisabled(String provider) {
		Toast.makeText(getActivity(), "GPS was just disabled. Canceling address search", Toast.LENGTH_SHORT).show();
		mHandler.removeCallbacks(mLocationUpdatesTimer);
		mLocationManager.removeUpdates(mLocationListener);
		stopGetAddressThreads();
		waiting_for_gps = false;
		mProgressView.setVisibility(View.GONE);
		mChoiceView.setVisibility(View.GONE);
		mInputView.setVisibility(View.VISIBLE);
		mCancelButton.setTag(R.id.key_type, TAG_NORMAL);
		mConfirmButton.setTag(R.id.key_type, TAG_NORMAL);
		mConfirmButton.setEnabled(true);
		mConfirmButton.setText(android.R.string.ok);
	}

	// A thread to try and search for the device's current location by address
	private class GetAddressThread extends Thread {
		private double mmLatitude;
		private double mmLongitude;
		private int mmMaxResults;
		private ArrayList<Address> mmAddressResult;
		private final Handler mmHandler;
		private volatile boolean mmRunning;
		private int mmIndex;

		GetAddressThread(double latitude, double longitude, int maxResults, Handler handler, int index) {
			mmLatitude = latitude;
			mmLongitude = longitude;
			mmMaxResults = maxResults;
			mmHandler = handler;
			mmIndex = index;
			mmRunning = false;
		}

		public void run() {
			Geocoder geocoder = new Geocoder(getActivity());
			Bundle data = new Bundle();
			data.putInt(PoolPal.EXTRA_DATA, mmIndex);
			try {
				mmAddressResult = (ArrayList<Address>) geocoder.getFromLocation(mmLatitude, mmLongitude, mmMaxResults);
				if (mmRunning) {
					data.putParcelableArrayList(PoolPal.EXTRA_ADDRESSES, mmAddressResult);
					mmHandler.obtainMessage(MESSAGE_GPS_ADDRESS_SUCCESS, data).sendToTarget();
				} else {
					mmHandler.obtainMessage(MESSAGE_STOP, data).sendToTarget();
				}
			} catch (IOException e) {
				if (mmRunning) {
					e.printStackTrace();
					mmHandler.obtainMessage(MESSAGE_ERROR, data).sendToTarget();
				}
			}
		}

		synchronized void startThread() {
			if (!mmRunning) {
				mmRunning = true;
				start();
			}
		}

		synchronized void stopThread() {
			if (mmRunning) {
				mmRunning = false;
				interrupt();
			}
		}
	}

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// Get the data
			Bundle data = (Bundle) msg.obj;

			switch (msg.what) {
				// Message sent by a GetLocationThread when it successfully finds an address
				case MESSAGE_GPS_ADDRESS_SUCCESS:
					if (waiting_for_gps) {
						final ArrayList<Address> address_list = new ArrayList<>();

						// Remove any incomplete or duplicate addresses from the list
						for (Parcelable a : data.getParcelableArrayList(PoolPal.EXTRA_ADDRESSES)) {
							if (PoolAddress.isGeocoded((Address) a)) {
								if (!mAdapter.contains((Address) a)) {
									address_list.add(PoolAddress.setLines((Address) a));
								}
							}
						}

						// Check if any valid addresses were found
						if (!address_list.isEmpty()) {
							// Make sure progress text is updated
							((TextView) mProgressView.findViewById(R.id.progress_text)).setText(R.string.waiting_for_more_gps);
							mProgressView.findViewById(R.id.divider).setVisibility(View.VISIBLE);

							// Make sure the progress graphic is small
							ImageView graphic = mProgressView.findViewById(R.id.progress_graphic);
							graphic.setImageResource(R.drawable.spinner_48_inner);
							Animation rotateAnim = AnimationUtils.loadAnimation(mCtx, R.anim.progress_indeterminate_animation);
							graphic.startAnimation(rotateAnim);

							// Make sure the choice view is visible
							mChoiceView.setVisibility(View.VISIBLE);
							mPrompt.setText(R.string.choose_address);

							mAdapter.add(address_list);
							mAdapter.notifyDataSetChanged();
							if (mAdapter.getCount() > 4) {
								removeCallbacks(mLocationUpdatesTimer);
								mLocationManager.removeUpdates(mLocationListener);
								waiting_for_gps = false;
								mProgressView.setVisibility(View.GONE);
							}
						}
					}
					mGetAddressThreads.remove(data.getInt(PoolPal.EXTRA_DATA));
					break;
				// Message sent by a GetLocationThread when it has been stopped prematurely
				case MESSAGE_STOP:
					// Message sent by a GetLocationThread when it encounters an error
					mGetAddressThreads.remove(data.getInt(PoolPal.EXTRA_DATA));
				case MESSAGE_ERROR:
					if (waiting_for_gps) {
						Toast.makeText(mCtx, "Could not get an address because of network errors", Toast.LENGTH_SHORT).show();
						removeCallbacks(mLocationUpdatesTimer);
						mLocationManager.removeUpdates(mLocationListener);
						stopGetAddressThreads();
						waiting_for_gps = false;
						mProgressView.setVisibility(View.GONE);
						mChoiceView.setVisibility(View.GONE);
						mInputView.setVisibility(View.VISIBLE);
						mCancelButton.setTag(R.id.key_type, TAG_NORMAL);
						mConfirmButton.setTag(R.id.key_type, TAG_NORMAL);
						mConfirmButton.setEnabled(true);
						mConfirmButton.setText(android.R.string.ok);
					}
					break;
				default:
					Log.d("DSN Debug", "Message type \"" + msg.what + "\" not supported");
			}
		}
	};

	private Runnable mLocationUpdatesTimer = new Runnable() {

		public void run() {
			mLocationManager.removeUpdates(mLocationListener);
			stopGetAddressThreads();
			waiting_for_gps = false;
			mProgressView.setVisibility(View.GONE);
			mConfirmButton.setEnabled(true);
			// If no addresses were found, show an error message and return to the manual
			// inputs
			if (mChoiceList.getAdapter().getCount() == 0) {
				Toast.makeText(mCtx, "Could not get an accurate GPS location", Toast.LENGTH_SHORT).show();
				mChoiceView.setVisibility(View.GONE);
				mInputView.setVisibility(View.VISIBLE);
				mCancelButton.setTag(R.id.key_type, TAG_NORMAL);
				mConfirmButton.setTag(R.id.key_type, TAG_NORMAL);
				mConfirmButton.setText(android.R.string.ok);
			} else {
				// Set the confirm button to start watching location updates again
				mConfirmButton.setTag(R.id.key_type, TAG_GPS);
			}
		}
	};
}
