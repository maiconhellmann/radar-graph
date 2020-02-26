package com.epolly.radargraph.model

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
class Mock {
    companion object {
        fun createDataList(): DataList<String> {

            val typeList = listOf(
                VertexType(1, "Type one"), //
                VertexType(2, "Type two"), //
                VertexType(3, "Type three"), //
                VertexType(4, "Type four")) //

            return DataList(
                listOf(
                    //first
                    Data(
                        1, "First", mutableListOf( //
                            Vertex(typeList[0], "100"), //
                            Vertex(typeList[1], "200"), //
                            Vertex(typeList[2], "300"), //
                            Vertex(typeList[3], "320")) //
                    ),
                    // second
                    Data(
                        1, "Second", mutableListOf( //
                            Vertex(typeList[0], "150"), //
                            Vertex(typeList[1], "180"))))) //
        }
    }
}
