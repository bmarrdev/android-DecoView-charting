# DecoView Changelog

## [Unreleased]

- Update to latest support libraries
- Minimum SDK raised to 9 (Android 2.3)

## 1.0.0

- Support for Android Studio 2.0
- Update targetSdkVersion to 23
- Update to latest AppCompat library 'com.android.support:appcompat-v7:23.3.0'
- Verify support for Android N
- Verify split screen behavior on Android N
- Promote release to 1.0

## 0.9.6

- Fix issue where line width of arc was incorrect if onPause was called during the Event Show effect

## 0.9.5

- Remove `android:allowBackup="true"` from library project to prevent compilation issues for projects that require this attribute to be overridden.
This manifest merger issue is discussed in the AOSP issue tracker issue 70073.

## 0.9.4

- Added pause() and resume() support for data series move animation
- Added pause/resume sample fragment to sample app
- Added support for 0 duration move events (no animation)
- Changed default move effect animation duration from 2000ms to duration calculated by total series spin duration
- Deprecated function DecoView.getSeriesItem(int index). Use DecoView.getChartSeries(index).getSeriesItem()

## 0.9.3

- All xml attributes prefixed with 'dv_'

## 0.9.2

- Add Travis Continuous Integration
- Added support back to Android 2.2 (API 8)
- Added new example charts to sample app
- Reverse primary and secondary color for drawing gradient

## 0.9.1

Initial implementation
