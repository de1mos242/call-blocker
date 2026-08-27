package com.de1mos.callblocker

import android.Manifest
import android.app.role.RoleManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowInsets
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit

class MainActivity : ComponentActivity() {
    private lateinit var roleManager: RoleManager
    private lateinit var whitelistStore: WhitelistStore
    private lateinit var roleStatus: TextView
    private lateinit var contactsStatus: TextView
    private lateinit var roleButton: Button
    private lateinit var contactsButton: Button
    private lateinit var numberInput: EditText
    private lateinit var numberList: LinearLayout

    private val roleRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        updateStatus()
        if (isScreeningRoleHeld()) requestContactsAccess()
    }

    private val contactsRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        updateStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.root).setOnApplyWindowInsetsListener { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsets.Type.systemBars() or
                    WindowInsets.Type.displayCutout() or
                    WindowInsets.Type.ime(),
            )
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }

        roleManager = getSystemService(RoleManager::class.java)
        whitelistStore = WhitelistStore(this)
        roleStatus = findViewById(R.id.role_status)
        contactsStatus = findViewById(R.id.contacts_status)
        roleButton = findViewById(R.id.role_button)
        contactsButton = findViewById(R.id.contacts_button)
        numberInput = findViewById(R.id.number_input)
        numberList = findViewById(R.id.number_list)

        roleButton.setOnClickListener { requestScreeningRole() }
        contactsButton.setOnClickListener { requestContactsAccess() }
        findViewById<Button>(R.id.add_button).setOnClickListener { addNumber() }
        numberInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_DONE) return@setOnEditorActionListener false
            addNumber()
            true
        }

        updateStatus()
        renderNumbers()
        promptForInitialSetup()
    }

    override fun onResume() {
        super.onResume()
        if (::roleManager.isInitialized) updateStatus()
    }

    private fun promptForInitialSetup() {
        val setupPreferences = getSharedPreferences(PREFERENCES_SETUP, MODE_PRIVATE)
        if (
            setupPreferences.getBoolean(KEY_SETUP_PROMPTED, false) ||
            !roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)
        ) {
            return
        }
        setupPreferences.edit { putBoolean(KEY_SETUP_PROMPTED, true) }

        if (!isScreeningRoleHeld()) {
            requestScreeningRole()
        } else {
            requestContactsAccess()
        }
    }

    private fun requestScreeningRole() {
        if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            roleRequest.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
        }
    }

    private fun requestContactsAccess() {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            contactsRequest.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun updateStatus() {
        val roleAvailable = roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)
        val roleHeld = roleAvailable && isScreeningRoleHeld()
        roleStatus.setText(
            when {
                !roleAvailable -> R.string.role_unavailable
                roleHeld -> R.string.role_active
                else -> R.string.role_inactive
            },
        )
        roleButton.visibility = if (roleAvailable && !roleHeld) View.VISIBLE else View.GONE

        val contactsGranted =
            checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        contactsStatus.setText(
            if (contactsGranted) R.string.contacts_granted else R.string.contacts_missing,
        )
        contactsStatus.visibility = if (roleHeld) View.VISIBLE else View.GONE
        contactsButton.visibility = if (roleHeld && !contactsGranted) View.VISIBLE else View.GONE
    }

    private fun isScreeningRoleHeld(): Boolean =
        roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)

    private fun addNumber() {
        val rawNumber = numberInput.text.toString()
        if (!whitelistStore.add(rawNumber)) {
            numberInput.error = getString(R.string.invalid_number)
            return
        }
        numberInput.text.clear()
        renderNumbers()
    }

    private fun renderNumbers() {
        numberList.removeAllViews()
        val numbers = whitelistStore.numbers()
        findViewById<TextView>(R.id.empty_message).visibility =
            if (numbers.isEmpty()) View.VISIBLE else View.GONE

        numbers.forEach { number ->
            val row = layoutInflater.inflate(R.layout.whitelist_row, numberList, false)
            row.findViewById<TextView>(R.id.allowed_number).text = number
            row.findViewById<Button>(R.id.remove_button).apply {
                contentDescription = getString(R.string.remove_number_description, number)
                setOnClickListener {
                    whitelistStore.remove(number)
                    renderNumbers()
                }
            }
            numberList.addView(row)
        }
    }

    private companion object {
        const val PREFERENCES_SETUP = "setup"
        const val KEY_SETUP_PROMPTED = "prompted"
    }
}
