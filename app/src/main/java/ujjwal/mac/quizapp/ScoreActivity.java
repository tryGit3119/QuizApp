package ujjwal.mac.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;


public class ScoreActivity extends AppCompatActivity {


    int Main_Activity_RequestCode;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference scoreHistoryReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Main_Activity_RequestCode = 8080;
        setContentView(R.layout.activity_score);
        int score = getIntent().getIntExtra("SCORE", 0);
        ((TextView) findViewById(R.id.scoreTV)).setText(String.valueOf(score));
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        Date date = new Date();
        SimpleDateFormat DateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        String sdate = DateFormat.format(date).toString();
        SimpleDateFormat Time = new SimpleDateFormat("hh:mm");
        String stime = Time.format(date).toString();

        mFirebaseDatabase.getReference().child("History").push().setValue(new Score(sdate, stime, score)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(ScoreActivity.this, "Score Updated", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ScoreActivity.this, "Could not update score", Toast.LENGTH_SHORT).show();
            }
        });

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