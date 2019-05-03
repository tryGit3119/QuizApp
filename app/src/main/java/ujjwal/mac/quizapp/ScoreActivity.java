package ujjwal.mac.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;


public class ScoreActivity extends AppCompatActivity {


    int Main_Activity_RequestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Main_Activity_RequestCode = 8080;
        setContentView(R.layout.activity_score);
        ((TextView) findViewById(R.id.scoreTV)).setText(String.valueOf(getIntent().getIntExtra("SCORE", 0)));
        (findViewById(R.id.play_again)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ScoreActivity.this, MainActivity.class);
                startActivityForResult(i, Main_Activity_RequestCode);
            }
        });
        (findViewById(R.id.exit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }
}
