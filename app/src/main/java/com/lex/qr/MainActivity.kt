package com.lex.qr

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.Green
import com.lex.qr.ui.theme.QRTheme
import com.lightspark.composeqr.QrCodeView
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    private lateinit var client: HttpClient

    private suspend fun generateQRCode(onGenerate: (Boolean) -> Unit, onReceiveData: (CreateClassResponse) -> Unit) {
        val request = CreateClassRequest(
            staffId = "12059a92-65d1-481d-b9e8-5ea397ae2725",
            subjectId = "14ef8f6c-16ca-44f1-a006-ff4e5fc32b03",
            groupId = "60f4accc-abac-446d-b4bc-cd7538334079",
            geolocation = "1234"
            )
        val response:CreateClassResponse? = client.post("https://qrcode-wva2.shuttle.app/api/class/create") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(request)
        }.body()
        if (response != null) {
            onGenerate(true)
            onReceiveData(response)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        client = HttpClient(Android) {
            install(ContentNegotiation) {
                json()
            }
        }

        val lightTransparentStyle = SystemBarStyle.light(
            scrim = TRANSPARENT,
            darkScrim = TRANSPARENT
        )
        enableEdgeToEdge(
            statusBarStyle = lightTransparentStyle,
            navigationBarStyle = lightTransparentStyle
        )
        setContent {
            QRTheme {
                Box (modifier = Modifier
                    .fillMaxSize()
                    .background(color = Blue)) {
                    MainPage()
                }
            }
        }
    }

    @Composable
    fun MainPage() {
        var role by remember { mutableStateOf(Role.STAFF) }
        var key by remember { mutableStateOf("") }
        var isSuccessJoining by remember { mutableStateOf(false) }
        var students by remember { mutableStateOf<List<Student>>(emptyList()) }
        val scanScope  = rememberCoroutineScope()
        val scanLauncher = rememberLauncherForActivityResult(ScanContract()) {
                result ->
            if (result.contents != null) {
                val request = JoinClassRequest(
                    classId = key,
                    studentId = "7a78fe40-a1d6-4073-8a69-4edd88083bf4",
                    studentGeolocation = "1234"
                )
                scanScope.launch {
                    val response:JoinClassResponse? = client.post("https://qrcode-wva2.shuttle.app/api/class/join") {
                        headers {
                            append(HttpHeaders.ContentType, "application/json")
                        }
                        setBody(request)
                    }.body()
                    if (response != null) {
                        isSuccessJoining = response.isSuccess
                    }
                }
            }
        }
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)

        val user = when (role) {
            Role.STAFF -> "Преподаватель"
            Role.STUDENT -> "Студент"
        }

        Box (modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 64.dp)
            .shadow(12.dp)
            .background(color = Color.White, shape = RoundedCornerShape(12.dp))
            .padding(20.dp)
        ) {

            var isCodeActive by remember { mutableStateOf(false) }
            val onGenerate = { value: Boolean -> isCodeActive = value }
            var createClassResponse by remember { mutableStateOf<CreateClassResponse?>(null) }
            val onCreateClass = { value: CreateClassResponse -> createClassResponse = value }
            val createClassScope = rememberCoroutineScope()
            val getStudentsScope = rememberCoroutineScope()

            val onListClick = {
                if (key != "") {
                    isCodeActive = false
                    //Запрос на список студентов
                    getStudentsScope.launch {
                        val response: List<Student>? =
                            client.get("https://qrcode-wva2.shuttle.app/api/class/students/$key")
                            {
                                headers {
                                    append(HttpHeaders.ContentType, "application/json")
                                }
                            }.body()
                        if (response != null) {
                            students = response
                        }
                    }
                    Unit
                }
            }
            var onQRClick = { scanLauncher.launch(options) }

            if (role == Role.STAFF) {
                onQRClick = {
                        if (createClassResponse!=null) {
                            isCodeActive = true
                        }
                        students = emptyList()
                        createClassScope.launch { generateQRCode(onGenerate, onCreateClass)
                    }
                }
            }

            //ДЛЯ ТЕСТА СМЕНА РОЛЕЙ
            val changeRole = {
                if (role == Role.STAFF) {
                    role = Role.STUDENT
                    isCodeActive = false
                } else {
                    role = Role.STAFF
                }
            }
            /////
            Text(
                text = user,
                color = Blue,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.TopCenter),
                fontSize = 32.sp
            )
            if (isCodeActive && createClassResponse != null) {
                val id = createClassResponse?.publicId
                id?.let {
                    key = id
                    QrCodeView(
                        data = it,
                        modifier = Modifier
                            .size(300.dp)
                            .align(Alignment.Center)
                    )
                }
            }
            if (role == Role.STUDENT && isSuccessJoining) {
                Icon(
                    modifier = Modifier.align(Alignment.Center),
                    imageVector = ImageVector.vectorResource(
                        id = R.drawable.baseline_check_circle_outline_24
                    ),
                    contentDescription = "Scan is success",
                    tint = Green
                )
            }
            if (students.isNotEmpty() && role == Role.STAFF) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(students) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .border(width = 4.dp, color = Blue, shape = RoundedCornerShape(8.dp))
                            ,
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            ),
                        ) {
                            var color = Color.Red
                            if (item.isActive) {
                                color = Green
                            }
                            Text(
                                color = color,
                                text = "${item.firstName} ${item.lastName}",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                            }
                        }
                    }
                }
            if (role == Role.STAFF) {
                IconButton(
                    onClick = onListClick, modifier = Modifier
                        .align(Alignment.BottomStart)
                        .border(width = 2.dp, color = Blue, shape = RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(
                            id = R.drawable.baseline_format_list_bulleted_24
                        ),
                        contentDescription = "List of Students",
                        tint = Blue
                    )
                }
            }
            IconButton(
                onClick = onQRClick, modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .border(width = 2.dp, color = Blue, shape = RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        id = R.drawable.baseline_qr_code_24
                    ),
                    contentDescription = "QR Generator",
                    tint = Blue
                )
            }
            //ДЛЯ ТЕСТА СМЕНА РОЛЕЙ
            IconButton(
                onClick = changeRole, modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .border(width = 2.dp, color = Blue, shape = RoundedCornerShape(40.dp))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        id = R.drawable.baseline_replay_24
                    ),
                    contentDescription = "Reverse Role",
                    tint = Blue
                )
            }
            }
        }
}
enum class Role {
    STAFF, STUDENT
}

@Serializable
data class CreateClassRequest(
    @SerialName("staff_id") val staffId: String,
    @SerialName("subject_id")val subjectId: String,
    @SerialName("group_id") val groupId: String,
    val geolocation: String
)

@Serializable
data class CreateClassResponse(
    @SerialName("public_id") val publicId: String,
    @SerialName("subject_id") val subjectId: String,
    @SerialName("group_id") val groupId: String
)

@Serializable
data class JoinClassRequest(
    @SerialName("class_id") val classId: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("student_geolocation") val studentGeolocation: String
)

@Serializable
data class JoinClassResponse(
    @SerialName("is_success") val isSuccess: Boolean
)

@Serializable
data class Student(
    @SerialName("student_id") val studentId: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("public_id") val publicId: String
)

//ГЕОЛОКАЦИЮ ОТПРАВЛЯТЬ СО СТУДЕНТСКОГО АККА
//СДЕЛАТЬ АКТИВНОСТЬ QR CODE НА 20 МИНУТ
//УБРАТЬ ХАРДКОД
//ДЕКОМПОЗИРОВАТЬ
//ЧЕЛОВЕК МОЖЕТ ТОЛЬКО ОДИН РАЗ ОТСКАНИРОВАТЬ КОД СО СВОЕГО АККА И УСТРОЙСТВА (НУЖНО ОТПРАВЛЯТЬ УСТРОЙСТВО НА СЕРВЕР)

