/**
 *  Intel Switch
 *
 *  Author: SmartThings
 *  Date: 2014-04-06
 */
 // for the UI
metadata {
	definition (name: "Intel Switch", namespace: "smartthings", author: "SmartThings") {
		capability "Switch"
        capability "Refresh"

		command "subscribe"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
		}

        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}


        main "switch"
        details(["switch","refresh"])
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'switch' attribute

}

// handle commands
def on() {
	log.debug "Executing 'on'"
	// TODO: handle 'on' command
   	parent.put("/dev/${getId()}/switch", ["switch":"on"])
}

def off() {
	log.debug "Executing 'off'"
	// TODO: handle 'off' command
    parent.put("/dev/${getId()}/switch", ["switch":"off"])
}

def subscribe() {
	log.debug "Executing 'subscribe'"
	// TODO: handle 'subscribe' command
	parent.post("/dev/${getId()}/switch", ["suburi":"/dev/00000000-0000-0000-0000-000000000013/switch", "ip":"192.168.1.22", "port":39400])
}

def refresh() {
	log.debug "Executing 'refresh'"
	parent.get("/dev/${getId()}/switch")
}

def getId() {
	return this.device.deviceNetworkId.split("/")[-1]
}
