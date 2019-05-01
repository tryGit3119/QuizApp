package ujjwal.mac.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    String[] levels = {
            "easy", "medium", "hard"
    };

    int difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        difficulty = -1;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                difficulty = 2 * i + 3;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ((Button) findViewById(R.id.playButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (difficulty != -1) {
                    Intent i = new Intent(MainActivity.this, Main2Activity.class);
                    i.putExtra("DIFF", difficulty);
                    startActivity(i);
                } else {
                    Toast.makeText(MainActivity.this, "Select Difficulty Level", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
