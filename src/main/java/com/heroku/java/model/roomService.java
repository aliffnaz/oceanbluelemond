package com.heroku.java.model;

public class roomService extends service {

    private int maxQuantity;

    public roomService(){

    }

    public roomService(int serviceID, String serviceName, String serviceType, double servicePrice, String serviceStatus, int maxQuantity){
        super(serviceID, serviceName, serviceType, servicePrice, serviceStatus);
        this.maxQuantity = maxQuantity;
    }

    public int getMaxQuantity() {
		return maxQuantity;
	}
	public void setMaxQuantity(int maxQuantity) {
		this.maxQuantity = maxQuantity;
	}

}
