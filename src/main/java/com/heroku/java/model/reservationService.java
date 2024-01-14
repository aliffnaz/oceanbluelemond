package com.heroku.java.model;

public class reservationService {

    private String reservationServiceID;
    private String serviceID;
    private String reservationID;
    private String serviceDuration;
    private String serviceQuantity;

    public reservationService() {

    }

    public reservationService(String reservationServiceID, String serviceID, String reservationID, String serviceDuration, String serviceQuantity){
      this.reservationServiceID = reservationServiceID;
      this.serviceID = serviceID;
      this.reservationID = reservationID;
      this.serviceDuration = serviceDuration;
      this.serviceQuantity = serviceQuantity;
    }

    public String getReservationServiceID() {
		return reservationServiceID;
	}
	public void setReservationServiceID(String reservationServiceID) {
		this.reservationServiceID = reservationServiceID;
	}
	public String getServiceID() {
		return serviceID;
	}
	public void setServiceID(String serviceID) {
		this.serviceID = serviceID;
	}
	public String getReservationID() {
		return reservationID;
	}
	public void setReservationID(String reservationID) {
		this.reservationID = reservationID;
	}
	public String getServiceDuration() {
		return serviceDuration;
	}
	public void setServiceDuration(String serviceDuration) {
		this.serviceDuration = serviceDuration;
	}
	public String getServiceQuantity() {
		return serviceQuantity;
	}
	public void setServiceQuantity(String serviceQuantity) {
		this.serviceQuantity = serviceQuantity;
	}


}
