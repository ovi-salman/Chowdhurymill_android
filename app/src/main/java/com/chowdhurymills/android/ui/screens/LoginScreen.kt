package com.chowdhurymills.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.chowdhurymills.android.data.ApiResult
import com.chowdhurymills.android.data.MillApi
import com.chowdhurymills.android.data.SessionStore
import com.chowdhurymills.android.data.Staff
import com.chowdhurymills.android.ui.components.AlertBanner
import com.chowdhurymills.android.ui.components.BrandMark
import com.chowdhurymills.android.ui.components.MillTextField
import com.chowdhurymills.android.ui.components.PrimaryButton
import com.chowdhurymills.android.ui.theme.Card
import com.chowdhurymills.android.ui.theme.Gold
import com.chowdhurymills.android.ui.theme.Line
import com.chowdhurymills.android.ui.theme.Olive
import com.chowdhurymills.android.ui.theme.Paper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    api: MillApi,
    store: SessionStore,
    onSignedIn: (Staff) -> Unit,
    onOpenSettings: () -> Unit,
) {
    var staffId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var roleChoice by remember { mutableStateOf("Admin") }
    var otherRole by remember { mutableStateOf("") }
    var roleMenuOpen by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF243F33), Olive),
                    ),
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BrandMark()
            Text(
                "The day’s oil, written down cleanly.",
                style = MaterialTheme.typography.displayLarge,
                color = Color(0xFFF6F1E7),
            )
            Text(
                "Receipts on one side, payments on the other. Staff sign in to post the chart or open an account.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFF6F1E7).copy(alpha = 0.78f),
            )
            Text("Chowdhury Mills IT", color = Color(0xFFF6F1E7).copy(alpha = 0.7f))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Card)
                    .border(1.dp, Line, RoundedCornerShape(24.dp))
                    .padding(24.dp),
            ) {
                Text("STAFF SIGN IN", style = MaterialTheme.typography.labelMedium, color = Gold)
                Spacer(Modifier.height(6.dp))
                Text("Welcome back", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Use your staff ID and the password saved for this app. Set the mill server address if this phone is not on the same machine.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                AlertBanner(error)
                Spacer(Modifier.height(8.dp))
                MillTextField(staffId, { staffId = it }, "Staff ID")
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(Modifier.height(10.dp))
                ExposedDropdownMenuBox(expanded = roleMenuOpen, onExpandedChange = { roleMenuOpen = it }) {
                    OutlinedTextField(
                        value = roleChoice,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuOpen) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    )
                    ExposedDropdownMenu(expanded = roleMenuOpen, onDismissRequest = { roleMenuOpen = false }) {
                        listOf("Admin", "Manager", "SR", "Another role").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    roleChoice = option
                                    roleMenuOpen = false
                                },
                            )
                        }
                    }
                }
                if (roleChoice == "Another role") {
                    Spacer(Modifier.height(10.dp))
                    MillTextField(otherRole, { otherRole = it }, "Role name")
                }
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = "Sign in",
                    busy = busy,
                    onClick = {
                        scope.launch {
                            busy = true
                            error = ""
                            val staffType = if (roleChoice == "Another role") otherRole.trim() else roleChoice
                            when (val result = api.login(staffId.trim(), password, staffType)) {
                                is ApiResult.Ok -> {
                                    when (val me = api.me()) {
                                        is ApiResult.Ok -> {
                                            val signed = me.value.staff
                                            if (signed == null) error = "Signed in, but session could not be read."
                                            else onSignedIn(signed)
                                        }
                                        is ApiResult.Err -> error = me.message
                                    }
                                }
                                is ApiResult.Err -> error = result.message
                            }
                            busy = false
                        }
                    },
                )
                TextButton(onClick = onOpenSettings, modifier = Modifier.align(Alignment.End)) {
                    Text("Server address", color = Olive)
                }
            }
        }
    }
}
