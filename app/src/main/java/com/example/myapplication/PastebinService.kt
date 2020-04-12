import com.example.myapplication.SMSContact
import retrofit2.Call
import retrofit2.http.GET

interface PastebinService {
    @GET("NNKVVupx")
    fun getPasteBinJSON(): Call<SMSContact>
}