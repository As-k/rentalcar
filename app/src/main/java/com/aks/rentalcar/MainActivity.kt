package com.aks.rentalcar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aks.rentalcar.ui.theme.RentalCarTheme
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        enableEdgeToEdge()
        setContent {
            RentalCarTheme {
                MainPage()
            }
        }

        //Creating a local share preference
        RentalCarFirebaseMessagingService.sharedPref =
            getSharedPreferences("sharedPref", Context.MODE_PRIVATE)

        //Retrieve the refresh FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            RentalCarFirebaseMessagingService.token = token

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.i(TAG, msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()

        })
    }

    // Notification Permission required above 'TIRAMISU' version
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    0
                )
            }
        }
    }

    @Composable
    fun MainPage() {
        // Create two mutable state for remember radio button state.
        val topicCarASelected = remember { mutableStateOf(false) }
        val topicCarBSelected = remember { mutableStateOf(false) }

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 30.dp, end = 30.dp)
        ) {
            val (headLine, radioBtnCarA, radioTextCarA, radioBtnCarB, radioTextCarB) = createRefs()

            Text(text = "Select the Rental Car", fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black, modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(headLine) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top, margin = 100.dp)
                    })

            RadioButton(selected = topicCarASelected.value,
                onClick = {
                    topicCarASelected.value = !topicCarASelected.value
                    // Subscribe the client app to topic for "CarA"
                    // Now addOnCompleteListener will help that topic
                    // which we have subscribe it successful or failed.
                    if (topicCarASelected.value) {
                        Firebase.messaging.subscribeToTopic("CarA")
                            .addOnCompleteListener { taskResult ->
                                if (taskResult.isSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Car A subscribe successfully.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    // if it failed then we have revert back state of radio button to its previous state.
                                    // means unselected.
                                    topicCarASelected.value = false
                                    Toast.makeText(
                                        applicationContext,
                                        "Car A subscribe failed.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    } else {
                        // Unsubscribe the client app to topic for "CarA"
                        Firebase.messaging.unsubscribeFromTopic("CarA")
                            .addOnCompleteListener { taskResult ->
                                if (taskResult.isSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Car A unsubscribe successfully.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    topicCarASelected.value = true
                                    Toast.makeText(
                                        applicationContext,
                                        "Car A unsubscribe failed.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                },
                modifier = Modifier
                    .constrainAs(radioBtnCarA) {
                        start.linkTo(parent.start)
                        top.linkTo(headLine.bottom, margin = 50.dp)
                    })


            // Fix Alignment for 'Car A' text field.
            Text(
                text = "Car A",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(radioTextCarA) {
                        top.linkTo(radioBtnCarA.top)
                        bottom.linkTo(radioBtnCarA.bottom)
                        start.linkTo(radioBtnCarA.end, margin = 2.dp)
                    }
            )

            RadioButton(selected = topicCarBSelected.value,
                onClick = {
                    topicCarBSelected.value = !topicCarBSelected.value
                    // Subscribe the client app to topic for "CarB"
                    // Now addOnCompleteListener will help that topic
                    // which we have subscribe it successful or failed.
                    if (topicCarBSelected.value) {
                        Firebase.messaging.subscribeToTopic("CarB")
                            .addOnCompleteListener { taskResult ->
                                if (taskResult.isSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Car B subscribe successfully.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    // if it failed then we have revert back state of radio button to its previous state.
                                    // means unselected.
                                    topicCarBSelected.value = false
                                    Toast.makeText(
                                        applicationContext,
                                        "Car B subscribe failed.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    } else {
                        // Unsubscribe the client app to topic for "CarB"
                        Firebase.messaging.unsubscribeFromTopic("CarB")
                            .addOnCompleteListener { taskResult ->
                                if (taskResult.isSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Car B unsubscribe successfully.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    topicCarBSelected.value = true
                                    Toast.makeText(
                                        applicationContext,
                                        "Car B unsubscribe failed.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    }
                },
                modifier = Modifier
                    .constrainAs(radioBtnCarB) {
                        start.linkTo(parent.start)
                        top.linkTo(radioBtnCarA.bottom, margin = 16.dp)
                    })

            // Fix Alignment for 'Car B' text field.
            Text(
                text = "Car B",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(radioTextCarB) {
                        top.linkTo(radioBtnCarB.top)
                        bottom.linkTo(radioBtnCarB.bottom)
                        start.linkTo(radioBtnCarB.end, margin = 2.dp)
                    }
            )
        }
    }
}


