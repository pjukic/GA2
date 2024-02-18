package guardianangel;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import guardianangel.exceptions.RecorderIsAlreadyRecordingException;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;


public class GAAudioRecorder {
    private static final Logger LOG = LoggerFactory.getLogger(GAAudioRecorder.class);
    MediaRecorder mediaRecorder;
    boolean isRecording;
    Context context;
    String token;
    Prefs prefs;

    public GAAudioRecorder(Context context) {
        super();
        this.context = context;
        prefs = new Prefs(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public void init() throws RecorderIsAlreadyRecordingException {
        if (prefs.getPreferences().contains(context.getString(R.string.guardian_angel_token_preference_key))) {
            token = prefs.getPreferences().getString(context.getString(R.string.guardian_angel_token_preference_key), "");
        } else {
            // raise a Token not found exception
        }
        if(isRecording){
            throw new RecorderIsAlreadyRecordingException("");
        }
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(getPathToTempAudioFile());
        //try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOG.info("===> Media init successful");
    }

    public String getPathToTempAudioFile() {
        return context.getExternalFilesDir(null).getAbsolutePath() + "/temp_audio.mp4";
    }

    public void stop() {
        LOG.info("===> stopping recording");
        mediaRecorder.stop();
        mediaRecorder.release();
        isRecording = false;
        File audioFile = new File(getPathToTempAudioFile());
        GAServices.postAudioFileToServer(audioFile, token);
    }

    public void abort() {
        LOG.info("===> aborting recording");
        mediaRecorder.stop();
        mediaRecorder.release();
    }

    public void start() throws RecorderIsAlreadyRecordingException {
        LOG.info("===> starting recording");
        if(isRecording){
            throw new RecorderIsAlreadyRecordingException("");
        }
        isRecording = true;
        mediaRecorder.start();
    }

    public void handleStopRecordingAfterXSeconds(int second, TextView animatedTextView){
        Thread recordThread = new Thread(() -> {
            try {
                Thread.sleep(second * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stop();
            animatedTextView.clearAnimation();
        });
        recordThread.start();
    }
}
