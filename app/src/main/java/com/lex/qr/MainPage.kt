package com.lex.qr

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.Green
import com.lightspark.composeqr.QrCodeView
import kotlinx.coroutines.launch

enum class StaffPage {
    MAIN, SUBJECT, GROUP
}
@Composable
fun MainPage(api: API, user: User, key:String?, onLogout: (User?) -> Unit, changeKey: (String?) -> Unit) {

    var title by remember { mutableStateOf("${user.firstName} ${user.lastName}") }

    Box (modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 28.dp, vertical = 64.dp)
        .shadow(12.dp)
        .background(color = Color.White, shape = RoundedCornerShape(12.dp))
        .padding(20.dp)
    ) {

        Title(title, Modifier.align(Alignment.TopCenter))

        IconButton(
            onClick = { onLogout(null) }, modifier = Modifier.align(Alignment.BottomEnd)
                .border(width = 2.dp, color = Blue, shape = RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(
                    id = R.drawable.baseline_logout_24
                ),
                contentDescription = "Logout",
                tint = Blue
            )
        }

        if (user.role == Role.STAFF) {
            val createClassScope = rememberCoroutineScope()
            val getSubjectsScope = rememberCoroutineScope()
            val getGroupsScope = rememberCoroutineScope()

            var createClassResponse by remember { mutableStateOf<CreateClassResponse?>(null) }
            var page by remember { mutableStateOf(StaffPage.MAIN) }
            var groups by remember { mutableStateOf<List<Group>>(emptyList()) }
            var subjects by remember { mutableStateOf<List<Subject>>(emptyList()) }

            var selectedSubject by remember { mutableStateOf<Subject?>(null) }
            var selectedGroup by remember { mutableStateOf<Group?>(null) }

            when(page) {
                StaffPage.MAIN -> {

                    val getStudentsScope = rememberCoroutineScope()
                    var students by remember { mutableStateOf<List<Student>>(emptyList()) }

                    if (key!=null && students.isEmpty()) {
                        QrCodeView(
                            data = key,
                            modifier = Modifier
                                .size(300.dp)
                                .align(Alignment.Center)
                        )
                    }

                    if (students.isNotEmpty()) {
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
                    IconButton(
                        onClick = {
                            getStudentsScope.launch {
                                if (students.isNotEmpty()) {
                                    students = emptyList()
                                }
                                else {
                                    key?.let {
                                        val response = api.getStudents(key)
                                        response?.let {
                                            students = response
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
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
                    QRCodeButton(
                        Modifier.align(Alignment.BottomCenter)
                    ) {
                        page = StaffPage.SUBJECT
                        title = "Выберите предмет"
                        getSubjectsScope.launch {
                            val response = api.getSubjects()
                            response?.let {
                                subjects = response
                            }
                        }
                    }
                }
                StaffPage.SUBJECT -> {
                    if (subjects.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(subjects) { item ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.clickable {
                                        page = StaffPage.GROUP
                                        title = "Выберите группу"
                                        selectedSubject = item
                                        getGroupsScope.launch {
                                            val response = api.getGroups()
                                            response?.let {
                                                groups = response
                                            }
                                        }
                                    }
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .border(width = 4.dp, color = Blue, shape = RoundedCornerShape(8.dp)),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 4.dp
                                    ),
                                ) {
                                    Text(
                                        color = Blue,
                                        text = item.name,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                StaffPage.GROUP -> {
                    if (groups.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(groups) { item ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    modifier = Modifier.clickable {
                                        page = StaffPage.MAIN
                                        title = "${user.firstName} ${user.lastName}"
                                        selectedGroup = item
                                        createClassScope.launch {
                                            if (selectedSubject!=null && selectedGroup!=null) {
                                                val request = CreateClassRequest(
                                                    staffId = user.id,
                                                    subjectId = selectedSubject!!.id,
                                                    groupId = selectedGroup!!.id,
                                                    geolocation = "1234"
                                                )
                                                val response:CreateClassResponse? = api.createClass(request)
                                                response?.let {
                                                    createClassResponse = response
                                                    changeKey(createClassResponse!!.publicId)
                                                }
                                            }
                                        }
                                    }
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .border(width = 4.dp, color = Blue, shape = RoundedCornerShape(8.dp)),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 4.dp
                                    ),
                                ) {
                                    Text(
                                        color = Blue,
                                        text = item.name,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            val scanScope  = rememberCoroutineScope()

            var isSuccessJoining by remember { mutableStateOf(false) }

            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setPrompt("")
            options.setCameraId(0)
            options.setBeepEnabled(false)
            options.setBarcodeImageEnabled(true)

            val scanLauncher = rememberLauncherForActivityResult(ScanContract()) {
                result ->
                    if (result.contents != null && key !=null) {
                        scanScope.launch {
                            val response = api.joinClass(
                                JoinClassRequest(
                                    classId = key!!,
                                    studentId = user.id,
                                    studentGeolocation = "1234"
                                )
                            )
                            if (response != null) {
                                isSuccessJoining = response.isSuccess
                            }
                        }
                    }
            }
            if (isSuccessJoining) {
                Icon(
                    modifier = Modifier.align(Alignment.Center),
                    imageVector = ImageVector.vectorResource(
                        id = R.drawable.baseline_check_circle_outline_24
                    ),
                    contentDescription = "Scan is success",
                    tint = Green
                )
            }

            QRCodeButton(
                Modifier.align(Alignment.BottomCenter)
            ) { scanLauncher.launch(options) }

        }
    }
}
@Composable
fun QRCodeButton(modifier: Modifier, onQRClick: () -> Unit){
    IconButton(
        onClick = onQRClick, modifier = modifier
            .border(width = 2.dp, color = Blue, shape = RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(
                id = R.drawable.baseline_qr_code_24
            ),
            contentDescription = "QR Generator or Scan",
            tint = Blue
        )
    }
}