package com.prz.ai.chat;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * @author Adrian
 * 
 * Klasa odpowiedzialna za obs³ugê strony serwera. 
 * Implementacja zdarzeñ @OnOpen, @OnMessage, @OnClose i @OnError
 */
@ServerEndpoint("/chatroomserverendpoint")
public class ChatroomServerEndpoint {

	// Set zawieraj¹cy userów chatu
	static Set<Session> mChatroomUsers = Collections.synchronizedSet(new HashSet<Session>());

	@OnOpen
	public void handleOpen(Session userSession) throws IOException {
		mChatroomUsers.add(userSession);
		
		Iterator<Session> iterator = mChatroomUsers.iterator();
		while(iterator.hasNext()) 
			iterator.next().getBasicRemote().sendText(buildJsonUsersData());
	}

	/**
	 * @param userSession
	 * @param message
	 * @throws IOException
	 * 
	 */
	@OnMessage
	public void handleMessage(Session userSession, String message) throws IOException {
		String username = (String) userSession.getUserProperties().get("username");
		Iterator<Session> iterator = mChatroomUsers.iterator();
		
		if (username == null) {
			userSession.getUserProperties().put("username", message);
			userSession.getBasicRemote().sendText(buildJsonMessageData("System", "you are now connected as " + message));
			while(iterator.hasNext()) 
				iterator.next().getBasicRemote().sendText(buildJsonUsersData());
		} else {
			
			while (iterator.hasNext())
				iterator.next().getBasicRemote().sendText(buildJsonMessageData(username, message));
		}
	}

	@OnClose
	public void handleClose(Session userSession) throws IOException {
		mChatroomUsers.remove(userSession);
		
		Iterator<Session> iterator = mChatroomUsers.iterator();
		while(iterator.hasNext()) 
			iterator.next().getBasicRemote().sendText(buildJsonUsersData());
	}
		
	private String buildJsonUsersData() {
		Iterator<String> iterator = getUserNames().iterator();
		JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
		while(iterator.hasNext())
			jsonArrayBuilder.add((String)iterator.next());
		String jsonUsers = Json.createObjectBuilder().add("users", jsonArrayBuilder).build().toString(); 
		
		return jsonUsers;
	}
	
	/**
	 * @param username
	 * @param message
	 * @return
	 * Funkcja odpowiedzialna za budowanie wiadomoœci w formacie JSON.
	 */
	private String buildJsonMessageData(String username, String message) {
		JsonObject jsonObject = Json.createObjectBuilder().add("message", username + ": " + message).build();
		StringWriter stringWriter = new StringWriter();
		try (JsonWriter jsonWriter = Json.createWriter(stringWriter)) {
			jsonWriter.write(jsonObject);
		}
		return stringWriter.toString();
	}
	
	private Set<String> getUserNames() {
		HashSet<String> userNames = new HashSet<String>();
		Iterator<Session> iterator = mChatroomUsers.iterator();
		while(iterator.hasNext())
			userNames.add(iterator.next().getUserProperties().get("username").toString());
		
		return userNames;
	}
}
