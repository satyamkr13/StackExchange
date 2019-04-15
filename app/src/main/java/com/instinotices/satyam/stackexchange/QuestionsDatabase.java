package com.instinotices.satyam.stackexchange;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.instinotices.satyam.stackexchange.CustomDataTypes.Question;

@Database(entities = Question.class, version = 1, exportSchema = false)
public abstract class QuestionsDatabase extends RoomDatabase {

    private static QuestionsDatabase instance;

    public static synchronized QuestionsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), QuestionsDatabase.class, "questions_table")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

    public abstract QuestionsDao questionsDao();
}
