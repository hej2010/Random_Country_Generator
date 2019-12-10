package se.swecookie.randomcountrygenerator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {
    private Button btnAccept;
    private Preferences preferences;
    private CheckBox cBPersonalisedAds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        if (!BuildConfig.FREE_VERSION) {
            onFinish();
            return;
        }

        CheckBox cBAgree = findViewById(R.id.cBAgree);
        btnAccept = findViewById(R.id.btnAccept);
        cBPersonalisedAds = findViewById(R.id.cBPersonalisedAds);
        preferences = new Preferences(this);

        cBAgree.setOnCheckedChangeListener((compoundButton, isChecked) -> btnAccept.setEnabled(isChecked));
        if (preferences.isAcceptedPP()) {
            onFinish();
        }
    }

    public void onButtonClicked(@NonNull View view) {
        switch (view.getId()) {
            case R.id.btnDecline:
                finish();
                break;
            case R.id.btnAccept:
                onAccept();
                break;
        }
    }

    private void onAccept() {
        preferences.setAccepted(true, cBPersonalisedAds.isChecked());
        onFinish();
    }

    private void onFinish() {
        finish();
        startActivity(new Intent(LauncherActivity.this, MainActivity.class));
        overridePendingTransition(0, 0);
    }
}
