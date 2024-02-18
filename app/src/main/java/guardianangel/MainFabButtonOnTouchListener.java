package guardianangel;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import guardianangel.exceptions.RecorderIsAlreadyRecordingException;

public class MainFabButtonOnTouchListener implements View.OnTouchListener {
    private static final Logger LOG = LoggerFactory.getLogger(MainFabButtonOnTouchListener.class);
    private final Context context;
    private final GuardianStopWatch fabButtonPressTimeTracker;

    private FloatingActionButton recordFabButton;
    private final GAAudioRecorder audioRecorder;
    private final TextView recordTextView;
    private final Animation recordTextViewAnimation;

    public MainFabButtonOnTouchListener(Context context,
                                        FloatingActionButton mainFabButton,
                                        TextView recordTextView,
                                        Animation recordTextViewAnimation) {
        this.recordFabButton = mainFabButton;
        this.context = context;
        this.audioRecorder = new GAAudioRecorder(context);
        fabButtonPressTimeTracker = new GuardianStopWatch();
        this.recordTextView = recordTextView;
        this.recordTextViewAnimation = recordTextViewAnimation;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        long TIME_FOR_PRESS_TO_BE_CLICK = 1200;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                recordTextView.startAnimation(recordTextViewAnimation);
                fabButtonPressTimeTracker.startTracking();
                try {
                    LOG.info("===>audioRecorder.init()...");
                    audioRecorder.init();
                    LOG.info("===>audioRecorder.start()...");
                    audioRecorder.start();
                } catch (RecorderIsAlreadyRecordingException e) {
                    Toast.makeText(context, "Recorder is already recording", Toast.LENGTH_SHORT).show();
                } catch (RuntimeException runtimeException) {
                    Toast.makeText(context, "Runtime: " + runtimeException.getMessage(), Toast.LENGTH_SHORT).show();
                    LOG.info("===>audioRecorder - onTouch - Exception: " + runtimeException.getMessage());
                }
                break;
            case MotionEvent.ACTION_UP:
                fabButtonPressTimeTracker.stopTracking();
                if (fabButtonPressTimeTracker.passedTimeWasLessThen(TIME_FOR_PRESS_TO_BE_CLICK)) {
                    Toast.makeText(context, "Recording for 8 seconds", Toast.LENGTH_SHORT).show();
                    audioRecorder.handleStopRecordingAfterXSeconds(8, recordTextView);
                } else {
                    Toast.makeText(context, "Recording was saved", Toast.LENGTH_SHORT).show();
                    audioRecorder.stop();
                    recordTextView.clearAnimation();
                }
                break;
        }
        return true;
    }
}
