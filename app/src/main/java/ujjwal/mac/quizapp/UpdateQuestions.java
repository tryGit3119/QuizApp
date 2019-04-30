package ujjwal.mac.quizapp;

import android.content.Intent;
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
import android.widget.Toast;

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
                int i = findQnA(dataSnapshot.getValue(QnA.class).getId());
                if (i != -1) {
                    database.remove(i);
                    database.add(dataSnapshot.getValue(QnA.class));
                    questionsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(UpdateQuestions.this, "Some error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                int i = findQnA(dataSnapshot.getValue(QnA.class).getId());
                if (i != -1) {
                    database.remove(i);
                    questionsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(UpdateQuestions.this, "Some error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private int findQnA(String id) {
        for (int i = 0; i < database.size(); i++) {
            if (database.get(i).getId().equals(id))
                return i;
        }
        return -1;
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
            final QnA currentQnA = (QnA) getItem(i);
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
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle b = new Bundle();
                    b.putSerializable("CURR_OBJ", currentQnA);
                    Intent i = new Intent(UpdateQuestions.this, AddQuestion.class);
                    i.putExtras(b);
                    startActivity(i);
                }
            });
            return view;
        }
    }


}