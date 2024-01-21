package com.heroku.java.model;

public class roomService extends service {

    private int balance;

    public roomService(){

    }

    public roomService(int serviceID, String serviceName, String serviceType, double servicePrice, String serviceStatus, int balance){
        super(serviceID, serviceName, serviceType, servicePrice, serviceStatus);
        this.balance = balance;
    }

    public int getBalance() {
		return balance;
	}
	public void setBalance(int balance) {
		this.balance = balance;
	}

}
