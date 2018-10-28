package com.devspacenine.poolpal.fragment;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.view.*;
import android.widget.*;

import com.devspacenine.externalstoragelibrary.*;
import com.devspacenine.poolpal.*;
import com.devspacenine.poolpal.R;

import java.io.*;
import java.text.*;
import java.util.*;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.*;

public class SetImageDialogFragment extends InputDialogFragment {

	private final ExternalStorageManager mExternalStorageManager;
	private TempDataListener mTempDataListener;

	public static SetImageDialogFragment newInstance(Bundle args) {

		SetImageDialogFragment frag = new SetImageDialogFragment();
		frag.setArguments(args);
		return frag;
	}

	public SetImageDialogFragment() {
		super();
		mExternalStorageManager = ExternalStorageManager.getInstance();
	}

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);

		// Make sure the calling fragment or activity implements OnDecisionMadeListener
		if (getArguments().containsKey(FRAGMENT_TAG)) {
			Fragment frag = ((FragmentActivity) activity).getSupportFragmentManager().findFragmentByTag(getArguments().getString(FRAGMENT_TAG));
			try {
				mTempDataListener = (TempDataListener) frag;
			} catch (ClassCastException e) {
				throw new ClassCastException(frag + " must implement the OnDecisionMadeListener interface.");
			}
		} else {
			try {
				mTempDataListener = (TempDataListener) activity;
			} catch (ClassCastException e) {
				throw new ClassCastException(activity + " must implement the OnDecisionMadeListener interface.");
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {

		Bundle args = getArguments();

		View v;
		v = inflater.inflate(R.layout.options_dialog, container, false);

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

		final ArrayList<String> options = new ArrayList<>(Arrays.asList("Choose from Gallery", "Capture a Photo"));
		ArrayAdapter<String> choices = new ArrayAdapter<>(mCtx, R.layout.options_item, options);
		final ListView list = mInputView.findViewById(R.id.list);
		list.setAdapter(choices);
		list.setOnItemClickListener((parent, view, position, id) -> {
			Intent intent;
			switch (position) {
				case 0:
					// Create an intent to open the image gallery browser
					intent = new Intent();
					intent.setType("image/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					intent.addCategory(Intent.CATEGORY_OPENABLE);
					break;
				case 1:
					// Make sure the SDCard is mounted
					if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
						Toast.makeText(mCtx, "SDCard must be mounted", Toast.LENGTH_LONG).show();
						return;
					}

					// Create or open the directory to save new images in
					File mediaStorageDir = mExternalStorageManager.getPictureDirectory("PoolPal");

					// Create the storage directory if it does not exist
					mediaStorageDir.mkdirs();

					// Create a media file name
					String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
					File pictureFile = new File(mediaStorageDir.getPath() + File.separator + mPool.getName() + "_" + timeStamp + ".jpg");
					Uri pictureUri = Uri.fromFile(pictureFile);

					// Create an intent to open the camera, and save the new image
					// to the Uri of the new file
					intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);

					// Save the uri as temp data so the parent activity can get it if the
					// capture intent returns successfully
					Bundle data = new Bundle();
					data.putString(PoolPal.EXTRA_IMAGE_URI, pictureUri.toString());
					mTempDataListener.saveTempData(data);
					break;
				default:
					return;
			}

			getActivity().startActivityForResult(intent, PoolPal.SET_IMAGE);
			dismiss();
		});

		return v;
	}
}
