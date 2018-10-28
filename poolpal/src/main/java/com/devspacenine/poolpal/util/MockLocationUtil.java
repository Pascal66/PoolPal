package com.devspacenine.poolpal.util;

import android.app.*;
import android.content.*;
import android.location.*;
import android.util.*;

public class MockLocationUtil {

	public static final String TAG = "DSN Debug";
	public static final String PROVIDER_NAME = "testProvider";

	public static void publishMockLocation(double latitude, double longitude, Context ctx, LocationListener listener) {
		LocationManager manager = (LocationManager) ctx.getSystemService(Service.LOCATION_SERVICE);
		publishMockLocation(latitude, longitude, ctx, manager, listener, PROVIDER_NAME);
	}

	public static void publishMockLocation(double latitude, double longitude, Context ctx, LocationManager manager, LocationListener listener, String provider) {
		Location newLocation = new Location(provider);

		newLocation.setLatitude(latitude);
		newLocation.setLongitude(longitude);
		newLocation.setTime(System.currentTimeMillis());
		newLocation.setAccuracy(25);
		try {
			manager.requestLocationUpdates(provider, 0, 0, listener);

			manager.setTestProviderLocation(provider, newLocation);
			Log.w(TAG, "published location: " + newLocation);

			Log.w(TAG, "LastKnownLocation of " + provider + " is: " + manager.getLastKnownLocation(provider));
		} catch (SecurityException ignore) {
		}
	}

	public static void createMockLocationProvider(LocationManager manager, String provider) {
		Log.w(TAG, "Providers: " + manager.getAllProviders().toString());
		if (manager.getProvider(provider) != null) {
			Log.w(TAG, "Removing provider " + provider);
			manager.removeTestProvider(provider);
		}
		Log.w(TAG, "Providers: " + manager.getAllProviders().toString());

		if (manager.getProvider(provider) == null) {
			Log.w(TAG, "Adding provider " + provider + " again");
			manager.addTestProvider(provider, "requiresNetwork".equals(""), "requiresSatellite".equals(""), "requiresCell".equals(""), "hasMonetaryCost".equals(""), "supportsAltitude".equals(""), "supportsSpeed".equals(""), "supportsBearing".equals(""), android.location.Criteria.POWER_LOW, android.location.Criteria.ACCURACY_FINE);
		}
		Log.w(TAG, "Providers: " + manager.getAllProviders().toString());

		manager.setTestProviderEnabled(provider, true);

		manager.setTestProviderStatus(provider, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
	}

	public static Location getLastKnownLocationInApplication(Context ctx) {
		LocationManager lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		return getLastKnownLocationInApplication(ctx, lm, PROVIDER_NAME);
	}

	public static Location getLastKnownLocationInApplication(Context ctx, LocationManager manager, String provider) {
		Location testLoc = null;
		try {
			if (manager.getAllProviders().contains(provider)) {
				testLoc = manager.getLastKnownLocation(provider);
				Log.d(TAG, "TestLocation: " + testLoc);
			}

			if (testLoc == null) {
				return manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}

		} catch (SecurityException ignore) {
		}
		return testLoc;
	}
}