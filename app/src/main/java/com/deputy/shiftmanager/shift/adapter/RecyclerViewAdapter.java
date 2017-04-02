/**
 * A class extending from RecycleViewAdapter that handles the RecycleView structure
 * to show all the shifts as a list view. It also handles the click of every shift item
 * so it shows the proper detail view depending on the device screen size
 */

package com.deputy.shiftmanager.shift.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.deputy.shiftmanager.R;
import com.deputy.shiftmanager.shift.ShiftDetailActivity;
import com.deputy.shiftmanager.shift.ShiftDetailFragment;
import com.deputy.shiftmanager.shift.ShiftListActivity;
import com.deputy.shiftmanager.shift.Util.date;
import com.deputy.shiftmanager.shift.model.Shift;

import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerViewAdapter
        extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private final String LOG_TAG = RecyclerViewAdapter.class.getSimpleName();
    private final List<Shift.ShiftItem> shiftItems;
    private final Context context;


    public RecyclerViewAdapter(List<Shift.ShiftItem> items, Context appContext) {
        shiftItems = items;
        context = appContext;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shift_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //Because the list of shift was sorted in reverse, instead of getting shift
        //in the position of the clicked value,
        //assign to holder.mItem  the shift in the position that matches the id of the clicked value
        //holder.mItem = mValues.get(position);
        holder.mItem = shiftItems.get(Integer.valueOf(shiftItems.get(position).id) - 1);
        holder.idView.setText(shiftItems.get(position).id);
        holder.contentView.setText(date.formatStringDate(shiftItems.get(position).start, context));

        Uri imageUri = Uri.parse(shiftItems.get(position).image);

        Picasso
                .with(context)
                .load(imageUri)
                .fit()
                //.memoryPolicy(MemoryPolicy.NO_CACHE)
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imageView);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //If device has two panes replace shift_detail_container
                if (ShiftListActivity.TWO_PANES) {
                    Bundle arguments = new Bundle();
                    arguments.putString(ShiftDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                    ShiftDetailFragment fragment = new ShiftDetailFragment();
                    fragment.setArguments(arguments);

                    FragmentManager fragmentManager =  ((FragmentActivity) context).getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.shift_detail_container, fragment)
                            .commit();
                } else {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ShiftDetailActivity.class);
                    intent.putExtra(ShiftDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return shiftItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView idView;
        public final TextView contentView;
        public final ImageView imageView;

        public Shift.ShiftItem mItem;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            this.idView = (TextView) view.findViewById(R.id.id);
            this.contentView = (TextView) view.findViewById(R.id.content);
            this.imageView = (ImageView) view.findViewById(R.id.image);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + contentView.getText() + "'";
        }


    }
}
