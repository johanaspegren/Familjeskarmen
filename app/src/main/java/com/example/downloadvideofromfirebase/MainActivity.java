package com.example.downloadvideofromfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DOWNLOAD_VIDEO";
    private VideoView videoViewTopLeft;
    private VideoView videoViewTopRight;
    private VideoView videoViewBottomLeft;
    private VideoView videoViewBottomRight;

    private TextView txtNameTopLeft;
    private TextView txtNameTopRight;
    private TextView txtNameBottomLeft;
    private TextView txtNameBottomRight;

    // Use firebase storage to store the video clips
    private StorageReference fbStorageReference;
    // Use FireBase database to keep track of the videos
    private DatabaseReference fbDatabaseReference;
    // Use FireBase database to handle family details
    private DatabaseReference fbFamDatabaseReference;

    ArrayList<Family> families = new ArrayList<>();
    ArrayList<VideoClip> clips = new ArrayList<>();

    Family myFamily;
    String familyName = "Arja Nilsson";

    Map<String, String> selectedCategories = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        videoViewTopLeft = findViewById(R.id.videoViewTopLeft);
        videoViewTopRight = findViewById(R.id.videoViewTopRight);
        videoViewBottomLeft = findViewById(R.id.videoViewBottomLeft);
        videoViewBottomRight = findViewById(R.id.videoViewBottomRight);

        txtNameTopLeft = findViewById(R.id.txtNameTopLeft);
        txtNameTopRight = findViewById(R.id.txtNameTopRight);
        txtNameBottomLeft = findViewById(R.id.txtNameBottomLeft);
        txtNameBottomRight = findViewById(R.id.txtNameBottomRight);

        // video clips are stored in firebase storage, and links in realtime database
        fbDatabaseReference = FirebaseDatabase.getInstance().getReference("videos");
        fbStorageReference = FirebaseStorage.getInstance().getReference("videos");

        fbFamDatabaseReference = FirebaseDatabase.getInstance().getReference("families");
        readData(fbFamDatabaseReference);

        fbDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Iterable<DataSnapshot> stuff = dataSnapshot.getChildren();
                for(DataSnapshot s : stuff){
                    VideoClip vc = s.getValue(VideoClip.class);
                    Log.d(TAG, "Value is: " + vc.toString());
                    clips.add(vc);
                }
                if(clips.isEmpty()){
                    Log.d(TAG, "No clips available: " );
                    Toast.makeText(getApplicationContext(), "No clips available", Toast.LENGTH_SHORT).show();
                }else{
                    for(String author: families.get(0).getAuthors()) {
                        selectedCategories.put(author, "Hälsning");
                        updateScreen(author);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    // read data from firebase
    private void readData(DatabaseReference ref) {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Iterable<DataSnapshot> stuff = dataSnapshot.getChildren();
                for(DataSnapshot s : stuff){
                    Family f = s.getValue(Family.class);
                    Log.d(TAG, "Family is: " + f.toString());
                    families.add(f);
                    if (f.getName().equals(familyName)){
                        myFamily = f;
                    }
                }
                if(families.isEmpty()) {
                    Log.d(TAG, "No families available: ");
                    Toast.makeText(getApplicationContext(), "No families available", Toast.LENGTH_SHORT).show();
                }else if(myFamily == null){
                        Log.d(TAG, "Families available but no match on myFamily: " );
                        Toast.makeText(getApplicationContext(), "No families available", Toast.LENGTH_SHORT).show();
                }else{
                    Log.d(TAG, "myFamily available: " );
                    setupScreen(myFamily);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void setupScreen(Family family) {
        // There are up to four videoplayers, one for each author and a few categories
        // for each author
        for(String a : family.getAuthors()){
//            getLatest(clips, a, VideoClip.GREETING);
            TextView t = getTextView(a);
            t.setText(a);
        }


    }


    private void updateScreen(String author) {

        // get cat
        int category = getCategory(author);
        Log.d(TAG, "update screen category: " + category);
        // sort author, category, latest
        VideoClip vc = getLatest(clips, author, category);
        if (vc == null){
            return;
        }
        Log.d(TAG, "latest is : " + vc.toString());
        // download clip from firebase storage
        // dir needed?
        File internalMovies = createDirectories("internalMovies");
        File destination = new File(internalMovies, vc.getStringStorageReference());

        try {
            if (destination.exists()) {
                Log.d(TAG, "file already exists: " + destination.toString());
                prepareVideo(Uri.fromFile(destination), author);

            } else {
                destination.createNewFile();
                //localFile = File.createTempFile("videos", ".mp4");
                Log.d(TAG, "file created: " + destination.toString());
                downloadFromCloud(vc, destination);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getCategory(String author) {
        // returnerar default 1

        String cat =  selectedCategories.get(author);
        Log.d(TAG, "category is " + cat);
        if (cat.equals("Tröst")) {return VideoClip.COMFORT;}
        if (cat.equals("Påminnelse")) {return VideoClip.REMINDER;}
        return VideoClip.GREETING;

        /*
            if(author.equals("Johan")){
            if(!spinnerTopLeft.isSelected()){return VideoClip.GREETING;}
            String c = spinnerTopLeft.getSelectedItem().toString().trim();
            if (c.equals("Tröst")) {return VideoClip.COMFORT;}
            if (c.equals("Påminnelse")) {return VideoClip.REMINDER;}
        }
        return VideoClip.GREETING;
        */

    }

    private File createDirectories(String mDir) {
        File dir = new File(this.getFilesDir(), mDir);
        if(!dir.exists()){
            dir.mkdir();
        }
        return dir;
    }

    private void prepareVideo(Uri videoUri, String author) {
        Log.d(TAG, "Run video " + videoUri.toString());
        VideoView vv = getVideoView(author);
        vv.setVideoURI(videoUri);
        vv.seekTo(1);
    }

    private void runVideo(Uri videoUri, String author) {
        VideoView vv = getVideoView(author);
        vv.setVideoURI(videoUri);
        vv.seekTo(1);
        vv.start();
    }

    private VideoView getVideoView(String author) {
        int index = myFamily.getAuthors().indexOf(author);
        if(index == 0){ return videoViewTopLeft; }
        if(index == 1){ return videoViewTopRight; }
        if(index == 2){ return videoViewBottomLeft; }
        if(index == 3){ return videoViewBottomRight; }
        return null;
    }

    private TextView getTextView(String author) {
        int index = myFamily.getAuthors().indexOf(author);
        if(index == 0){ return txtNameTopLeft; }
        if(index == 1){ return txtNameTopRight; }
        if(index == 2){ return txtNameBottomLeft; }
        if(index == 3){ return txtNameBottomRight; }
        return null;
    }

    private void downloadFromCloud(VideoClip videoClip, File destination) {
        Log.d(TAG, "download  : " + destination.toString());
        StorageReference ref =
                fbStorageReference.child( "Johan/" + videoClip.getStringStorageReference());
        ref.getFile(destination).addOnSuccessListener(
                new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Local temp file has been created
                        Log.d(TAG, "file fetched : " + destination.getAbsolutePath());
                        Toast.makeText(MainActivity.this, "File downloaded", Toast.LENGTH_SHORT).show();
                        prepareVideo(Uri.fromFile(destination), videoClip.getAuthor());
                        runVideo(Uri.fromFile(destination), videoClip.getAuthor());

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(MainActivity.this, "Fail" + exception.toString(), Toast.LENGTH_LONG).show();
                Log.d(TAG, "getfile failed Fail" + exception.toString());
                // Handle any errors
            }
        });

    }


    private VideoClip getLatest(ArrayList<VideoClip> clips, String author, int category) {
        long latest = 0;
        VideoClip latestClip = null;
        for(VideoClip c : clips){
            if(c.getAuthor().equals(author)){
                if(c.getCategory() == category){
                    if(c.getTimeStampUploaded() > latest){
                        latest = c.getTimeStampUploaded();
                        latestClip = c;
                    }
                }
            }
        }
        return latestClip;
    }

    public void runGreeting(View view) {
        String a = getAuthor(view);
        selectedCategories.put(a, "Hälsning");
        Log.d(TAG, "set category to hälsning" + selectedCategories.get(a));
        updateScreen(a);
        startVideo(getVideoView(a));

    }

    private String getAuthor(View view) {
        if(view.getId() == R.id.txtNameTopLeft) {return "Johan";}
        if(view.getId() == R.id.videoViewTopLeft) {return "Johan";}
        if(view.getId() == R.id.heartTopLeft) {return "Johan";}

        if(view.getId() == R.id.txtNameTopRight) {return "Aziza";}
        if(view.getId() == R.id.videoViewTopRight) {return "Aziza";}
        if(view.getId() == R.id.heartTopRight) {return "Aziza";}

        if(view.getId() == R.id.txtNameBottomRight) {return "Maria";}
        if(view.getId() == R.id.videoViewBottomRight) {return "Maria";}
        if(view.getId() == R.id.heartBottomRight) {return "Maria";}

        if(view.getId() == R.id.txtNameBottomLeft) {return "Jossan";}
        if(view.getId() == R.id.videoViewBottomLeft) {return "Jossan";}
        if(view.getId() == R.id.heartBottomLeft) {return "Jossan";}
        return null;
    }

    public void runComfort(View view) {
        //set tröst om finns
        String a = getAuthor(view);
        selectedCategories.put(a, "Tröst");
        Log.d(TAG, "set category to Tröst" + selectedCategories.get(a));
        updateScreen(a  );
        startVideo(getVideoView(a));
    }

    public void startVideo(View view) {
        Log.d(TAG, "start video");
        toggleClip((VideoView) view);
    }

    private void toggleClip(VideoView vv){
        Log.d(TAG, "toggleClip  is playing: " + vv.isPlaying());
        if(vv.isPlaying()){
            Log.d(TAG, "toggleClip  IN isplaying?: " + vv.isPlaying());
            vv.pause();
            return;
        }
        vv.seekTo(0);
        vv.start();
    }


}