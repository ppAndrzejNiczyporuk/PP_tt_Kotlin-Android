package pl.pp.tt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage


class SmsReciver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val extras = intent.extras
        if (extras != null) {
            val sms = extras.get("pdus") as Array<Any>
            for (i in sms.indices) {
                val format = extras.getString("format")
                var smsMessage = SmsMessage.createFromPdu(sms[i] as ByteArray, format)
                val phoneNumber = smsMessage.originatingAddress
                val p = TextUtils.fetchNumber(smsMessage.messageBody.toString())  //Get data from SMS
                var parcelNum = p!!.first
                var carrierTel = p!!.second

                if (parcelNum.length > 0) {
                    val i = Intent(context, MainActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    i.putExtra("parcelNum", parcelNum)
                    if (carrierTel.length > 0) {
                        i.putExtra("carrierTel", carrierTel)
                    }
                    context.startActivity(i)
                }
            }
        }
    }
}

