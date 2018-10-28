package com.devspacenine.poolpal.fragment;

import android.os.*;
import android.text.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;

import com.devspacenine.poolpal.*;
import com.devspacenine.poolpal.widget.*;

public class TextInputDialogFragment extends InputDialogFragment {

	public static TextInputDialogFragment newInstance(Bundle args) {

		TextInputDialogFragment frag = new TextInputDialogFragment();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {

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

		// Setup the layout depending on what value is being set
		switch (mRequestCode) {
			case PoolDataAdapter.SET_NAME:

				if (!mValues.containsKey(PoolDataAdapter.VALUE_NAME) || mValues.getString(PoolDataAdapter.VALUE_NAME) == null) {
					mValues.putString(PoolDataAdapter.VALUE_NAME, "");
				}
				final String initName = mValues.getString(PoolDataAdapter.VALUE_NAME);

				EditText name_input = mInputView.findViewById(R.id.input1);
				name_input.setHint(R.string.hint_pool_name);
				name_input.setText(mValues.getString(PoolDataAdapter.VALUE_NAME));

				name_input.setOnFocusChangeListener((v112, hasFocus) -> {
					if (hasFocus) {
						getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
					}
				});

				name_input.setOnEditorActionListener((v111, actionId, event) -> {
					if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) || actionId == EditorInfo.IME_ACTION_DONE) {
						if (mValues.getString(PoolDataAdapter.VALUE_NAME).equals("")) {
							mValues.putString(PoolDataAdapter.VALUE_NAME, initName);
							mValues.putString(PoolDataAdapter.VALUE, initName);
							negativeDecision();
						} else {
							positiveDecision();
						}
					}
					return true;
				});

				// Use TextWatchers to update the value if any edittexts change
				name_input.addTextChangedListener(new TextWatcher() {

					public void afterTextChanged(Editable s) {
						mValues.putString(PoolDataAdapter.VALUE_NAME, s.toString());
						mValues.putString(PoolDataAdapter.VALUE, mValues.getString(PoolDataAdapter.VALUE_NAME));
					}

					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
				});

				// Set click listeners for the confirm button
				mConfirmButton.setOnClickListener(v110 -> {
					if (mValues.getString(PoolDataAdapter.VALUE_NAME).equals("")) {
						mValues.putString(PoolDataAdapter.VALUE_NAME, initName);
						mValues.putString(PoolDataAdapter.VALUE, initName);
						negativeDecision();
					} else {
						positiveDecision();
					}
				});

				// Set click listeners for the cancel button
				mCancelButton.setOnClickListener(v19 -> {
					mValues.putString(PoolDataAdapter.VALUE_NAME, initName);
					mValues.putString(PoolDataAdapter.VALUE, initName);
					negativeDecision();
				});
				break;

			case PoolDataAdapter.SET_PUMP:

				// Set the current values
				if (!mValues.containsKey(PoolDataAdapter.VALUE_PUMP_BRAND) || mValues.getString(PoolDataAdapter.VALUE_PUMP_BRAND) == null) {
					mValues.putString(PoolDataAdapter.VALUE_PUMP_BRAND, "");
				}
				if (!mValues.containsKey(PoolDataAdapter.VALUE_PUMP_MODEL) || mValues.getString(PoolDataAdapter.VALUE_PUMP_MODEL) == null) {
					mValues.putString(PoolDataAdapter.VALUE_PUMP_MODEL, "");
				}
				final String initPumpBrand = mValues.getString(PoolDataAdapter.VALUE_PUMP_BRAND);
				final String initPumpModel = mValues.getString(PoolDataAdapter.VALUE_PUMP_MODEL);

				// Set the hint and initial values
				final EditText pump_brand = v.findViewById(R.id.input1);
				pump_brand.setHint(R.string.hint_brand);
				pump_brand.setText(initPumpBrand);
				final EditText pump_model = v.findViewById(R.id.input2);
				pump_model.setHint(R.string.hint_model);
				pump_model.setText(initPumpModel);

				// Use TextWatchers to update the value if any edittexts change
				pump_brand.addTextChangedListener(new TextWatcher() {

					public void afterTextChanged(Editable s) {
						mValues.putString(PoolDataAdapter.VALUE_PUMP_BRAND, s.toString());
						if (mValues.getString(PoolDataAdapter.VALUE_PUMP_BRAND).equals("") || mValues.getString(PoolDataAdapter.VALUE_PUMP_MODEL).equals("")) {
							mValues.putString(PoolDataAdapter.VALUE, "");
						} else {
							mValues.putString(PoolDataAdapter.VALUE, String.format("%1$s - %2$s", s.toString(), mValues.getString(PoolDataAdapter.VALUE_PUMP_MODEL)));
						}
					}

					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
				});

				pump_brand.setOnEditorActionListener((v18, actionId, event) -> {
					if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
						pump_model.requestFocus();
					}
					return true;
				});

				pump_model.addTextChangedListener(new TextWatcher() {

					public void afterTextChanged(Editable s) {
						mValues.putString(PoolDataAdapter.VALUE_PUMP_MODEL, s.toString());
						if (mValues.getString(PoolDataAdapter.VALUE_PUMP_BRAND).equals("") || mValues.getString(PoolDataAdapter.VALUE_PUMP_MODEL).equals("")) {
							mValues.putString(PoolDataAdapter.VALUE, "");
						} else {
							mValues.putString(PoolDataAdapter.VALUE, String.format("%1$s - %2$s", mValues.getString(PoolDataAdapter.VALUE_PUMP_BRAND), s.toString()));
						}
					}

					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
				});

				pump_model.setOnEditorActionListener((v17, actionId, event) -> {
					if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) || actionId == EditorInfo.IME_ACTION_DONE) {
						if (mValues.getString(PoolDataAdapter.VALUE_PUMP_MODEL).equals("") || mValues.getString(PoolDataAdapter.VALUE_PUMP_BRAND).equals("")) {
							negativeDecision();
						} else {
							positiveDecision();
						}
					}
					return true;
				});

				// Set click listeners for the confirm button
				mConfirmButton.setOnClickListener(v16 -> {
					if (mValues.getString(PoolDataAdapter.VALUE_PUMP_MODEL).equals("") || mValues.getString(PoolDataAdapter.VALUE_PUMP_BRAND).equals("")) {
						mValues.putString(PoolDataAdapter.VALUE_PUMP_BRAND, initPumpBrand);
						mValues.putString(PoolDataAdapter.VALUE_PUMP_MODEL, initPumpModel);
						mValues.putString(PoolDataAdapter.VALUE, String.format("%1$s - %2$s", initPumpBrand, initPumpModel));
						negativeDecision();
					} else {
						positiveDecision();
					}
				});

				// Set click listeners for the cancel button
				mCancelButton.setOnClickListener(v15 -> {
					mValues.putString(PoolDataAdapter.VALUE_PUMP_BRAND, initPumpBrand);
					mValues.putString(PoolDataAdapter.VALUE_PUMP_MODEL, initPumpModel);
					mValues.putString(PoolDataAdapter.VALUE, String.format("%1$s - %2$s", initPumpBrand, initPumpModel));
					negativeDecision();
				});
				break;

			case PoolDataAdapter.SET_CLEANER:

				// Set the current values
				if (!mValues.containsKey(PoolDataAdapter.VALUE_CLEANER_BRAND)) {
					mValues.putString(PoolDataAdapter.VALUE_CLEANER_BRAND, "");
				}
				if (!mValues.containsKey(PoolDataAdapter.VALUE_CLEANER_MODEL)) {
					mValues.putString(PoolDataAdapter.VALUE_CLEANER_MODEL, "");
				}
				final String initCleanerBrand = mValues.getString(PoolDataAdapter.VALUE_CLEANER_BRAND);
				final String initCleanerModel = mValues.getString(PoolDataAdapter.VALUE_CLEANER_MODEL);

				// Set the hint and initial values
				final EditText cleaner_brand = v.findViewById(R.id.input1);
				cleaner_brand.setHint(R.string.hint_brand);
				cleaner_brand.setText(initCleanerBrand);
				final EditText cleaner_model = v.findViewById(R.id.input2);
				cleaner_model.setHint(R.string.hint_model);
				cleaner_model.setText(initCleanerModel);

				// Use TextWatchers to update the value if any edittexts change
				cleaner_brand.addTextChangedListener(new TextWatcher() {

					public void afterTextChanged(Editable s) {
						mValues.putString(PoolDataAdapter.VALUE_CLEANER_BRAND, s.toString());
						if (mValues.getString(PoolDataAdapter.VALUE_CLEANER_BRAND).equals("") || mValues.getString(PoolDataAdapter.VALUE_CLEANER_MODEL).equals("")) {
							mValues.putString(PoolDataAdapter.VALUE, "");
						} else {
							mValues.putString(PoolDataAdapter.VALUE, String.format("%1$s - %2$s", s.toString(), mValues.getString(PoolDataAdapter.VALUE_CLEANER_MODEL)));
						}
					}

					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
				});

				cleaner_brand.setOnEditorActionListener((v14, actionId, event) -> {
					if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
						cleaner_model.requestFocus();
					}
					return true;
				});

				cleaner_model.addTextChangedListener(new TextWatcher() {

					public void afterTextChanged(Editable s) {
						mValues.putString(PoolDataAdapter.VALUE_CLEANER_MODEL, s.toString());
						if (mValues.getString(PoolDataAdapter.VALUE_CLEANER_BRAND).equals("") || mValues.getString(PoolDataAdapter.VALUE_CLEANER_MODEL).equals("")) {
							mValues.putString(PoolDataAdapter.VALUE, "");
						} else {
							mValues.putString(PoolDataAdapter.VALUE, String.format("%1$s - %2$s", mValues.getString(PoolDataAdapter.VALUE_CLEANER_BRAND), s.toString()));
						}
					}

					public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					}

					public void onTextChanged(CharSequence s, int start, int before, int count) {
					}
				});

				cleaner_model.setOnEditorActionListener((v13, actionId, event) -> {
					if ((actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) || actionId == EditorInfo.IME_ACTION_DONE) {
						if (mValues.getString(PoolDataAdapter.VALUE_CLEANER_MODEL).equals("") || mValues.getString(PoolDataAdapter.VALUE_CLEANER_BRAND).equals("")) {
							negativeDecision();
						} else {
							positiveDecision();
						}
					}
					return true;
				});

				// Set click listeners for the confirm button
				mConfirmButton.setOnClickListener(v12 -> {
					if (mValues.getString(PoolDataAdapter.VALUE_CLEANER_MODEL).equals("") || mValues.getString(PoolDataAdapter.VALUE_CLEANER_BRAND).equals("")) {
						mValues.putString(PoolDataAdapter.VALUE_CLEANER_BRAND, initCleanerBrand);
						mValues.putString(PoolDataAdapter.VALUE_CLEANER_MODEL, initCleanerModel);
						mValues.putString(PoolDataAdapter.VALUE, String.format("%1$s - %2$s", initCleanerBrand, initCleanerModel));
						negativeDecision();
					} else {
						positiveDecision();
					}
				});

				// Set click listeners for the cancel button
				mCancelButton.setOnClickListener(v1 -> {
					mValues.putString(PoolDataAdapter.VALUE_CLEANER_BRAND, initCleanerBrand);
					mValues.putString(PoolDataAdapter.VALUE_CLEANER_MODEL, initCleanerModel);
					mValues.putString(PoolDataAdapter.VALUE, String.format("%1$s - %2$s", initCleanerBrand, initCleanerModel));
					negativeDecision();
				});
				break;

			default:
				break;
		}

		return v;
	}
}