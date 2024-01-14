package com.heroku.java.model;

public class roomReservation {

    private String roomReservationID;
    private String roomNum;
    private String reservationID;

    public roomReservation() {

    }

    public roomReservation(String roomReservationID, String roomNum, String reservationID){
      this.roomReservationID = roomReservationID;
      this.roomNum = roomNum;
      this.reservationID = reservationID;
    }

}
