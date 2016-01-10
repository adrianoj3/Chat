
/**
 * Otwarcie po��czenia.
 */
var webSocket = new WebSocket("ws://localhost:8080/WebSocketChatNTP/chatroomserverendpoint");

window.onload = function() {
	// Hide chat content
	document.getElementById('Content').style.display='none'; 
	usersList.style.color = "orange";
};

/**
 * Obs�uga wydarze� powi�zanych z webSocketem.
 */

webSocket.onmessage = function processMessage(message) {
	var jsonData = JSON.parse(message.data);
	if (jsonData.message != null) 
		messagesTextArea.value += jsonData.message + "\n";
	
	if(jsonData.users != null) {
		usersList.value = "";
		var i = 0;
		while(i < jsonData.users.length)
			usersList.value += jsonData.users[i++] + "\n";
	}
};

window.onbeforeunload = function() {
	webSocket.onclose = function exit() {
		messagesTextArea.value = "";
	};
	webSocket.close();
};


/**
 * Wysya�anie wiadomo�ci na serwer. 
 */
function sendMessage() {
	if(messageText.value != "")
		webSocket.send(messageText.value);
	messageText.value = "";
}


/**
 * Ustawianie swojego nicku. 
 */
function doLogin() {
	if(uName.value == "") {
		alert("Musisz podac jakies imie");
		contiune;
	}
	else
		webSocket.send(uName.value);
	uName.value = "";
	document.getElementById('Content').style.display='block';
	document.getElementById('loginContent').style.display='none';
}
			
/**
 * Wysya�anie wiadomo�ci przez naci�ni�cie klawisza Enter.
 */
function onEnter(event) {
	var id = event.target.id;	
	if(event.keyCode == 13) {		
		if(id == "messageText") 
			sendMessage();
		else
			doLogin();
	}
}