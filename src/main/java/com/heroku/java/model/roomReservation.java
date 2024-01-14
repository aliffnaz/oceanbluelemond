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

    public String getRoomReservationID() {
		return roomReservationID;
	}
	public void setRoomReservationID(String roomReservationID) {
		this.roomReservationID = roomReservationID;
	}
	public String getRoomNum() {
		return roomNum;
	}
	public void setRoomNum(String roomNum) {
		this.roomNum = roomNum;
	}
	public String getReservationID() {
		return reservationID;
	}
	public void setReservationID(String reservationID) {
		this.reservationID = reservationID;
	}

}
