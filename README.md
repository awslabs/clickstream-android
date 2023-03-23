# AWS Solution Clickstream Analytics SDK for Android

## Introduction
Clickstream Android SDK can help you easily report in-app events on Android. After the event is reported, statistics and analysis of specific scenario data can be completed on AWS Clickstream solution.

The SDK relies on the Amplify for Android SDK Core Library and is developed according to the Amplify Android SDK plug-in specification, while using the same event definitions and attribute specifications as amplifyframework analytics. In addition to this, we've added commonly used preset event statistics to make it easier to use.

### Platform Support

The Clickstream SDK supports Android API level 16 (Android 4.1) and above.

## How to build locally
### Config your local environment
First of all you should install the latest version of [Android Studio](https://developer.android.com/studio).
####  Config your checkstyle:
1. Open your Android Studio -> Preferences -> Tools -> check style window.
2. Change the check style version to 8.29.
3. Add config file from ./configuration/checkstyle.gradle. then check and apply.

####  Config your code format
1. Open your Android Studio -> Preferences -> Editor -> Code Style -> Java window.
2. Click the top setting icon -> import scheme -> checkstyle configuration
3. Select ./configuration/checkstyle.gradle file, then click ok to submit.
4. Config your Reformat code keymap to format your code with checkstyle configured above.

#### Config your java version
1. Open your Android Studio -> Preferences ->  Build, Execution, Deployment -> Build Tools -> Gradle window.
2. make sure your `Gradle JDK` version is set to the 1.8, then click apply and ok.

###  Build aar
open an terminal window,at the root project folder to execute:
```shell
./gradlew build -p clickstream
```

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This library is licensed under the [Apache 2.0 License](./LICENSE).