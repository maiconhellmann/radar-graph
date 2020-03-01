package com.epolly.radargraph

import Data
import DataList
import Vertex
import VertexType

/*
 * This file is part of radar-graph.
 * 
 * Created by maiconhellmann on 26/02/2020
 * 
 * (c) 2020 
 */
class DummyData {
    companion object {
        fun createDummyData1(): DataList {

            val typeList = listOf(
                VertexType(1, "Vertex type one"), //
                VertexType(2, "Vertex type two"), //
                VertexType(3, "Vertex type three"), //
                VertexType(4, "Vertex type four")
            ) //

            return DataList(
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
        }

        fun createDummyData2(): DataList {

            val typeList = listOf(
                VertexType(1, "Type A"), //
                VertexType(2, "Type B"), //
                VertexType(3, "Type C"), //
                VertexType(4, "Type D"), //
                VertexType(5, "Type E")
            ) //

            return DataList(
                title = "Dummy 2",
                dataList = listOf(
                    //first
                    Data(
                        id = 1, name = "First",
                        color = R.color.graph_third,
                        vertexList = mutableListOf( //
                            Vertex(typeList[0], "250"),
                            Vertex(typeList[1], "90"),
                            Vertex(typeList[2], "120"),
                            Vertex(typeList[3], "150"),
                            Vertex(typeList[4], "150")
                        ) //
                    ),
                    // second
                    Data(
                        id = 2,
                        name = "Second",
                        color = R.color.graph_fourth,
                        vertexList = mutableListOf(
                            Vertex(typeList[0], "150"),
                            Vertex(typeList[1], "220"),
                            Vertex(typeList[2], "220"),
                            Vertex(typeList[3], "500"),
                            Vertex(typeList[4], "20")
                        )
                    ),
                    // third
                    Data(
                        id = 3,
                        name = "Third",
                        color = R.color.graph_first,
                        vertexList = mutableListOf(
                            Vertex(typeList[0], "50"),
                            Vertex(typeList[1], "45"),
                            Vertex(typeList[2], "90"),
                            Vertex(typeList[3], "12")
                        )
                    )
                )
            ) //
        }
    }
}
