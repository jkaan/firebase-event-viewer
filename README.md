# Firebase Events Viewer

<p align="center">
  <img src="assets/analytics.png?raw=true" alt="logo firebase events viewer"  width="250px"/>
</p>

Shows events that are triggered with Firebase in the app while they happen in a notification.

## Installation

Add this to your root build.gradle:
```
allprojects {
 	repositories {
 		...
 		maven { url 'https://jitpack.io' }
    }
}
```

Add this to your app build.gradle:

`implementation 'com.github.jkaan:firebase-event-viewer:(insert latest version)'`

## Usage

Since Firebase does not have a way to hook into which events are triggered this library uses Logcat to read what events are logged to show them. Therefore you have to set these properties in the device/emulator:

- `adb shell setprop log.tag.FA VERBOSE`
- `adb shell setprop log.tag.FA-SVC VERBOSE`

Having done this, whenever an event is logged to Firebase, it will be visible in a notification. Currently you can only see the most relevant event.
