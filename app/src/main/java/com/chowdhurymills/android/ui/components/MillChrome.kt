package com.chowdhurymills.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chowdhurymills.android.data.DeskCard
import com.chowdhurymills.android.ui.theme.Card
import com.chowdhurymills.android.ui.theme.Cream
import com.chowdhurymills.android.ui.theme.Danger
import com.chowdhurymills.android.ui.theme.Gold
import com.chowdhurymills.android.ui.theme.Gold2
import com.chowdhurymills.android.ui.theme.Ink
import com.chowdhurymills.android.ui.theme.Line
import com.chowdhurymills.android.ui.theme.Muted
import com.chowdhurymills.android.ui.theme.Olive
import com.chowdhurymills.android.ui.theme.Olive2
import com.chowdhurymills.android.ui.theme.Paper

@Composable
fun BrandMark(compact: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(if (compact) 36.dp else 42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Brush.linearGradient(listOf(Gold2, Color(0xFF8D6A12)))),
            contentAlignment = Alignment.Center,
        ) {
            Text("CM", style = MaterialTheme.typography.titleLarge, color = Olive)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Chowdhury Mills", style = MaterialTheme.typography.titleLarge, color = Color(0xFFF6F1E7))
            Text(
                "Mustard oil ledger",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFF6F1E7).copy(alpha = 0.72f),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MillScaffold(
    eyebrow: String,
    title: String,
    lede: String? = null,
    navLabels: List<Pair<String, () -> Unit>> = emptyList(),
    onSignOut: (() -> Unit)? = null,
    page: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Paper)) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Olive)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            BrandMark(compact = true)
            Spacer(Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                navLabels.forEach { (label, onClick) ->
                    Text(
                        text = label,
                        color = Color(0xFFF6F1E7),
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFF6F1E7)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .border(1.dp, Color(0xFFF6F1E7).copy(alpha = 0.22f), RoundedCornerShape(999.dp))
                            .clickable(onClick = onClick)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
                if (onSignOut != null) {
                    Text(
                        text = "Sign out",
                        color = Color(0xFFF6F1E7),
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFF6F1E7)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .border(1.dp, Color(0xFFF6F1E7).copy(alpha = 0.22f), RoundedCornerShape(999.dp))
                            .clickable(onClick = onSignOut)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
        }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
        ) {
            Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelMedium, color = Gold)
            Spacer(Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.headlineLarge)
            if (!lede.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(lede, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(18.dp))
            page()
            Spacer(Modifier.height(28.dp))
            Text(
                "Chowdhury Mills IT",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    busy: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !busy,
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Olive, contentColor = Color(0xFFF6F1E7)),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        if (busy) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Color(0xFFF6F1E7),
                strokeWidth = 2.dp,
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            if (busy) "Working…" else text,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFFF6F1E7),
        )
    }
}

@Composable
fun MillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        readOnly = readOnly,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Gold,
            unfocusedBorderColor = Line,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedTextColor = Ink,
            unfocusedTextColor = Ink,
        ),
    )
}

@Composable
fun AlertBanner(message: String) {
    if (message.isBlank()) return
    Text(
        text = message,
        color = Danger,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFFDECEA))
            .padding(12.dp),
    )
}

@Composable
fun NoteBanner(message: String) {
    if (message.isBlank()) return
    Text(message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun DeskCardView(card: DeskCard, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Card)
            .border(1.dp, Line, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(card.kicker.uppercase(), style = MaterialTheme.typography.labelMedium, color = Olive2)
        Text(card.title, style = MaterialTheme.typography.headlineMedium)
        Text(card.copy, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SummaryBar(title: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Olive)
            .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Text(
                "ACCOUNT",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFF6F1E7).copy(alpha = 0.7f),
            )
            Text(title, style = MaterialTheme.typography.headlineMedium, color = Color(0xFFF6F1E7))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "OUTSTANDING",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFF6F1E7).copy(alpha = 0.7f),
            )
            Text(value, style = MaterialTheme.typography.headlineLarge, color = Color(0xFFF6F1E7))
        }
    }
}

@Composable
fun QuietButton(text: String, danger: Boolean = false, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text, color = if (danger) Danger else Olive, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LoadingBox() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Olive)
    }
}

@Composable
fun TableHeader(vararg cells: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Cream)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        cells.forEachIndexed { index, cell ->
            Text(
                cell.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Olive2,
                modifier = Modifier.weight(if (index == 0) 1.4f else 1f),
            )
        }
    }
}

fun formatAmount(value: Double): String {
    return if (value == value.toLong().toDouble()) value.toLong().toString() else "%.2f".format(value)
}
