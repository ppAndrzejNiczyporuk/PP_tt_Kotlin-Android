package pl.pp.tt

import android.util.Log
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import org.kxml2.kdom.Element
import org.kxml2.kdom.Node

/**
 * Object/singleton class that consists of utility helper methods for making a request for
 * extracting data (event) from an XML response of a SOAP web service.
 */
object QueryUtils {

    // Log tag constant.
    private val LOG_TAG = QueryUtils::class.java.simpleName

    // Elements of a SOAP envelop body.
    private val NAMESPACE = "http://sledzenie.pocztapolska.pl"
    private val METHOD_NAME = "sprawdzPrzesylke" //"wersja"

    // SOAP Action header field URI consisting of the namespace and method that's used to make a
    // call to the web service.
    private val SOAP_ACTION = NAMESPACE + "/" + METHOD_NAME

    // Web service URL (that should be openable) along with the Web Service Definition Language
    // (WSDL) that's used to view the WSDL file by simply adding "?WSDL" to the end of the URL.
    private val URL = "https://tt.poczta-polska.pl/Sledzenie/services/Sledzenie?WSDL"

    /**
     * Helper method that requests response data from a web service, and later returns a list of
     * data (event).
     */
    fun fetchEventData(userInput: String): List<PPEvent>? {

        // A simple dynamic object that can be used to build SOAP calls without
        // implementing KvmSerializable. Essentially, this is what goes inside the body of
        // a SOAP envelope - it is the direct subelement of the body and all further sub
        // elements. Instead of this class, custom classes can be used if they implement
        // the KvmSerializable interface.
        val request = SoapObject(NAMESPACE, METHOD_NAME)

        // The following adds a parameter (parameter name, user inputted value).
        request.addProperty("numer", userInput)

        // Declares the version of the SOAP request.
        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)

        //Create Header for autorization
        envelope.headerOut = buildAuthHeader()

        // Set the following variable to true for compatibility with what seems to be the
        // default encoding for .Net-Services.
        envelope.dotNet = false

        // Assigns the SoapObject to the envelope as the outbound message for the SOAP call.
        envelope.setOutputSoapObject(request)

        // A J2SE based HttpTransport layer instantiation of the web service URL and the
        // WSDL file.
        val httpTransport = HttpTransportSE(URL)
        try {

            // This is the actual part that will call the webservice by setting the desired
            // header SOAP Action header field.
            httpTransport.call(SOAP_ACTION, envelope)
            // val resultString = envelope.response.toString()
            //  Log.e(LOG_TAG, resultString.toString())

            //Get Status and if =0 show list of PPEvent else Show Toast
            val obj1 = envelope.response as SoapObject
            val obj2 = if (obj1.propertyCount == 3) obj1.getProperty(2) else null

            if (obj2.toString() == "0") {
                // Status Ok ->Return a list of data (PPEvent ) after extracting data from the response.
                val obj4 = obj1.getProperty(0) as SoapObject
                val obj5 = obj4.getProperty(10).toString() // rodzPrzes
                return extractDataFromResponse(obj1.getProperty(0) as SoapObject)
            }
            else {
                //Toast.makeText(applicationContext, "Błedny numer", Toast.LENGTH_SHORT).show()
                return null
            }
            //return extractDataFromXmlResponse(envelope)
        } catch (e: Exception) { // Many kinds of exceptions can be caught here
            Log.e(LOG_TAG, e.toString())
        }

        // Otherwise, returns null.
        return null
    }

    /**
     * Extracts data (PPEvent) from the response, and ultimately returns a list of PPEvent.
     */
    @Throws(Exception::class)
    private fun extractDataFromResponse(obj: SoapObject): List<PPEvent>? {
        val eventList = mutableListOf<PPEvent>()
        val obj1 = obj.getProperty("zdarzenia") as SoapObject //zadarzenia
        for (i in 0 until obj1.propertyCount) {
            val obj2 = obj1.getProperty(i) as SoapObject //zdarzenie
            val obj3 = obj2.getProperty("jednostka") as SoapObject //jednostka
            val ppEv = PPEvent(obj2.getProperty("nazwa").toString(), czas = obj2.getProperty("czas").toString(), jnazwa = obj3.getProperty("nazwa").toString())
            eventList.add(ppEv)
        }
        return eventList.asReversed()
    }

    fun buildAuthHeader(): Array<Element?> {
        val headers = arrayOfNulls<Element>(1)
        headers[0] = Element().createElement("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security")
        headers[0]?.setAttribute(null, "mustUnderstand", "1")
        val security = headers[0]

        //user token
        val usernametoken = Element().createElement(security?.namespace, "UsernameToken")
        usernametoken.setAttribute("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd", "Id", "UsernameToken-2")

        //username
        val username = Element().createElement(security?.namespace, "Username")
        username.addChild(Node.TEXT, "sledzeniepp")
        usernametoken.addChild(Node.ELEMENT, username)

        // password
        val password = Element().createElement(security?.namespace, "Password")
        password.setAttribute(null, "Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText")
        password.addChild(Node.TEXT, "PPSA")
        usernametoken.addChild(Node.ELEMENT, password)

        //Nonce
        val nonce = Element().createElement(security?.namespace, "Nonce")
        nonce.setAttribute(null, "EncodingType", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary")
        nonce.addChild(Node.TEXT, "X41PkdzntfgpowZsKegMFg==")
        usernametoken.addChild(Node.ELEMENT, nonce)

        headers[0]?.addChild(Node.ELEMENT, usernametoken)

        return headers
    }
}

/**
 * PPEvent to structura to load data for ListView
 */
class PPEvent(nazwa: String, czas: String, jnazwa: String) {
    val czas: String = czas
    val nazwa: String = nazwa
    val jednostkaNazwa: String = jnazwa

}
