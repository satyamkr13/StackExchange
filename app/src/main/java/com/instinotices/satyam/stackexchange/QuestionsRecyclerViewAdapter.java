package com.instinotices.satyam.stackexchange;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.instinotices.satyam.stackexchange.CustomDataTypes.Question;

import java.util.ArrayList;
import java.util.List;


public class QuestionsRecyclerViewAdapter extends RecyclerView.Adapter<QuestionsRecyclerViewAdapter.ViewHolder> {

    private final QuestionsRecyclerViewAdapter.InteractionListener mInteractionListener;
    private ArrayList<Question> mQuestions;

    public QuestionsRecyclerViewAdapter(List<Question> items, QuestionsRecyclerViewAdapter.InteractionListener listener) {
        mInteractionListener = listener;
        if (items != null) mQuestions = new ArrayList<>(items);
        else mQuestions = new ArrayList<>();
    }

    @Override
    public QuestionsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_question, parent, false);
        return new QuestionsRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final QuestionsRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.mTitle.setText(mQuestions.get(position).getTitle());
        String answerDetail = mQuestions.get(position).getAnswer_count() + " answers";
        holder.mAnswersDetail.setText(answerDetail);
        holder.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteractionListener.onSaveQuestion(mQuestions.get(holder.getAdapterPosition()));
            }
        });
        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteractionListener.onShare(mQuestions.get(holder.getAdapterPosition()));
            }
        });
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInteractionListener.onOpenQuestion(mQuestions.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mQuestions.size();
    }

    public void swapData(ArrayList<Question> questionsArrayList) {
        mQuestions = questionsArrayList;
        notifyDataSetChanged();
    }

    public interface InteractionListener {
        void onShare(Question question);

        void onSaveQuestion(Question question);

        void onOpenQuestion(Question question);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        TextView mTitle, mAnswersDetail;
        Button saveButton;
        ImageView shareButton;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = view.findViewById(R.id.textViewQuestionTitle);
            mAnswersDetail = view.findViewById(R.id.textViewAnswerCount);
            saveButton = view.findViewById(R.id.saveButton);
            shareButton = view.findViewById(R.id.shareQuestionButton);
        }
    }
}

