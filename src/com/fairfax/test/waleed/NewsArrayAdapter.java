package com.fairfax.test.waleed;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * ListAdapter using the ViewHolder pattern.
 */
public class NewsArrayAdapter extends ArrayAdapter<NewsItem> {
	private static final int IMAGE_WIDTH = 64;
	private static final int IMAGE_HEIGHT = 64;
	private final Activity context;
	private List<NewsItem> items;
	private List<NewsItem> filteredItems;
	private NewsItemFilter filter;
	
	static class ViewHolder {
		public TextView titleView;
		public TextView descriptionView;
		public ImageView newsImageView;
	}
	
	public NewsArrayAdapter(Activity context, List<NewsItem> items) {
		super(context, R.layout.rowlayout, items);
		this.context = context;
		this.items = items;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		// create the view if it does not already exist
		if (rowView == null) {
			LayoutInflater inflator = context.getLayoutInflater();
			rowView = inflator.inflate(R.layout.rowlayout, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.titleView = (TextView) rowView.findViewById(R.id.item_title);
			viewHolder.descriptionView = (TextView) rowView.findViewById(R.id.item_description);
			viewHolder.newsImageView = (ImageView) rowView.findViewById(R.id.item_image);
			rowView.setTag(viewHolder);
		}
		
		if (items != null) {
			ViewHolder vHolder = (ViewHolder) rowView.getTag();
			vHolder.titleView.setText(items.get(position).title);
			vHolder.descriptionView.setText(items.get(position).description);
			BitmapManager.getInstance().loadBitmap(items.get(position).imageHref,
					vHolder.newsImageView, IMAGE_WIDTH, IMAGE_HEIGHT);
		}
		
		return rowView;
	}
	
	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new NewsItemFilter();
		}
		return filter;
	}
	
	private class NewsItemFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence arg0) {
			FilterResults results = new FilterResults();
			List<NewsItem> filtered = new ArrayList<NewsItem>();
			for (NewsItem item : items) {
				if ((item.title == null) && (item.description == null) && (item.imageHref == null)) {
					continue;
				}
				filtered.add(item);
			}
			results.values = filtered;
			results.count = filtered.size();
			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			filteredItems = (List<NewsItem>) results.values;
			clear();
			for (NewsItem item : filteredItems) {
				add(item);
			}
		}
	}
}
