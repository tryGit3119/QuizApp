package ujjwal.mac.quizapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RadioButton.OnCheckedChangeListener {

    int ScoreActivityRequestCode;
    RadioButton o1, o2, o3, o4;
    private static int FIRST_ENTRY_IN_DATABASE = 0;
    ProgressBar progressBar;
    FrameLayout main_layout;
    TextView no_quests;
    Button next;
    TextView scoreTV;
    LinearLayout result_color;
    // game database
    private ArrayList<QnA> database;

    ShakeListener shakeListener;

    // game variables
    private String question;
    private int current_index;
    private int total;
    private int difficulty = 3;
    private Bitmap currentImage;


    // Current game object
    QnA currentQnA;

    // Database reference objects
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mQnADatabaseReference;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ScoreActivityRequestCode = 1020;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        difficulty = getIntent().getIntExtra("DIFF", 3);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mQnADatabaseReference = mFirebaseDatabase.getReference().child("questions");

        result_color = findViewById(R.id.my_toolbar);
        scoreTV = findViewById(R.id.score);
        progressBar = findViewById(R.id.pbHeaderProgress);
        main_layout = findViewById(R.id.main_layout);
        no_quests = findViewById(R.id.noQuest);
        current_index = 0;
        total = 0;
        progressBar.setVisibility(View.VISIBLE);
        no_quests.setVisibility(View.VISIBLE);
        main_layout.setVisibility(View.INVISIBLE);
        // loading game data
        loadQAndA();
        next = findViewById(R.id.next);
        o1 = findViewById(R.id.option1);
        o2 = findViewById(R.id.option2);
        o3 = findViewById(R.id.option3);
        o4 = findViewById(R.id.option4);
        o1.setOnCheckedChangeListener(this);
        o2.setOnCheckedChangeListener(this);
        o3.setOnCheckedChangeListener(this);
        o4.setOnCheckedChangeListener(this);
        shakeListener = new ShakeListener(this);
        shakeListener.setOnShakeListener(new ShakeListener.OnShakeListener() {
            @Override
            public void onShake() {
                next.callOnClick();
            }
        });
    }

    @Override
    protected void onPause() {
        if (shakeListener != null)
            shakeListener.pause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (shakeListener != null)
            shakeListener.pause();
        super.onStop();
    }

    @Override
    protected void onPostResume() {
        if (shakeListener != null)
            shakeListener.resume();
        else Toast.makeText(this, "shake listner null", Toast.LENGTH_SHORT).show();
        super.onPostResume();
    }

    @Override
    protected void onResume() {
        if (shakeListener != null)
            shakeListener.resume();
        else Toast.makeText(this, "shake listner null", Toast.LENGTH_SHORT).show();
        super.onResume();
    }

    private void loadQAndA() {

        // Instantiate database
        database = new ArrayList<>();

        // Set childEventListener on mQnADatabaseReference
        // For actively listening to changes in firebase's real time database
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    // add Messages the the list
                    QnA qAndA = dataSnapshot.getValue(QnA.class);
                    database.add(qAndA);

                    if (FIRST_ENTRY_IN_DATABASE == 0) {
                        progressBar.setVisibility(View.INVISIBLE);
                        no_quests.setVisibility(View.INVISIBLE);
                        main_layout.setVisibility(View.VISIBLE);
                        setClickListeners();
                        loadNewQandI();
                    }
                    FIRST_ENTRY_IN_DATABASE = 1;
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
            };

            // Event Listener for reading data from real time database
            mQnADatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void setClickListeners() {
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProg();
                checkAnswer();
                loadNewQandI();
            }
        });
    }

    private void checkAnswer() {
        boolean correct = false;
        switch (currentQnA.getCorrect_option()) {
            case 1:
                correct = o1.isChecked();
                break;
            case 2:
                correct = o2.isChecked();
                break;
            case 3:
                correct = o3.isChecked();
                break;
            case 4:
                correct = o4.isChecked();
                break;
        }
        if (correct) {
            total += 1;
            result_color.setBackgroundColor(Color.GREEN);
            Toast.makeText(this, "Correct Answer", Toast.LENGTH_SHORT).show();
        } else {
            result_color.setBackgroundColor(Color.RED);
            Toast.makeText(this, "Wrong Answer", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProg() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProg() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void loadNewQandI() {
        scoreTV.setText(String.valueOf(total));
        Log.d("aaa", total + "");
        if (current_index >= database.size()) {
            Toast.makeText(this, "no more questions", Toast.LENGTH_SHORT).show();
            next.setVisibility(View.INVISIBLE);
            hideProg();
            startScoreActivity();
            return;
        }
        // pick a question from set
        currentQnA = database.get(current_index);
        new AsyncTask<Void, Void, Void>() {
            Bitmap btmp;

            @Override
            protected Void doInBackground(Void... params) {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                try {

                    btmp = Glide.
                            with(Main2Activity.this).
                            asBitmap().
                            load(currentQnA.getPhotoUrl()).
                            into(1000, 1000).
                            get();
                    currentImage = btmp;
                } catch (Exception e) {
                    Toast.makeText(Main2Activity.this, "Could not load image", Toast.LENGTH_SHORT).show();
                    hideProg();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void dummy) {
                if (null != btmp) {
                    // Update the image of a question's Image View
//                    Log.d("CHECK", "IN TP");
                    if (btmp != null) {
                        displayImage(btmp);
                    }
                    hideProg();
                }
            }
        }.execute();
        // if (current_index == database.size() - 1)
        //    next.setVisibility(View.INVISIBLE);

        question = currentQnA.getQuestion();
        o1.setText(currentQnA.getOption1());
        o2.setText(currentQnA.getOption2());
        o3.setText(currentQnA.getOption3());
        o4.setText(currentQnA.getOption4());

        TextView questionView = (TextView) findViewById(R.id.question);
        questionView.setText(question);
        current_index += 1;
    }

    private void startScoreActivity() {
        Intent i = new Intent(Main2Activity.this, ScoreActivity.class);
        i.putExtra("SCORE", total);
        startActivityForResult(i, ScoreActivityRequestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ScoreActivityRequestCode)
            finish();
    }

    private void displayImage(Bitmap btmp) {
        // get segmented images
        ArrayList<Bitmap> imgs = splitBitmap(btmp);

        // add segmented images to the image grid
        GridView grid = (GridView) findViewById(R.id.grid);
        grid.setAdapter(new ImageAdapter(Main2Activity.this, imgs));
        grid.setNumColumns((int) Math.sqrt(imgs.size()));
    }

    private ArrayList<Bitmap> splitBitmap(Bitmap img) {

        GridView grid = (GridView) findViewById(R.id.grid);
        Bitmap picture = Bitmap.createScaledBitmap(img, grid.getWidth(), grid.getHeight(), true);

        //Number of rows
        int xCount = difficulty;

        //Number of columns
        int yCount = difficulty;

        ArrayList<Bitmap> imgs = new ArrayList<>();
        int width, height, k = 0;

        // Divide the original bitmap width by the desired vertical column count
        width = picture.getWidth() / xCount;

        // Divide the original bitmap height by the desired horizontal row count
        height = picture.getHeight() / yCount;

        // Loop the array and create bitmaps for each coordinate
        for (int x = 0; x < xCount; ++x) {
            for (int y = 0; y < yCount; ++y) {
                // Create the sliced bitmap
                imgs.add(Bitmap.createBitmap(picture, x * width, y * height, width, height));
                k++;
            }
        }

        // Randomly shuffle the array
        long seed = System.nanoTime();
        Collections.shuffle(imgs, new Random(seed));

        // Return the array
        return imgs;
    }


    private class ImageAdapter extends BaseAdapter {
        private Context mContext;
        public ArrayList<Bitmap> imgs = new ArrayList<>();

        public ImageAdapter(Context c, ArrayList<Bitmap> imgs) {
            mContext = c;
            this.imgs = imgs;
        }

        public int getCount() {
            return imgs.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(0, 0, 0, 0);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setImageBitmap(imgs.get(position));
            return imageView;

        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.add_question) {
            // Handle the camera action
            startActivity(new Intent(Main2Activity.this, AddQuestion.class));
        } else if (id == R.id.update_quest) {
            startActivity(new Intent(Main2Activity.this, UpdateQuestions.class));
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
            switch (compoundButton.getId()) {
                case R.id.option1:
                    o2.setChecked(false);
                    o3.setChecked(false);
                    o4.setChecked(false);
                    break;
                case R.id.option2:
                    o1.setChecked(false);
                    o3.setChecked(false);
                    o4.setChecked(false);
                    break;
                case R.id.option3:
                    o2.setChecked(false);
                    o1.setChecked(false);
                    o4.setChecked(false);
                    break;
                case R.id.option4:
                    o2.setChecked(false);
                    o3.setChecked(false);
                    o1.setChecked(false);
                    break;
            }
        }
    }
}