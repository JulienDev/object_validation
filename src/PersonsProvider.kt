import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.http.GET
import rx.Observable
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class PersonsProvider {

    fun getRetrofit() : PersonsAPI {
        val gson = GsonBuilder().setPrettyPrinting().create()

        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(getHttpClient())
                .baseUrl("https://www.google.fr/")
                .build()
        return retrofit.create(PersonsAPI::class.java)
    }

    fun getHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor { chain ->
                    val content = readFileAsLinesUsingReadLines("persons.json")
                    Response.Builder()
                            .body(ResponseBody.create(MediaType.parse("application/json"), content))
                            .message("")
                            .request(chain.request())
                            .protocol(Protocol.HTTP_2)
                            .code(200)
                            .build()
                }
                .build()
    }


}

interface PersonsAPI {

    @GET("/")
    fun getPersons(): Observable<Array<PersonWS>>

}