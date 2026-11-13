package com.badass.bigopk

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Calendar

private const val API_BASE = "https://bigo-pk-api.wandering-forest-74e9.workers.dev"
private val mainHandler = Handler(Looper.getMainLooper())
private val Black = Color(0xFF090909)
private val Red = Color(0xFFD40000)
private val DarkCard = Color(0xFF171719)
private val Muted = Color(0xFFB8B8BE)
private enum class AppPage(val label: String) { HOME("Home"), BOOK("Book"), MY_OPK("My OPK"), ADMIN("Admin") }
private data class PkSlot(val id: String, val time: String, val status: String)
private data class OpkBooking(val code: String, val date: String, val time: String, val name: String, val status: String, val opponentName: String, val opponentId: String, val hostLevel: String, val event: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { BadassNativeApp() } }
}

private fun apiRequest(path: String, method: String = "GET", body: String? = null): JSONObject {
    val c = URL(API_BASE + path).openConnection() as HttpURLConnection
    try {
        c.requestMethod = method; c.connectTimeout = 15000; c.readTimeout = 15000
        c.setRequestProperty("Accept", "application/json")
        if (body != null) {
            c.doOutput = true; c.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            c.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) }
        }
        val code = c.responseCode
        val raw = (if (code in 200..299) c.inputStream else c.errorStream)?.bufferedReader()?.use { it.readText() } ?: "{}"
        val json = JSONObject(raw)
        if (code !in 200..299 || !json.optBoolean("ok", false))
            throw IllegalStateException(json.optString("error", json.optString("message", "Request failed (HTTP $code)")))
        return json
    } finally { c.disconnect() }
}

@Composable
private fun BadassNativeApp() {
    var page by remember { mutableStateOf(AppPage.HOME) }
    MaterialTheme(colorScheme = darkColorScheme(primary = Red, background = Black, surface = DarkCard, onBackground = Color.White, onSurface = Color.White)) {
        Scaffold(containerColor = Black, topBar = {
            Column(Modifier.fillMaxWidth().background(Black).padding(horizontal = 20.dp, vertical = 15.dp)) {
                Text("BADASS", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Black, letterSpacing = 4.sp)
                Text("BIGO PK SLOTS", color = Color(0xFFFFA5A5), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
        }, bottomBar = {
            NavigationBar(containerColor = Color(0xFF111113), contentColor = Color.White) {
                AppPage.values().forEach { p ->
                    val icon = when (p) { AppPage.HOME -> Icons.Default.Home; AppPage.BOOK -> Icons.Default.EventAvailable; AppPage.MY_OPK -> Icons.Default.ConfirmationNumber; AppPage.ADMIN -> Icons.Default.AdminPanelSettings }
                    NavigationBarItem(selected = page == p, onClick = { page = p }, icon = { Icon(icon, p.label) }, label = { Text(p.label, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, selectedTextColor = Color.White, indicatorColor = Red, unselectedIconColor = Muted, unselectedTextColor = Muted))
                }
            }
        }) { pad ->
            Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                when (page) { AppPage.HOME -> HomeScreen { page = AppPage.BOOK }; AppPage.BOOK -> BookingScreen(); AppPage.MY_OPK -> MyOpkScreen(); AppPage.ADMIN -> AdminScreen() }
                Text("BADASS BIGO PK • NATIVE ANDROID", color = Color(0xFF77777D), fontSize = 10.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(11.dp), content = content)
    }
}
@Composable
private fun HomeScreen(onBook: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionCard {
            Text("WELCOME TO", color = Color(0xFFFF7777), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("BADASS BIGO PK", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
            Text("Your PK slot booking hub", color = Muted)
            Button(onClick = onBook, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("BOOK A SLOT", fontWeight = FontWeight.Bold) }
        }
        SectionCard { Text("PK SLOT BOOKING", color = Color.White, fontWeight = FontWeight.Bold); Text("Choose a date and reserve an available time.", color = Muted) }
        SectionCard { Text("MY OPK", color = Color.White, fontWeight = FontWeight.Bold); Text("Find your bookings using your BIGO ID.", color = Muted) }
        SectionCard { Text("ADMIN CENTER", color = Color.White, fontWeight = FontWeight.Bold); Text("Secure tools for managing PK bookings.", color = Muted) }
    }
}

@Composable
private fun BookingScreen() {
    var bigoId by remember { mutableStateOf("") }
    var playerName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var months by remember { mutableStateOf(emptyList<String>()) }
    var slots by remember { mutableStateOf(emptyList<PkSlot>()) }
    var chosen by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Connecting to booking server…") }
    var busy by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }

    fun loadMonths() {
        busy = true; status = "Loading open booking months…"
        Thread {
            try {
                val a = apiRequest("/api/months/open").optJSONArray("months")
                val list = mutableListOf<String>()
                if (a != null) for (i in 0 until a.length()) list.add(a.optString(i))
                val valid = list.filter { it.matches(Regex("\\d{4}-(0[1-9]|1[0-2])")) }.sorted()
                mainHandler.post {
                    months = valid
                    val cal = Calendar.getInstance()
                    val current = "%04d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                    if (date.isBlank() && valid.isNotEmpty()) {
                        val m = if (valid.contains(current)) current else valid.first()
                        date = m + "-" + (if (m == current) cal.get(Calendar.DAY_OF_MONTH) else 1).toString().padStart(2, '0')
                    }
                    status = if (valid.isEmpty()) "Bookings are currently closed." else "Open months loaded. Choose date and load slots."
                    busy = false
                }
            } catch (e: Exception) { mainHandler.post { status = e.message ?: "Could not load months."; busy = false } }
        }.start()
    }
    fun loadSlots() {
        if (date.length != 10 || !months.contains(date.take(7))) { status = "Choose a date inside an open booking month."; return }
        busy = true; chosen = ""; slots = emptyList(); status = "Loading slots…"
        Thread {
            try {
                val a = apiRequest("/api/slots?date=$date").optJSONArray("slots")
                val list = mutableListOf<PkSlot>()
                if (a != null) for (i in 0 until a.length()) {
                    val x = a.getJSONObject(i)
                    list.add(PkSlot(x.optString("id"), x.optString("time"), x.optString("status", "available")))
                }
                mainHandler.post { slots = list.sortedBy { it.time }; status = if (list.isEmpty()) "No slots found for this date." else "Tap an AVAILABLE time."; busy = false }
            } catch (e: Exception) { mainHandler.post { status = e.message ?: "Could not load slots."; busy = false } }
        }.start()
    }
    fun submit() {
        val id = bigoId.trim()
        if (!id.startsWith("BDS") || !id.drop(3).matches(Regex("[A-Za-z0-9._-]{3,40}"))) { status = "Invalid ID. It must start with BDS and contain 3–40 more characters."; return }
        if (playerName.isBlank()) { status = "Please enter your BIGO name."; return }
        if (chosen.isBlank()) { status = "Select an available time first."; return }
        busy = true; done = false; status = "Submitting booking…"
        Thread {
            try {
                val body = JSONObject().put("date", date).put("time", chosen).put("bigoId", id).put("name", playerName.trim()).toString()
                val result = apiRequest("/api/bookings", "POST", body)
                val booking = result.optJSONObject("booking")
                val code = booking?.optString("bookingCode", "").orEmpty()
                mainHandler.post {
                    done = true
                    status = "Booking request submitted! Waiting for Admin approval." + if (code.isNotBlank()) " Booking code: $code" else ""
                    chosen = ""; busy = false; loadSlots()
                }
            } catch (e: Exception) {
                mainHandler.post {
                    status = when (e.message) {
                        "MONTHLY_LIMIT_REACHED" -> "Monthly booking limit reached for this BIGO ID."
                        "SLOT_TAKEN" -> "That slot was just booked. Please choose another."
                        else -> e.message ?: "Booking failed."
                    }
                    busy = false
                }
            }
        }.start()
    }
    LaunchedEffect(Unit) { loadMonths() }

    SectionCard {
        Text("BOOK A PK SLOT", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
        Text("Live connection to the same booking service used by the website.", color = Muted, fontSize = 13.sp)
        OutlinedTextField(bigoId, { bigoId = it }, label = { Text("BDS BIGO ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(playerName, { playerName = it }, label = { Text("BIGO Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(date, { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Text(if (months.isEmpty()) "No open months loaded" else "Open months: " + months.joinToString(), color = Muted, fontSize = 11.sp)
        Button(onClick = { loadMonths() }, enabled = !busy, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("REFRESH OPEN MONTHS") }
        Button(onClick = { loadSlots() }, enabled = !busy && months.isNotEmpty(), modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("LOAD AVAILABLE SLOTS") }
        if (slots.isNotEmpty()) {
            Text("AVAILABLE TIMES", color = Color.White, fontWeight = FontWeight.Bold)
            slots.forEach { slot ->
                val available = slot.status == "available"
                Surface(Modifier.fillMaxWidth().clickable(enabled = available) { chosen = slot.time; status = "Selected $date • " + slot.time },
                    color = if (chosen == slot.time) Red else Color(0xFF29292D), shape = RoundedCornerShape(8.dp)) {
                    Row(Modifier.padding(13.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(slot.time, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(if (available) "AVAILABLE" else if (slot.status == "full") "BOOKED" else "CLOSED",
                            color = if (available) Color(0xFF8BE0A1) else Muted, fontSize = 11.sp)
                    }
                }
            }
        }
        if (chosen.isNotBlank()) Text("Selected time: $chosen", color = Color(0xFFFFAAAA), fontWeight = FontWeight.Bold)
        Button(onClick = { submit() }, enabled = !busy && chosen.isNotBlank(), modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("SUBMIT BOOKING REQUEST") }
        Text(status, color = if (done) Color(0xFF8BE0A1) else Color(0xFFFFB6B6), fontSize = 12.sp)
    }
}

@Composable
private fun MyOpkScreen() {
    var id by remember { mutableStateOf("") }
    var bookings by remember { mutableStateOf(emptyList<OpkBooking>()) }
    var statusText by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }

    fun searchBookings() {
        val lookupId = id.trim()
        if (!lookupId.matches(Regex("[A-Za-z0-9._-]{3,40}"))) {
            statusText = "Enter a valid BIGO ID (3–40 letters or numbers)."
            bookings = emptyList()
            searched = false
            return
        }
        busy = true
        searched = false
        bookings = emptyList()
        statusText = "Searching your bookings…"
        Thread {
            try {
                val response = apiRequest("/api/lookup?bigoId=" + java.net.URLEncoder.encode(lookupId, "UTF-8"))
                val array = response.optJSONArray("bookings")
                val results = mutableListOf<OpkBooking>()
                if (array != null) for (i in 0 until array.length()) {
                    val b = array.getJSONObject(i)
                    results.add(OpkBooking(
                        code = b.optString("bookingCode", b.optString("booking_code", "")),
                        date = b.optString("date", ""),
                        time = b.optString("time", ""),
                        name = b.optString("name", b.optString("bigo_name", "")),
                        status = b.optString("status", "Pending approval"),
                        opponentName = b.optString("opponentName", b.optString("opponent_name", "")),
                        opponentId = b.optString("opponentBigoId", b.optString("opponent_bigo_id", "")),
                        hostLevel = b.optString("hostLevel", b.optString("host_level", "")),
                        event = b.optString("opkEvent", b.optString("opk_event", ""))
                    ))
                }
                mainHandler.post {
                    bookings = results
                    searched = true
                    statusText = if (results.isEmpty()) "No bookings found for this BIGO ID." else "Found " + results.size + " booking(s)."
                    busy = false
                }
            } catch (e: Exception) {
                mainHandler.post { statusText = e.message ?: "Lookup failed. Please try again."; busy = false }
            }
        }.start()
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionCard {
            Text("MY OPK", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("Find your bookings using the BIGO ID entered when booking.", color = Muted, fontSize = 13.sp)
            OutlinedTextField(id, { id = it }, label = { Text("BIGO ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Button(onClick = { searchBookings() }, enabled = !busy, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Red)) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (busy) "SEARCHING…" else "FIND MY BOOKINGS")
            }
            if (statusText.isNotBlank()) Text(statusText,
                color = if (searched && bookings.isNotEmpty()) Color(0xFF8BE0A1) else Color(0xFFFFB6B6), fontSize = 12.sp)
        }
        bookings.forEach { booking ->
            SectionCard {
                Text(booking.date + " • " + booking.time, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Status: " + booking.status, color = if (booking.status.contains("approved", true)) Color(0xFF8BE0A1) else Color(0xFFFFC36B), fontWeight = FontWeight.Bold)
                if (booking.name.isNotBlank()) Text("BIGO Name: " + booking.name, color = Muted, fontSize = 13.sp)
                if (booking.code.isNotBlank()) Text("Booking Code: " + booking.code, color = Muted, fontSize = 12.sp)
                if (booking.opponentName.isNotBlank()) Text("Opponent: " + booking.opponentName, color = Color.White, fontSize = 13.sp)
                if (booking.opponentId.isNotBlank()) Text("Opponent BIGO ID: " + booking.opponentId, color = Muted, fontSize = 12.sp)
                if (booking.hostLevel.isNotBlank()) Text("Host Level: " + booking.hostLevel, color = Muted, fontSize = 12.sp)
                if (booking.event.isNotBlank()) Text("OPK Event: " + booking.event, color = Muted, fontSize = 12.sp)
            }
        }
    }
}
private const val FIREBASE_WEB_API_KEY = "AIzaSyC4wcnp9FmyL8Api2kvcLcV68gCpnn09xE"
private data class AdminBooking(val id: String, val date: String, val time: String, val name: String, val bigoId: String, val status: String, val code: String, val opponentId: String)

private fun adminHttp(path: String, method: String = "GET", token: String? = null, body: String? = null, absolute: Boolean = false): JSONObject {
    val connection = (if (absolute) URL(path) else URL(API_BASE + path)).openConnection() as HttpURLConnection
    try {
        connection.requestMethod = method
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.setRequestProperty("Accept", "application/json")
        if (token != null) connection.setRequestProperty("Authorization", "Bearer $token")
        if (body != null) { connection.doOutput = true; connection.setRequestProperty("Content-Type", "application/json; charset=utf-8"); connection.outputStream.use { it.write(body.toByteArray(StandardCharsets.UTF_8)) } }
        val code = connection.responseCode
        val raw = (if (code in 200..299) connection.inputStream else connection.errorStream)?.bufferedReader()?.use { it.readText() } ?: "{}"
        val json = JSONObject(raw)
        if (code !in 200..299) throw IllegalStateException(json.optString("error", json.optString("message", "Request failed (HTTP $code)")))
        if (!absolute && !json.optBoolean("ok", false)) throw IllegalStateException(json.optString("error", "Request failed"))
        return json
    } finally { connection.disconnect() }
}

@Composable
private fun AdminScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var rows by remember { mutableStateOf(emptyList<AdminBooking>()) }
    var message by remember { mutableStateOf("Sign in using your approved BADASS Admin account.") }
    var busy by remember { mutableStateOf(false) }

    fun loadBookings(idToken: String) {
        busy = true; message = "Loading bookings from Cloudflare D1…"
        Thread {
            try {
                val response = adminHttp("/api/admin/bookings", token = idToken)
                val array = response.optJSONArray("bookings")
                val result = mutableListOf<AdminBooking>()
                if (array != null) for (i in 0 until array.length()) {
                    val b = array.getJSONObject(i)
                    result.add(AdminBooking(b.optString("id"), b.optString("date"), b.optString("time"), b.optString("bigo_name", b.optString("name")), b.optString("bigo_id", b.optString("bigoId")), b.optString("status", "Pending approval"), b.optString("booking_code", b.optString("bookingCode")), b.optString("opponent_bigo_id", b.optString("opponentBigoId"))))
                }
                mainHandler.post { rows = result; message = "Loaded " + result.size + " booking(s) from the existing database."; busy = false }
            } catch (e: Exception) { mainHandler.post { message = e.message ?: "Could not load bookings."; busy = false } }
        }.start()
    }

    fun signIn() {
        if (email.isBlank() || password.isBlank()) { message = "Enter your Admin email and password."; return }
        busy = true; message = "Signing in securely…"
        Thread {
            try {
                val body = JSONObject().put("email", email.trim()).put("password", password).put("returnSecureToken", true).toString()
                val auth = adminHttp("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + FIREBASE_WEB_API_KEY, "POST", body = body, absolute = true)
                val idToken = auth.optString("idToken")
                if (idToken.isBlank()) throw IllegalStateException("Firebase did not return a sign-in token.")
                val verifiedRole = try { adminHttp("/api/superadmin/admins", token = idToken); "superadmin" } catch (_: Exception) { try { adminHttp("/api/admin/bookings", token = idToken); "admin" } catch (_: Exception) { "" } }
                if (verifiedRole.isBlank()) throw IllegalStateException("This Firebase account is not enabled in BADASS Admin Management.")
                mainHandler.post { token = idToken; role = verifiedRole; password = ""; message = "Secure " + verifiedRole.uppercase() + " login successful."; if (verifiedRole == "admin") loadBookings(idToken) else { message = "Superadmin authenticated. Account management will be added next."; busy = false } }
            } catch (e: Exception) { mainHandler.post { message = e.message ?: "Admin login failed."; busy = false } }
        }.start()
    }

    fun changeStatus(booking: AdminBooking, newStatus: String) {
        if (booking.id.isBlank()) { message = "This booking has no valid record ID."; return }
        busy = true; message = "Updating booking status…"
        Thread {
            try {
                val encodedId = java.net.URLEncoder.encode(booking.id, "UTF-8")
                adminHttp("/api/admin/bookings/" + encodedId + "/status", "PATCH", token, JSONObject().put("status", newStatus).toString())
                mainHandler.post { message = "Booking " + newStatus.lowercase() + " successfully."; loadBookings(token) }
            } catch (e: Exception) { mainHandler.post { message = e.message ?: "Could not update booking."; busy = false } }
        }.start()
    }

    if (token.isBlank()) {
        SectionCard {
            Text("ADMIN CENTER", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("Use the same approved Firebase Admin account as badassopkx.com.", color = Muted, fontSize = 13.sp)
            OutlinedTextField(email, { email = it }, label = { Text("Admin email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(password, { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation())
            Button(onClick = { signIn() }, enabled = !busy, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text(if (busy) "PLEASE WAIT…" else "SIGN IN AS ADMIN") }
            Text(message, color = Color(0xFFFFB6B6), fontSize = 12.sp)
        }
    } else {
        SectionCard {
            Text("ADMIN DASHBOARD", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("Authenticated as " + role.uppercase(), color = Color(0xFF8BE0A1), fontWeight = FontWeight.Bold)
            if (role == "admin") Text(rows.size.toString() + " booking(s) loaded from Cloudflare D1.", color = Muted)
            Button(onClick = { if (role == "admin") loadBookings(token) }, enabled = !busy && role == "admin", modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("REFRESH BOOKINGS") }
            OutlinedButton(onClick = { token = ""; role = ""; rows = emptyList(); message = "Signed out." }, modifier = Modifier.fillMaxWidth()) { Text("SIGN OUT") }
            Text(message, color = Muted, fontSize = 12.sp)
        }
        if (role == "admin") rows.forEach { booking ->
            SectionCard {
                Text(booking.date + " • " + booking.time, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(booking.name.ifBlank { "BIGO Player" }, color = Color.White, fontWeight = FontWeight.Bold)
                Text("BIGO ID: " + booking.bigoId, color = Muted, fontSize = 12.sp)
                if (booking.code.isNotBlank()) Text("Code: " + booking.code, color = Muted, fontSize = 11.sp)
                Text("Status: " + booking.status, color = Color(0xFFFFC36B), fontWeight = FontWeight.Bold)
                if (booking.opponentId.isNotBlank()) Text("Opponent ID: " + booking.opponentId, color = Muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { changeStatus(booking, "Approved") }, enabled = !busy && booking.status.lowercase() !in listOf("approved", "opponent assigned", "cancelled", "rejected"), modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF237A42))) { Text("APPROVE") }
                    Button(onClick = { changeStatus(booking, "Cancelled") }, enabled = !busy && booking.status.lowercase() !in listOf("cancelled", "rejected"), modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Red)) { Text("CANCEL") }
                }
            }
        }
    }
}
