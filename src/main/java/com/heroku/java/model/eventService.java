package com.heroku.java.model;

public class eventService extends service {

    private String eventCapacity;

    public eventService(){

    }

    public eventService(String serviceID, String serviceName, String serviceType, String servicePrice, String serviceStatus, String eventCapacity){
        super(serviceID, serviceName, serviceType, servicePrice, serviceStatus);
        this.eventCapacity = eventCapacity;
    }

    public String getEventCapacity() {
		return eventCapacity;
	}
	public void setEventCapacity(String eventCapacity) {
		this.eventCapacity = eventCapacity;
	}

}
