package com.instinotices.satyam.stackexchange;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;

public class TagsRecyclerViewAdapter extends RecyclerView.Adapter<TagsRecyclerViewAdapter.ViewHolder> {

    private final InteractionListener mInteractionListener;
    private ArrayList<String> mTags;
    private ArrayList<String> mSelectedTags;
    private ArrayList<Boolean> checkedStatus;
    private int mCheckedItemCount = 0;

    public TagsRecyclerViewAdapter(ArrayList<String> items, ArrayList<String> selectedTags, InteractionListener listener) {
        mTags = items;
        mInteractionListener = listener;
        mSelectedTags = selectedTags;
        mCheckedItemCount = mSelectedTags.size();
        checkedStatus = new ArrayList<>();
        for (String tag : mTags
        ) {
            if (selectedTags.contains(tag)) {
                checkedStatus.add(true);
            } else checkedStatus.add((false));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mTagName = mTags.get(position);
        final String tagName = mTags.get(position);
        final int position1 = position;
        holder.mCheckBox.setText(mTags.get(position));
        holder.mCheckBox.setOnCheckedChangeListener(null);
        holder.mCheckBox.setChecked(checkedStatus.get(position));
        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkedStatus.set(position1, isChecked);
                if (isChecked) {
                    mSelectedTags.add(tagName);
                    mCheckedItemCount += 1;
                } else {
                    mSelectedTags.remove(tagName);
                    mCheckedItemCount -= 1;
                }
                mInteractionListener.onCheckedItemCountChanged(mCheckedItemCount);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTags.size();
    }

    public ArrayList<String> getCheckedItems() {
        return mSelectedTags;
    }

    interface InteractionListener {
        void onCheckedItemCountChanged(int count);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final CheckBox mCheckBox;
        public String mTagName;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mCheckBox = view.findViewById(R.id.checkBox);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mCheckBox.getText() + "'";
        }
    }
}
