package com.heroku.java.model;

public class room {
   
    private String roomNum;
    private String roomType;
    private int maxGuest;
    private double roomRate;
    private String roomSize;
    private String roomStatus;

    public room(){
        
    }

    public String getRoomNum() {
        return this.roomNum;
    }

    public void setRoomNum(String roomNum) {
        this.roomNum = roomNum;
    }
    
    public String getRoomType() {
        return this.roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public int getMaxGuest() {
        return this.maxGuest;
    }

    public void setMaxGuest(int maxGuest) {
        this.maxGuest = maxGuest;
    }

    public double getRoomRate() {
        return this.roomRate;
    }

    public void setRoomRate(double roomRate) {
        this.roomRate = roomRate;
    }

    public String getRoomSize() {
        return this.roomSize;
    }

    public void setRoomSize(String roomSize) {
        this.roomSize = roomSize;
    }

    public String getRoomStatus() {
        return this.roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }

}
