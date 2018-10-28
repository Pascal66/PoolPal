package com.devspacenine.poolpal.fragment;

import android.os.*;
import android.view.*;

import com.devspacenine.poolpal.*;

import androidx.fragment.app.*;

public class ChartsFragment extends Fragment {

	/**
	 * Inflate and setup the UI
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.charts, container, false);

		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (outState.isEmpty()) {
			outState.putBoolean("bug:fix", true);
		}
	}
}
