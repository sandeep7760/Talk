package com.example.talk

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.talk.databinding.ActivityOtpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OtpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityOtpBinding
    private lateinit var checkInternet: InternetConnectivity
    private val profile:Boolean = true

    var verificationId: String? = null

    var dialog: ProgressDialog? = null

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkInternet = InternetConnectivity()
        binding = ActivityOtpBinding.inflate(layoutInflater)
        val root = setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        supportActionBar!!.hide()

        dialog = ProgressDialog(this)
        dialog!!.setMessage("Sending OTP...")
        dialog!!.setCancelable(false)
        dialog!!.show()

        val phoneNumber = intent.getStringExtra("PhoneNumber")

        binding.phoneLbl.text = "Verify +91 $phoneNumber"


        try {

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91$phoneNumber")
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    override fun onCodeSent(
                        verifyId: String,
                        p1: PhoneAuthProvider.ForceResendingToken
                    ) {
                        Toast.makeText(
                            this@OtpActivity,
                            "OTP sent successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        super.onCodeSent(verifyId, p1)
                        dialog!!.dismiss()
                        verificationId = verifyId

                    }

                    override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                        val code: String? = phoneAuthCredential.smsCode
                        Toast.makeText(
                            this@OtpActivity,
                            "on Verification complete",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (code != null) {
                            verifyCode(code)
                        }
                    }

                    override fun onVerificationFailed(e: FirebaseException) {

                        if (checkInternet.isConnected(this@OtpActivity)) {
                            Toast.makeText(
                                this@OtpActivity,
                                "Verification Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog!!.dismiss()
                            val intent = Intent(this@OtpActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        } else {
                            Toast.makeText(
                                this@OtpActivity,
                                "No Internet Connection",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog!!.dismiss()
                            val intent = Intent(this@OtpActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finishAffinity()
                        }


                    }
                })
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            dialog!!.dismiss()
            Toast.makeText(this@OtpActivity, "Firebase Error", Toast.LENGTH_LONG).show()
        }



        binding.otpView.setOtpCompletionListener { otp ->
            binding.btnGetOtp.text = "VERIFYING..."
            val credential = PhoneAuthProvider.getCredential(
                verificationId!!,
                otp!!
            )

            if (checkInternet.isConnected(this)) {
                try {

                    auth.signInWithCredential(credential).addOnCompleteListener { task ->

                        if (task.isSuccessful) {
                            val intent = Intent(this@OtpActivity, ProfileActivity::class.java)

                            startActivity(intent)
                            finishAffinity()
                        } else {
                            Toast.makeText(
                                this@OtpActivity,
                                "Please enter Correct OTP.",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.btnGetOtp.text = "VERIFY"
                        }
                    }
                } catch (e: Exception) {
                    TODO("$e")
                }

            } else {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                binding.otpView.text = null
                binding.btnGetOtp.text = "VERIFY"
            }

        }

//        binding.btnGetOtp.setOnClickListener {
//
//            if (TextUtils.isEmpty(binding.otpView.text.toString())) {
//                Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT)
//                    .show()
//                verifyCode(binding.otpView.text.toString())
//            }
//
//        }


        return root

    }

    private fun verifyCode(code: String) {
        val credential: PhoneAuthCredential? =
            verificationId?.let { PhoneAuthProvider.getCredential(it, code) };
        if (credential != null) {

            signInWithCredential(credential)
        }


    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val i = Intent(this@OtpActivity, MainActivity::class.java)
                    startActivity(i)
                    finish()
                } else {

                    Toast.makeText(this@OtpActivity, "${task.exception}", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    override fun onBackPressed() {
        val i = 2
        if (i == 1) {
            super.onBackPressed()
        } else {
            Toast.makeText(this, "back Pressed", Toast.LENGTH_LONG).show()
        }
    }
}