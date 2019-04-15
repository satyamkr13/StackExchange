package com.instinotices.satyam.stackexchange;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.instinotices.satyam.stackexchange.CustomDataTypes.Question;
import com.instinotices.satyam.stackexchange.CustomDataTypes.QuestionItems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class QuestionsRepository {
    private QuestionsDao questionsDao;
    private Context context;
    private String tag;
    private LiveData<List<Question>> offlineLiveData;
    private MutableLiveData<List<Question>> onlineLiveData;
    private MediatorLiveData<List<Question>> mediatorLiveData;
    private boolean was_online;

    QuestionsRepository(Application application) {
        context = application.getApplicationContext();
        mediatorLiveData = new MediatorLiveData<>();
        QuestionsDatabase questionsDatabase = QuestionsDatabase.getInstance(context);
        questionsDao = questionsDatabase.questionsDao();
    }

    void insert(Question question) {
        Executors.newSingleThreadExecutor().execute(() -> {
            questionsDao.insert(question);
        });
    }

    void setTag(String tag) {
        this.tag = tag;
        populateLiveData();
    }

    void delete(Question question) {
        Executors.newSingleThreadExecutor().execute(() -> {
            questionsDao.delete(question);
        });
    }

    private void populateLiveData() {
        if (isNetworkAvailable(context)) {
            fetchOnlineQuestions();
        } else if (offlineLiveData == null) {
            fetchOfflineQuestions();
        }
    }

    LiveData<List<Question>> getQuestions() {
        return mediatorLiveData;
    }

    private void fetchOnlineQuestions() {
        if (onlineLiveData == null) {
            onlineLiveData = new MutableLiveData<>();
            mediatorLiveData.addSource(onlineLiveData, mediatorLiveData::setValue);
        }
        if (! was_online){
            mediatorLiveData.removeSource(offlineLiveData);
            offlineLiveData = null;
        }
        was_online = true;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.stackexchange.com/2.2/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        StackExchangeAPI stackExchangeAPI = retrofit.create(StackExchangeAPI.class);
        Call<QuestionItems> call = stackExchangeAPI.getQuestions(tag, UserInterestActivity.ORDER_DESC, UserInterestActivity.SORT_ACTIVITY, UserInterestActivity.SITE_STACKOVERFLOW);
        call.enqueue(new Callback<QuestionItems>() {
            @Override
            public void onResponse(Call<QuestionItems> call, Response<QuestionItems> response) {
                if (response.isSuccessful()) {
                    QuestionItems questionItems = response.body();
                    ArrayList<Question> questions = questionItems.getItems();
                    onlineLiveData.setValue(questions);
                } else {
                    Toast.makeText(context, "Failed to fetch questions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<QuestionItems> call, Throwable t) {
                Toast.makeText(context, "Retrofit internal error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchOfflineQuestions() {
        Toast.makeText(context, "Your device is offline", Toast.LENGTH_SHORT).show();
        offlineLiveData = questionsDao.getOfflineQuestions();
        mediatorLiveData.addSource(offlineLiveData, mediatorLiveData::setValue);
        if (was_online){
            mediatorLiveData.removeSource(onlineLiveData);
            onlineLiveData = null;
        }
        was_online = false;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }
}
