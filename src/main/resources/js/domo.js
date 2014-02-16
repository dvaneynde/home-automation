/*
 * JS code for domotic system
 */

var isRefreshing = false;

function sendQuickie(name) {
	// alert("sendQuickie, name="+name);
	$.ajax("quick/" + name);
}

/**
 * element is een HTMLInputElement localhost:8080/domo/act/LichtInkom/toggle
 */
function sendToggle(element) {
	// alert("toggle(): name=" + element.name);
	if (window.isRefreshing) {
		console.log("sendToggle() " + element.name
				+ ": ignored, since we're refreshing actuators.");
		return;
	}
	$.ajax("act/" + element.name + "/toggle");
	// alert("voor disable dimmer...");
	$("#" + element.name + "_lvl").slider("option", "disabled",
			!element.checked).slider('refresh');
	console.log("sendToggle() " + element.name)
	// alert("na disable dimmer !")
}

function sendLevelDL(inputRange) {
	// alert("sendLevelDL(): send act/" + inputRange.name + "/" +
	// inputRange.value);
	if (window.isRefreshing) {
		console.log("sendLevelDL() " + inputRange.name
				+ ": ignored, since we're refreshing actuators.");
		return;
	}

	// bij toggle wordt dit opgeroepen - indirect - vanuit
	// sendToggle().slider()...
	// sliderDisabled en checkboxChecked lijken nieuwe status aan te geven
	// met [$("#" + inputRange.name).attr('checked') == "checked"] lijkt het
	// niet te werken
	// sliderDisabled = $("#" + inputRange.id).is(':disabled');
	checkboxChecked = $("#" + inputRange.name).is(':checked');
	if (checkboxChecked) {
		console.log("sendLevelDL() " + inputRange.name + ": send level="
				+ inputRange.value);
		$.ajax("act/" + inputRange.name + "/" + inputRange.value);
	} else {
		console.log("sendLevelDL() " + inputRange.name
				+ ": not sending level since checkbox un-checked");

	}
}

function refreshActuatorsTxt() {
	$.get("actuators_txt", function(data) {
		console.log("Load was performed. Data=" + data);
	});
}

function refreshActuators() {
	$.getJSON("actuators", function(data) {
		// alert("Load was performed. Data0=" + data[0].name);
		window.isRefreshing = true;
		for (i = 0; i < data.length; i++) {
			act = data[i];
			//curOn = $("#" + act.name).prop("checked");
			//act.on = !curOn;
			$("#" + act.name).prop("checked", act.on).checkboxradio("refresh");
			if (act.type === "DimmedLamp") {
				// TODO: onderstaand code triggert sendLevelDL(), maar
				// bovenstaande niet sendToggle()... Hoe komt dat, te
				// ondervangen? Wel niet altijd, sendToggle() wordt blijkbaar enkel opgeroepen bij verandering, sendLevelDL() altijd?
				$("#" + act.name + "_lvl").val(act.level);
				$("#" + act.name + "_lvl")
						.slider("option", "disabled", !act.on)
						.slider('refresh');
			}
		}
		window.isRefreshing = false;
	});
}

function autorefresh() {
	refreshActuators();
    setTimeout( autorefresh, 1000 );
}

autorefresh();

// function registerDimmedLampChangeEvents(el, timeout) {
// var timer;
// var trig = function() {
// alert("Send name=" + el.name + ", level=" + el.value);
// };
// el.bind("change", function() {
// zolang changes snel binnenkomen wordt timer reset; als ze 'timeout' lang niet
// meer binnenkomen wordt trig opgeroepen
// if (timer) {
// clearTimeout(timer);
// }
// timer = setTimeout(trig, timeout);
// });
// }
