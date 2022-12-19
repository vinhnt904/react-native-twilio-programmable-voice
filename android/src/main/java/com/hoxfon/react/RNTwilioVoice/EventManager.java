package com.hoxfon.react.RNTwilioVoice;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.twilio.voice.Call;
import com.twilio.voice.CallInvite;

import static com.hoxfon.react.RNTwilioVoice.TwilioVoiceModule.TAG;

public class EventManager {

    private ReactApplicationContext mContext;
    private WritableArray delayedEvents;
    private String audioDeviceName;

    public static final String EVENT_DEVICE_READY = "deviceReady";
    public static final String EVENT_DEVICE_NOT_READY = "deviceNotReady";
    public static final String EVENT_CONNECTION_DID_CONNECT = "connectionDidConnect";
    public static final String EVENT_CONNECTION_DID_DISCONNECT = "connectionDidDisconnect";
    public static final String EVENT_DEVICE_DID_RECEIVE_INCOMING = "deviceDidReceiveIncoming";
    public static final String EVENT_CALL_STATE_RINGING = "callStateRinging";
    public static final String EVENT_CALL_REJECTED = "callRejected";
    public static final String EVENT_CALL_INVITE_CANCELLED = "callInviteCancelled";
    public static final String EVENT_CONNECTION_IS_RECONNECTING = "connectionIsReconnecting";
    public static final String EVENT_CONNECTION_DID_RECONNECT = "connectionDidReconnect";
    public static final String EVENT_AUDIO_DEVICES_UPDATED = "audioDevicesUpdated";

    public EventManager(ReactApplicationContext context) {
        mContext = context;
        delayedEvents = Arguments.createArray();
        audioDeviceName = "Default";
    }

    public void setAudioDeviceName(String name){
        audioDeviceName = name;
    }

    public WritableMap getEventParamFromCallInvite(CallInvite callInvite) {
        WritableMap params = Arguments.createMap();
        params.putString(Constants.CALL_SID,    callInvite.getCallSid());
        params.putString(Constants.CALL_FROM,   callInvite.getFrom());
        params.putString(Constants.CALL_TO,     callInvite.getTo());
        return params;
    }

    public WritableMap getEventParamFromCall(Call call, @Nullable String toNumber) {
        WritableMap params = Arguments.createMap();
        params.putString(Constants.CALL_SID, call.getSid());
        params.putString(Constants.CALL_FROM, call.getFrom());
        if(call.getTo() == null) params.putString(Constants.CALL_TO, toNumber);
        else params.putString(Constants.CALL_TO, call.getTo());
        return params;
    }

    public WritableArray getCacheEvents() {
        WritableArray array = delayedEvents;
        delayedEvents = Arguments.createArray();
        return array;
    }

    public WritableMap makeupEventParamAndSetToCache(String eventName, @Nullable WritableMap params) {
        WritableMap data = params == null ? Arguments.createMap() : params.copy();
        data.putString("timeStamp", String.valueOf(System.currentTimeMillis()));
        data.putString("audio_device", audioDeviceName);

        int resultMic = ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO);
        data.putString("microphone_permission", resultMic == PackageManager.PERMISSION_GRANTED ? "is_authorized" : "NOT_authorized");

        if(!eventName.equals(EVENT_AUDIO_DEVICES_UPDATED)){
            WritableMap cacheData = Arguments.createMap();
            cacheData.putString("event", eventName);
            cacheData.putMap("data", data.copy());
            delayedEvents.pushMap(cacheData.copy());
        }
        return data;
    }

    public void sendEvent(String eventName, @Nullable WritableMap params) {
        WritableMap eventParams = makeupEventParamAndSetToCache(eventName, params);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "sendEvent "+eventName+" params " + eventParams);
        }

        if (mContext.hasActiveReactInstance()) {
            mContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, eventParams);
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "failed Catalyst instance not active");
            }
        }
    }
}
