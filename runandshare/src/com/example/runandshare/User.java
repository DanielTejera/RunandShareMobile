package com.example.runandshare;

public class User {

	String userName;
	String password;
	String count;
	String idAndroid;
	String active;
	String actualTraining;
	String followedTraining;

	public User() {
		this.userName = "";
		this.password = "";
		this.count = "0";
		this.idAndroid = "";
		this.active = "";
		this.actualTraining = "";
		this.followedTraining = "";

	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getIDAndroid() {
		return idAndroid;
	}

	public void setIDAndroid(String idAndroid) {
		this.idAndroid = idAndroid;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getActualTraining() {
		return actualTraining;
	}

	public void setActualTraining(String actualTraining) {
		this.actualTraining = actualTraining;
	}

	public String getFollowedTraining() {
		return followedTraining;
	}

	public void setFollowedTraining(String followedTraining) {
		this.followedTraining = followedTraining;
	}

}