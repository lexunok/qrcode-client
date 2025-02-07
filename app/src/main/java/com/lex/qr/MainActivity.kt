package com.lex.qr

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.registerForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.lex.qr.ui.theme.QRTheme
import com.lightspark.composeqr.QrCodeView
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val scanLauncher = registerForActivityResult(ScanContract()) {
        result ->
            if (result.contents != null) {

            }
    }
    private fun scanQRCode() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("");
        options.setCameraId(0);
        options.setBeepEnabled(false);
        options.setBarcodeImageEnabled(true);
        scanLauncher.launch(options)
    }
    private fun generateQRCode(onGenerate: (Boolean) -> Unit, onReceiveData: (Data) -> Unit) {
        //Запрос на сервер для получения UUID чтобы сформировать код
        val uuid = UUID.randomUUID()
        val data = Data(uuid.toString())

        onGenerate(true)
        onReceiveData(data)

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    MainPage(Role.STUDENT)
                }
            }
        }
    }
    enum class Role {
        STAFF, STUDENT
    }
    data class Data(val uuid: String)

    @Composable
    fun MainPage(role: Role) {

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
            val onGenerate = { value : Boolean -> isCodeActive = value }
            var data by remember { mutableStateOf<Data?>(null) }
            val onReceiveData = { value : Data -> data = value }

            var onButtonClick = {scanQRCode()}
            if (role == Role.STAFF) {
                onButtonClick = {generateQRCode(onGenerate, onReceiveData)}
            }

            Text(
                text = user,
                color = Blue,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.TopCenter),
                fontSize = 32.sp
            )
            if (isCodeActive && data != null) {
                val uuid = data?.uuid
                QrCodeView(
                    data = "https://localhost:3000/${uuid}" ,
                    modifier = Modifier
                        .size(300.dp)
                        .align(Alignment.Center)
                )
            }
            IconButton(onClick = onButtonClick, modifier = Modifier
                .align(Alignment.BottomCenter)
                .border(width = 2.dp, color = Blue, shape = RoundedCornerShape(12.dp))
                .padding(8.dp)) {
                Icon(
                    imageVector = ImageVector.vectorResource(
                        id = R.drawable.baseline_qr_code_24),
                    contentDescription = "QR Generator",
                    tint = Blue
                )
            }
        }

    }
    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        QRTheme {
            Box (modifier = Modifier
                .fillMaxSize()
                .background(color = Blue)) {
                MainPage(Role.STUDENT)
            }
        }
    }
}

//ГЕОЛОКАЦИЮ ОТПРАВЛЯТЬ СО СТУДЕНТСКОГО АККА
//СДЕЛАТЬ АКТИВНОСТЬ QR CODE НА 20 МИНУТ
//ЧЕЛОВЕК МОЖЕТ ТОЛЬКО ОДИН РАЗ ОТСКАНИРОВАТЬ КОД СО СВОЕГО АККА И УСТРОЙСТВА (НУЖНО ОТПРАВЛЯТЬ УСТРОЙСТВО НА СЕРВЕР)


