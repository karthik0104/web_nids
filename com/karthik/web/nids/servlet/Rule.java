package com.karthik.web.nids.servlet;

public class Rule {

	private int src_bytes_lower;
	private int src_bytes_upper;
	private int dst_bytes_lower;
	private int dst_bytes_upper;
	private int count_lower;
	private int count_upper;
	private int srv_count_lower;
	private int srv_count_upper;
	private double fitness;

	public void setRule(int src_bytes_lower, int src_bytes_upper,
			int dst_bytes_lower, int dst_bytes_upper, int count_lower,
			int count_upper, int srv_count_lower, int srv_count_upper,
			double fitness) {

		this.src_bytes_lower = src_bytes_lower;
		this.src_bytes_upper = src_bytes_upper;
		this.dst_bytes_lower = dst_bytes_lower;
		this.dst_bytes_upper = dst_bytes_upper;
		this.count_lower = count_lower;
		this.count_upper = count_upper;
		this.srv_count_lower = srv_count_lower;
		this.srv_count_upper = srv_count_upper;
		this.fitness = fitness;
	}

	public double getFitness() {
		return this.fitness;
	}

	public int getSourceBytesLower() {
		return this.src_bytes_lower;
	}

	public int getSourceBytesUpper() {
		return this.src_bytes_upper;
	}

	public int getDestBytesLower() {
		return this.dst_bytes_lower;
	}

	public int getDestBytesUpper() {
		return this.dst_bytes_upper;
	}
	
	public int getCountLower() {
		return this.count_lower;
	}	
	
	public int getCountUpper() {
		return this.count_upper;
	}
	
	public int getSrvCountLower() {
		return this.srv_count_lower;
	}	
	
	public int getSrvCountUpper() {
		return this.srv_count_upper;
	}

}