package com.amithkk.breathsampler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.TokenWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amithkk.breathsampler.databinding.ActivityBreathRecorderBinding;
import com.amithkk.breathsampler.pojo.BreathResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class BreathRecorderActivity extends AppCompatActivity {

    private final String LOG_TAG = "AmithBreathRecorder";
    private final int BREATH_TIME = 15000;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    ActivityBreathRecorderBinding bnd;
    private static String fileName = null; // Will hold temporary local save filename
    private MediaRecorder recorder = null; // Instance of android API MediaRecorder for capturing mic
    private StorageReference mStorageRef; // Firebase Cloud Storage reference holder
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bnd = DataBindingUtil.setContentView(this, R.layout.activity_breath_recorder);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION); // Ask ActivityCompat to pop up our permissions dialog (For Android 6.0+)


    }

    public void beginRecordSequence(){

        mStorageRef = FirebaseStorage.getInstance().getReference(); // Connect to firebase and get instance
        db = FirebaseDatabase.getInstance().getReference();
        // Record to the external cache directory for visibility
        fileName = getExternalCacheDir().getAbsolutePath(); // Get a temporary directory to record to
        fileName += "/"+ UUID.randomUUID().toString()+".3gp"; // name the file <UUID>.3gp

        recorder = new MediaRecorder(); // Init MediaRecorder
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC); // We want to record from Primary MIC
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // Output with 3GP Codec for reasonable losslessness
        recorder.setOutputFile(fileName); // we want to output to this file
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // Encode with Adaptive Multi-Rate Audio Codec

        try {
            recorder.prepare(); // Capture Mic and File-Lock Output file
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed. Check Mic");
            Toast.makeText(this, "Mic cannot be captured", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        try{recorder.start();}
        catch (IllegalStateException e) {
            Log.e(LOG_TAG, "Permission Result Groundhog Condition");

        }


        CountDownTimer cnt = new CountDownTimer(BREATH_TIME, 400) {
            @Override
            public void onTick(long untilFinished) {
                bnd.remainingTime.setText(untilFinished/1000+"s");
                float progress =((BREATH_TIME - untilFinished)/(BREATH_TIME*1.0f)) * 100.0f;
                Log.d("AppTAG", "Prog:"+progress);
                bnd.percentageChartView2.setProgress(progress, true);
            }

            @Override
            public void onFinish() {
                bnd.percentageChartView2.setProgress(100.0f, true);
                Toast.makeText(BreathRecorderActivity.this, "Complete, Uploading", Toast.LENGTH_SHORT).show();
                recorder.stop();
                recorder = null;

                uploadToFirebaseCS(fileName);
            }
        };

        cnt.start();

        bnd.quitRecordingBtn.setOnClickListener(view -> {
            cnt.cancel();
            recorder=null;
            finish();
        });
    }

    public void uploadToFirebaseCS(String fileName)
    {
        File f = new File(fileName);

        Uri file = Uri.fromFile(f); // Get full path of stored temp file
        StorageReference audioRef = mStorageRef.child("breath/"+UUID.randomUUID().toString()+".3gp"); // Get a file reference from Firebase Cloud Storage

        bnd.centerDrawable.setImageDrawable(getDrawable(R.drawable.ic_upload));
        bnd.modeTV.setText("Uploading to Cloud");
        bnd.remainingTime.setText( Integer.parseInt(String.valueOf(f.length()/1024))+"KB");

        audioRef.putFile(file) //Upload File
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Uri downloadUrl = taskSnapshot.getUploadSessionUri(); // Get URL of uploaded content

                        // Android artefact to copy URL to clipboard
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newRawUri("audio",downloadUrl);
                        clipboard.setPrimaryClip(clip);
                        bnd.modeTV.setText("Database Sync");
                        BreathResult br  = new BreathResult(downloadUrl.toString(), BREATH_TIME,  System.currentTimeMillis() / 1000L);
                        db.child("recordLogs").push().setValue(br).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                setResult(RESULT_OK);
                                finish();
                            }
                        });

                    }
                })


                .addOnFailureListener(exception -> {
                    //Handle case where the upload failed
                    Log.d(LOG_TAG, exception.getMessage());
                    setResult(RESULT_CANCELED);
                    finish();
                });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if(permissionToRecordAccepted) {
                        beginRecordSequence();}
                break;
        }
        if (!permissionToRecordAccepted) {

            Toast.makeText(this, "Need Mic Access, Quitting", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();}

    }
}
