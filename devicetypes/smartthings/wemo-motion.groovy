/**
 *  Wemo Motion
 *
 *  Author: superuser
 *  Date: 2013-10-11
 */
 // for the UI
metadata {
	// Automatically generated. Make future change here.
	definition (name: "Wemo Motion", namespace: "smartthings", author: "SmartThings") {
		capability "Motion Sensor"
		capability "Refresh"
		capability "Sensor"

		command "subscribe"
		command "resubscribe"
		command "unsubscribe"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	// UI tile definitions
	tiles {
		standardTile("motion", "device.motion", width: 2, height: 2) {
			state("active", label:'motion', icon:"st.motion.motion.active", backgroundColor:"#53a7c0")
			state("inactive", label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff")
		}
		standardTile("refresh", "device.motion", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "motion"
		details (["motion", "refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

	def map = stringToMap(description)
	def headerString = new String(map.headers.decodeBase64())
	def result = []

	if (headerString.contains("SID: uuid:")) {
		def sid = (headerString =~ /SID: uuid:.*/) ? ( headerString =~ /SID: uuid:.*/)[0] : "0"

		sid -= "SID: uuid:".trim()
		log.debug "FOUND sid: ${sid}"

		updateDataValue("subscriptionId", sid)
	}

	if (map.body) {

		def bodyString = new String(map.body.decodeBase64())
		def body = new XmlSlurper().parseText(bodyString)

		if (body?.property?.TimeSyncRequest?.text())
		{
			log.debug "Got TimeSyncRequest"
		   result << timeSyncResponse()
		}
		else if (body?.Body?.SetBinaryStateResponse?.BinaryState?.text())
		{
			log.debug "Got SetBinaryStateResponse = ${body?.Body?.SetBinaryStateResponse?.BinaryState?.text()}"
		}
		else if (body?.property?.BinaryState?.text())
		{
			def value = body?.property?.BinaryState?.text().toInteger() == 1 ? "active" : "inactive"
			log.debug "NOTIFY - BinaryState = ${value}"
			result << createEvent(name: "motion", value: value)
		}
		else if (body?.property?.TimeZoneNotification?.text())
		{
			log.debug "NOTIFY - TimeZoneNotification = ${body?.property?.TimeZoneNotification?.text()}"
		}

	}

	result
}

////////////////////////////
private getTime() {
	// This is essentially System.currentTimeMillis()/1000, but System is disallowed by the sandbox.
	((new GregorianCalendar().time.time / 1000l).toInteger()).toString()
}

private getCallBackAddress() {
	device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
	def parts = device.deviceNetworkId.split(":")
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}



////////////////////////////
def refresh() {
	log.debug "Executing WeMo Motion 'subscribe', then 'timeSyncResponse', then 'getStatus'"
	[subscribe(), timeSyncResponse(), getStatus()]
}

////////////////////////////
def getStatus() {
log.debug "Executing WeMo Motion 'getStatus'"
new physicalgraph.device.HubAction("""POST /upnp/control/basicevent1 HTTP/1.1
SOAPACTION: "urn:Belkin:service:basicevent:1#GetBinaryState"
Content-Length: 277
Content-Type: text/xml; charset="utf-8"
HOST: ${getHostAddress()}
User-Agent: CyberGarage-HTTP/1.0

<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<s:Body>
<u:GetBinaryState xmlns:u="urn:Belkin:service:basicevent:1">
</u:GetBinaryState>
</s:Body>
</s:Envelope>""", physicalgraph.device.Protocol.LAN)
}

////////////////////////////
def subscribe() {
log.debug "Executing 'subscribe()'"
def address = getCallBackAddress()
new physicalgraph.device.HubAction("""SUBSCRIBE /upnp/event/basicevent1 HTTP/1.1
HOST: ${getHostAddress()}
CALLBACK: <http://${address}/>
NT: upnp:event
TIMEOUT: Second-4200
User-Agent: CyberGarage-HTTP/1.0


""", physicalgraph.device.Protocol.LAN)
}

////////////////////////////
def resubscribe() {
log.debug "Executing 'resubscribe()'"

def sid = getDeviceDataByName("subscriptionId")

new physicalgraph.device.HubAction("""SUBSCRIBE /upnp/event/basicevent1 HTTP/1.1
HOST: ${getHostAddress()}
SID: uuid:${sid}
TIMEOUT: Second-4200


""", physicalgraph.device.Protocol.LAN)

}

////////////////////////////
def unsubscribe() {
def sid = getDeviceDataByName("subscriptionId")
new physicalgraph.device.HubAction("""UNSUBSCRIBE publisher path HTTP/1.1
HOST: ${getHostAddress()}
SID: uuid:${sid}


""", physicalgraph.device.Protocol.LAN)
}

////////////////////////////
//TODO: Use UTC Timezone
def timeSyncResponse() {
log.debug "Executing 'timeSyncResponse()'"
new physicalgraph.device.HubAction("""POST /upnp/control/timesync1 HTTP/1.1
Content-Type: text/xml; charset="utf-8"
SOAPACTION: "urn:Belkin:service:timesync:1#TimeSync"
Content-Length: 376
HOST: ${getHostAddress()}
User-Agent: CyberGarage-HTTP/1.0

<?xml version="1.0" encoding="utf-8"?>
<s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
 <s:Body>
  <u:TimeSync xmlns:u="urn:Belkin:service:timesync:1">
   <UTC>${getTime()}</UTC>
   <TimeZone>-05.00</TimeZone>
   <dst>1</dst>
   <DstSupported>1</DstSupported>
  </u:TimeSync>
 </s:Body>
</s:Envelope>
""", physicalgraph.device.Protocol.LAN)
}