package com.chowdhurymills.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chowdhurymills.android.data.MillApi
import com.chowdhurymills.android.data.MillCatalog
import com.chowdhurymills.android.data.SessionStore
import com.chowdhurymills.android.data.Staff
import com.chowdhurymills.android.ui.components.DeskCardView
import com.chowdhurymills.android.ui.components.MillScaffold
import com.chowdhurymills.android.ui.components.MillTextField
import com.chowdhurymills.android.ui.components.NoteBanner
import com.chowdhurymills.android.ui.components.PrimaryButton
import com.chowdhurymills.android.ui.navigation.navItemsFor
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    staff: Staff,
    @Suppress("UNUSED_PARAMETER") api: MillApi,
    onOpen: (String) -> Unit,
    onSignOutAsync: suspend () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val eyebrow = when {
        staff.staffType == "Admin" -> "Admin desk"
        staff.permissions.all { it == "own_ledger" } || staff.staffType == "SR" && staff.permissions.isEmpty() -> "SR desk"
        else -> "${staff.staffType} desk"
    }
    val title = if (eyebrow == "SR desk") "Your account book" else "Today’s mill desk"
    val lede = when (eyebrow) {
        "Admin desk" -> "Post the chart, check a date, then open a ledger, the balances, or a new account."
        "SR desk" -> "Open the ledger tied to your staff account. Receipts sit on the left and payments on the right."
        else -> "The pages this role can open."
    }
    val cards = MillCatalog.cardsFor(staff)

    MillScaffold(
        eyebrow = eyebrow,
        title = title,
        lede = lede,
        navLabels = navItemsFor(staff, onOpen),
        onSignOut = {
            scope.launch { onSignOutAsync() }
        },
    ) {
        if (cards.isEmpty()) {
            NoteBanner("This role has no pages yet. An admin can open some from Staff.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                cards.forEach { card ->
                    DeskCardView(card) { onOpen(card.route) }
                }
            }
        }
    }
}

@Composable
fun ServerSettingsScreen(
    store: SessionStore,
    onBack: () -> Unit,
) {
    var url by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        url = store.baseUrl()
    }

    MillScaffold(
        eyebrow = "Connection",
        title = "Mill server",
        lede = "Point this app at the same Chowdhury Mills web server. Emulator default is http://10.0.2.2:3210. On a phone use your LAN or hosted address, for example http://192.168.1.10:3000.",
        navLabels = listOf("Back" to onBack),
    ) {
        MillTextField(url, { url = it; saved = false }, "Base URL")
        Spacer(Modifier.height(12.dp))
        PrimaryButton(
            text = if (saved) "Saved" else "Save address",
            onClick = {
                scope.launch {
                    store.setBaseUrl(url)
                    saved = true
                }
            },
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Mutating requests send an Origin header matching this host so the web API’s same-origin check accepts the app.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun StaffPlaceholderScreen(
    staff: Staff,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    MillScaffold(
        eyebrow = "People",
        title = "Staff",
        lede = "Staff and role editing stays on the web desk for this first Android slice. Sign in as Admin on the web app to add people and permissions.",
        navLabels = navItemsFor(staff, onNavigate),
        onSignOut = onSignOut,
    ) {
        NoteBanner("Coming next: create staff, reset passwords, and assign role permissions from the phone.")
        Spacer(Modifier.height(12.dp))
        PrimaryButton(text = "Back to desk", onClick = onBack)
    }
}
