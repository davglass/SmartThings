/**
 *  Intel Camera
 *
 *  Author: SmartThings
 *  Date: 2014-04-06
 */
 // for the UI
metadata {
	definition (name: "Intel Camera", namespace: "smartthings", author: "SmartThings") {
		capability "Motion Sensor"

		attribute "streams", "string"
		attribute "facedetect", "string"

		command "subscribe"
        command "getStreams"
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
	// TODO: handle 'motion' attribute
	// TODO: handle 'streams' attribute
	// TODO: handle 'facedetect' attribute

}

// handle commands
def subscribe() {
	log.debug "Executing 'subscribe'"
	// TODO: handle 'subscribe' command
	parent.post("/dev/${getId()}/streams", ["suburi":"/dev/00000000-0000-0000-0000-000000000012/facedetect", "ip":"192.168.1.22", "port":39400])
}

def getStreams() {
	parent.get("/dev/${getId()}/streams")
}

def getId() {
	return this.device.deviceNetworkId.split("/")[-1]
}