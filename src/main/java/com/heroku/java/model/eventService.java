package com.heroku.java.model;

public class eventService extends service {

    private int eventCapacity;

    public eventService(){

    }

    public eventService(int serviceID, String serviceName, String serviceType, double servicePrice, String serviceStatus, int eventCapacity){
        super(serviceID, serviceName, serviceType, servicePrice, serviceStatus);
        this.eventCapacity = eventCapacity;
    }

    public int getEventCapacity() {
		return eventCapacity;
	}
	public void setEventCapacity(int eventCapacity) {
		this.eventCapacity = eventCapacity;
	}

}
