package com.karthik.web.nids.servlet;

public class Customization {

	private String file_path, cvs;
	private int mae;

	// Getter Setter methods

	public void setFilePath(String file_path) {
		this.file_path = file_path;
	}

	public String getFilePath() {
		return file_path;
	}

	public void setMaxAllowedEvolutions(int mae) {
		this.mae = mae;
	}

	public int getMaxAllowedEvolutions() {
		return mae;
	}

	public void setCVSRevision(String cvs) {
		this.cvs = cvs;
	}

	public String getCVSRevision() {
		return cvs;
	}

}