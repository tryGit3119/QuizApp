package ujjwal.mac.quizapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ScoreHistory extends AppCompatActivity {

    ArrayList<Score> scores;
    ScoreAdapter scoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_history);
        scores = new ArrayList<>();
        scoreAdapter = new ScoreAdapter();
        ListView listView = findViewById(R.id.scoreLV);
        listView.setAdapter(scoreAdapter);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.getReference().child("History").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                scores.add(dataSnapshot.getValue(Score.class));
                scoreAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private class ScoreAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return scores.size();
        }

        @Override
        public Object getItem(int i) {
            return scores.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null)
                view = LayoutInflater.from(ScoreHistory.this).inflate(R.layout.score_list_item, viewGroup, false);
            Score score = (Score) getItem(i);
            ((TextView) view.findViewById(R.id.score)).setText(String.valueOf(score.getScore()));
            ((TextView) view.findViewById(R.id.date)).setText(score.getDate());
            ((TextView) view.findViewById(R.id.time)).setText(score.getTime());
            return view;
        }
    }
}
