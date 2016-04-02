package app.com.vaipo.Utils;


import android.os.Handler;

import com.dd.processbutton.ProcessButton;

import java.util.Random;

public class ProgressGenerator {

    final private Handler mHandler;
    private LocalRunnable mRunnable;

    public interface OnCompleteListener {

        public void onComplete();

        public void onDismiss();

        public void onError();
    }

    private OnCompleteListener mListener;
    private int mProgress;

    public ProgressGenerator(OnCompleteListener listener) {
        mListener = listener;
        mHandler = new Handler();
    }

    public void start(final ProcessButton button) {
        mRunnable = new LocalRunnable(button);
        mHandler.postDelayed(mRunnable, generateDelay());
    }

    public void dismiss(final ProcessButton button) {
        mHandler.removeCallbacks(mRunnable);
        mListener.onDismiss();
    }

    public void complete(final ProcessButton button) {
        mProgress = 100;
        button.setProgress(mProgress);
        mHandler.removeCallbacks(mRunnable);
    }

    private Random random = new Random();

    private int generateDelay() {
        return random.nextInt(1000);
    }

    private class LocalRunnable implements Runnable {
        ProcessButton button;

        LocalRunnable(ProcessButton button) {
            this.button = button;
        }

        @Override
        public void run() {
            mProgress += 1;
            button.setProgress(mProgress);
            if (mProgress < 100) {
                //handler.postDelayed(this, generateDelay());
                mHandler.postDelayed(this, 2000);

            } else {
                mListener.onComplete();
            }
        }
    }
}

