package ujjwal.mac.quizapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class AddQuestion extends AppCompatActivity {

    private static final int RC_PHOTO_PICKER = 2;

    // Database reference objects
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mQnADatabaseReference;
    private ChildEventListener mChildEventListener;

    // Firebase Storage Object
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mQuestionPhotosStorageReference;

    // variables
    private String question = null;
    //private ArrayList<String> ansList;
    private String photoUrl = null;
    private Uri photoUri;

    EditText option1, option2, option3, option4;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);
        option1 = (EditText) findViewById(R.id.eto1);
        option2 = (EditText) findViewById(R.id.eto2);
        option3 = (EditText) findViewById(R.id.eto3);
        option4 = (EditText) findViewById(R.id.eto4);
        progressBar = findViewById(R.id.add_prog);
        // Database objects instantiated
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mQnADatabaseReference = mFirebaseDatabase.getReference().child("questions");

        // Storage objects initialized
        mFirebaseStorage = FirebaseStorage.getInstance();
        mQuestionPhotosStorageReference = mFirebaseStorage.getReference().child("question_images");

        // Instantiate answer list
        //  ansList = new ArrayList<>();

        // Layout references
        ImageButton mPhotoPickerButton = findViewById(R.id.photoPickerButton);
        Button upload = findViewById(R.id.upload);
        final EditText questionField = findViewById(R.id.question);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });


        // add answer to answer list
        // Upload QnA object to firebase
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // disable touch on entire activity
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                // get question from Question field
                question = questionField.getText().toString();
                int radioButtonId = ((RadioGroup) findViewById(R.id.rgroup)).getCheckedRadioButtonId();
                final int correct_option;
                switch (radioButtonId) {
                    case R.id.option1:
                        correct_option = 1;
                        break;
                    case R.id.option2:
                        correct_option = 2;
                        break;
                    case R.id.option3:
                        correct_option = 3;
                        break;
                    case R.id.option4:
                        correct_option = 4;
                        break;
                    default:
                        correct_option = 1;
                }
                // check parameters are filled
                if (checkOptions() && photoUri != null && question != null && !question.isEmpty()) {


                    progressBar.setVisibility(View.VISIBLE);
                    // Upload picture to firebase storage
                    StorageReference photoRef = mQuestionPhotosStorageReference.child(photoUri.getLastPathSegment());
                    photoRef.putFile(photoUri).addOnSuccessListener(AddQuestion.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // get uri from from firebase storage
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            photoUrl = downloadUrl.toString();

                            // upload to firebase real time database
                            String key = mQnADatabaseReference.push().getKey();
                            QnA qAndA = new QnA(key, question, photoUrl, option1.getText().toString(),
                                    option2.getText().toString(),
                                    option3.getText().toString(),
                                    option4.getText().toString(), correct_option);
                            mQnADatabaseReference.child(key).setValue(qAndA).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    Toast.makeText(AddQuestion.this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }

    private boolean checkOptions() {
        String o1 = option1.getText().toString();
        String o2 = option2.getText().toString();
        String o3 = option3.getText().toString();
        String o4 = option4.getText().toString();
        if (o1 == null || o1.isEmpty() ||
                o2 == null || o2.isEmpty() ||
                o3 == null || o3.isEmpty() ||
                o4 == null || o4.isEmpty())
            return false;

        return true;
    }

    // Overriding onActivityResult for handling photo picker
    // this method gets called before the onResume method of the activity lifecycle
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            // get image uri
            Uri selectImageUri = data.getData();
            photoUri = selectImageUri;
            // display image
            ImageView photoImageView = findViewById(R.id.imgViewer);
            Glide.with(photoImageView.getContext())
                    .load(selectImageUri.toString())
                    .into(photoImageView);
        }
    }
}