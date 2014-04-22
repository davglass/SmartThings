/**
 *  Hub IP Notifier
 *
 *  Author: luke
 *  Date: 2014-01-28
 */
preferences {
	page(name: "pageWithIp", title: "Hub IP Notifier", install: true)

}

def pageWithIp() {
	def currentIp = state.localip ?: 'unknown'
	def registerDate = state.lastRegister ?: null
	dynamicPage(name: "pageWithIp", title: "Hub IP Notifier", install: true, uninstall: true) {
		section("When Hub Comes Online") {
			input "hub", "hub", title: "Select a hub"
		}
		section("Last Registration Details") {
			if(hub && registerDate) {
				   paragraph """Your hub last registered with IP:
$currentIp
on:
$registerDate"""
			} else if (hub && !registerDate) {
				paragraph "Your hub has not (re)registered since you installed this app"
			} else {
				paragraph "Check back here after installing to see the current IP of your hub"
			}
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(hub, "hubInfo", registrationHandler, [filterEvents: false])
}

def registrationHandler(evt) {
	def hubInfo = evt.description.split(',').inject([:]) { map, token ->
		token.split(':').with { map[it[0].trim()] = it[1] }
		map
	}
	state.localip = hubInfo.localip
	state.lastRegister = new Date()
	sendNotificationEvent("${hub.name} registered in prod with IP: ${hubInfo.localip}")
}