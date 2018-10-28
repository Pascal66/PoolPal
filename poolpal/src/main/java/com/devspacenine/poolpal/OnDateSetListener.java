package com.devspacenine.poolpal;

import com.devspacenine.poolpal.fragment.*;

import java.util.*;

public interface OnDateSetListener {
	/**
	 * this method is called when a date was selected by the user
	 *
	 * @param view the caller of the method
	 */
	void onDateSet(DatePickerDialogFragment view, Calendar selectedDate);
}
