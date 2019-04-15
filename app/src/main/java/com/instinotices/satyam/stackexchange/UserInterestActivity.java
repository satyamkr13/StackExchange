package com.instinotices.satyam.stackexchange;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.instinotices.satyam.stackexchange.CustomDataTypes.TagItems;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserInterestActivity extends AppCompatActivity implements TagsRecyclerViewAdapter.InteractionListener {
    public static final String ORDER_DESC = "desc";
    public static final String SORT_POPULAR = "popular";
    public static final String SORT_ACTIVITY = "activity";
    public static final String SITE_STACKOVERFLOW = "stackoverflow";
    SharedPreferences sharedPreferences;
    private RecyclerView recyclerView;
    private TagsRecyclerViewAdapter mAdapter;
    private Button continueButton;
    private ArrayList<String> popularTags;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_interest);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        getTags();
        continueButton = findViewById(R.id.continueButton);
        progressBar = findViewById(R.id.progressBarTags);
    }

    private void getTags() {
        popularTags = new ArrayList<>();
        // Fetch popular tags from using retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.stackexchange.com/2.2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        StackExchangeAPI stackExchangeAPI = retrofit.create(StackExchangeAPI.class);
        Call<TagItems> call = stackExchangeAPI.getTags(ORDER_DESC, SORT_POPULAR, SITE_STACKOVERFLOW);
        call.enqueue(new Callback<TagItems>() {
            @Override
            public void onResponse(Call<TagItems> call, Response<TagItems> response) {
                if (response.isSuccessful()) {
                    TagItems tagItems = response.body();
                    ArrayList<TagItems.Tag> tagArrayList = tagItems.getTags();
                    for (TagItems.Tag tag : tagArrayList
                    ) {
                        popularTags.add(tag.getName());
                    }

                    displayTags();
                } else {
                    Toast.makeText(UserInterestActivity.this, "Bad response from StackExchange", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TagItems> call, Throwable t) {
                Toast.makeText(UserInterestActivity.this, "Internal Retrofit2 error", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }

    private void displayTags() {
        recyclerView = findViewById(R.id.recyclerView);
        // Get previously selected tags (if any)
        ArrayList<String> selectedTags = QuestionListActivity.getSelectedTags(sharedPreferences);
        if (selectedTags==null) selectedTags = new ArrayList<>();
        // Enable/Disable continue button
        onCheckedItemCountChanged(selectedTags.size());
        // Set up recycler view and hide progress bar
        mAdapter = new TagsRecyclerViewAdapter(popularTags, selectedTags, this);
        recyclerView.setAdapter(mAdapter);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onCheckedItemCountChanged(int count) {
        // Enable the count button when exactly 4 tags selected
        if (count != 4) {
            continueButton.setEnabled(false);
        } else continueButton.setEnabled(true);
    }


    public void onContinue(View view) {
        saveSelectedTags();
        // We're done! Launch Home Activity i.e QuestionListActivity
        Intent intent = new Intent(this, QuestionListActivity.class);
        startActivity(intent);
    }

    void saveSelectedTags() {
        // Save tags to SharedPreferences
        Gson gson = new Gson();
        String json = gson.toJson(mAdapter.getCheckedItems());
        sharedPreferences.edit().putString(QuestionListActivity.SELECTED_TAGS, json).commit();
    }
}
