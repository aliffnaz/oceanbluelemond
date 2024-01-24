package com.heroku.java.model;

public class roomReservation {

    private int roomReservationID;
    private String roomNum;
    private int reservationID;

    public roomReservation() {

    }

    public roomReservation(int roomReservationID, String roomNum, int reservationID){
      this.roomReservationID = roomReservationID;
      this.roomNum = roomNum;
      this.reservationID = reservationID;
    }

    public int getRoomReservationID() {
		return roomReservationID;
	}
	public void setRoomReservationID(int roomReservationID) {
		this.roomReservationID = roomReservationID;
	}
	public String getRoomNum() {
		return roomNum;
	}
	public void setRoomNum(String roomNum) {
		this.roomNum = roomNum;
	}
	public int getReservationID() {
		return reservationID;
	}
	public void setReservationID(int reservationID) {
		this.reservationID = reservationID;
	}

}
