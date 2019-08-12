package com.examole.spring.source.bean;

public class Student {
	private String name;
	private String adress;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAdress() {
		return adress;
	}
	public void setAdress(String adress) {
		this.adress = adress;
	}
	@Override
	public String toString() {
		return "Student [name=" + name + ", adress=" + adress + "]";
	}
}
