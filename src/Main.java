package com.github.leonxie2003.plannerbot;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

public class Main {
	public static void main(String[] args)
	{
		DiscordApiBuilder builder = new DiscordApiBuilder();
		DiscordApi api = builder.setToken("bottoken").login().join();
		
		Plan plan = new Plan(null);
		
		api.addListener(new CommandListener(api, plan));
	}
}
