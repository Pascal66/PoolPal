package com.devspacenine.poolpal.view;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.util.*;

import com.devspacenine.poolpal.*;

import androidx.appcompat.widget.*;

public class RobotoEditText extends AppCompatEditText {

	public RobotoEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		//Typeface.createFromAsset doesn't work in the layout editor. Skipping...
		if (isInEditMode()) {
			return;
		}

		Typeface typeface = Typeface.createFromAsset(context.getAssets(), getStyle(context, attrs, defStyle));
		setTypeface(typeface);
	}

	public RobotoEditText(Context context, AttributeSet attrs) {
		super(context, attrs);

		//Typeface.createFromAsset doesn't work in the layout editor. Skipping...
		if (isInEditMode()) {
			return;
		}

		Typeface typeface = Typeface.createFromAsset(context.getAssets(), getStyle(context, attrs, 0));
		setTypeface(typeface);
	}

	public RobotoEditText(Context context) {
		this(context, null);
	}

	public String getStyle(Context ctx, AttributeSet attrs, int defStyle) {

		if (attrs != null) {
			TypedArray styledAttrs = ctx.obtainStyledAttributes(attrs, R.styleable.RobotoEditText, 0, defStyle);
			int style = styledAttrs.getInt(R.styleable.RobotoEditText_textStyle, 0);

			styledAttrs.recycle();

			switch (style) {
				case 7:
				case 6:
					return "Roboto-LightItalic.ttf";
				case 5:
				case 4:
					return "Roboto-Light.ttf";
				case 3:
					return "Roboto-BoldItalic.ttf";
				case 2:
					return "Roboto-Italic.ttf";
				case 1:
					return "Roboto-Bold.ttf";
				case 0:
				default:
					return "Roboto-Regular.ttf";
			}
		} else {
			return "Roboto-Regular.ttf";
		}
	}
}
