package com.satyajit.emojifime.detection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.media.FaceDetector;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.satyajit.emojifime.R;


import java.util.ArrayList;
import java.util.List;

public class EmojiFiMe {

    private static final String LOG_TAG = "EmojiFiMe";
    private static final double SMILING_PROB = .15;
    private static final double EYE_OPEN_PROB = .5;

    //detect the number of faces in the image and log the result
    public static void detectFaces(Context context, Bitmap bitmap){
        //create a inputImage for processing
        InputImage image = InputImage.fromBitmap(bitmap,0);

        final String[] resource = new String[1];

        //change the default settings
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).build();
        //create a FaceDetector object and disable tracking and enable classification
        com.google.mlkit.vision.face.FaceDetector faceDetector = FaceDetection.getClient(options);

        Task<List<Face>> result = faceDetector.process(image).addOnSuccessListener(new OnSuccessListener<List<Face>>() {
            @Override
            public void onSuccess(List<Face> faces) {
                //Task successfully completed
                int number = faces.size();
                Toast.makeText(context.getApplicationContext(), "Number of faces detected "+number, Toast.LENGTH_SHORT).show();

                String emojiBitmap = null;
                ImageView i = new ImageView(context);

                //for setting emoji for multiple faces, we need a condition
                if(number==0){
                    Toast.makeText(context.getApplicationContext(), "No faces detected "+number, Toast.LENGTH_SHORT).show();
                }else{
                    for(Face face: faces){
                        //get the emoji

                        switch(getClassification(face)){
                            case SMILE:
                                emojiBitmap = "Smile";
                                i.setImageResource(R.drawable.smile);
                                break;
                            case FROWN:
                                emojiBitmap = "Sad";
                                i.setImageResource(R.drawable.frown);
                                break;
                            case RIGHT_WINK:
                                emojiBitmap = "Right eye Wink";
                                i.setImageResource(R.drawable.rightwink);
                                break;
                            case LEFT_WINK:
                                emojiBitmap = "Left eye wink";
                                i.setImageResource(R.drawable.leftwink);
                                break;
                            case LEFT_WINK_FROWN:
                                emojiBitmap = "Left eye closes & sad";
                                i.setImageResource(R.drawable.leftwinkfrown);
                                break;
                            case RIGHT_WINK_FROWN:
                                emojiBitmap = "Right eye closes & sad";
                                i.setImageResource(R.drawable.rightwinkfrown);
                                break;
                            case CLOSED_EYE_FROWN:
                                emojiBitmap = "Eyes Closed & sad";
                                i.setImageResource(R.drawable.closed_frown);
                                break;
                            case CLOSED_EYE_SMILE:
                                emojiBitmap = "Eyes Closed & smile";
                                i.setImageResource(R.drawable.closed_smile);
                                break;
                            default:
                                Toast.makeText(context, R.string.no_emoji, Toast.LENGTH_SHORT).show();
                        }

                    }
                    resource[0] = emojiBitmap;
                    Toast.makeText(context, "Facial Expression: "+resource[0], Toast.LENGTH_SHORT).show();
                    Toast toast = new Toast(context);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(i);
                    toast.show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context.getApplicationContext(), "Some error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //determine which emoji
    private static Emoji getClassification(Face face) {
        //we have enabled the classification so,

        if(face.getSmilingProbability()!=null){
            float smileProb = face.getSmilingProbability();
            Log.d(LOG_TAG,"Smiling Probability: "+smileProb);
        }
        if(face.getRightEyeOpenProbability()!=null){
            float rightEyeProb = face.getRightEyeOpenProbability();
            Log.d(LOG_TAG,"Right eye Probability: "+rightEyeProb);
        }
        if(face.getLeftEyeOpenProbability()!=null){
            float leftEyeProb = face.getLeftEyeOpenProbability();
            Log.d(LOG_TAG,"Left eye Probability: "+leftEyeProb);
        }

        boolean smiling = face.getSmilingProbability()>SMILING_PROB;//returns true if person is smiling
        boolean leftEyeOpen = face.getLeftEyeOpenProbability()>EYE_OPEN_PROB;//returns left true if eye is open
        boolean rightEyeOpen = face.getRightEyeOpenProbability()>EYE_OPEN_PROB;//return true if right eye is open

        Emoji emoji;
        if(smiling){
            if(!leftEyeOpen&&rightEyeOpen){
                emoji = Emoji.LEFT_WINK;
            } else if(!rightEyeOpen && leftEyeOpen){
                emoji = Emoji.RIGHT_WINK;
            } else if (!leftEyeOpen){
                emoji = Emoji.CLOSED_EYE_SMILE;
            } else {
                emoji = Emoji.SMILE;
            }
        } else {
            if (!leftEyeOpen && rightEyeOpen) {
                emoji = Emoji.LEFT_WINK_FROWN;
            }  else if(!rightEyeOpen && leftEyeOpen){
                emoji = Emoji.RIGHT_WINK_FROWN;
            } else if (!leftEyeOpen){
                emoji = Emoji.CLOSED_EYE_FROWN;
            } else {
                emoji = Emoji.FROWN;
            }
        }

        Log.d(LOG_TAG, "whichEmoji: " + emoji.name());
        return emoji;

    }

    private enum Emoji{
        SMILE,
        FROWN,
        LEFT_WINK,
        RIGHT_WINK,
        LEFT_WINK_FROWN,
        RIGHT_WINK_FROWN,
        CLOSED_EYE_SMILE,
        CLOSED_EYE_FROWN
    }

}
