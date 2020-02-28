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
        fun createDummyData1(): DataList<String> {

            val typeList = listOf(
                VertexType(1, "Type one"), //
                VertexType(2, "Type two"), //
                VertexType(3, "Type three"), //
                VertexType(4, "Type four")
            ) //

            return DataList(
                listOf(
                    //first
                    Data(
                        1, "First", mutableListOf( //
                            Vertex(typeList[0], "100"), //
                            Vertex(typeList[1], "200"), //
                            Vertex(typeList[2], "300"), //
                            Vertex(typeList[3], "320")
                        ) //
                    ),
                    // second
                    Data(
                        2, "Second", mutableListOf( //
                            Vertex(typeList[0], "150"), //
                            Vertex(typeList[1], "220")
                        )
                    )
                )
            ) //
        }

        fun createDummyData2(): DataList<String> {

            val typeList = listOf(
                VertexType(1, "Type A"), //
                VertexType(2, "Type B"), //
                VertexType(3, "Type C"), //
                VertexType(4, "Type D"), //
                VertexType(5, "Type E")
            ) //

            return DataList(
                listOf(
                    //first
                    Data(
                        1, "First", mutableListOf( //
                            Vertex(typeList[0], "250"), //
                            Vertex(typeList[1], "90"), //
                            Vertex(typeList[2], "120"), //
                            Vertex(typeList[3], "150")
                        ) //
                    ),
                    // second
                    Data(
                        2, "Second", mutableListOf( //
                            Vertex(typeList[0], "150"), //
                            Vertex(typeList[1], "220"), //
                            Vertex(typeList[2], "220"), //
                            Vertex(typeList[3], "500")
                        )
                    ),
                    // third
                    Data(
                        3, "Third", mutableListOf( //
                            Vertex(typeList[0], "50"), //
                            Vertex(typeList[1], "45"), //
                            Vertex(typeList[2], "90"), //
                            Vertex(typeList[3], "12")
                        )
                    )
                )
            ) //
        }
    }
}
