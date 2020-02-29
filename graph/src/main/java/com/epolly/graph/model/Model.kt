import androidx.annotation.ColorRes

/**
 * Wraps a list of Data(list of vertex).
 * Each element of the list is a different graph. The last one will be drawn on top of the others.
 * There is no limitation, it can contain 0 .. N elements.
 * If a vertex doesn't have a value for a specific and distict VertexType this wrapper will add a null value for it.
 */
class DataList<T>(
    /**
     * Graph's title to be displayed.
     * TODO: Not implemented
     */
    val title: String? = null,
    dataList: List<Data<T>>,

    /**
     * Converts a vertex model to a String. Used to display a label
     */
    private val asString: ((Vertex<T>) -> String)? = null,

    /**
     * Converts a vertex model to a number to be able to calc the position on the graph
     */
    private val asNumber: ((Vertex<T>) -> Number)? = null
) {
    val typeList: MutableSet<VertexType> = mutableSetOf()
    val dataList: List<Data<T>>

    init {
        typeList.addAll(dataList.flatMap { it.vertexList.map { dataValue -> dataValue.type } })

        // Validate
        dataList.forEach { data ->
            typeList.forEach { dataType ->
                // Add a default value if there is no vertex for all the types used
                data.vertexList.firstOrNull { it.type == dataType } ?: data.vertexList.add(
                    Vertex(
                        dataType,
                        null
                    )
                )

                data.vertexList.forEach {
                    it.asNumber = asNumber
                    it.asString = asString
                }
            }
        }

        this.dataList = dataList
    }

    override fun toString(): String {
        return dataList.toString()
    }
}

/**
 * Wraps a list of vertex values
 */
class Data<T>(
    val id: Int, val name: String = "",
    @ColorRes val color: Int,
    val vertexList: MutableList<Vertex<T>>
) {

    override fun toString(): String {
        return "$id - $name - $vertexList"
    }
}

/**
 * Represents each value of a vertex. If the value is null it will be considered the minimum possible value(zero)
 */
class Vertex<T>(
    val type: VertexType,
    val value: T? = null,
    var asString: ((Vertex<T>) -> String)? = null,
    var asNumber: ((Vertex<T>) -> Number)? = null
) {
    override fun toString(): String {
        if (asString == null && value == null) return ""

        return asString?.invoke(this) ?: value.toString()
    }

    fun asNumber(): Number {
        if (asNumber == null && value == null) return 0

        return asNumber?.invoke(this) ?: value.toString().toInt()
    }
}

/**
 * Type of the dataModel. The label is shown on each vertex
 */
data class VertexType(
    val id: Int, val label: String
)
