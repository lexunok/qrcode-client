package com.lex.qr


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.provider.Settings
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.lex.qr.components.NavButton
import com.lex.qr.ui.theme.Blue
import com.lex.qr.ui.theme.Green
import com.lightspark.composeqr.QrCodeView
import kotlinx.coroutines.launch

enum class StaffPage {
    MAIN, SUBJECT, GROUP
}
@Composable
fun MainPage(
    api: API,
    user: User,
    key: String?,
    isLoading: Boolean,
    onLogout: (User?) -> Unit,
    onLoading: (Boolean) -> Unit,
    changeKey: (String?) -> Unit
) {

    var title by remember { mutableStateOf("${user.firstName} ${user.lastName}") }
    val context = LocalContext.current
    val locationSettingsClient = LocationServices.getSettingsClient(context)
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    Box (modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 28.dp, vertical = 64.dp)
        .shadow(12.dp)
        .background(color = Color.White, shape = RoundedCornerShape(12.dp))
        .padding(20.dp)
    ) {

        Title(title, Modifier.align(Alignment.TopCenter))

        if (isLoading) {
            CircularProgressIndicator(color = Blue, modifier = Modifier.size(100.dp).align(Alignment.Center), strokeWidth = 12.dp)
        }

        NavButton(
            Modifier.align(Alignment.BottomEnd),
            R.drawable.baseline_logout_24,
            "Logout"
        ) { onLogout(null) }

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
                    NavButton(
                        Modifier.align(Alignment.BottomStart),
                        R.drawable.baseline_format_list_bulleted_24,
                        "List of Students"
                    ) {
                        getStudentsScope.launch {
                            if (students.isNotEmpty()) {
                                students = emptyList()
                            }
                            else {
                                key?.let {
                                    onLoading(true)
                                    val response = api.getStudents(key)
                                    response?.let {
                                        students = response
                                    }
                                    onLoading(false)
                                }
                            }
                        }
                    }
                    NavButton(
                        Modifier.align(Alignment.BottomCenter),
                        R.drawable.baseline_qr_code_24,
                        "QR Generator or Scan"
                    ) {
                        getSubjectsScope.launch {
                            onLoading(true)
                            val response = api.getSubjects()
                            response?.let {
                                subjects = response
                            }
                            page = StaffPage.SUBJECT
                            title = "Выберите предмет"
                            onLoading(false)
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
                                        getGroupsScope.launch {
                                            onLoading(true)
                                            val response = api.getGroups()
                                            response?.let {
                                                groups = response
                                            }
                                            page = StaffPage.GROUP
                                            title = "Выберите группу"
                                            selectedSubject = item
                                            onLoading(false)
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
                                        if (ContextCompat.checkSelfPermission(
                                                context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                                            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
                                            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

                                            locationSettingsClient.checkLocationSettings(builder.build())
                                                .addOnSuccessListener {
                                                    val locationTask: Task<Location> = fusedLocationClient.lastLocation
                                                    locationTask.addOnSuccessListener { location ->
                                                        location?.let {
                                                            selectedGroup = item
                                                            val latitude = it.latitude
                                                            val longitude = it.longitude
                                                            createClassScope.launch {
                                                                onLoading(true)
                                                                val request = CreateClassRequest(
                                                                    staffId = user.id,
                                                                    subjectId = selectedSubject!!.id,
                                                                    groupId = selectedGroup!!.id,
                                                                    geolocation = "$latitude|$longitude"
                                                                )
                                                                val response: CreateClassResponse? =
                                                                    api.createClass(request)
                                                                response?.let {
                                                                    createClassResponse = response
                                                                    changeKey(createClassResponse!!.publicId)
                                                                }
                                                                page = StaffPage.MAIN
                                                                title = "${user.firstName} ${user.lastName}"
                                                                onLoading(false)
                                                            }
                                                        }
                                                    }
                                                }
                                                .addOnFailureListener {
                                                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                                }
                                        } else {
                                            ActivityCompat.requestPermissions(context as Activity, arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION), 1)
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
            var lastLocation by remember { mutableStateOf<String?>(null) }

            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setPrompt("")
            options.setCameraId(0)
            options.setBeepEnabled(false)
            options.setBarcodeImageEnabled(true)

            lastLocation?.let {
                Title(lastLocation!!, Modifier.align(Alignment.Center))
            }

            val scanLauncher = rememberLauncherForActivityResult(ScanContract()) {
                result ->
                    if (result.contents != null && lastLocation!=null) {
                        scanScope.launch {
                            onLoading(true)
                            val response = api.joinClass(
                                JoinClassRequest(
                                    classId = result.contents,
                                    studentId = user.id,
                                    studentGeolocation = lastLocation!!
                                )
                            )
                            if (response != null) {
                                isSuccessJoining = response.isSuccess
                            }
                            onLoading(false)
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
            NavButton(
                Modifier.align(Alignment.BottomCenter),
                R.drawable.baseline_qr_code_24,
                "QR Generator or Scan"
            ) {
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
                    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

                    locationSettingsClient.checkLocationSettings(builder.build())
                        .addOnSuccessListener {
                            val locationTask: Task<Location> = fusedLocationClient.lastLocation
                            locationTask.addOnSuccessListener { location ->
                                location?.let {
                                    lastLocation = "${it.latitude}|${it.longitude}"
                                    scanLauncher.launch(options)
                                }
                            }
                        }
                        .addOnFailureListener {
                            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }
                } else {
                    ActivityCompat.requestPermissions(context as Activity, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
            }
        }
    }
}