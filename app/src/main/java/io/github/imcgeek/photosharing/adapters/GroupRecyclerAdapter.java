package io.github.imcgeek.photosharing.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.github.imcgeek.photosharing.MainActivity;
import io.github.imcgeek.photosharing.R;
import io.github.imcgeek.photosharing.dataModels.Groups;

/**
 * Created by imcgeek on 25/2/18.
 */

public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.GroupViewHolder> {
    private List<Groups> groupsList;
    private Context context;

    public class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView groupTitle;
        public ImageView thumbnail, overflow;

        public GroupViewHolder(View view) {
            super(view);
            groupTitle = (TextView) view.findViewById(R.id.groupTitle);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            overflow = (ImageView) view.findViewById(R.id.overflow);
            view.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context,MainActivity.class);
            /*intent.putExtra("type",formtype);
            intent.putExtra("input",dataset.get(getLayoutPosition()));
            intent.putExtra("key",dataset.get(getLayoutPosition()).getBuyerKey());*/
            context.startActivity(intent);
        }
    }

    public GroupRecyclerAdapter(Context mContext,List<Groups> groupsList) {
        this.groupsList = groupsList;
        this.context = mContext;
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_group,parent,false);
        return new GroupRecyclerAdapter.GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GroupViewHolder holder, int position) {
        Groups groups = groupsList.get(position);
        holder.groupTitle.setText(groups.getGroupName());
    }

    @Override
    public int getItemCount() {
        return groupsList.size();
    }
}
