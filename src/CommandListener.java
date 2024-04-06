package com.github.leonxie2003.plannerbot;

import java.util.StringTokenizer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class CommandListener implements MessageCreateListener {
	private DiscordApi api;
	private Plan plan;
	private ScheduledExecutorService scheduler;
	
	public CommandListener(DiscordApi api, Plan p) {
		this.api = api;
		plan = p;
	}
	
	@Override
	public void onMessageCreate(MessageCreateEvent event) {
		String message = event.getMessageContent();
		StringTokenizer st = new StringTokenizer(message);
		String command = st.nextToken();
		TextChannel channel = event.getChannel();
		
		if (command.equals("!ping")) {
			channel.sendMessage("pong!");
	    } else if (command.equals("!newplan")) {
			executeNewPlan(plan, channel, event.getMessageAuthor());
		} else if (command.equals("!add")) { 
			executeAdd(plan, channel, st);
		} else if (command.equals("!plan")) {
			executePlan(plan, channel);
		} else if (command.equals("!addbreak")) {
			executeAddBreak(plan, channel, st);
		} else if (command.equals("!start")) {
			scheduler = executeStart(api, plan, channel);
		} else if (command.equals("!undo")) {
			executeUndo(plan, channel);
		} else if (command.equals("!end")) {
			executeEnd(plan, channel, scheduler);
		} else if (command.equals("!clear")) {
			executeClear(plan, channel);
		} else if (command.equals("!help")) {
			executeHelp(channel);
		}
	}
	
	private static void executeNewPlan(Plan plan, TextChannel channel, MessageAuthor ma) {
		plan.reset();
		plan.changeUser(ma.asUser().get());
		channel.sendMessage("Creating new plan for " + ma.asUser().get().getMentionTag() + "...");
		executePlan(plan, channel);
	}
	
	private static void executeAdd(Plan plan, TextChannel channel, StringTokenizer st) {
		String name = st.nextToken();
		String t = st.nextToken();
		try {
			double time = Double.parseDouble(t);
			plan.addTask(name, time);
			channel.sendMessage("Adding " + name + ": " + time + " minutes");
		} catch(NumberFormatException e) {
			channel.sendMessage("Please enter a number for the time in minutes.");
		}
	}
	
	private static void executePlan(Plan plan, TextChannel channel) {
		if(plan.getUser() == null) {
			channel.sendMessage("Please create a new plan with !newplan.");
		} else {
			MessageBuilder mb = new MessageBuilder();
			mb.setEmbed(new EmbedBuilder()
					.setTitle("Plan")
					.setDescription(plan.toString()));
			mb.send(channel);	
		}
	}
	
	private static void executeAddBreak(Plan plan, TextChannel channel, StringTokenizer st) {
		String t = st.nextToken();
		try {
			double time = Double.parseDouble(t);
			plan.addBreak(time);
			channel.sendMessage("Adding a break lasting " + time + " minutes.");
		} catch(NumberFormatException e) {
			channel.sendMessage("Please enter a number for the time in minutes.");
		}
	}

	private static ScheduledExecutorService executeStart(DiscordApi api, Plan plan, TextChannel channel) {
		ScheduledExecutorService scheduler = api.getThreadPool().getScheduler();
		long timeElapsed = 0;
		for(int i = 0; i < plan.taskListSize(); i++) {
			Task task = plan.getTask(i);
			String name = task.getName();
			long time = (long) (task.getTime() * 60);
			scheduler.schedule(new MessageSender(plan.getUser().getMentionTag() + ": " + " Start working on " + name + " now!", channel), timeElapsed, TimeUnit.SECONDS);
			scheduler.schedule(new MessageSender(plan.getUser().getMentionTag() + ": You should still be working on " + name + "!", channel), time/2 + timeElapsed, TimeUnit.SECONDS);
			scheduler.schedule(new MessageSender(plan.getUser().getMentionTag()+ ": " + name + " is complete!", channel), time + timeElapsed, TimeUnit.SECONDS);
			timeElapsed += time;
		}
		scheduler.schedule(new MessageSender(plan.getUser().getMentionTag()+ ": Your plan is completed! Good job!", channel), timeElapsed, TimeUnit.SECONDS);
		return scheduler;
	}
	
	private static void executeUndo(Plan plan, TextChannel channel) {
		plan.removeTask(plan.getTask(plan.taskListSize() - 1).getName());
		channel.sendMessage("Undo completed!");
	}
	
	private static void executeEnd(Plan plan, TextChannel channel, ScheduledExecutorService scheduler) {
		if (scheduler != null) {
			scheduler.shutdown();	
		}
		plan.reset();
		channel.sendMessage("Your plan ended early!");
	}
	
	private static void executeClear(Plan plan, TextChannel channel) {
		plan.reset();
		channel.sendMessage("Cleared plan!");
		executePlan(plan, channel);
	}

	private static void executeHelp(TextChannel channel) {
		MessageBuilder mb = new MessageBuilder();
		mb.setEmbed(new EmbedBuilder()
				.setTitle("Help")
				.setDescription("!newplan - Clears the current plan and creates a new one. \n" + 
						"!plan - View the current plan. \n" + 
						"!add task time - Add a new task to the current plan. \n" +
						"!addbreak time - Add a break to the current plan. \n" + 
						"!undo - Removes most recent task or break added. \n" + 
						"!clear - Completely clear the current plan. \n" + 
						"!remove task - Remove a specific task from the current plan. \n" +
						"!start - Start the current plan. \n" + 
						"!end - End the current plan early."));
		mb.send(channel);
	}
}
