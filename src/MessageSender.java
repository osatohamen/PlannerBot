package com.github.leonxie2003.plannerbot;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;

public class MessageSender implements Runnable
{
	private String message;
	TextChannel channel;
	
	public MessageSender(String msg, TextChannel c) 
	{
		message = msg;
		channel = c;
	}
	
	@Override
	public void run() {
		channel.sendMessage(message);
	}
}
