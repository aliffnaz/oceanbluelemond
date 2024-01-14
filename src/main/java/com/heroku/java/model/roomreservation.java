package com.heroku.java.model;

public class roomreservation {

    private String roomreservationid;
    private String roomnum;
    private String reservationid;

    public roomreservation() {

    }

    public roomreservation(String roomreservationid, String roomnum, String reservationid){
      this.roomreservationid = roomreservationid;
      this.roomnum = roomnum;
      this.reservationid = reservationid;
    }

}
