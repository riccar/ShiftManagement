package com.deputy.shiftmanager.shift.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.deputy.shiftmanager.R;
import com.deputy.shiftmanager.shift.ShiftDetailActivity;
import com.deputy.shiftmanager.shift.ShiftDetailFragment;
import com.deputy.shiftmanager.shift.ShiftListActivity;
import com.deputy.shiftmanager.shift.model.Shift;

import java.util.List;

/**
 * Created by Ricardo on 23/02/2017.
 */

public class RecyclerViewAdapter
        extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private final List<Shift.ShiftItem> mValues;
    private final Context mContext;

    public RecyclerViewAdapter(List<Shift.ShiftItem> items, Context appContext) {
        mValues = items;
        mContext = appContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shift_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).id);
        holder.mContentView.setText(mValues.get(position).start);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //If device has two panes replace shift_detail_container
                if (ShiftListActivity.TWO_PANES) {
                    Bundle arguments = new Bundle();
                    arguments.putInt(ShiftDetailFragment.ARG_ITEM_ID, Integer.valueOf(holder.mItem.id));
                    //arguments.putInt(ShiftDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                    //arguments.putBundle("shifts", mValues);
                    ShiftDetailFragment fragment = new ShiftDetailFragment();
                    fragment.setArguments(arguments);

                    FragmentManager fragmentManager =  ((FragmentActivity) mContext).getSupportFragmentManager();
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
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;

        public Shift.ShiftItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);

        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }


    }
}
