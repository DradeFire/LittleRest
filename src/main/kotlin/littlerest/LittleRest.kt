package littlerest

import com.google.gson.Gson
import littlerest.entities.LittleResponse
import littlerest.entities.RequestNotExistException
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.net.URL
import java.time.Duration

class LittleRest private constructor(
    private val client: OkHttpClient,
    private val mapRequests: HashMap<String, Request>,
    private val mapClasses: HashMap<String, Class<*>>,
    private val gson: Gson
) {

    fun <T> sendSyncRequest(nameRequest: String) : LittleResponse <T> {
        return mapRequests[nameRequest]?.let {
            val response = client.newCall(it).execute()
            LittleResponse(
                response.body,
                response.request,
                response.protocol,
                response.code,
                response.message,
                response.handshake,
                response.headers.newBuilder(),
                mapClasses[nameRequest] as Class<T>,
                response.networkResponse,
                response.cacheResponse,
                response.priorResponse,
                response.sentRequestAtMillis,
                response.receivedResponseAtMillis,
                gson
            )
        } ?: throw RequestNotExistException(nameRequest)
    }

    fun <T> sendAsyncRequest(
        nameRequest: String,
        onSuccess: (call: Call, response: LittleResponse<T>) -> Unit,
        onFailure: (call: Call, e: IOException) -> Unit
    ) {
        mapRequests[nameRequest]?.let {
            client.newCall(it).enqueue(
                object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        onFailure(call, e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val resp = LittleResponse(
                            response.body,
                            response.request,
                            response.protocol,
                            response.code,
                            response.message,
                            response.handshake,
                            response.headers.newBuilder(),
                            mapClasses[nameRequest] as Class<T>,
                            response.networkResponse,
                            response.cacheResponse,
                            response.priorResponse,
                            response.sentRequestAtMillis,
                            response.receivedResponseAtMillis,
                            gson
                        )
                        onSuccess(call, resp)
                    }

                }
            )
            return
        }
        throw RequestNotExistException(nameRequest)
    }

    class Builder {

        private var clientBuilder = OkHttpClient.Builder()
        private val mapRequests = HashMap<String, Request>()
        private val mapClasses = HashMap<String, Class<*>>()
        private var gson = Gson()

        fun addCustomClientBuilder(client: OkHttpClient.Builder) {
            clientBuilder = client
        }

        fun createAndAddRequest(
            key: String,
            responseBodyClass: Class<*> = Any::class.java,
            urlString: String? = null,
            urlURL: URL? = null,
            urlHttpUrl: HttpUrl? = null,
            isGet: Boolean = false,
            isPost: Boolean = false,
            isPatch: Boolean = false,
            isPut: Boolean = false,
            isDelete: Boolean = false,
            bodyString: String? = null,
            bodyRequestBody: RequestBody? = null,
            headers: HashMap<String, String> = hashMapOf(),
        ) = apply {
            val request = Request.Builder().apply {
                if (isGet) {
                    get()
                }
                if (isPost) {
                    if (bodyRequestBody != null) {
                        post(bodyRequestBody)
                    } else {
                        bodyString?.let {
                            post(it.toRequestBody())
                        } ?: post("".toRequestBody())
                    }
                }
                if (isPatch) {
                    if (bodyRequestBody != null) {
                        patch(bodyRequestBody)
                    } else {
                        bodyString?.let {
                            patch(it.toRequestBody())
                        } ?: patch("".toRequestBody())
                    }
                }
                if (isPut) {
                    if (bodyRequestBody != null) {
                        put(bodyRequestBody)
                    } else {
                        bodyString?.let {
                            put(it.toRequestBody())
                        } ?: put("".toRequestBody())
                    }
                }
                if (isDelete) {
                    if (bodyRequestBody != null) {
                        delete(bodyRequestBody)
                    } else {
                        bodyString?.let {
                            delete(it.toRequestBody())
                        } ?: delete("".toRequestBody())
                    }
                }

                for ((header, value) in headers) {
                    addHeader(header, value)
                }

                if (urlString != null) {
                    url(urlString)
                } else if (urlURL != null) {
                    url(urlURL)
                } else if(urlHttpUrl != null) {
                    url(urlHttpUrl)
                } else {
                    throw Exception("Url not added")
                }
            }.build()
            mapRequests[key] = request
            mapClasses[key] = responseBodyClass
        }

        fun addLoggingInterceptor(level: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY) = apply {
            clientBuilder.addNetworkInterceptor(HttpLoggingInterceptor().apply { this.level = level })
        }

        fun addCustomInterceptor(interceptor: Interceptor) = apply {
            clientBuilder.addInterceptor(interceptor)
        }

        fun addCustomNetworkInterceptor(interceptor: Interceptor) = apply {
            clientBuilder.addNetworkInterceptor(interceptor)
        }

        fun addAllTimeouts(seconds: Long) = apply {
            clientBuilder.callTimeout(Duration.ofSeconds(seconds))
            clientBuilder.connectTimeout(Duration.ofSeconds(seconds))
            clientBuilder.readTimeout(Duration.ofSeconds(seconds))
            clientBuilder.writeTimeout(Duration.ofSeconds(seconds))
        }

        fun addConverter(gson: Gson) = apply {
            this@Builder.gson = gson
        }

        fun build() = LittleRest(
            clientBuilder.build(),
            mapRequests,
            mapClasses,
            gson
        )

    }

}