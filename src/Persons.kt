import com.google.gson.Gson
import java.io.File
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

//ANNOTATION
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Mandatory

//THROWABLE
class EntityInvalid(message: String?) : Throwable(message)

//NETWORK MODEL
data class PersonWS(
        val name: String,
        val age: Int,
        val description: String,
        val shows: List<ShowWS>
)

data class ShowWS(
        val name: String,
        val channel: String
)

//ENTITY
data class PersonEntity(
        val name: String,
        val age: Int,
        val description: String,
        val shows: List<ShowEntity>
)

data class ShowEntity(
        val name: String,
        val channel: String
) : EntityValidation

abstract class AbsMapper

class PersonsMapper : AbsMapper() {

    fun convert(personsWS: Array<PersonWS>): List<PersonEntity> {
        val persons = arrayListOf<PersonEntity>()
        personsWS.forEach {personWS ->
            try {
                val personEntity = convert(personWS)
                persons.add(personEntity)
            } catch (e:Exception) {
                println(e.message)
            }
        }
        return persons
    }

    fun convert(personWS: PersonWS): PersonEntity {
        val shows = arrayListOf<ShowEntity>()
        personWS.shows.forEach {showWS ->
            try {
                val showEntity = convert(showWS)
                shows.add(showEntity)
            } catch (e:Exception) {
                println(e.message)
            }
        }
        val personEntity = PersonEntity(personWS.name, personWS.age, personWS.description, shows)
        return personEntity
    }

    fun convert(WS: ShowWS): ShowEntity {
        return ShowEntity(WS.name, WS.channel)
    }

}

interface EntityValidation {
    @Throws(EntityInvalid::class)
    fun validate() {
        javaClass.kotlin.memberProperties.forEach {
            val isMandatory = it.javaField?.isAnnotationPresent(Mandatory::class.java) ?: false
            if (isMandatory) {
                val value = it.get(this)
                when (value) {
                    is String -> if (value.isBlank()) throw EntityInvalid("${it.name} must not be empty")
                }
                println(value)
            }
        }
    }
}

fun main(args: Array<String>) {
    val fileContent = readFileAsLinesUsingReadLines("persons.json")
    val personsWS = Gson().fromJson(fileContent, Array<PersonWS>::class.java)
    val personsEntity = PersonsMapper().convert(personsWS)
    println(personsEntity)
}

//UTIL
fun readFileAsLinesUsingReadLines(fileName: String): String = File(fileName).readText()