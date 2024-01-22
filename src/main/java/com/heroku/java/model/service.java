package com.heroku.java.model;

public class service {

    private int serviceID;
    private String serviceName;
    private String serviceType;
    private double servicePrice;
    private String serviceStatus;
    private int serviceDuration;
    private int serviceQuantity;

    public service(){
	    
    }

    public service(int serviceID, String serviceName, String serviceType, double servicePrice, String serviceStatus){
        this.serviceID = serviceID;
        this.serviceName = serviceName;
        this.serviceType = serviceType;
        this.servicePrice = servicePrice;
        this.serviceStatus = serviceStatus;
    }
    
    public int getServiceID() {
		return serviceID;
	}
	public void setServiceID(int serviceID) {
		this.serviceID = serviceID;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getServiceType() {
		return serviceType;
	}
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	public double getServicePrice() {
		return servicePrice;
	}
	public void setServicePrice(double servicePrice) {
		this.servicePrice = servicePrice;
	}
	public String getServiceStatus() {
		return serviceStatus;
	}
	public void setServiceStatus(String serviceStatus) {
		this.serviceStatus = serviceStatus;
	}
	public int getServiceDuration() {
		return serviceDuration;
	}
	public void setServiceDuration(int serviceDuration) {
		this.serviceDuration = serviceDuration;
	}
	public int getServiceQuantity() {
		return serviceQuantity;
	}
	public void setServiceQuantity(int serviceQuantity) {
		this.serviceQuantity = serviceQuantity;
	}

}
