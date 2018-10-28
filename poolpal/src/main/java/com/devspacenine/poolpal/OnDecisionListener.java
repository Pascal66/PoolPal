package com.devspacenine.poolpal;

import android.os.*;

public interface OnDecisionListener {

	void onPositiveDecision(int requestCode, Bundle values);

	void onNegativeDecision(int requestCode);
}
