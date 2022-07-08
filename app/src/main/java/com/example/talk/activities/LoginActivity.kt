package com.example.talk.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.talk.InternetConnectivity
import com.example.talk.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private lateinit var checkInternet: InternetConnectivity


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkInternet = InternetConnectivity()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val root = setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        supportActionBar!!.hide()


        if (auth.currentUser != null) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnGetOtp.setOnClickListener {
            onGetOtpClick()
        }

        return root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun onGetOtpClick() {
        if (checkInternet.isConnected(this)) {
//            if (isConnected()) {
            Toast.makeText(this, "Internet Connected", Toast.LENGTH_SHORT).show();
            val phoneNumber = binding.edtMobile.text.toString()
            val regexPhone = Regex("^[7689][0-9]{9}$")
            if (!phoneNumber.isEmpty() && !phoneNumber.matches(regexPhone) == false) {
                binding.edtMobile.error = null
                val intent = Intent(this@LoginActivity, OtpActivity::class.java)
                intent.putExtra("PhoneNumber", binding.edtMobile.text.toString())
                startActivity(intent)
                finish()
            } else {
                binding.edtMobile.error = "Please Enter Correct Phone Number"
            }
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
    }


//    @RequiresApi(Build.VERSION_CODES.M)
//    fun isConnected(): Boolean {
//        var connected = false
//        try {
//            val cm =
//                applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            val nInfo = cm.activeNetworkInfo
//            connected = nInfo != null && nInfo.isAvailable && nInfo.isConnected
//            return connected
//        } catch (e: Exception) {
////            Log.e("Connectivity Exception", e.message)
//        }
//        return connected
//    }


//    fun isOnline(context: Context): Boolean {
//        val connectivityManager =
//            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        if (connectivityManager != null) {
//            val capabilities =
//                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
//            if (capabilities != null) {
//                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
//                    return true
//                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
//                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
//                    return true
//                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
//                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
//                    return true
//                }
//            }
//        }
//        return false
//    }
}