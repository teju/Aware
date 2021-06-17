package com.pascalabs.util.log.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.iapps.common_library.R;
import com.iapps.logs.com.pascalabs.util.log.model.BeanLog;

import java.util.ArrayList;

public class LogAdapter
	extends BaseAdapter implements Filterable {

	private ArrayList<BeanLog> items;
	private ArrayList<BeanLog> itemsOriginal;
	private Context context;
	private boolean isFilterable = true;
	public void setFilterable(boolean filterable) {
		isFilterable = filterable;
	}

	public LogAdapter(Context context, ArrayList<BeanLog> list) {
		items = new ArrayList<BeanLog>();
		itemsOriginal = list;
		this.context = context;
		this.items = list;
	}

	public ArrayList<BeanLog> getItems() {
		return items;
	}

	public void setItems(ArrayList<BeanLog> items) {
		this.itemsOriginal = items;
	}

	@Override
	public int getCount() {
        try {
            return items.size();
        } catch (Exception e) {
            return 0;
        }
    }

	@Override
	public BeanLog getItem(int pos) {
		return items.get(pos);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		holder = new ViewHolder();
		if (convertView == null) {

			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final Context contextThemeWrapper = context;
			LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

			convertView = localInflater.inflate(R.layout.cell_log, parent, false);
		}

		holder.type = (TextView) convertView.findViewById(R.id.logtype);
		holder.date = (TextView) convertView.findViewById(R.id.logdate);
		holder.event = (TextView) convertView.findViewById(R.id.logevent);

        BeanLog o = getItem(position);
        holder.event.setText(o.getEvent());
		holder.type.setText(o.getType());
        try {
			holder.date.setText("(" + o.getTimestamp() + ") " + DateUtils.getRelativeTimeSpanString(o.getTimestampMilis()));
        } catch (Exception e) {
			holder.date.setText("");
		}

		convertView.setTag(holder);

		return convertView;
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}

	public static class ViewHolder {
		TextView type;
		TextView event;
		TextView date;
	}

	private Filter mFilter = new Filter() {

		@Override
		public String convertResultToString(Object resultValue) {
			return "";
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			String filterString = constraint.toString().toLowerCase().trim();

			if(filterString.equalsIgnoreCase("show all")){
				FilterResults results = new FilterResults();
				results.values = itemsOriginal;
				results.count = itemsOriginal.size();
				return  results;
			}

			FilterResults results = new FilterResults();
			if(isFilterable) {
				final ArrayList<BeanLog> originalItems = itemsOriginal;
				int count = originalItems.size();
				final ArrayList<BeanLog> nValue = new ArrayList<>(count);

				for(int i = 0 ;  i < count; i++) {
					try {
						if(originalItems.get(i).getType().equalsIgnoreCase(filterString.toLowerCase())){
							BeanLog beanLog = new BeanLog(originalItems.get(i).getType(),
									originalItems.get(i).getEvent(),
									originalItems.get(i).getTimestamp());
							beanLog.setTimestampMilis(originalItems.get(i).getTimestampMilis());
							beanLog.setBeanLogAPI(originalItems.get(i).getBeanLogAPI());
							nValue.add(beanLog);
						}
					} catch (Exception e) {}
				}
				results.values = nValue;
				results.count = nValue.size();
			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			if(isFilterable) {
				items = (ArrayList<BeanLog>) results.values;
			}
			notifyDataSetChanged();
		}
	};
}
