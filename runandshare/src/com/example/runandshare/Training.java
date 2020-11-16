package com.example.runandshare;

public class Training {

	String trainingName;
	String followTrainingName;
	String count;
	String visits;
	String date;
	Boolean edit;
	Boolean load;
	Boolean stats;

	public Boolean getStats() {
		return stats;
	}

	public void setStats(Boolean stats) {
		this.stats = stats;
	}

	public Training() {
		this.trainingName = "";
		this.count = "";
		this.visits = "";
		this.date = "";
		this.edit = false;
		this.load = false;
		this.stats = false;
	}

	public String getFollowTrainingName() {
		return followTrainingName;
	}

	public void setFollowTrainingName(String followTrainingName) {
		this.followTrainingName = followTrainingName;
	}

	public String getVisits() {
		return visits;
	}

	public void setVisits(String visits) {
		this.visits = visits;
	}

	public String getTrainingName() {
		return trainingName;
	}

	public void setTrainingName(String trainingName) {
		this.trainingName = trainingName;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public Boolean getEdit() {
		return edit;
	}

	public void setEdit(Boolean edit) {
		this.edit = edit;
	}

	public Boolean getLoad() {
		return load;
	}

	public void setLoad(Boolean load) {
		this.load = load;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

}