package com.github.leonxie2003.plannerbot;

public class Task {
	private String name;
	private double time;
	
	public Task(String n, double t) {
		name = n;
		time = t;
	}
	
	public String getName() {
		return name;
	}
	
	public double getTime() {
		return time;
	}
}
