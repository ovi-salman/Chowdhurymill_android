package com.chowdhurymills.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.chowdhurymills.android.data.ApiResult
import com.chowdhurymills.android.data.MillApi
import com.chowdhurymills.android.data.MillCatalog
import com.chowdhurymills.android.data.PostingLine
import com.chowdhurymills.android.data.Staff
import com.chowdhurymills.android.ui.components.AlertBanner
import com.chowdhurymills.android.ui.components.MillScaffold
import com.chowdhurymills.android.ui.components.MillTextField
import com.chowdhurymills.android.ui.components.NoteBanner
import com.chowdhurymills.android.ui.components.PrimaryButton
import com.chowdhurymills.android.ui.navigation.navItemsFor
import com.chowdhurymills.android.ui.theme.Line
import com.chowdhurymills.android.ui.theme.Muted
import com.chowdhurymills.android.ui.theme.PaymentBg
import com.chowdhurymills.android.ui.theme.ReceiptBg
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DraftLine(
    val name: String,
    val quantity: String = "",
    val amount: String = "",
    val side: String,
    val locked: Boolean,
)

@Composable
fun PostingScreen(
    staff: Staff,
    api: MillApi,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    val lines = remember {
        mutableStateListOf<DraftLine>().apply {
            MillCatalog.namedPostingPairs.forEach { (receipt, payment) ->
                add(DraftLine(name = receipt, side = "receipt", locked = receipt.isNotBlank()))
                add(DraftLine(name = payment, side = "payment", locked = payment.isNotBlank()))
            }
            // A few open search pairs for mobile (full 30 blank pairs are better on web).
            repeat(6) {
                add(DraftLine(name = "", side = "receipt", locked = false))
                add(DraftLine(name = "", side = "payment", locked = false))
            }
        }
    }
    var error by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var suggestIndex by remember { mutableStateOf(-1) }
    val scope = rememberCoroutineScope()

    fun update(index: Int, transform: (DraftLine) -> DraftLine) {
        lines[index] = transform(lines[index])
    }

    MillScaffold(
        eyebrow = "Daily posting",
        title = "Receipts and payments",
        lede = "Named product lines match the web chart. Six open pairs follow for searched accounts. Leave an amount blank to skip that line.",
        navLabels = navItemsFor(staff, onNavigate),
        onSignOut = onSignOut,
    ) {
        MillTextField(date, { date = it }, "Posting date (YYYY-MM-DD)")
        Spacer(Modifier.height(12.dp))
        PrimaryButton(
            text = "Save posting",
            busy = busy,
            onClick = {
                scope.launch {
                    busy = true
                    error = ""
                    message = ""
                    val payload = lines.map {
                        PostingLine(
                            name = it.name,
                            quantity = it.quantity,
                            amount = it.amount.toDoubleOrNull() ?: 0.0,
                            side = it.side,
                        )
                    }
                    when (val result = api.postDay(date.trim(), payload)) {
                        is ApiResult.Ok -> {
                            val skipped = if (result.value.skipped.isNotEmpty()) {
                                " Skipped unknown accounts: ${result.value.skipped.joinToString(", ")}."
                            } else ""
                            message = if (result.value.saved > 0) {
                                "Saved ${result.value.saved} line${if (result.value.saved == 1) "" else "s"}.$skipped"
                            } else {
                                "Nothing to save.$skipped"
                            }
                            if (result.value.saved > 0) {
                                for (i in lines.indices) {
                                    lines[i] = lines[i].copy(quantity = "", amount = "")
                                }
                            }
                        }
                        is ApiResult.Err -> error = result.message
                    }
                    busy = false
                }
            },
        )
        Spacer(Modifier.height(8.dp))
        AlertBanner(error)
        NoteBanner(message)
        Spacer(Modifier.height(8.dp))
        Text("Receipt · Payment", style = MaterialTheme.typography.labelMedium, color = Muted)
        Spacer(Modifier.height(8.dp))

        for (pairIndex in 0 until lines.size / 2) {
            val receiptIndex = pairIndex * 2
            val paymentIndex = receiptIndex + 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(receiptIndex, paymentIndex).forEach { index ->
                    val line = lines[index]
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (line.side == "receipt") ReceiptBg else PaymentBg)
                            .border(1.dp, Line, RoundedCornerShape(14.dp))
                            .padding(8.dp),
                    ) {
                        MillTextField(
                            value = line.name,
                            onValueChange = { value ->
                                if (!line.locked) {
                                    update(index) { it.copy(name = value) }
                                    suggestIndex = index
                                    scope.launch {
                                        if (value.trim().length >= 1) {
                                            when (val result = api.searchAccounts(value.trim())) {
                                                is ApiResult.Ok -> suggestions = result.value.names
                                                is ApiResult.Err -> suggestions = emptyList()
                                            }
                                        } else {
                                            suggestions = emptyList()
                                        }
                                    }
                                }
                            },
                            label = if (line.side == "receipt") "Receipt" else "Payment",
                            readOnly = line.locked,
                        )
                        if (!line.locked && suggestIndex == index && suggestions.isNotEmpty()) {
                            suggestions.take(6).forEach { suggestion ->
                                androidx.compose.material3.TextButton(
                                    onClick = {
                                        update(index) { it.copy(name = suggestion) }
                                        suggestions = emptyList()
                                        suggestIndex = -1
                                    },
                                ) { Text(suggestion, color = OliveSafe) }
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        MillTextField(line.quantity, { value -> update(index) { it.copy(quantity = value) } }, "Qty")
                        Spacer(Modifier.height(6.dp))
                        MillTextField(line.amount, { value -> update(index) { it.copy(amount = value) } }, "Amount")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

private val OliveSafe = com.chowdhurymills.android.ui.theme.Olive
