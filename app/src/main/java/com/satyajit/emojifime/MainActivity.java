package com.satyajit.emojifime;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.satyajit.emojifime.databinding.ActivityMainBinding;
import com.satyajit.emojifime.detection.EmojiFiMe;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";

    public static final int REQUEST_STORAGE_PERMISSION = 1;
    private static final String FILE_PROVIDER_AUTHORITY = "com.satyajit.fileprovider";

    private ActivityMainBinding binding;

    private String mTempPhotoPath;
    private Bitmap mResultsBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check for external storage permission
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
                    //request for permission
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                    ,REQUEST_STORAGE_PERMISSION);
                }else{
                    launchCamera();
                }
            }


        });

        binding.clearFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //reset into initial state
                binding.emojiImageView.setImageResource(0);
                binding.buttonView.setVisibility(View.VISIBLE);
                binding.emojifyMeTextView.setVisibility(View.VISIBLE);
                binding.emojiList.setVisibility(View.VISIBLE);
                binding.shareFabButton.setVisibility(View.GONE);
                binding.clearFabButton.setVisibility(View.GONE);
                binding.saveFabButton.setVisibility(View.GONE);

                //delete the temp image file
                BitmapUtils.deleteImageFile(getApplicationContext(),mTempPhotoPath);

            }
        });

        binding.saveFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //from BitmapUtils class

                //first delete the temp image file
                BitmapUtils.deleteImageFile(getApplicationContext(),mTempPhotoPath);

                //second save the image
                BitmapUtils.saveImage(getApplicationContext(),mResultsBitmap);
            }
        });

        binding.shareFabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //from BitmapUtils class
                //delete
                BitmapUtils.deleteImageFile(getApplicationContext(),mTempPhotoPath);

                //share
                BitmapUtils.shareImage(getApplicationContext(),mTempPhotoPath);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchCamera();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    //creates a temporary image file and capture an image to store in it.
    private void launchCamera() {

        //camera implicit intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //check if there is a camera activity to handle this intent
        if(cameraIntent.resolveActivity(getPackageManager())!=null){
            //create a temporary files using BitmapUtils class
            File photoFile = null;
            try{
                //we need this because after capturing the image will be stored in this temp file
                photoFile = BitmapUtils.createTempImageFile(this);
            }catch (Exception e){
                e.printStackTrace();
            }
            //continue after the file successfully created
            if(photoFile!=null){
                //get the path
                mTempPhotoPath = photoFile.getAbsolutePath();

                //get the temp file uri using FileProvider class
                Uri photoURI = FileProvider.getUriForFile(this,FILE_PROVIDER_AUTHORITY,photoFile);

                //pass the uri to the implicit intent
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                activityResultLauncher.launch(cameraIntent);

            }
        }
    }
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()==RESULT_OK){
                        processAndSetImage(result.getData());
                    }
                    else{
                        //delete from bitmapUtils class
                        BitmapUtils.deleteImageFile(getApplicationContext(),mTempPhotoPath);
                    }
                }
            }
    );

    private void processAndSetImage(Intent data) {
        binding.buttonView.setVisibility(View.GONE);
        binding.emojifyMeTextView.setVisibility(View.GONE);
        binding.emojiList.setVisibility(View.GONE);
        binding.shareFabButton.setVisibility(View.VISIBLE);
        binding.clearFabButton.setVisibility(View.VISIBLE);
        binding.saveFabButton.setVisibility(View.VISIBLE);

        //resample the image sample,store it in Bitmap and set it to imageview
        mResultsBitmap = BitmapUtils.resamplePic(getApplicationContext(),mTempPhotoPath);

        //call the detectFace method pass the resampled bitmap to the method
        EmojiFiMe.detectFaces(getApplicationContext(), mResultsBitmap);
        //set the bitmap to imageview
        binding.emojiImageView.setImageBitmap(mResultsBitmap);


    }
}