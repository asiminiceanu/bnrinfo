package com.android.bnrinfo;

import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayAdapter extends BaseAdapter {

    /*
     * The context inflater
     */
    private LayoutInflater mInflater;

    /*
     * Application context
     */
    Context mcontext;

    /**
     * The display adapter constructor
     * 
     * @param context
     */
    public DisplayAdapter(Context context) {
        // Cache the LayoutInflate to avoid asking for a new one each time.
        mInflater = LayoutInflater.from(context);
        // Save the context
        mcontext = context;
    }

    /**
     * The number of items in the list is determined by the number of speeches
     * in our array.
     * 
     * @see android.widget.ListAdapter#getCount()
     */
    @Override
	public int getCount() {
        //return BnrInfo.ratesType.size();
    	return BnrInfo.currencies.size();
    }

    /**
     * Since the data comes from an array, just returning the index is
     * sufficient to get at the data. If we were using a more complex data
     * structure, we would return whatever object represents one row in the
     * list.
     * 
     * @see android.widget.ListAdapter#getItem(int)
     */
    @Override
	public Object getItem(int position) {
        return position;
    }

    /**
     * Use the array index as a unique id.
     * 
     * @see android.widget.ListAdapter#getItemId(int)
     */
    @Override
	public long getItemId(int position) {
        return position;
    }

    /**
     * Make a view to hold each row.
     * 
     * @see android.widget.ListAdapter#getView(int, android.view.View,
     *      android.view.ViewGroup)
     */
    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary
        // calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no
        // need
        // to re-inflate it. We only inflate a new View when the convertView
        // supplied
        // by ListView is null.
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);

            // Creates a ViewHolder and store references to the two children
            // views
            // we want to bind data to.
            holder = new ViewHolder();
            holder.rateType = (TextView) convertView.findViewById(R.id.rate_type);
            holder.rateValue = (TextView) convertView.findViewById(R.id.rate_value);
            holder.rateDiff = (TextView) convertView.findViewById(R.id.rate_diff);
            holder.rateDesc = (TextView) convertView.findViewById(R.id.rate_desc);
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);

            convertView.setTag(holder);
        } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind the data efficiently with the holder.
        Map<String, String> item = BnrInfo.currencies.get(position);

        holder.rateType.setText(item.get("multiplier") + " " + item.get("currency"));
        holder.rateValue.setText(item.get("value") + " RON");

        String diffText = new String(item.get("diff"));
        holder.rateDiff.setText(diffText.toString());

        if (diffText.startsWith("+")) {
            holder.rateDiff.setTextColor(mcontext.getResources().getColor(R.color.color_plus));
        } else {
            holder.rateDiff.setTextColor(mcontext.getResources().getColor(R.color.color_minus));
        }

        holder.rateDesc.setText(mcontext.getString(mcontext.getResources().getIdentifier(
                "desc_" + item.get("currency"), "string", "com.android.bnrinfo")));

        Bitmap bm = BitmapFactory.decodeResource(
                mcontext.getResources(),
                mcontext.getResources().getIdentifier("ico_" + item.get("currency").toLowerCase(),
                        "drawable", "com.android.bnrinfo"));
        holder.icon.setImageBitmap(bm);

        return convertView;
    }

    static class ViewHolder {
        TextView rateType;
        TextView rateValue;
        TextView rateDiff;
        TextView rateDesc;
        ImageView icon;
    }
}