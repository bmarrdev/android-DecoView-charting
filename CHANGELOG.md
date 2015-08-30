# DecoView Changelog

v0.9.4
===

- Added pause() and resume() support for data series move animation
- Added pause/resume sample fragment to sample app
- Added support for 0 duration move events (no animation)
- Changed default move effect animation duration from 2000ms to duration calculated by total series spin duration
- Deprecated function DecoView.getSeriesItem(int index). Use DecoView.getChartSeries(index).getSeriesItem()
- Updated compileSdkVersion 23
- Updated buildToolsVersion "23.0.0"
- Updated targetSdkVersion 23

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
