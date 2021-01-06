package com.masuwes.googlecodelabs

import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.masuwes.googlecodelabs.databinding.ActivityMainBinding
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE = 1210
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var executor: Executor
    private lateinit var biometricManager: BiometricManager
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var biometricCallback: BiometricPrompt.AuthenticationCallback
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        biometricManager = BiometricManager.from(this)

        // verify that fingerprints feature is available in device
        verifyingBiometricExistence()

        executor = ContextCompat.getMainExecutor(this)
        biometricCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.e("Auth", "onAuthenticationSucceeded Called")
                binding.txtResult.text = resources.getString(R.string.biometric_success)
            }
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                binding.txtResult.text = resources.getString(R.string.biometric_failed)
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.e("Auth", "onAuthenticationError Called")
                binding.txtResult.text = resources.getString(R.string.biometric_error, errString)
            }
        }

        promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                // Allow device PIN/PASSWORD
                .setDeviceCredentialAllowed(true)
                .build()

        binding.btnShowDialog.setOnClickListener {
            startBiometricDialog()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            startBiometricDialog()
        }
    }

    private fun startBiometricDialog() {
        biometricPrompt = BiometricPrompt(this, executor, biometricCallback)
        biometricPrompt.authenticate(promptInfo)
    }

    private fun verifyingBiometricExistence() {
        when(biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                "App can authenticate using biometrics.".showToast()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                "No biometric features available on this device.".showToast()
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                "Biometric features are currently unavailable.".showToast()
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                "The user hasn't associated any biometric credentials with their account.".showToast()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                        )
                    }
                    startActivityForResult(enrollIntent, REQUEST_CODE)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val enrollIntent = Intent(Settings.ACTION_FINGERPRINT_ENROLL)
                    startActivityForResult(enrollIntent, REQUEST_CODE)
                } else {
                    "No FINGERPRINT in Device".showToast()
                }
        }
    }
}

    private fun String.showToast() {
        Toast.makeText(applicationContext, this, Toast.LENGTH_SHORT).show()
    }
}



















