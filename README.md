# radar-graph
This graph is not completely fully implemented.

Backlog:
* Display the data model label name
* Display the data model legends
* Display graph name if set
* Release it as a library

## Usage
### Layout:
```
<com.epolly.radargraph.RadarGraphView
        android:id="@+id/graph"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        android:background="#FFFFFF"
        app:circlesAmount="4"
        app:circlesColor="@color/defaultOval"
        app:circlesMarginPercent="30"
        app:axisPointRadiusPercent="1"
        app:axisLineStrokePercent=".3"
        app:noDataFoundText="@string/default_no_data_found"
        app:axisLineColor="@color/defaultLineAxis"
        app:axisTitleTextColor="@color/grey"
        app:axisCircleColor="@color/defaultCircleAxis"/>
```

### Code
```
graph.dataModel= emptyList<DataList<String>>()
```

### Demo
<img src="https://github.com/maiconhellmann/radar-graph/blob/master/doc/ss.png" width="50%">
