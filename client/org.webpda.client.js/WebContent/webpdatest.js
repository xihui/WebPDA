
function createPV(){
	var pvName = document.getElementById("pvName").value.trim();
	var json = JSON.stringify({
		"commandName": "CreatePV",
		"pvName": pvName
	});
	sendText(json);
}


