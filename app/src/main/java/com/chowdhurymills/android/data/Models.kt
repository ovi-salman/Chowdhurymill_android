package com.chowdhurymills.android.data

import kotlinx.serialization.Serializable

@Serializable
data class Staff(
    val staffId: String = "",
    val staffType: String = "",
    val accountName: String = "",
    val permissions: List<String> = emptyList(),
)

@Serializable
data class MeResponse(val staff: Staff? = null)

@Serializable
data class LoginRequest(
    val staffId: String,
    val password: String,
    val staffType: String,
)

@Serializable
data class LoginResponse(
    val ok: Boolean = false,
    val redirect: String? = null,
    val error: String? = null,
)

@Serializable
data class ErrorResponse(val error: String? = null)

@Serializable
data class Account(
    val id: Long = 0,
    val name: String = "",
    val phone: String = "",
    val details: String = "",
)

@Serializable
data class DirectoryResponse(val accounts: List<Account> = emptyList())

@Serializable
data class NamesResponse(val names: List<String> = emptyList())

@Serializable
data class LedgerRow(
    val date: String = "",
    val quantity: String = "",
    val amount: Double = 0.0,
)

@Serializable
data class LedgerResponse(
    val accountName: String = "",
    val total: Double = 0.0,
    val credit: Double? = null,
    val debit: Double? = null,
    val ranged: Boolean = false,
    val from: String? = null,
    val to: String? = null,
    val rows: List<LedgerRow> = emptyList(),
    val missing: Boolean = false,
)

@Serializable
data class DayRow(
    val id: Long = 0,
    val accountName: String = "",
    val quantity: String = "",
    val amount: Double = 0.0,
)

@Serializable
data class DayResponse(
    val date: String = "",
    val rows: List<DayRow> = emptyList(),
)

@Serializable
data class OutstandingRow(
    val id: Long = 0,
    val name: String = "",
    val outstanding: Double = 0.0,
)

@Serializable
data class OutstandingResponse(
    val date: String = "",
    val credit: Boolean = false,
    val rows: List<OutstandingRow> = emptyList(),
)

@Serializable
data class PostingLine(
    val name: String,
    val quantity: String,
    val amount: Double,
    val side: String,
)

@Serializable
data class PostingRequest(
    val date: String,
    val lines: List<PostingLine>,
)

@Serializable
data class PostingResponse(
    val saved: Int = 0,
    val skipped: List<String> = emptyList(),
    val error: String? = null,
)

@Serializable
data class CreateAccountRequest(
    val name: String,
    val phone: String = "",
    val details: String = "",
)

@Serializable
data class CreateAccountResponse(
    val id: Long? = null,
    val name: String? = null,
    val error: String? = null,
)

@Serializable
data class UpdateTransactionRequest(
    val id: Long,
    val quantity: String,
    val amount: Double,
)

@Serializable
data class DeleteTransactionRequest(val id: Long)

@Serializable
data class OkResponse(val ok: Boolean = false)

data class DeskCard(
    val key: String,
    val route: String,
    val kicker: String,
    val title: String,
    val copy: String,
)

object MillCatalog {
    val deskCards = listOf(
        DeskCard(
            key = "posting",
            route = "posting",
            kicker = "Posting",
            title = "Daily posting",
            copy = "The full chart: named product lines, then open search lines through the last pair.",
        ),
        DeskCard(
            key = "day",
            route = "day",
            kicker = "Review",
            title = "Check a date",
            copy = "See that day’s receipts and payments, then update a line or delete it.",
        ),
        DeskCard(
            key = "print",
            route = "print",
            kicker = "Sheet",
            title = "Print a date",
            copy = "The same day’s lines, laid out for paper, without update or delete.",
        ),
        DeskCard(
            key = "directory",
            route = "directory",
            kicker = "Book",
            title = "Account directory",
            copy = "Every account name, phone, and place from the mill book.",
        ),
        DeskCard(
            key = "ledger",
            route = "ledger",
            kicker = "Ledger",
            title = "Account details",
            copy = "Search a name and read receipts, payments, and the outstanding balance.",
        ),
        DeskCard(
            key = "range",
            route = "range",
            kicker = "Range",
            title = "Between dates",
            copy = "One account’s receipts, payments, and outstanding from a start date to an end date.",
        ),
        DeskCard(
            key = "balances",
            route = "balances",
            kicker = "Balances",
            title = "Outstanding to a date",
            copy = "Every account that has a posting on or before the closing date.",
        ),
        DeskCard(
            key = "credit",
            route = "credit",
            kicker = "Credit",
            title = "Credit accounts",
            copy = "The same closing date, limited to accounts whose outstanding is below zero.",
        ),
        DeskCard(
            key = "create",
            route = "create",
            kicker = "Book",
            title = "Create account",
            copy = "Add a name, phone, and place to the account book.",
        ),
        DeskCard(
            key = "own_ledger",
            route = "own_ledger",
            kicker = "Ledger",
            title = "Show account details",
            copy = "See every posting on your account and the total still outstanding.",
        ),
    )

    val adminOnlyStaff = DeskCard(
        key = "staff",
        route = "staff",
        kicker = "People",
        title = "Staff",
        copy = "Add a staff member, set the ID and password, and choose what that role can open.",
    )

    // Matches web/lib/rows.js named pairs, then open search pairs.
    val namedPostingPairs = listOf(
        "Chaka Mustard Oil" to "Mustard",
        "Tin" to "Chaka Mustard Oil",
        "Khol" to "Khol",
        "Bosta" to "Bosta",
        "Pet Bottle" to "Chowdhury Mills",
        "Soyabin" to "Pet Bottle",
        "Nitol Vegitable" to "Office",
        "Koyel" to "Salary",
        "Fresh Soyabin" to "Koyel",
        "Fresh Milk" to "Transport",
        "Fresh Tea" to "",
        "Fresh Suger" to "",
        "Fresh Ata" to "",
        "Fresh Water" to "",
    )

    fun allows(staff: Staff?, key: String): Boolean {
        if (staff == null) return false
        if (staff.staffType == "Admin") return true
        return staff.permissions.contains(key)
    }

    fun homeRoute(staff: Staff?): String {
        if (staff == null) return "login"
        if (staff.staffType == "Admin") return "home"
        val perms = staff.permissions.ifEmpty {
            when (staff.staffType) {
                "Manager" -> listOf("posting", "day", "print", "directory")
                "SR" -> listOf("own_ledger")
                else -> emptyList()
            }
        }
        return if (perms.any { it != "own_ledger" }) "home" else "home"
    }

    fun cardsFor(staff: Staff): List<DeskCard> {
        val cards = deskCards.filter { allows(staff, it.key) }
            .filterNot { it.key == "own_ledger" && staff.accountName.isBlank() }
        return if (staff.staffType == "Admin") {
            cards.filter { it.key != "own_ledger" } + adminOnlyStaff
        } else {
            cards
        }
    }
}
