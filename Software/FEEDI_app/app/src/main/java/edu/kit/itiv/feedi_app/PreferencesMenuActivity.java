package edu.kit.itiv.feedi_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class PreferencesMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences_menu);
    }

    public void onClickPreferencesPointNavigation(View view) {
        Intent openPreferencesPointNavigationActivity = new Intent(this, PreferencesPointNavigationActivity.class);
        startActivity(openPreferencesPointNavigationActivity);
    }

    public void onClickPreferencesPointToNorth(View view) {
        Intent openPreferencesPointToNorthActivity = new Intent(this, PreferencesPointToNorthActivity.class);
        startActivity(openPreferencesPointToNorthActivity);
    }
}
