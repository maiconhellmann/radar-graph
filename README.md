# radar-graph
<p float="left">
    <img src="https://github.com/maiconhellmann/radar-graph/blob/master/doc/ss.png" width="200">
    <img src="https://github.com/maiconhellmann/radar-graph/blob/master/doc/sample.gif" width="200">
</p>

## Usage

### Import
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

```
dependencies {
	        implementation 'com.github.maiconhellmann:radar-graph:0.1.0'
	}

```

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
val typeList = listOf(
                VertexType(1, "Vertex type one"), //
                VertexType(2, "Vertex type two"), //
                VertexType(3, "Vertex type three"), //
                VertexType(4, "Vertex type four")) //

val dataModel = DataList(
    title = "Dummy 1",
    dataList = listOf(
        //first
        Data(
            id = 1,
            name = "First",
            color = R.color.graph_first,
            vertexList = mutableListOf( //
                Vertex(typeList[0], "100"),
                Vertex(typeList[1], "200"),
                Vertex(typeList[2], "300"),
                Vertex(typeList[3], "320")
            )
        ),
        // second
        Data(
            id = 2,
            name = "Second",
            color = R.color.graph_second,
            vertexList = mutableListOf(
                Vertex(typeList[0], "150"),
                Vertex(typeList[1], "220")
            )
        )
    )
) //

graph.dataModel= dataModel
```
