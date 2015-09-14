# DecoView Changelog

v0.95
===

Remove `android:allowBackup="true"` from library project to prevent compilation issues for projects that require this attribute to be overridden.
This manifest merger issue is discussed in the AOSP issue tracker issue 70073.

v0.9.4
===

- Added pause() and resume() support for data series move animation
- Added pause/resume sample fragment to sample app
- Added support for 0 duration move events (no animation)
- Changed default move effect animation duration from 2000ms to duration calculated by total series spin duration
- Deprecated function DecoView.getSeriesItem(int index). Use DecoView.getChartSeries(index).getSeriesItem()

v0.9.3
===

- All xml attributes prefixed with 'dv_'

v0.9.2
===

- Add Travis Continuous Integration
- Added support back to Android 2.2 (API 8)
- Added new example charts to sample app
- Reverse primary and secondary color for drawing gradient


v0.9.1
===

Initial implementation
