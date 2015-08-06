# DecoView

Animated arc based graphing library for Android.


Usage
===
```xml
    <com.hookedonplay.decoviewlib.DecoView
        xmlns:custom="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/dynamicArcView"
        android:layout_margin="8dp">
```

```java
  
	DecoView arcView = (DecoView)findViewById(R.id.dynamicArcView);
	
	// Create background track
	arcView.addSeries(new SeriesItem.Builder(Color.argb(255, 218, 218, 218))
		.setRange(0, 100, 100)
		.setInitialVisibility(false)
		.setLineWidth(getDimension(32f))
		.build());
		
	//Create data series track
	SeriesItem seriesItem1 = new SeriesItem.Builder(Color.argb(255, 64, 196, 0))
		.setRange(0, 100, 0)		
		.setLineWidth(32f)		
		.build();

	int series1Index = arcView.addSeries(seriesItem1);
	
	arcView.addEvent(new DecoEvent.Builder(EventType.EVENT_SHOW, true)
		.setDelay(1000)
		.setDuration(2000)
		.setLinkedViews(linkedViews)
		.build());

	arcView.addEvent(new DecoEvent.Builder(25).setIndex(mSeries1Index).setDelay(4000).build());
	arcView.addEvent(new DecoEvent.Builder(100).setIndex(mSeries1Index).setDelay(8000).build());

```

Requirements
===

Android 2.3+

With Thanks
===
- Jake Wharton for <a href="https://github.com/JakeWharton/NineOldAndroids/">NineOldAndroids</a> allowing support for Android 2.3+ devices.

License
===

    Copyright 2015 Brent Marriott

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
