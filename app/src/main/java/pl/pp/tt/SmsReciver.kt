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
                val messageText = smsMessage.messageBody.toString()
                val textpos1 = "Twoja przesylke nr"
                val textpos2 = "https://emonitoring.poczta-polska.pl/?numer="

                //TODO kontrola długości SMS
                /*
                "W dn. 2019-06-03 kurier Pocztex bedzie probowal doreczyc Twoja przesylke nr 00459007738543538511. Kontakt do kuriera: tel. 887603226."

                "W dn. 2019-06-13 zostala do Ciebie nadana przesylka kurierska Pocztex   https://emonitoring.poczta-polska.pl/?numer=00459007738545307917"
                 */
                val pos1 = messageText.indexOf(textpos1)
                val pos2 = messageText.indexOf(textpos2)
                var parcelNum = ""
                var carrierTel = ""

                if (pos1 > 0) {
                    val pos12 = messageText.indexOf(". Kontakt")
                    parcelNum = messageText.substring(pos1+textpos1.length, pos12).trim()
                    carrierTel = messageText.substring(messageText.length - 10, messageText.length - 1).trim()
                } else if (pos2 > 0) {
                    //Toast.makeText(context, " ${messageText.substring(pos2+textpos2.length)}", Toast.LENGTH_LONG).show()
                    parcelNum = messageText.substring(pos2 + textpos2.length).trim()
                }
                if (parcelNum.length > 0) {
                    val i = Intent(context, MainActivity::class.java)
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    if (parcelNum.length > 0) {
                        i.putExtra("parcelNum", parcelNum)
                    }
                    if (carrierTel.length > 0) {
                        i.putExtra("carrierTel", carrierTel)
                    }
                    context.startActivity(i)
                }
            }
        }
    }
}

