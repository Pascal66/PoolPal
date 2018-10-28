package com.devspacenine.poolpal.fragment;

import android.content.res.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;

import com.devspacenine.poolpal.*;
import com.devspacenine.poolpal.view.*;
import com.devspacenine.poolpal.view.RangeSeekBar.*;
import com.devspacenine.poolpal.widget.*;

import java.util.*;

public class SetDimensionsDialogFragment extends InputDialogFragment implements OnRangeSeekBarChangeListener<Integer> {

	public static SetDimensionsDialogFragment newInstance(Bundle args) {

		SetDimensionsDialogFragment frag = new SetDimensionsDialogFragment();
		frag.setArguments(args);
		return frag;
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

		// Set the title
		TextView title = v.findViewById(R.id.title);
		title.setText(args.getString(TITLE));

		// Set the prompt
		mPrompt = v.findViewById(R.id.prompt);
		mPrompt.setVisibility(View.VISIBLE);
		mPrompt.setText(args.getString(DETAILS));

		if (!(mValues.containsKey(PoolDataAdapter.VALUE))) {
			mValues.putString(PoolDataAdapter.VALUE, "");
		}

		mCancelButton = v.findViewById(R.id.cancel);
		mConfirmButton = v.findViewById(R.id.confirm);

		// Initialize the type tag of the cancel button
		mCancelButton.setTag(R.id.key_type, "normal");
		mConfirmButton.setTag(R.id.key_type, "normal");

		// Set click listeners for the confirm button
		mConfirmButton.setOnClickListener(v13 -> {
			if (mValues.getString(PoolDataAdapter.VALUE).equals("")) {
				mListener.onNegativeDecision(mRequestCode);
			} else {
				mListener.onPositiveDecision(mRequestCode, mValues);
			}
			dismiss();
		});

		if (!mValues.containsKey(PoolDataAdapter.VALUE_MIN_DEPTH)) {
			mValues.putDouble(PoolDataAdapter.VALUE_MIN_DEPTH, 3d);
		}
		if (!mValues.containsKey(PoolDataAdapter.VALUE_MAX_DEPTH)) {
			mValues.putDouble(PoolDataAdapter.VALUE_MAX_DEPTH, 9d);
		}
		if (!mValues.containsKey(PoolDataAdapter.VALUE_VOLUME)) {
			mValues.putDouble(PoolDataAdapter.VALUE_VOLUME, -1.00d);
		}
		final double initMinDepth = mValues.getDouble(PoolDataAdapter.VALUE_MIN_DEPTH);
		final double initMaxDepth = mValues.getDouble(PoolDataAdapter.VALUE_MAX_DEPTH);
		final double initVolume = mValues.getDouble(PoolDataAdapter.VALUE_VOLUME);

		RelativeLayout depth = v.findViewById(R.id.depth);
		if (depth != null) {
			// Get values from resources and create the range seek bar
			String[] depths = res.getStringArray(R.array.depth_values);
			RangeSeekBar<Integer> seekBar = new RangeSeekBar<>(0, depths.length - 1, mCtx);
			seekBar.setNotifyWhileDragging(true);
			seekBar.setSelectedMinValue((int) mValues.getDouble(PoolDataAdapter.VALUE_MIN_DEPTH));
			seekBar.setSelectedMaxValue((int) mValues.getDouble(PoolDataAdapter.VALUE_MAX_DEPTH));
			seekBar.setOnRangeSeekBarChangeListener(this);
			ViewGroup parent = (ViewGroup) depth.getParent();
			parent.addView(seekBar, parent.indexOfChild(depth) + 1);

			// Set the min and max depth values
			TextView minView = v.findViewById(R.id.min);
			TextView maxView = v.findViewById(R.id.max);
			minView.setText(String.format(Locale.getDefault(), "%1$.0f ft", mValues.getDouble(PoolDataAdapter.VALUE_MIN_DEPTH)));
			maxView.setText(String.format(Locale.getDefault(), "%1$.0f ft", mValues.getDouble(PoolDataAdapter.VALUE_MAX_DEPTH)));
		}

		final EditText volume = v.findViewById(R.id.volume);
		if (volume != null) {
			Double val = mValues.getDouble(PoolDataAdapter.VALUE_VOLUME);

			volume.setText((val > 0) ? String.format(Locale.getDefault(), "%1$,.0f", val) : "");

			// Use a TextWatcher to update the value when the input changes
			volume.addTextChangedListener(new TextWatcher() {

				private boolean isUserInput = true;
				private int cursorPosition = 0;

				public void afterTextChanged(Editable s) {
					if (isUserInput) {
						String t = s.toString().replace(",", "");

						// Don't parse an empty string
						if (t.equals("")) {
							isUserInput = false;
							volume.setText("");
							volume.setSelection(0, 0);
							return;
						}

						int origLength = s.toString().length();
						double n = Double.parseDouble(t);
						int pos = volume.getSelectionStart();

						String newText = String.format(Locale.getDefault(), "%1$,.0f", n);
						int newLength = newText.length();
						cursorPosition = pos - (origLength - newLength);

						isUserInput = false;
						volume.setText(newText);
						volume.setSelection(cursorPosition, cursorPosition);
					} else {
						if (s.toString().equals("")) {
							mValues.putDouble(PoolDataAdapter.VALUE_VOLUME, -1.00d);
						} else {
							mValues.putDouble(PoolDataAdapter.VALUE_VOLUME, Double.parseDouble(s.toString().replace(",", "")));
						}
						if (mValues.getDouble(PoolDataAdapter.VALUE_VOLUME) > 0) {
							mValues.putString(PoolDataAdapter.VALUE, String.format(Locale.getDefault(), "%1$,.0f ft - %2$,.0f ft\n%3$s g", mValues.getDouble(PoolDataAdapter.VALUE_MIN_DEPTH), mValues.getDouble(PoolDataAdapter.VALUE_MAX_DEPTH), String.format(Locale.getDefault(), "%1$,.0f", mValues.getDouble(PoolDataAdapter.VALUE_VOLUME))));
						} else {
							mValues.putString(PoolDataAdapter.VALUE, "");
						}
						isUserInput = true;
					}
				}

				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
			});

			volume.setOnEditorActionListener((v12, actionId, event) -> {
				if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) || actionId == EditorInfo.IME_ACTION_DONE) {
					if (mValues.getDouble(PoolDataAdapter.VALUE_VOLUME) == -1.00d) {
						negativeDecision();
					} else {
						positiveDecision();
					}
				}
				return true;
			});
		}

		// Set click listeners for the cancel button
		mCancelButton.setOnClickListener(v1 -> {
			mValues.putDouble(PoolDataAdapter.VALUE_MIN_DEPTH, initMinDepth);
			mValues.putDouble(PoolDataAdapter.VALUE_MAX_DEPTH, initMaxDepth);
			mValues.putDouble(PoolDataAdapter.VALUE_VOLUME, initVolume);
			mValues.putString(PoolDataAdapter.VALUE, String.format(Locale.getDefault(), "%1$,.0f ft - %2$,.0f ft\n%3$s g", initMinDepth, initMaxDepth, initVolume));
			negativeDecision();
		});

		return v;
	}

	@Override
	public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {

		((TextView) mInputView.findViewById(R.id.min)).setText(String.format(Locale.getDefault(), "%1$d ft", minValue));
		mValues.putDouble(PoolDataAdapter.VALUE_MIN_DEPTH, Double.valueOf(minValue));
		((TextView) mInputView.findViewById(R.id.max)).setText(String.format(Locale.getDefault(), "%1$d ft", maxValue));
		mValues.putDouble(PoolDataAdapter.VALUE_MAX_DEPTH, Double.valueOf(maxValue));
		if (mValues.getDouble(PoolDataAdapter.VALUE_VOLUME) > 0) {
			mValues.putString(PoolDataAdapter.VALUE, String.format(Locale.getDefault(), "%1$.0f ft - %2$.0f ft\n%3$s g", mValues.getDouble(PoolDataAdapter.VALUE_MIN_DEPTH), mValues.getDouble(PoolDataAdapter.VALUE_MAX_DEPTH), String.format("%1$,.0f", mValues.getDouble(PoolDataAdapter.VALUE_VOLUME))));
		} else {
			mValues.putString(PoolDataAdapter.VALUE, "");
		}
	}
}
