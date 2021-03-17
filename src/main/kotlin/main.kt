import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import java.io.File
import java.io.IOException
import kotlin.jvm.Throws

data class JSONTest (
    val input: String,
    val expects: String
)

/**
 * Run with `kotlinc src/main/kotlin/main.kt` in terminal at project root level.
 */
fun main(args: Array<String>) {
    val tests: Array<JSONTest> = arrayOf(
        JSONTest(
            """{"a":1,"b":true,"c":{"d":3,"e":"test","f":false}}""",
            """{"a":1.0,"b":true,"c.d":3.0,"c.e":"test","c.f":false}"""
        ),
        JSONTest(
            """{"a":2,"b":false,"c":{"d":3,"e":{"f":4,"g":false,"h":"foo bar"}}}""",
            """{"a":2.0,"b":false,"c.d":3.0,"c.e.f":4.0,"c.e.g":false,"c.e.h":"foo bar"}"""
        ),
        JSONTest(
            """{"a":{"b":{"c":{"d":0,"e":{"f":"Hello","g":"world!","h":true}},"i":{"j":0,"k":{"l":"foo"},"m":"bar"}},"n":false,"o":{"p":0.4,"q":true}}}""",
            """{"a.b.c.d":0.0,"a.b.c.e.f":"Hello","a.b.c.e.g":"world!","a.b.c.e.h":true,"a.b.i.j":0.0,"a.b.i.k.l":"foo","a.b.i.m":"bar","a.n":false,"a.o.p":0.4,"a.o.q":true}"""
        )
    )

    for (test in tests) {
        assertEquals(flattenJson(test.input) == test.expects)
    }
}

@Throws(Exception::class)
fun assertEquals(expression: Boolean) {
    if (!expression) println("Expression does not fulfill expected output") // throw java.lang.Exception("Expression is not fulfilled")
    else println("Expression fulfills expected output")
}

/**
 * This function flattens the embedded data from a given String in a JSON-like formatted String
 * @param jsonString A String containing JSON-like formatted data
 * @return A String containing the same data as the jsonString argument, but flattened
 */
fun flattenJson(jsonString: String): String {
    var jsonMap: MutableMap<String, Any> = HashMap()
    val flattenedMap: MutableMap<String, Any> = mutableMapOf()

    jsonMap = Gson().fromJson(jsonString, jsonMap.javaClass)

    for ((k, v) in jsonMap) {
        if (v is LinkedTreeMap<*, *>) {
            val pairs = flattenObject(k, v)
            for (p in pairs) flattenedMap[p.first] = p.second
        } else flattenedMap[k] = v
    }

    return Gson().toJson(flattenedMap)
}

/**
 * Recursive function to flatten the JSON values inside an object
 * @param key Current key of the object to check
 * @param value Value of the JSON object to check
 * @return A List of Pair<String, Any> containing all the sub-objects found in a JSON sub-object
 */
fun flattenObject (key: String, value: LinkedTreeMap<*, *>): List<Pair<String, Any>> {
    val mutlist: MutableList<Pair<String, Any>> = mutableListOf()
    for ((k, v) in value) {
        if (v is LinkedTreeMap<*, *>) mutlist.addAll(flattenObject("$key.$k", v))
        else mutlist.add(Pair("$key.$k", v))
    }
    return mutlist.toList()
}

/**
 * This function retrieves a the contents of a json file as a String
 * @param fileName Name of the JSON file to read. It must be located in the "resources" folder.
 * @return The contents of the JSON file as a String
 */
fun getJsonAsString(fileName: String): String? {
    val jsonStr: String
    try {
        jsonStr = File(ClassLoader.getSystemResource(fileName).file).readText()
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
    return jsonStr
}
