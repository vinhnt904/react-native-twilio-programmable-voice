package com.hoxfon.react.RNTwilioVoice;

import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;

public class IncomingCallActivity extends ReactActivity {
    public static ReactActivity currentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentActivity = this;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );
    }

    @Override
    protected String getMainComponentName() {
        return "IncomingCallActivity";
    }

    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new ReactActivityDelegate(this, getMainComponentName()) {
            @Nullable
            @Override protected Bundle getLaunchOptions() {
                Boolean isAnswered = false;
                if(getIntent() != null) {
                    String action = getIntent().getAction();
                    if(action != null && action.equals(Constants.ACTION_ACCEPT)){
                        isAnswered = true;
                    }
                }
                Bundle bundle = new Bundle();
                bundle.putBoolean("isAnswered", isAnswered);
                return bundle;
            }
        };
    }
}
