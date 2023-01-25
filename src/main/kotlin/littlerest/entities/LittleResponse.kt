package littlerest.entities

import com.google.gson.Gson
import okhttp3.*

class LittleResponse <T>(
    response: ResponseBody? = null,
    var request: Request? = null,
    var protocol: Protocol? = null,
    var code: Int = -1,
    var message: String? = null,
    var handshake: Handshake? = null,
    var headers: Headers.Builder,
    classType: Class<T>? = null,
    var networkResponse: Response? = null,
    var cacheResponse: Response? = null,
    var priorResponse: Response? = null,
    var sentRequestAtMillis: Long = 0,
    var receivedResponseAtMillis: Long = 0,
    gson: Gson = Gson()
) {
    var body: T? = gson.fromJson(response?.string(), classType)
}
