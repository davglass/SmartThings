/**
 *  Intel Bridge Device
 *
 *  Author: SmartThings
 *  Date: 2014-04-04
 */
 // for the UI
metadata {
	definition (name: "Intel Bridge", namespace: "smartthings", author: "SmartThings") {

		command "getSubPaths"
		command "getDevices"
		command "getSubscriptions"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

    def results = []
	//def result = parent.parse(this, description)

}

// handle commands
def getSubPaths() {
	log.debug "Executing 'getSubPaths'"
	// TODO: handle 'getSubPaths' command
    get("/")
}

def getDevices() {
	log.debug "Executing 'getDevices'"
	// TODO: handle 'getDevices' command
    get("/dev")
}

def getSubscriptions() {
	log.debug "Executing 'getSubscriptions'"
	// TODO: handle 'getSubscriptions' command
	get("/sub")
}

private get(path) {

	log.debug "GET:  $path"

    def hubAction = new physicalgraph.device.HubAction(
		method: "GET",
		path: path,
		headers: [HOST:getHostAddress()]
	)
}

private put(path) {

	log.debug "PUT:  $path"

    def hubAction = new physicalgraph.device.HubAction(
		method: "PUT",
		path: path,
		headers: [HOST:getHostAddress()]
	)
}

private getHostAddress() {
	def parts = device.deviceNetworkId.split(":")
	def ip = convertHexToIP(parts[0])
	def port = convertHexToInt(parts[1])
	return ip + ":" + port
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}
