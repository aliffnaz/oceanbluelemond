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

}
