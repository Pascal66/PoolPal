package com.devspacenine.poolpal.fragment;

import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;

import com.devspacenine.poolpal.*;

import androidx.fragment.app.*;

public class TutorialFragment extends DialogFragment {

	public static final int WELCOME = 1;
	public static final int CONTINUE_EDITING = 2;

	// Creator tags
	public static final String REQUEST_CODE = "request_code";
	public static final String TITLE = "title";
	public static final String LAYOUT = "layout";
	public static final String FRAGMENT_TAG = "fragment_tag";
	public static final String DATA = "data";

	// Data tags
	public static final String DATA_TITLE = "title";
	public static final String DATA_DETAILS = "details";
	public static final String DATA_CANCEL = "cancel";
	public static final String DATA_CONFIRM = "confirm";

	public static TutorialFragment newInstance(int requestCode, int layout, Bundle data, String fragmentTag) {

		TutorialFragment frag = new TutorialFragment();
		Bundle args = new Bundle();

		args.putInt(REQUEST_CODE, requestCode);
		args.putInt(LAYOUT, layout);
		args.putBundle(DATA, data);
		args.putString(FRAGMENT_TAG, fragmentTag);

		frag.setArguments(args);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedState) {

		super.onCreate(savedState);

		setStyle(STYLE_NO_FRAME, R.style.Theme_PoolPal_Transparent);
		setCancelable(false);

		if (savedState != null) {
		} else {
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Bundle args = getArguments();

		Bundle data = args.getBundle(DATA);

		View v = inflater.inflate(args.getInt(LAYOUT), container, false);

		TextView title = v.findViewById(R.id.title);
		title.setText(data.getInt(DATA_TITLE));

		switch (args.getInt(REQUEST_CODE)) {
			case WELCOME:
				((TextView) v.findViewById(R.id.details)).setText(data.getInt(DATA_DETAILS));

				TextView start = v.findViewById(R.id.cancel);
				start.setText(data.getInt(DATA_CANCEL));

				start.setOnClickListener(v13 -> {
					Intent intent = new Intent(getActivity(), EditPoolActivity.class);
					intent.putExtra(PoolPal.EXTRA_PAGE, R.layout.init_pool);
					startActivity(intent);
					dismiss();
				});

				TextView tour = v.findViewById(R.id.confirm);
				tour.setText(data.getInt(DATA_CONFIRM));

				break;

			case CONTINUE_EDITING:
				((TextView) v.findViewById(R.id.details)).setText(data.getInt(DATA_DETAILS));

				TextView finish = v.findViewById(R.id.cancel);
				finish.setText(data.getInt(DATA_CANCEL));
				finish.setOnClickListener(v12 -> {
					getActivity().finish();
					dismiss();
				});

				TextView add_more = v.findViewById(R.id.confirm);
				add_more.setText(data.getInt(DATA_CONFIRM));

				add_more.setOnClickListener(v1 -> {
					Intent intent = new Intent(getActivity(), EditPoolActivity.class);
					startActivity(intent);
					getActivity().finish();
					dismiss();
				});

				break;

			default:
				break;
		}

		return v;
	}
}
