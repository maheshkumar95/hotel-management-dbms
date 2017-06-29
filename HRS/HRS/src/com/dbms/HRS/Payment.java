package com.dbms.HRS;

public class Payment {
	
	private int id;
	private int madeBy;
	String status;
	float totaldues;
	float amountpaid;
	
	
	public Payment() {
		super();
		// TODO Auto-generated constructor stub
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public int getMadeBy() {
		return madeBy;
	}


	public void setMadeBy(int madeBy) {
		this.madeBy = madeBy;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public float getTotaldues() {
		return totaldues;
	}


	public void setTotaldues(float totaldues) {
		this.totaldues = totaldues;
	}


	public float getAmountpaid() {
		return amountpaid;
	}


	public void setAmountpaid(float amountpaid) {
		this.amountpaid = amountpaid;
	}	
	
	
}
