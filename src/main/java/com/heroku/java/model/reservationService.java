package com.heroku.java.model;

public class reservationService {

    private int reservationServiceID;
    private int serviceID;
    private int reservationID;
    private int serviceDuration;
    private int serviceQuantity;

    public reservationService() {

    }

    public reservationService(int reservationServiceID, int serviceID, int reservationID, int serviceDuration, int serviceQuantity){
      this.reservationServiceID = reservationServiceID;
      this.serviceID = serviceID;
      this.reservationID = reservationID;
      this.serviceDuration = serviceDuration;
      this.serviceQuantity = serviceQuantity;
    }

    public int getReservationServiceID() {
		return reservationServiceID;
	}
	public void setReservationServiceID(int reservationServiceID) {
		this.reservationServiceID = reservationServiceID;
	}
	public int getServiceID() {
		return serviceID;
	}
	public void setServiceID(int serviceID) {
		this.serviceID = serviceID;
	}
	public int getReservationID() {
		return reservationID;
	}
	public void setReservationID(int reservationID) {
		this.reservationID = reservationID;
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
