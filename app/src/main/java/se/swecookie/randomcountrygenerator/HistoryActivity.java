package se.swecookie.randomcountrygenerator;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private List<CountryHistory> history;
    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        appDatabase = AppDatabase.getAppDatabase(this);
        history = new ArrayList<>();
        mAdapter = new HistoryAdapter(history);
        recyclerView.setAdapter(mAdapter);

        getData();
    }

    private void getData() {
        new Thread(() -> {
            List<CountryHistory> l = appDatabase.countryDao().getAll();
            Collections.reverse(l);
            history.clear();
            history.addAll(l);
            updateData();
        }).start();
    }

    private void updateData() {
        runOnUiThread(() -> {
            mAdapter.notifyDataSetChanged();
            findViewById(R.id.txtNoHistory).setVisibility(history.isEmpty() ? View.VISIBLE : View.GONE);
            findViewById(R.id.btnClearHistory).setVisibility(history.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    public void onButtonClicked(View view) {
        if (view.getId() == R.id.btnClearHistory) {
            new Thread(() -> {
                appDatabase.countryDao().nukeTable();
                getData();
            }).start();
        }
    }
}
