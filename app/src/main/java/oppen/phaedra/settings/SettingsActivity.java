package oppen.phaedra.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import oppen.phaedra.R;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton close = findViewById(R.id.close);
        close.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        EditText homeCapsuleEdit = findViewById(R.id.home_capsule);
        String home = prefs.getString("home", "gemini://oppen.digital");
        homeCapsuleEdit.setText(home);

        SharedPreferences.Editor edit = prefs.edit();
        String[] colourValues = getResources().getStringArray(R.array.colour_values);

        String savedBackgroundColour = prefs.getString("background", "#XXXXXX");
        String savedForegroundColour = prefs.getString("foreground", "#XXXXXX");

        int savedBackgroundIndex = 0;
        int savedForegroundIndex = 0;

        for(int i = 0 ; i < colourValues.length ; i++){
            String colourValue = colourValues[i];
            if(colourValue.equals(savedBackgroundColour)){
                savedBackgroundIndex = i;
            }
            if(colourValue.equals(savedForegroundColour)){
                savedForegroundIndex = i;
            }
        }

        Spinner backgroundColourSpinner = findViewById(R.id.background_colour_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.colour_names, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        backgroundColourSpinner.setAdapter(adapter);
        backgroundColourSpinner.setSelection(savedBackgroundIndex);

        backgroundColourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String backgroundColour = colourValues[position];
                edit.putString("background", backgroundColour);
                edit.commit();
                setResult(RESULT_OK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner foregroundColourSpinner = findViewById(R.id.foreground_colour_spinner);
        ArrayAdapter<CharSequence> foregroundAdapter = ArrayAdapter.createFromResource(this, R.array.colour_names, android.R.layout.simple_spinner_item);
        foregroundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        foregroundColourSpinner.setAdapter(foregroundAdapter);
        foregroundColourSpinner.setSelection(savedForegroundIndex);

        foregroundColourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String foregroundColour = colourValues[position];
                edit.putString("foreground", foregroundColour);
                edit.commit();
                setResult(RESULT_OK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ImageButton save = findViewById(R.id.save);
        save.setOnClickListener(v -> {
            String homeCapsule = homeCapsuleEdit.getText().toString();
            edit.putString("home", homeCapsule);
            edit.commit();
            setResult(RESULT_OK);
            finish();
        });

    }
}
