package se.swecookie.randomcountrygenerator;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CountryHistory.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract CountryDao countryDao();
    private static AppDatabase appDatabase = null;

    static AppDatabase getAppDatabase(@NonNull Context context) {
        if (appDatabase == null) {
            appDatabase = Room.databaseBuilder(context, AppDatabase.class, "history").build();
        }
        return appDatabase;
    }
}

