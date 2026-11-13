package com.badass.bigopk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Black = Color(0xFF090909)
private val Red = Color(0xFFD40000)
private val DarkCard = Color(0xFF171719)
private val Muted = Color(0xFFB8B8BE)

private enum class AppPage(val label: String) {
    HOME("Home"), BOOK("Book"), MY_OPK("My OPK"), ADMIN("Admin")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BadassNativeApp() }
    }
}

@Composable
private fun BadassNativeApp() {
    var page by remember { mutableStateOf(AppPage.HOME) }
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Red,
            background = Black,
            surface = DarkCard,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Scaffold(
            containerColor = Black,
            topBar = {
                Column(
                    Modifier.fillMaxWidth().background(Black)
                        .padding(horizontal = 20.dp, vertical = 15.dp)
                ) {
                    Text("BADASS", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                    Text("BIGO PK SLOTS", color = Color(0xFFFFA5A5), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF111113), contentColor = Color.White) {
                    AppPage.values().forEach { item ->
                        val icon = when (item) {
                            AppPage.HOME -> Icons.Default.Home
                            AppPage.BOOK -> Icons.Default.EventAvailable
                            AppPage.MY_OPK -> Icons.Default.ConfirmationNumber
                            AppPage.ADMIN -> Icons.Default.AdminPanelSettings
                        }
                        NavigationBarItem(
                            selected = page == item,
                            onClick = { page = item },
                            icon = { Icon(icon, contentDescription = item.label) },
                            label = { Text(item.label, fontSize = 10.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = Red,
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (page) {
                    AppPage.HOME -> HomeScreen(onBook = { page = AppPage.BOOK })
                    AppPage.BOOK -> BookingScreen()
                    AppPage.MY_OPK -> MyOpkScreen()
                    AppPage.ADMIN -> AdminScreen()
                }
                Text(
                    "BADASS BIGO PK • NATIVE ANDROID",
                    color = Color(0xFF77777D),
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        content = { Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(11.dp), content = content) }
    )
}

@Composable
private fun HomeScreen(onBook: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionCard {
            Text("WELCOME TO", color = Color(0xFFFF7777), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("BADASS BIGO PK", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
            Text("Your PK slot booking hub", color = Muted, fontSize = 14.sp)
            Button(
                onClick = onBook,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Red),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(Icons.Default.EventAvailable, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("BOOK A SLOT", fontWeight = FontWeight.Bold)
            }
        }
        FeatureRow(Icons.Default.CalendarMonth, "PK SLOT BOOKING", "Choose a date and reserve an available time.")
        FeatureRow(Icons.Default.ConfirmationNumber, "MY OPK", "Find your bookings using your BIGO ID.")
        FeatureRow(Icons.Default.AdminPanelSettings, "ADMIN CENTER", "Secure tools for managing PK bookings.")
        SectionCard {
            Text("ONE CONNECTED BOOKING SYSTEM", color = Color.White, fontWeight = FontWeight.Bold)
            Text("The native app will use the same booking service as the BADASS website. Your website records will remain in the existing system.", color = Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun FeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color(0xFFFF5555), modifier = Modifier.size(29.dp))
            Spacer(Modifier.width(13.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(detail, color = Muted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun BookingScreen() {
    var bigoId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    SectionCard {
        Text("BOOK A PK SLOT", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text("Enter your details, then choose an available date and time.", color = Muted, fontSize = 13.sp)
        OutlinedTextField(value = bigoId, onValueChange = { bigoId = it }, label = { Text("BDS BIGO ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("BIGO Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = selectedDate, onValueChange = { selectedDate = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Text("Available time slots will load from the booking server in the next integration step.", color = Muted, fontSize = 12.sp)
        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Red)) {
            Text("CHECK AVAILABLE SLOTS")
        }
        Text("Booking submission is not enabled yet. We will connect it to the live API before allowing reservations.", color = Color(0xFFFFB6B6), fontSize = 11.sp)
    }
}

@Composable
private fun MyOpkScreen() {
    var bigoId by remember { mutableStateOf("") }
    SectionCard {
        Text("MY OPK", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text("Enter the BIGO ID used for your booking to find your PK schedules.", color = Muted, fontSize = 13.sp)
        OutlinedTextField(value = bigoId, onValueChange = { bigoId = it }, label = { Text("BIGO ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Red)) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("FIND MY BOOKINGS")
        }
        Text("Private booking lookup will be connected to the existing API.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun AdminScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    SectionCard {
        Text("ADMIN LOGIN", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text("Sign in with your authorized BADASS Admin account.", color = Muted, fontSize = 13.sp)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Admin email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Red)) {
            Text("SIGN IN")
        }
        Text("Admin authentication and management controls will be connected securely. No admin actions are active yet.", color = Color(0xFFFFB6B6), fontSize = 11.sp)
    }
}
