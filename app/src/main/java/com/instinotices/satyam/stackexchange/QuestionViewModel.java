package com.instinotices.satyam.stackexchange;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.instinotices.satyam.stackexchange.CustomDataTypes.Question;

import java.util.List;

public class QuestionViewModel extends AndroidViewModel {
    private QuestionsRepository questionsRepository;
    private LiveData<List<Question>> liveData;
    private MutableLiveData<List<Question>> editableLiveData;

    public QuestionViewModel(@NonNull Application application) {
        super(application);
        questionsRepository = new QuestionsRepository(application);
        //editableLiveData = questionsRepository.getQuestions();
        liveData = questionsRepository.getQuestions();
    }


    public void setTag(String tag) {
        questionsRepository.setTag(tag);
    }

    public void insert(Question question) {
        questionsRepository.insert(question);
    }

    public void delete(Question question) {
        questionsRepository.delete(question);
    }

    public LiveData<List<Question>> getLiveData() {
        return liveData;
    }
}
