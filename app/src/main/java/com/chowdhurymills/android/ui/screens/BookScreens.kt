package com.chowdhurymills.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chowdhurymills.android.data.Account
import com.chowdhurymills.android.data.ApiResult
import com.chowdhurymills.android.data.DayRow
import com.chowdhurymills.android.data.LedgerRow
import com.chowdhurymills.android.data.MillApi
import com.chowdhurymills.android.data.MillCatalog
import com.chowdhurymills.android.data.OutstandingRow
import com.chowdhurymills.android.data.Staff
import com.chowdhurymills.android.ui.components.AlertBanner
import com.chowdhurymills.android.ui.components.MillScaffold
import com.chowdhurymills.android.ui.components.MillTextField
import com.chowdhurymills.android.ui.components.NoteBanner
import com.chowdhurymills.android.ui.components.PrimaryButton
import com.chowdhurymills.android.ui.components.QuietButton
import com.chowdhurymills.android.ui.components.SummaryBar
import com.chowdhurymills.android.ui.components.TableHeader
import com.chowdhurymills.android.ui.components.formatAmount
import com.chowdhurymills.android.ui.navigation.navItemsFor
import com.chowdhurymills.android.ui.theme.Card
import com.chowdhurymills.android.ui.theme.Cream
import com.chowdhurymills.android.ui.theme.Ink
import com.chowdhurymills.android.ui.theme.Line
import com.chowdhurymills.android.ui.theme.Muted
import com.chowdhurymills.android.ui.theme.Olive
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun DirectoryScreen(
    staff: Staff,
    api: MillApi,
    onBack: () -> Unit,
    onOpenLedger: (String) -> Unit,
    onSignOut: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }
    var term by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val openLedger = MillCatalog.allows(staff, "ledger")

    LaunchedEffect(Unit) {
        when (val result = api.directory()) {
            is ApiResult.Ok -> accounts = result.value.accounts
            is ApiResult.Err -> error = result.message
        }
    }

    val shown = accounts.filter {
        val needle = term.trim().lowercase()
        if (needle.isEmpty()) true
        else listOf(it.name, it.phone, it.details).joinToString(" ").lowercase().contains(needle)
    }

    MillScaffold(
        eyebrow = "Account book",
        title = "Directory",
        lede = "${accounts.size} accounts from the mill book. Phone and place sit beside the name.",
        navLabels = navItemsFor(staff, onNavigate),
        onSignOut = onSignOut,
    ) {
        MillTextField(term, { term = it }, "Find an account")
        Spacer(Modifier.height(12.dp))
        AlertBanner(error)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Card)
                .border(1.dp, Line, RoundedCornerShape(18.dp)),
        ) {
            TableHeader("Name", "Phone", "Place")
            shown.forEach { account ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = openLedger) { onOpenLedger(account.name) }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                ) {
                    Text(
                        account.name,
                        color = if (openLedger) Olive else Ink,
                        fontWeight = if (openLedger) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1.4f),
                    )
                    Text(account.phone.ifBlank { "—" }, color = Muted, modifier = Modifier.weight(1f))
                    Text(account.details.ifBlank { "—" }, color = Muted, modifier = Modifier.weight(1f))
                }
            }
            if (shown.isEmpty()) {
                Text("No account matches that search.", color = Muted, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun LedgerScreen(
    staff: Staff,
    api: MillApi,
    initialName: String,
    ranged: Boolean,
    ownOnly: Boolean = false,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf(if (ownOnly) staff.accountName else "") }
    var total by remember { mutableStateOf(0.0) }
    var credit by remember { mutableStateOf<Double?>(null) }
    var debit by remember { mutableStateOf<Double?>(null) }
    var rows by remember { mutableStateOf<List<LedgerRow>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            busy = true
            error = ""
            val result = if (ownOnly) {
                api.ledger()
            } else if (ranged) {
                api.ledger(name = name.trim(), from = from.trim(), to = to.trim())
            } else {
                api.ledger(name = name.trim())
            }
            when (result) {
                is ApiResult.Ok -> {
                    accountName = result.value.accountName
                    total = result.value.total
                    credit = result.value.credit
                    debit = result.value.debit
                    rows = result.value.rows
                    if (result.value.missing) error = "No account with that name."
                }
                is ApiResult.Err -> error = result.message
            }
            busy = false
        }
    }

    LaunchedEffect(initialName, ownOnly) {
        if (ownOnly || initialName.isNotBlank()) load()
    }

    MillScaffold(
        eyebrow = when {
            ownOnly -> "Ledger"
            ranged -> "Range"
            else -> "Ledger"
        },
        title = when {
            ownOnly -> "Show account details"
            ranged -> "Between dates"
            else -> "Account details"
        },
        lede = when {
            ownOnly -> "See every posting on your account and the total still outstanding."
            ranged -> "One account’s receipts, payments, and outstanding from a start date to an end date."
            else -> "Search a name and read receipts, payments, and the outstanding balance."
        },
        navLabels = navItemsFor(staff, onNavigate),
        onSignOut = onSignOut,
    ) {
        if (!ownOnly) {
            MillTextField(name, { name = it }, "Account name")
            if (ranged) {
                Spacer(Modifier.height(8.dp))
                MillTextField(from, { from = it }, "From (YYYY-MM-DD)")
                Spacer(Modifier.height(8.dp))
                MillTextField(to, { to = it }, "To (YYYY-MM-DD)")
            }
            Spacer(Modifier.height(12.dp))
            PrimaryButton(text = "Show ledger", busy = busy, onClick = { load() })
            Spacer(Modifier.height(12.dp))
        }
        AlertBanner(error)
        if (accountName.isNotBlank()) {
            SummaryBar(accountName, formatAmount(total))
            if (ranged && credit != null && debit != null) {
                Spacer(Modifier.height(8.dp))
                Text("Credit ${formatAmount(credit!!)} · Debit ${formatAmount(debit!!)}", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Card)
                    .border(1.dp, Line, RoundedCornerShape(18.dp)),
            ) {
                TableHeader("Date", "Qty", "Amount")
                rows.forEach { entry ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
                        Text(entry.date, modifier = Modifier.weight(1.4f))
                        Text(entry.quantity, modifier = Modifier.weight(1f), color = Muted)
                        Text(formatAmount(entry.amount), modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    }
                }
                if (rows.isEmpty()) {
                    Text("No lines yet.", color = Muted, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun DayScreen(
    staff: Staff,
    api: MillApi,
    printable: Boolean,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var shownDate by remember { mutableStateOf("") }
    var rows by remember { mutableStateOf<List<DayRow>?>(null) }
    var error by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var draft by remember { mutableStateOf<DayRow?>(null) }
    var draftQty by remember { mutableStateOf("") }
    var draftAmount by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            error = ""
            message = ""
            draft = null
            when (val result = api.day(date.trim())) {
                is ApiResult.Ok -> {
                    shownDate = result.value.date
                    rows = result.value.rows
                }
                is ApiResult.Err -> {
                    error = result.message
                    rows = emptyList()
                }
            }
        }
    }

    MillScaffold(
        eyebrow = if (printable) "Print" else "By date",
        title = if (printable) "Print a day’s lines" else "Check posting by date",
        lede = if (printable) {
            "Receipts stay on the left and payments on the right. This sheet is read-only."
        } else {
            "Open one date. Update a quantity or amount, or delete a line. Payments keep their minus sign."
        },
        navLabels = navItemsFor(staff, onNavigate),
        onSignOut = onSignOut,
    ) {
        MillTextField(date, { date = it }, "Date (YYYY-MM-DD)")
        Spacer(Modifier.height(12.dp))
        PrimaryButton(text = "Show lines", onClick = { load() })
        Spacer(Modifier.height(12.dp))
        AlertBanner(error)
        NoteBanner(message)
        if (shownDate.isNotBlank()) {
            Text("Date : $shownDate", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
        }
        draft?.let { current ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Card)
                    .border(1.dp, Line, RoundedCornerShape(18.dp))
                    .padding(16.dp),
            ) {
                Text("UPDATE LINE ${current.id}", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                MillTextField(current.accountName, {}, "Head", readOnly = true)
                Spacer(Modifier.height(8.dp))
                MillTextField(draftQty, { draftQty = it }, "Quantity")
                Spacer(Modifier.height(8.dp))
                MillTextField(draftAmount, { draftAmount = it }, "Amount")
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    text = "Submit",
                    busy = busy,
                    onClick = {
                        scope.launch {
                            busy = true
                            when (
                                val result = api.updateTransaction(
                                    current.id,
                                    draftQty,
                                    draftAmount.toDoubleOrNull() ?: 0.0,
                                )
                            ) {
                                is ApiResult.Ok -> {
                                    draft = null
                                    message = "The line has been updated."
                                    load()
                                }
                                is ApiResult.Err -> error = result.message
                            }
                            busy = false
                        }
                    },
                )
                QuietButton("Cancel", onClick = { draft = null })
            }
            Spacer(Modifier.height(12.dp))
        }
        rows?.let { list ->
            if (list.isEmpty()) {
                NoteBanner("0 results")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Card)
                        .border(1.dp, Line, RoundedCornerShape(18.dp)),
                ) {
                    TableHeader("Side", "ID", "Account", "Qty", "Amount")
                    list.forEach { entry ->
                        val receipt = entry.amount > 0
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                            Row(Modifier.fillMaxWidth()) {
                                Text(if (receipt) "Receipt" else "Payment", modifier = Modifier.weight(1f), color = Olive, fontWeight = FontWeight.Bold)
                                Text(entry.id.toString(), modifier = Modifier.weight(0.8f))
                                Text(entry.accountName, modifier = Modifier.weight(1.4f))
                                Text(entry.quantity, modifier = Modifier.weight(0.8f), color = Muted)
                                Text(formatAmount(entry.amount), modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            }
                            if (!printable) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    QuietButton("Update") {
                                        draft = entry
                                        draftQty = entry.quantity
                                        draftAmount = entry.amount.toString()
                                    }
                                    QuietButton("Delete", danger = true, onClick = {
                                        scope.launch {
                                            when (val result = api.deleteTransaction(entry.id)) {
                                                is ApiResult.Ok -> {
                                                    message = "The line has been deleted."
                                                    rows = rows?.filterNot { it.id == entry.id }
                                                    if (draft?.id == entry.id) draft = null
                                                }
                                                is ApiResult.Err -> error = result.message
                                            }
                                        }
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BalancesScreen(
    staff: Staff,
    api: MillApi,
    creditOnly: Boolean,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var rows by remember { mutableStateOf<List<OutstandingRow>>(emptyList()) }
    var error by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    MillScaffold(
        eyebrow = if (creditOnly) "Credit" else "Balances",
        title = if (creditOnly) "Credit accounts" else "Outstanding to a date",
        lede = if (creditOnly) {
            "The same closing date, limited to accounts whose outstanding is below zero."
        } else {
            "Every account that has a posting on or before the closing date."
        },
        navLabels = navItemsFor(staff, onNavigate),
        onSignOut = onSignOut,
    ) {
        MillTextField(date, { date = it }, "Closing date (YYYY-MM-DD)")
        Spacer(Modifier.height(12.dp))
        PrimaryButton(
            text = "Show balances",
            busy = busy,
            onClick = {
                scope.launch {
                    busy = true
                    error = ""
                    when (val result = api.outstanding(date.trim(), creditOnly)) {
                        is ApiResult.Ok -> rows = result.value.rows
                        is ApiResult.Err -> error = result.message
                    }
                    busy = false
                }
            },
        )
        Spacer(Modifier.height(12.dp))
        AlertBanner(error)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Card)
                .border(1.dp, Line, RoundedCornerShape(18.dp)),
        ) {
            TableHeader("ID", "Name", "Outstanding")
            rows.forEach { entry ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(entry.id.toString(), modifier = Modifier.weight(0.8f))
                    Text(entry.name, modifier = Modifier.weight(1.6f))
                    Text(formatAmount(entry.outstanding), modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                }
            }
            if (rows.isEmpty()) {
                Text("No balances for that date yet.", color = Muted, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun CreateAccountScreen(
    staff: Staff,
    api: MillApi,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    MillScaffold(
        eyebrow = "Book",
        title = "Create account",
        lede = "Add a name, phone, and place to the account book.",
        navLabels = navItemsFor(staff, onNavigate),
        onSignOut = onSignOut,
    ) {
        MillTextField(name, { name = it }, "Account name")
        Spacer(Modifier.height(8.dp))
        MillTextField(phone, { phone = it }, "Phone")
        Spacer(Modifier.height(8.dp))
        MillTextField(details, { details = it }, "Place / details")
        Spacer(Modifier.height(12.dp))
        AlertBanner(error)
        NoteBanner(message)
        PrimaryButton(
            text = "Create account",
            busy = busy,
            onClick = {
                scope.launch {
                    busy = true
                    error = ""
                    message = ""
                    when (val result = api.createAccount(name.trim(), phone.trim(), details.trim())) {
                        is ApiResult.Ok -> {
                            if (result.value.error != null) error = result.value.error!!
                            else {
                                message = "Created ${result.value.name}."
                                name = ""
                                phone = ""
                                details = ""
                            }
                        }
                        is ApiResult.Err -> error = result.message
                    }
                    busy = false
                }
            },
        )
    }
}
