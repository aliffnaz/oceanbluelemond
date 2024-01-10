package com.heroku.java.model;

public class roomService extends service {

    private String balance;

    public roomService(){

    }

    public roomService(String serviceID, String serviceName, String serviceType, String servicePrice, String serviceStatus, String balance){
        super(serviceID, serviceName, serviceType, servicePrice, serviceStatus);
        this.balance = balance;
    }

    public String getBalance() {
		return balance;
	}
	public void setBalance(String balance) {
		this.balance = balance;
	}

}
