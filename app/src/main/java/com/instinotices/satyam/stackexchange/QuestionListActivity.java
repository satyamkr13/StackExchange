package com.instinotices.satyam.stackexchange;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.instinotices.satyam.stackexchange.CustomDataTypes.Question;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuestionListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, QuestionsRecyclerViewAdapter.InteractionListener {
    public final static String LOGIN_STATUS = "login-status";
    public final static String STATUS_LOGGED_OUT = "logged-out";
    public final static String STATUS_LOGGED_IN = "logged-in";
    public final static String SELECTED_TAGS = "selected-tags";

    private SharedPreferences sharedPreferences;
    private QuestionViewModel questionViewModel;
    private RecyclerView mRecyclerView;
    private QuestionsRecyclerViewAdapter mAdapter;
    private ArrayList<String> selectedTags;
    private ProgressBar progressBar;
    private TextView noQuestionsDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        selectedTags = getSelectedTags(sharedPreferences);

        if (sharedPreferences.getString(LOGIN_STATUS, STATUS_LOGGED_OUT).equals(STATUS_LOGGED_OUT)) {
            // User not signed in, open sign in screen
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else if (selectedTags==null){
            // User signed in, but tags not selected
            Intent intent = new Intent(this, UserInterestActivity.class);
            startActivity(intent);
            finish();
        } else {
            // User signed in, load rest of things


            setupNavigationDrawer(toolbar);
            mRecyclerView = findViewById(R.id.questionsRecyclerView);
            mAdapter = new QuestionsRecyclerViewAdapter(new ArrayList<>(), QuestionListActivity.this);
            mRecyclerView.setAdapter(mAdapter);
            noQuestionsDisplay = findViewById(R.id.textViewNoQuestions);
            progressBar = findViewById(R.id.progressBarQuestionList);
            // Initialize View Model
            questionViewModel = ViewModelProviders.of(this).get(QuestionViewModel.class);
            questionViewModel.setTag(selectedTags.get(0));
            questionViewModel.getLiveData().observe(this, new Observer<List<Question>>() {
                @Override
                public void onChanged(@Nullable List<Question> questions) {
                    progressBar.setVisibility(View.GONE);
                    noQuestionsDisplay.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    if (questions != null) mAdapter.swapData(new ArrayList<>(questions));
                    else {
                        noQuestionsDisplay.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    private void setupNavigationDrawer(Toolbar toolbar) {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        Menu submenu = menu.addSubMenu("Your tags");
        // Dynamically add tags
        int i = 0;
        for (String tag : selectedTags
        ) {
            submenu.add(0, i, i, tag);
            i += 1;
        }
        navigationView.invalidate();
    }

    private void selectTag(String tag) {
        // This method will automatically cause repository to modify contents of liveData.
        questionViewModel.setTag(tag);
    }

    public static ArrayList<String> getSelectedTags(SharedPreferences sharedPreferences) {
        // Extract ArrayList<String> from JSON string saved in sharedPreferences
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SELECTED_TAGS, "");
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Close navigation drawer if open, otherwise perform normal back operation
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_user_interests) {
            // Send user to choose interests
            Intent intent = new Intent(this, UserInterestActivity.class);
            startActivity(intent);
        } else {
            selectTag(selectedTags.get(id));
            // Show loading animation only if internet available
            if (QuestionsRepository.isNetworkAvailable(this)) {
                mRecyclerView.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
            }
        }
        // Close the navigation drawer when any item clicked
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onShare(Question question) {
        // Share the question link as text
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, question.getLink());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share via"));
    }

    @Override
    public void onSaveQuestion(Question question) {
        questionViewModel.insert(question);
        Snackbar.make(mRecyclerView, "Question saved", Snackbar.LENGTH_SHORT)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        questionViewModel.delete(question);
                    }
                })
                .show();
    }

    @Override
    public void onOpenQuestion(Question question) {
        // Open the question in Chrome Custom Tab
        Uri url = Uri.parse(question.getLink());
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(this, url);
    }
}
