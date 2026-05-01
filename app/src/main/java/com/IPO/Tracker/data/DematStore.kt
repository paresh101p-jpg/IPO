package com.IPO.Tracker.data

import android.content.Context
import com.IPO.Tracker.model.DematAccount
import org.json.JSONObject
import java.util.UUID

object DematStore {
    private const val PREFS_NAME = "demat_account_prefs"
    private const val KEY_ACCOUNTS = "demat_account_list"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun serialize(account: DematAccount): String {
        return JSONObject()
            .put("id", account.id)
            .put("name", account.name)
            .put("panNumber", account.panNumber)
            .put("dpId", account.dpId)
            .put("clientId", account.clientId)
            .put("upiId", account.upiId)
            .toString()
    }

    private fun parse(raw: String): DematAccount? {
        return try {
            val json = JSONObject(raw)
            DematAccount(
                id = json.optString("id", UUID.randomUUID().toString()),
                name = json.optString("name", "Unnamed"),
                panNumber = json.optString("panNumber", ""),
                dpId = json.optString("dpId", ""),
                clientId = json.optString("clientId", ""),
                upiId = json.optString("upiId", "")
            ).takeIf { it.panNumber.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    fun getAccounts(context: Context): List<DematAccount> {
        val set = prefs(context).getStringSet(KEY_ACCOUNTS, emptySet()) ?: emptySet()
        return set.mapNotNull { parse(it) }.sortedBy { it.name }
    }

    private fun saveAccounts(context: Context, accounts: List<DematAccount>) {
        val set = accounts.map { serialize(it) }.toSet()
        prefs(context).edit().putStringSet(KEY_ACCOUNTS, set).apply()
    }

    fun addAccount(context: Context, account: DematAccount) {
        val updated = getAccounts(context) + account
        saveAccounts(context, updated)
    }

    fun removeAccount(context: Context, accountId: String) {
        val updated = getAccounts(context).filterNot { it.id == accountId }
        saveAccounts(context, updated)
    }

    fun createAccount(name: String, panNumber: String, dpId: String, clientId: String, upiId: String): DematAccount {
        return DematAccount(
            id = UUID.randomUUID().toString(),
            name = name,
            panNumber = panNumber,
            dpId = dpId,
            clientId = clientId,
            upiId = upiId
        )
    }
}
