package com.chowdhurymills.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chowdhurymills.android.data.ApiResult
import com.chowdhurymills.android.data.MillApi
import com.chowdhurymills.android.data.MillCatalog
import com.chowdhurymills.android.data.SessionStore
import com.chowdhurymills.android.data.Staff
import com.chowdhurymills.android.ui.components.LoadingBox
import com.chowdhurymills.android.ui.screens.BalancesScreen
import com.chowdhurymills.android.ui.screens.CreateAccountScreen
import com.chowdhurymills.android.ui.screens.DayScreen
import com.chowdhurymills.android.ui.screens.DirectoryScreen
import com.chowdhurymills.android.ui.screens.HomeScreen
import com.chowdhurymills.android.ui.screens.LedgerScreen
import com.chowdhurymills.android.ui.screens.LoginScreen
import com.chowdhurymills.android.ui.screens.PostingScreen
import com.chowdhurymills.android.ui.screens.ServerSettingsScreen
import com.chowdhurymills.android.ui.screens.StaffPlaceholderScreen

@Composable
fun MillNavHost(api: MillApi, store: SessionStore) {
    val nav = rememberNavController()
    var staff by remember { mutableStateOf<Staff?>(null) }
    var bootstrapped by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        when (val result = api.me()) {
            is ApiResult.Ok -> staff = result.value.staff
            is ApiResult.Err -> staff = null
        }
        bootstrapped = true
    }

    if (!bootstrapped) {
        LoadingBox()
        return
    }

    fun goHome(next: Staff?) {
        staff = next
        nav.navigate("home") {
            popUpTo("login") { inclusive = true }
            launchSingleTop = true
        }
    }

    fun signOut() {
        // Fire and forget; clear local session regardless.
        staff = null
        nav.navigate("login") {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    val start = if (staff != null) "home" else "login"

    NavHost(navController = nav, startDestination = start) {
        composable("login") {
            LoginScreen(
                api = api,
                store = store,
                onSignedIn = { goHome(it) },
                onOpenSettings = { nav.navigate("settings") },
            )
        }
        composable("settings") {
            ServerSettingsScreen(
                store = store,
                onBack = { nav.popBackStack() },
            )
        }
        composable("home") {
            val current = staff
            if (current == null) {
                LaunchedEffect(Unit) { nav.navigate("login") { popUpTo(0) { inclusive = true } } }
                LoadingBox()
            } else {
                HomeScreen(
                    staff = current,
                    api = api,
                    onOpen = { route -> nav.navigate(route) },
                    onSignOutAsync = {
                        api.logout()
                        signOut()
                    },
                )
            }
        }
        composable("directory") {
            Guarded(staff, "directory", nav) { current ->
                DirectoryScreen(
                    staff = current,
                    api = api,
                    onBack = { nav.popBackStack() },
                    onOpenLedger = { name -> nav.navigate("ledger?name=${android.net.Uri.encode(name)}") },
                    onSignOut = { signOut() },
                    onNavigate = { nav.navigate(it) },
                )
            }
        }
        composable(
            route = "ledger?name={name}",
            arguments = listOf(navArgument("name") { type = NavType.StringType; defaultValue = "" }),
        ) { entry ->
            Guarded(staff, "ledger", nav, alsoOwn = true) { current ->
                LedgerScreen(
                    staff = current,
                    api = api,
                    initialName = entry.arguments?.getString("name").orEmpty(),
                    ranged = false,
                    onBack = { nav.popBackStack() },
                    onSignOut = { signOut() },
                    onNavigate = { nav.navigate(it) },
                )
            }
        }
        composable("range") {
            Guarded(staff, "range", nav) { current ->
                LedgerScreen(
                    staff = current,
                    api = api,
                    initialName = "",
                    ranged = true,
                    onBack = { nav.popBackStack() },
                    onSignOut = { signOut() },
                    onNavigate = { nav.navigate(it) },
                )
            }
        }
        composable("own_ledger") {
            Guarded(staff, "own_ledger", nav) { current ->
                LedgerScreen(
                    staff = current,
                    api = api,
                    initialName = "",
                    ranged = false,
                    ownOnly = true,
                    onBack = { nav.popBackStack() },
                    onSignOut = { signOut() },
                    onNavigate = { nav.navigate(it) },
                )
            }
        }
        composable("day") {
            Guarded(staff, "day", nav) { current ->
                DayScreen(
                    staff = current,
                    api = api,
                    printable = false,
                    onBack = { nav.popBackStack() },
                    onSignOut = { signOut() },
                    onNavigate = { nav.navigate(it) },
                )
            }
        }
        composable("print") {
            Guarded(staff, "print", nav) { current ->
                DayScreen(
                    staff = current,
                    api = api,
                    printable = true,
                    onBack = { nav.popBackStack() },
                    onSignOut = { signOut() },
                    onNavigate = { nav.navigate(it) },
                )
            }
        }
        composable("balances") {
            Guarded(staff, "balances", nav) { current ->
                BalancesScreen(
                    staff = current,
                    api = api,
                    creditOnly = false,
                    onBack = { nav.popBackStack() },
                    onSignOut = { signOut() },
                    onNavigate = { nav.navigate(it) },
                )
            }
        }
        composable("credit") {
            Guarded(staff, "credit", nav) { current ->
                BalancesScreen(
                    staff = current,
                    api = api,
                    creditOnly = true,
                    onBack = { nav.popBackStack() },
                    onSignOut = { signOut() },
                    onNavigate = { nav.navigate(it) },
                )
            }
        }
        composable("create") {
            Guarded(staff, "create", nav) { current ->
                CreateAccountScreen(
                    staff = current,
                    api = api,
                    onBack = { nav.popBackStack() },
                    onSignOut = { signOut() },
                    onNavigate = { nav.navigate(it) },
                )
            }
        }
        composable("posting") {
            Guarded(staff, "posting", nav) { current ->
                PostingScreen(
                    staff = current,
                    api = api,
                    onBack = { nav.popBackStack() },
                    onSignOut = { signOut() },
                    onNavigate = { nav.navigate(it) },
                )
            }
        }
        composable("staff") {
            Guarded(staff, "staff", nav, adminOnly = true) { current ->
                StaffPlaceholderScreen(
                    staff = current,
                    onBack = { nav.popBackStack() },
                    onSignOut = { signOut() },
                    onNavigate = { nav.navigate(it) },
                )
            }
        }
    }
}

@Composable
private fun Guarded(
    staff: Staff?,
    key: String,
    nav: androidx.navigation.NavHostController,
    alsoOwn: Boolean = false,
    adminOnly: Boolean = false,
    content: @Composable (Staff) -> Unit,
) {
    val current = staff
    if (current == null) {
        LaunchedEffect(Unit) { nav.navigate("login") { popUpTo(0) { inclusive = true } } }
        LoadingBox()
        return
    }
    val allowed = when {
        adminOnly -> current.staffType == "Admin"
        alsoOwn -> MillCatalog.allows(current, key) || MillCatalog.allows(current, "own_ledger")
        else -> MillCatalog.allows(current, key)
    }
    if (!allowed) {
        LaunchedEffect(Unit) { nav.popBackStack() }
        LoadingBox()
        return
    }
    content(current)
}

fun navItemsFor(staff: Staff, onNavigate: (String) -> Unit): List<Pair<String, () -> Unit>> {
    return listOfNotNull(
        "Home" to { onNavigate("home") },
        if (MillCatalog.allows(staff, "posting")) "Daily posting" to { onNavigate("posting") } else null,
        if (MillCatalog.allows(staff, "day")) "By date" to { onNavigate("day") } else null,
        if (MillCatalog.allows(staff, "print")) "Print" to { onNavigate("print") } else null,
        if (MillCatalog.allows(staff, "directory")) "Directory" to { onNavigate("directory") } else null,
        if (MillCatalog.allows(staff, "ledger")) "Ledger" to { onNavigate("ledger") } else null,
        if (MillCatalog.allows(staff, "range")) "Range" to { onNavigate("range") } else null,
        if (MillCatalog.allows(staff, "balances")) "Balances" to { onNavigate("balances") } else null,
        if (MillCatalog.allows(staff, "credit")) "Credit" to { onNavigate("credit") } else null,
        if (MillCatalog.allows(staff, "create")) "New account" to { onNavigate("create") } else null,
        if (MillCatalog.allows(staff, "own_ledger")) "My account" to { onNavigate("own_ledger") } else null,
        if (staff.staffType == "Admin") "Staff" to { onNavigate("staff") } else null,
    )
}
