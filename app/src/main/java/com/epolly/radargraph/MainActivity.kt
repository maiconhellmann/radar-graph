package com.epolly.radargraph

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.graph

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dummyData1 = DummyData.createDummyData1()
        val dummyData2 = DummyData.createDummyData2()

        graph.isAnimationEnabled = true
        graph.dataModel = dummyData1

        graph.setOnClickListener {
            if (graph.dataModel.dataList.size == dummyData1.dataList.size) {
                graph.dataModel = dummyData2
            } else {
                graph.dataModel = dummyData1
            }
        }
    }
}
