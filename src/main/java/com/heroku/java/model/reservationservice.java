package com.heroku.java.model;

public class reservationservice {

    private String reservationserviceid;
    private String serviceid;
    private String reservationid;
    private String serviceduration;
    private String servicequantity;

    public reservationservice() {

    }

    public reservationservice(String reservationserviceid, String serviceid, String reservationid, String serviceduration, String servicequantity){
      this.reservationserviceid = reservationserviceid;
      this.serviceid = serviceid;
      this.reservationid = reservationid;
      this.serviceduration = serviceduration;
      this.servicequantity = servicequantity;
    }

}
