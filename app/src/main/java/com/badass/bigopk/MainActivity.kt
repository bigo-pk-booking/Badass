package com.badass.bigopk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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

private val Ink = Color(0xFF101014)
private val Red = Color(0xFFD40000)
private val Paper = Color(0xFFF6F6F7)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { BadassApp() }
    }
}

@Composable
fun BadassApp() {
    var page by remember { mutableStateOf("Home") }
    MaterialTheme(colorScheme = lightColorScheme(primary = Red, background = Paper, surface = Color.White)) {
        Scaffold(
            containerColor = Paper,
            topBar = {
                Column(Modifier.fillMaxWidth().background(Ink).padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text("BADASS", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                    Text("BIGO PK SLOTS", color = Color(0xFFFFB7B7), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    listOf("Home", "Book", "My OPK", "Admin").forEach { item ->
                        val icon = when(item) { "Home" -> Icons.Default.Home; "Book" -> Icons.Default.EventAvailable; "My OPK" -> Icons.Default.ConfirmationNumber; else -> Icons.Default.AdminPanelSettings }
                        NavigationBarItem(selected = page == item, onClick = { page = item }, icon = { Icon(icon, contentDescription = item) }, label = { Text(item) })
                    }
                }
            }
        ) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item { when(page) {
                    "Home" -> HomeScreen(onBook = { page = "Book" })
                    "Book" -> PlaceholderScreen("Book a PK Slot", "Choose your date and available time slot. Live booking will be connected after backend verification.")
                    "My OPK" -> PlaceholderScreen("My OPK", "Your private booking lookup will appear here once the existing booking API is connected.")
                    else -> PlaceholderScreen("Admin", "Admin sign-in and booking controls will be connected to the existing secure authentication system.")
                } }
                item { Text("BADASS BIGO PK • Native Android", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) }
            }
        }
    }
}

@Composable
private fun HomeScreen(onBook: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("WELCOME TO", color = Red, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text("BADASS BIGO PK", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.Black)
                Text("Your PK slot booking hub", color = Color.DarkGray, fontSize = 14.sp)
                Button(onClick = onBook, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Red), shape = RoundedCornerShape(5.dp)) {
                    Icon(Icons.Default.EventAvailable, contentDescription = null)
                    Spacer(Modifier.width(8.dp)); Text("BOOK A SLOT", fontWeight = FontWeight.Bold)
                }
            }
        }
        FeatureCard(Icons.Default.CalendarMonth, "PK Slot Booking", "Browse dates and reserve an available slot.")
        FeatureCard(Icons.Default.ConfirmationNumber, "My OPK", "Check your booking using your private reference code.")
        FeatureCard(Icons.Default.Groups, "BIGO PK Community", "A simple place to coordinate your PK matches.")
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F1)), shape = RoundedCornerShape(6.dp)) {
            Text("NOTE: This is the first app foundation. Live bookings and admin actions are not active until the existing backend integration is verified.", Modifier.padding(14.dp), color = Color(0xFF8B2020), fontSize = 12.sp)
        }
    }
}

@Composable
private fun FeatureCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String) {
    Card(shape = RoundedCornerShape(7.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Red, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(14.dp))
            Column { Text(title, fontWeight = FontWeight.Bold, color = Ink); Text(detail, color = Color.Gray, fontSize = 12.sp) }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, detail: String) {
    Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, fontSize = 23.sp, fontWeight = FontWeight.Black, color = Ink)
        Text(detail, color = Color.DarkGray, fontSize = 14.sp)
        AssistChip(onClick = {}, label = { Text("Coming in next build") }, leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) })
    }
}
