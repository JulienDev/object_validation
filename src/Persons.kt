import java.io.File
import rx.Single
import rx.schedulers.Schedulers

//NETWORK MODEL
data class PersonWS(
        val name: String,
        val age: Int,
        val description: String,
        val shows: List<ShowWS>,
        val spouse: PersonWS
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
)

class PersonsMapper {

    fun convert(personsWS: Array<PersonWS>): Single<List<PersonEntity>> {
        val persons = arrayListOf<PersonEntity>()
        personsWS.forEach { personWS ->
            try {
                val personEntity = convert(personWS)
                persons.add(personEntity)
            } catch (e: Exception) {
                e.printStackTrace()
                return Single.error(e)
            }
        }
        return Single.just(persons)
    }

    private fun convert(personWS: PersonWS): PersonEntity {
        val shows = arrayListOf<ShowEntity>()
        personWS.shows.forEach { showWS ->
            try {
                val showEntity = convert(showWS)
                shows.add(showEntity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return PersonEntity(personWS.name, personWS.age, personWS.description, shows)
    }

    private fun convert(WS: ShowWS): ShowEntity {
        return ShowEntity(WS.name, WS.channel)
    }

}

fun main(args: Array<String>) {
    PersonsProvider().getRetrofit().getPersons()
            .flatMapSingle {
                PersonsMapper().convert(it)
            }
            .subscribeOn(Schedulers.io())
            .toBlocking()
            .subscribe({
                println(it)
            },{
                it.printStackTrace()
            })
}

//UTIL
fun readFileAsLinesUsingReadLines(fileName: String): String = File(fileName).readText()