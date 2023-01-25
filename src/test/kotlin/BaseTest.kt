import littlerest.LittleRest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class BaseTest {
    private val server = MockWebServer()
    private var rest: LittleRest? = null

    @Before
    fun setUp() {
        server.dispatcher = dispatcher
        server.start(8080)

        val url = "http://${server.hostName}:8080"
        rest = LittleRest.Builder()
            .createAndAddRequest(
                "api-test-specific-code",
                urlString = "$url/api-test-specific-code",
                isGet = true,
            )
            .createAndAddRequest(
                "api-test-get",
                responseBodyClass = TestClass::class.java,
                urlString = "$url/api-test-get",
                isGet = true,
            )
            .createAndAddRequest(
                "api-test-post",
                urlString = "$url/api-test-post",
                isPost = true,
            )
            .createAndAddRequest(
                "api-test-put",
                urlString = "$url/api-test-put",
                isPut = true,
            )
            .createAndAddRequest(
                "api-test-patch",
                urlString = "$url/api-test-patch",
                isPatch = true,
            )
            .createAndAddRequest(
                "api-test-delete",
                urlString = "$url/api-test-delete",
                isDelete = true,
            )

            .createAndAddRequest(
                "api-test-post-auth-data",
                bodyString = """
                    {
                        "login":"login",
                        "password":"password"
                    }
                """.trimIndent(),
                urlString = "$url/api-test-post-auth-data",
                isPost = true,
            )
            .createAndAddRequest(
                "api-test-post-auth-token",
                urlString = "$url/api-test-post-auth-token",
                isPost = true,
                headers = hashMapOf("Authorization" to "token@12345")
            )
            .createAndAddRequest(
                "api-test-not-exist",
                urlString = "$url/nOtExIsTuRl-lol_lmao",
                isGet = true,
            )
//            .createAndAddRequest(
//                "api-test-get-timeout",
//                urlString = "$url/api-test-get-timeout",
//                isPost = true,
//            )
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `api-test-specific-code`() {
        val response = rest?.sendSyncRequest<Any>("api-test-specific-code")
        assert(228 == response?.code)
    }

    @Test
    fun api_test_get() {
        val response = rest?.sendSyncRequest<TestClass>("api-test-get")
        val body = response?.body
        assert(200 == response?.code)
        assert(2 == body?.data?.id)
        assert("fuchsia rose" == body?.data?.name)
        assert(2001 == body?.data?.year)
        assert("#C74375" == body?.data?.color)
        assert("17-2031" == body?.data?.pantone_value)
        assert("https://reqres.in/#support-heading" == body?.support?.url)
        assert("To keep ReqRes free, contributions towards server costs are appreciated!" == body?.support?.text)
    }

    @Test
    fun `api-test-post`() {
        val response = rest?.sendSyncRequest<Any>("api-test-post")
        assert(200 == response?.code)
    }

    @Test
    fun `api-test-patch`() {
        val response = rest?.sendSyncRequest<Any>("api-test-patch")
        assert(200 == response?.code)
    }

    @Test
    fun `api-test-put`() {
        val response = rest?.sendSyncRequest<Any>("api-test-put")
        assert(200 == response?.code)
    }

    @Test
    fun `api-test-delete`() {
        val response = rest?.sendSyncRequest<Any>("api-test-delete")
        assert(200 == response?.code)
    }

    @Test
    fun `api-test-post-auth-data`() {
        val response = rest?.sendSyncRequest<Any>("api-test-post-auth-data")
        assert(200 == response?.code)
    }

    @Test
    fun `api-test-post-auth-token`() {
        val response = rest?.sendSyncRequest<Any>("api-test-post-auth-token")
        assert(200 == response?.code)
    }

    @Test
    fun `api-test-get-timeout`() {

    }

    @Test
    fun `api-test-not-exist`() {
        val response = rest?.sendSyncRequest<Any>("api-test-not-exist")
        assert(404 == response?.code)
    }

    data class TestClass(
        val data: Data,
        val support: Support
    )

    data class Data (
        val id: Int,
        val name: String,
        val year: Int,
        val color: String,
        val pantone_value: String
    )

    data class Support(
        val url: String,
        val text: String
    )

}

val dispatcher = object : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        return when(request.path) {
            "/api-test-specific-code" -> {
                MockResponse()
                    .setResponseCode(228)
            }
            "/api-test-get" -> {
                MockResponse()
                    .setResponseCode(200)
                    .setBody("""
                        {"data":{"id":2,"name":"fuchsia rose","year":2001,"color":"#C74375","pantone_value":"17-2031"},"support":{"url":"https://reqres.in/#support-heading","text":"To keep ReqRes free, contributions towards server costs are appreciated!"}}
                    """.trimIndent())
            }
            "/api-test-post" -> {
                val response = MockResponse()
                return if (request.method == "POST") {
                    response.setResponseCode(200)
                } else {
                    response.setResponseCode(400)
                }
            }
            "/api-test-patch" -> {
                val response = MockResponse()
                return if (request.method == "PATCH") {
                    response.setResponseCode(200)
                } else {
                    response.setResponseCode(400)
                }
            }
            "/api-test-put" -> {
                val response = MockResponse()
                return if (request.method == "PUT") {
                    response.setResponseCode(200)
                } else {
                    response.setResponseCode(400)
                }
            }
            "/api-test-delete" -> {
                val response = MockResponse()
                return if (request.method == "DELETE") {
                    response.setResponseCode(200)
                } else {
                    response.setResponseCode(400)
                }
            }
            "/api-test-post-auth-data" -> {
                val response = MockResponse()
                val body = request.body.readUtf8()
                return if (
                    request.method == "POST" &&
                    body.contains("\"login\":\"login\"") &&
                    body.contains("\"password\":\"password\"")
                ) {
                    response.setResponseCode(200)
                } else {
                    response.setResponseCode(400)
                }
            }
            "/api-test-post-auth-token" -> {
                val response = MockResponse()
                return if (
                    request.method == "POST" &&
                    request.headers["Authorization"] == "token@12345"
                ) {
                    response.setResponseCode(200)
                } else {
                    response.setResponseCode(400)
                }
            }
            "/api-test-get-timeout" -> {
                MockResponse()
                    .setBodyDelay(5000L, TimeUnit.SECONDS)
                    .setResponseCode(200)
            }
            else -> MockResponse().setResponseCode(404)
        }
    }

}