package ujjwal.mac.quizapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;


public class Main2Activity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RadioButton.OnCheckedChangeListener {

    RadioButton o1, o2, o3, o4;
    private static int FIRST_ENTRY_IN_DATABASE = 0;


    // game database
    private ArrayList<QnA> database;

    // game variables
    private String question;
    private int total;
    private HashMap<String, Integer> score = new HashMap<>();
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mQnADatabaseReference = mFirebaseDatabase.getReference().child("questions");

        // loading game data
        loadQAndA();

        o1 = findViewById(R.id.option1);
        o2 = findViewById(R.id.option2);
        o3 = findViewById(R.id.option3);
        o4 = findViewById(R.id.option4);
        o1.setOnCheckedChangeListener(this);
        o2.setOnCheckedChangeListener(this);
        o3.setOnCheckedChangeListener(this);
        o4.setOnCheckedChangeListener(this);

    }

    private void loadQAndA() {

        // Instantiate database
        database = new ArrayList<>();

        // Set childEventListener on mQnADatabaseReference
        // For actively listening to changes in firebase's real time database
        if(mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    // add Messages the the list
                    QnA qAndA = dataSnapshot.getValue(QnA.class);
                    database.add(qAndA);

                    if (FIRST_ENTRY_IN_DATABASE == 0){
                        loadNewQandI();
                    }
                    FIRST_ENTRY_IN_DATABASE = 1;
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {}

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            };

            // Event Listener for reading data from real time database
            mQnADatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void loadNewQandI() {

        // end of questions is not visible



        // preloader

        // initialize turn variables
        total = 0;

        // pick a question from set
        Random random = new Random(System.nanoTime());
        currentQnA = database.get(random.nextInt(database.size()));
        question = currentQnA.getQuestion();

        // display question text
        TextView questionView = (TextView) findViewById(R.id.question);
        questionView.setText(question);

        //get total answers for the question


        //if the question is accessed for the first time, add it to the array
        if(!score.containsKey(question)){
            score.put(question, 0);
        }

        // display game variables
        //TextView scoreTotal = (TextView) findViewById(R.id.scoreTotal);
        //String out = "Score: " + Integer.toString(score.get(question)) + "/Total: " + Integer.toString(total);
        //scoreTotal.setText(out);


        // Background task for fetching image of a question
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
                            into(1000,1000).
                            get();
                    currentImage = btmp;
                } catch (Exception e) {
                    e.printStackTrace();
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
                    // remove preloader
                    //TODO
                }
            }
        }.execute();

//        Log.d("URL", currentQnA.getPhotoUrl());

    }

    private void displayImage(Bitmap btmp){
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
        public ArrayList <Bitmap> imgs = new ArrayList<>();

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
        } else if (id == R.id.nav_share) {

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
