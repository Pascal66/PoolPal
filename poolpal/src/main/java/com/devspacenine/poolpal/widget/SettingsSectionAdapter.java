package com.devspacenine.poolpal.widget;

import android.os.*;
import android.view.*;
import android.widget.*;

import com.devspacenine.poolpal.*;
import com.devspacenine.poolpal.object.*;

import java.util.*;

import androidx.fragment.app.*;

public class SettingsSectionAdapter extends BaseAdapter {

	public final Map<String, Adapter> sections = new LinkedHashMap<>();
	public final ArrayAdapter<String> headers;
	public final static int TYPE_SECTION_HEADER = 0;
	private Pool mPool;

	public SettingsSectionAdapter(FragmentActivity context, Pool pool) {
		headers = new ArrayAdapter<>(context, R.layout.settings_header, R.id.section_title);
		mPool = pool;
	}

	public void addSection(String section, Adapter adapter) {
		this.headers.add(section);
		this.sections.put(section, adapter);
	}

	public void setPool(Pool pool) {
		mPool = pool;
		// Set the pool for every applicable adapter
		for (String section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			if (adapter instanceof PoolDataAdapter) {
				((PoolDataAdapter) adapter).setPool(pool);
			}
		}
	}

	public Pool getPool() {
		return mPool;
	}

	public Object getItem(int position) {
		for (String section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) return section;
			if (position < size) return adapter.getItem(position - 1);

			// otherwise jump into next section
			position -= size;
		}
		return null;
	}

	public Adapter getAdapter(int position) {
		for (String section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) return headers;
			if (position < size) return adapter;

			position -= size;
		}
		return null;
	}

	public int getCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for (Adapter adapter : this.sections.values())
			total += adapter.getCount() + 1;
		return total;
	}

	public int getViewTypeCount() {
		// assume that headers count as one, then total all sections
		int total = 1;
		for (Adapter adapter : this.sections.values())
			total += adapter.getViewTypeCount();
		return total;
	}

	public int getItemViewType(int position) {
		int type = 1;
		for (String section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) return TYPE_SECTION_HEADER;
			if (position < size) return type + adapter.getItemViewType(position - 1);

			// otherwise jump into next section
			position -= size;
			type += adapter.getViewTypeCount();
		}
		return -1;
	}

	public boolean areAllItemsSelectable() {
		return false;
	}

	public boolean isEnabled(int position) {
		return (getItemViewType(position) != TYPE_SECTION_HEADER);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int sectionnum = 0;

		for (String section : this.sections.keySet()) {
			Adapter adapter = sections.get(section);
			int size = adapter.getCount() + 1;

			// check if position inside this section
			if (position == 0) return headers.getView(sectionnum, convertView, parent);
			if (position < size) return adapter.getView(position - 1, convertView, parent);

			// otherwise jump into next section
			position -= size;
			sectionnum++;
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public boolean allItemsSet() {

		for (int i = 0; i < getCount(); i++) {
			Object o = getItem(i);
			if (o instanceof Bundle) {
				if (!((Bundle) o).getBoolean(PoolDataAdapter.ITEM_SET, false)) {
					return false;
				}
			}
		}
		return true;
	}
}