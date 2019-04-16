package com.instinotices.satyam.stackexchange;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.customtabs.CustomTabsIntent;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.instinotices.satyam.stackexchange.CustomDataTypes.Question;

import java.lang.reflect.Type;
import java.util.ArrayList;

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

    /**
     * Retrieves selected tags ArrayList from SharedPreferences stored as JSON object.
     *
     * @return ArrayList of selected tags
     */
    public static ArrayList<String> getSelectedTags(SharedPreferences sharedPreferences) {
        // Extract ArrayList<String> from JSON string saved in sharedPreferences
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SELECTED_TAGS, "");
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

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
            // Use the first selected tag by default
            questionViewModel.setTag(selectedTags.get(0));
            // Add an observer to change the content of RecyclerView when LiveData changes
            questionViewModel.getLiveData().observe(this, questions -> {
                // Hide progressBar and "No Questions" error text view.
                progressBar.setVisibility(View.GONE);
                noQuestionsDisplay.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                // Swap the adapter if there are some questions to display, otherwise show "No questions" message
                if (questions != null && questions.size() > 0)
                    mAdapter.swapData(new ArrayList<>(questions));
                else {
                    noQuestionsDisplay.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    private void setupNavigationDrawer(Toolbar toolbar) {

        // Add listener to the drawer and to add the hamburger menu icon
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        // Automatically rotate the arrow
        toggle.syncState();

        // Recieve onNavigationItemSelected callbacks in this class
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        Menu submenu = menu.addSubMenu("Your tags");
        // Dynamically add tags as menu items
        int itemId = 0;
        for (String tag : selectedTags
        ) {
            // Order same as item id.
            submenu.add(0, itemId, itemId, tag);
            itemId += 1;
        }
        // Re-create the navigationView to display newly added items
        navigationView.invalidate();
    }

    private void selectTag(String tag) {
        // This method call will automatically cause repository to modify contents of liveData.
        questionViewModel.setTag(tag);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
            // Show loading animation only if internet is available, because fetching data  from internet may take some time
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

    /**
     * This method gets invoked whenever user presses share button on any question
     *
     * @param question - The question whose link is to be shared
     */
    @Override
    public void onShare(Question question) {
        // Share the question link as text
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, question.getLink());
        sendIntent.setType("text/plain");
        // Always ask which app to use for sharing
        startActivity(Intent.createChooser(sendIntent, "Share via"));
    }

    /**
     * This method gets invoked whenever user presses save button on any question
     * @param question - The question which is to be saved locally.
     */
    @Override
    public void onSaveQuestion(Question question) {
        // Ask view model to save this question
        questionViewModel.insert(question);
        // Notify user that question has been saved, along with an option to undo this action.
        Snackbar.make(mRecyclerView, "Question saved", Snackbar.LENGTH_SHORT)
                .setAction("UNDO", v -> {
                    // Ask view model to delete this question on UNDO request
                    questionViewModel.delete(question);
                })
                .show();
    }

    /**
     * This method gets invoked whenever user clicks on any question to view it.
     * This method uses Chrome Custom Tabs as an in-app browser to display full question.
     * @param question - The question whose details are to be shown
     */
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
