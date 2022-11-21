# react-native-twilio-programmable-voice

This is a react-native wrapper around [Twilio Programmable Voice SDK](https://www.twilio.com/voice), which allows your react-native app to make and receive calls.

This module is not affiliated with nor officially maintained by Twilio. It is maintained by open source contributors working during their nights and weekends.

Tested with:

- react-native 0.68.5

- Android 13

- iOS 16

## Release

### Twilio Programmable Voice SDK

I am currently updating the library to catchup with all changes published on the latest Android and iOS Twilio Voice SDK:

- iOS 6.4.1 [iOS changelog](https://www.twilio.com/docs/voice/voip-sdk/ios/changelog)
- Android 6.1.1 [Android changelog](https://www.twilio.com/docs/voice/voip-sdk/android/3x-changelog)


### Breaking changes

- iOS 6.4.1
- Android 6.1.1

 `audioSwitch` have been implemented. `setSpeakerPhone()` has been removed from Android, use selectAudioDevice(name: string) instead.

#### Background incoming calls

- When the app is not in foreground incoming calls result in a heads-up notification with action to "ACCEPT" and "REJECT".
- ReactMethod `accept` does not dispatch any event. In v4 it dispatched `connectionDidDisconnect`.
- ReactMethod `reject` dispatches a `callInviteCancelled` event instead of `connectionDidDisconnect`.
- ReactMethod `ignore` does not dispatch any event. In v4 it dispatched `connectionDidDisconnect`.

Firebase Messaging 19.0.+ is imported by this module, so there is no need to import it in your app's `bundle.gradle` file.

To show heads up notifications, you must add the following lines to your application's `android/app/src/main/AndroidManifest.xml`:

```xml
<!-- [START instanceId_listener] -->
<service
	android:name="com.hoxfon.react.TwilioVoice.fcm.VoiceFirebaseInstanceIDService"
	android:exported="false">
		<intent-filter>
			<action  android:name="com.google.android.gms.iid.InstanceID"  />
		</intent-filter>
</service>
<!-- [END instanceId_listener] -->
```


#### Audio Switch

Access to native Twilio SDK AudioSwitch module for Android has been added to the JavaScript API:

```javascript
// getAudioDevices returns all audio devices connected
// {
//     "Speakerphone": false,
//     "Earnpiece": true, // true indicates the selected device
// }
getAudioDevices()

// getSelectedAudioDevice returns the selected audio device
getSelectedAudioDevice()

// selectAudioDevice selects the passed audio device for the current active call
selectAudioDevice(name: string)
```

#### Event deviceDidReceiveIncoming

When a call invite is received, the [SHAKEN/STIR](https://www.twilio.com/docs/voice/trusted-calling-using-shakenstir) `caller_verification` field has been added to the list of params for  `deviceDidReceiveIncoming`. Values are: `verified`, `unverified`, `unknown`.

## ICE

See https://www.twilio.com/docs/stun-turn

```bash
curl -X POST https://api.twilio.com/2010-04-01/Accounts/ACb0b56ae3bf07ce4045620249c3c90b40/Tokens.json \
-u ACb0b56ae3bf07ce4045620249c3c90b40:f5c84f06e5c02b55fa61696244a17c84
```

```java
Set<IceServer> iceServers = new HashSet<>();
// server URLs returned by calling the Twilio Rest API to generate a new token
iceServers.add(new IceServer("stun:global.stun.twilio.com:3478?transport=udp"));
iceServers.add(new IceServer("turn:global.turn.twilio.com:3478?transport=udp","8e6467be547b969ad913f7bdcfb73e411b35f648bd19f2c1cb4161b4d4a067be","n8zwmkgjIOphHN93L/aQxnkUp1xJwrZVLKc/RXL0ZpM="));
iceServers.add(new IceServer("turn:global.turn.twilio.com:3478?transport=tcp","8e6467be547b969ad913f7bdcfb73e411b35f648bd19f2c1cb4161b4d4a067be","n8zwmkgjIOphHN93L/aQxnkUp1xJwrZVLKc/RXL0ZpM="));
iceServers.add(new IceServer("turn:global.turn.twilio.com:443?transport=tcp","8e6467be547b969ad913f7bdcfb73e411b35f648bd19f2c1cb4161b4d4a067be","n8zwmkgjIOphHN93L/aQxnkUp1xJwrZVLKc/RXL0ZpM="));

IceOptions iceOptions = new IceOptions.Builder()
		.iceServers(iceServers)
		.build();

ConnectOptions connectOptions = new ConnectOptions.Builder(accessToken)
		.iceOptions(iceOptions)
		.enableDscp(true)
		.params(twiMLParams)
		.build();
```


### Installation

Before starting, we recommend you get familiar with [Twilio Programmable Voice SDK](https://www.twilio.com/docs/api/voice-sdk).
It's easier to integrate this module into your react-native app if you follow the Quick start tutorial from Twilio, because it makes very clear which setup steps are required.

```bash
npm install react-native-twilio-programmable-voice --save
```

- **React Native 0.60+**

[CLI autolink feature](https://github.com/react-native-community/cli/blob/master/docs/autolinking.md) links the module while building the app.

- **React Native <= 0.59**

```bash
react-native link react-native-twilio-programmable-voice
```

### iOS Installation

If you can't or don't want to use autolink, you can also manually link the library using the instructions below (click on the arrow to show them):

<details>
<summary>Manually link the library on iOS</summary>

Follow the [instructions in the React Native documentation](https://facebook.github.io/react-native/docs/linking-libraries-ios#manual-linking) to manually link the framework

After you have linked the library with `react-native link react-native-twilio-programmable-voice`
check that `libRNTwilioVoice.a` is present under YOUR_TARGET > Build Phases > Link Binaries With Libraries. If it is not present you can add it using the + sign at the bottom of that list.
</details>

```bash
cd ios && pod install
```

#### `AppDelegate.m`
```
...
- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    // RNTwilioVoice
    [[bridge moduleForClass:[RNTwilioVoice class]] initTwilioVoice:@"2zigexn"];
    ...
}
```

#### CallKit

The iOS library works through [CallKit](https://developer.apple.com/reference/callkit) and handling calls is much simpler than the  Android implementation as CallKit handles the inbound calls answering, ignoring, or rejecting. Outbound calls must be controlled by custom React-Native screens and controls.

To pass caller's name to CallKit via Voip push notification add custom parameter 'CallerName' to Twilio Dial verb.

```xml
    <Dial>
    <Client>
        <Identity>Client</Identity>
        <Parameter name="CallerName">NAME TO DISPLAY</Parameter>
    </Client>
    </Dial>
```

#### VoIP Service Certificate

Twilio Programmable Voice for iOS utilizes Apple's VoIP Services and VoIP "Push Notifications" instead of FCM. You will need a VoIP Service Certificate from Apple to receive calls. Follow [the official Twilio instructions](https://github.com/twilio/voice-quickstart-ios#7-create-voip-service-certificate) to complete this step.

### Android Installation

Setup FCM

You must download the file `google-services.json` from the Firebase console.
It contains keys and settings for all your applications under Firebase. This library obtains the resource `senderID` for registering for remote GCM from that file.

#### `android/build.gradle`

```groovy
buildscript {
    dependencies {
        // override the google-service version if needed
        // https://developers.google.com/android/guides/google-services-plugin
        classpath 'com.google.gms:google-services:4.3.12'
    }
}

// this plugin looks for google-services.json in your project
apply plugin: 'com.google.gms.google-services'
```

#### `AndroidManifest.xml`

```xml
    <uses-permission android:name="android.permission.VIBRATE" />

    <application ....>
        <!-- Twilio Voice -->
        <!-- [START fcm_listener] -->
        <service
            android:name="com.hoxfon.react.RNTwilioVoice.fcm.VoiceFirebaseMessagingService"
            android:stopWithTask="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
        <!-- [END fcm_listener] -->
```

If you can't or don't want to use autolink, you can also manually link the library using the instructions below (click on the arrow to show them):

<details>
<summary>Manually link the library on Android</summary>

Make the following changes:

#### `android/settings.gradle`

```groovy
include ':react-native-twilio-programmable-voice'
project(':react-native-twilio-programmable-voice').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-twilio-programmable-voice/android')
```

#### `android/app/build.gradle`

```groovy
dependencies {
   implementation project(':react-native-twilio-programmable-voice')
}
```

#### `android/app/src/main/.../MainApplication.java`
On top, where imports are:

```java
import com.hoxfon.react.RNTwilioVoice.TwilioVoicePackage;  // <--- Import Package
```

Add the `TwilioVoicePackage` class to your list of exported packages.

```java
@Override
protected List<ReactPackage> getPackages() {
    return Arrays.asList(
            new MainReactPackage(),
            new TwilioVoicePackage()         // <---- Add the package
            // new TwilioVoicePackage(false) // <---- pass false if you don't want to ask for microphone permissions
    );
}
```
</details>

## Usage
#### Init Twilio

```javascript
import TwilioVoice from 'react-native-twilio-programmable-voice'

// ...

// initialize the Programmable Voice SDK passing an access token obtained from the server.
// Listen to deviceReady and deviceNotReady events to see whether the initialization succeeded.
async function initTelephony() {
    try {
        const accessToken = await getAccessTokenFromServer()
        const success = await TwilioVoice.initWithToken(accessToken)
    } catch (err) {
        console.err(err)
    }
}

```

#### Register background call android
Add code in file `<root_project>/index.js`
```javascript
    function IncomingCallActivity(props){
        const {isAnswered} = props
        return {
            <View style={{flex: 1, justifyContent: 'center', alignItems: 'center'}}>
                <Text>Background call android will be open this screen</Text>
            </View>
        }
    }
    AppRegistry.registerComponent('IncomingCallActivity', () => IncomingCallActivity);

```

## Events

```javascript
// add listeners (flowtype notation)
TwilioVoice.addEventListener('deviceReady', function() {
    // no data
})
TwilioVoice.addEventListener('deviceNotReady', function(data) {
    // {
    //     err: string
    // }
})
TwilioVoice.addEventListener('connectionDidConnect', function(data) {
    // {
    //     call_sid: string,  // Twilio call sid
    //     call_state: 'CONNECTED' | 'ACCEPTED' | 'CONNECTING' | 'RINGING' | 'DISCONNECTED' | 'CANCELLED',
    //     call_from: string, // "+441234567890"
    //     call_to: string,   // "client:bob"
    // }
})
TwilioVoice.addEventListener('connectionIsReconnecting', function(data) {
    // {
    //     call_sid: string,  // Twilio call sid
    //     call_from: string, // "+441234567890"
    //     call_to: string,   // "client:bob"
    // }
})
TwilioVoice.addEventListener('connectionDidReconnect', function(data) {
    // {
    //     call_sid: string,  // Twilio call sid
    //     call_from: string, // "+441234567890"
    //     call_to: string,   // "client:bob"
    // }
})
TwilioVoice.addEventListener('connectionDidDisconnect', function(data: mixed) {
    //   | null
    //   | {
    //       err: string
    //     }
    //   | {
    //         call_sid: string,  // Twilio call sid
    //         call_state: 'CONNECTED' | 'ACCEPTED' | 'CONNECTING' | 'RINGING' | 'DISCONNECTED' | 'CANCELLED',
    //         call_from: string, // "+441234567890"
    //         call_to: string,   // "client:bob"
    //         err?: string,
    //     }
})
TwilioVoice.addEventListener('callStateRinging', function(data: mixed) {
    //   {
    //       call_sid: string,  // Twilio call sid
    //       call_state: 'CONNECTED' | 'ACCEPTED' | 'CONNECTING' | 'RINGING' | 'DISCONNECTED' | 'CANCELLED',
    //       call_from: string, // "+441234567890"
    //       call_to: string,   // "client:bob"
    //   }
})
TwilioVoice.addEventListener('callInviteCancelled', function(data: mixed) {
    //   {
    //       call_sid: string,  // Twilio call sid
    //       call_from: string, // "+441234567890"
    //       call_to: string,   // "client:bob"
    //   }
})

// iOS Only
TwilioVoice.addEventListener('callRejected', function(value: 'callRejected') {})

TwilioVoice.addEventListener('deviceDidReceiveIncoming', function(data) {
    // {
    //     call_sid: string,  // Twilio call sid
    //     call_from: string, // "+441234567890"
    //     call_to: string,   // "client:bob"
    // }
})

// Android Only
TwilioVoice.addEventListener('proximity', function(data) {
    // {
    //     isNear: boolean
    // }
})

// Android Only
TwilioVoice.addEventListener('wiredHeadset', function(data) {
    // {
    //     isPlugged: boolean,
    //     hasMic: boolean,
    //     deviceName: string
    // }
})

// ...

// start a call
TwilioVoice.connect({To: '+61234567890'})

// hangup
TwilioVoice.disconnect()

// accept an incoming call (Android only, in iOS CallKit provides the UI for this)
TwilioVoice.accept()

// reject an incoming call (Android only, in iOS CallKit provides the UI for this)
TwilioVoice.reject()

// ignore an incoming call (Android only)
TwilioVoice.ignore()

// mute or un-mute the call
// mutedValue must be a boolean
TwilioVoice.setMuted(mutedValue)

// put a call on hold
TwilioVoice.hold(holdValue)

// send digits
TwilioVoice.sendDigits(digits)

// Ensure that an active call is displayed when the app comes to foreground
TwilioVoice.getActiveCall()
    .then(activeCall => {
        if (activeCall){
            _displayActiveCall(activeCall)
        }
    })

// Ensure that call invites are displayed when the app comes to foreground
TwilioVoice.getCallInvite()
    .then(callInvite => {
        if (callInvite){
            _handleCallInvite(callInvite)
        }
    })

// Unregister device with Twilio
TwilioVoice.unregister()
```

## Help wanted

There is no need to ask permissions to contribute. Just open an issue or provide a PR. Everybody is welcome to contribute.

ReactNative success is directly linked to its module ecosystem. One way to make an impact is helping contributing to this module or another of the many community lead ones.

![help wanted](images/vjeux_tweet.png "help wanted")

## Twilio Voice SDK reference

[iOS changelog](https://www.twilio.com/docs/voice/voip-sdk/ios/changelog)
[Android changelog](https://www.twilio.com/docs/voice/voip-sdk/android/3x-changelog)

## Credits

[voice-quickstart-android](https://github.com/twilio/voice-quickstart-android)

[voice-quickstart-ios](https://github.com/twilio/voice-quickstart-ios)

[react-native-push-notification](https://github.com/zo0r/react-native-push-notification)

## License

MIT
