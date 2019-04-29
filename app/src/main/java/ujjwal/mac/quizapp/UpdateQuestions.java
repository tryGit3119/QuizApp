package ujjwal.mac.quizapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class UpdateQuestions extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mQnADatabaseReference;

    private ArrayList<QnA> database;
    QuestionsAdapter questionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_questions);
        database = new ArrayList<>();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mQnADatabaseReference = mFirebaseDatabase.getReference().child("questions");
        questionsAdapter = new QuestionsAdapter();
        ((ListView) findViewById(R.id.updateQuestLV)).setAdapter(questionsAdapter);
        mQnADatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                database.add(dataSnapshot.getValue(QnA.class));
                questionsAdapter.notifyDataSetChanged();
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

    private class QuestionsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return database.size();
        }

        @Override
        public Object getItem(int i) {
            return database.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(UpdateQuestions.this).inflate(R.layout.question_list_item, viewGroup, false);
            }
            final ProgressBar progressBar = view.findViewById(R.id.list_item_pb);
            ImageView imageView = view.findViewById(R.id.list_item_image);
            QnA currentQnA = (QnA) getItem(i);
            Glide.with(UpdateQuestions.this).
                    asBitmap().
                    load(currentQnA.getPhotoUrl()).
                    centerCrop().
                    listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.INVISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.INVISIBLE);
                            return false;
                        }
                    }).
                    into(imageView);
            ((TextView) view.findViewById(R.id.list_item_questTV)).setText(currentQnA.getQuestion());

            return view;
        }
    }
}


