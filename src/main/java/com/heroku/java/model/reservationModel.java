package com.heroku.java.model;

public class reservationModel {

    private String reservationID;
    private String guestID;
    private String guestQuantity;
    private String durationOfStay;
    private String dateStart;
    private String dateEnd;
    private String totalAdult;
    private String totalKids;
    private String reserveStatus;
    private String totalRoom;
    private String totalPayment;

    public reservationModel() {

    }

    /**
     * @return String return the reservationID
     */
    public String getReservationID() {
        return reservationID;
    }

    /**
     * @param reservationID the reservationID to set
     */
    public void setReservationID(String reservationID) {
        this.reservationID = reservationID;
    }

    /**
     * @return String return the guestID
     */
    public String getGuestID() {
        return guestID;
    }

    /**
     * @param guestID the guestID to set
     */
    public void setGuestID(String guestID) {
        this.guestID = guestID;
    }

    /**
     * @return String return the guestQuantity
     */
    public String getGuestQuantity() {
        guestQuantity = this.totalAdult + this.totalKids;
    }

    /**
     * @param guestQuantity the guestQuantity to set
     */
    public void setGuestQuantity(String guestQuantity) {
        this.guestQuantity = guestQuantity;
    }

    /**
     * @return String return the durationOfStay
     */
    public String getDurationOfStay() {
        return durationOfStay;
    }

    /**
     * @param durationOfStay the durationOfStay to set
     */
    public void setDurationOfStay(String durationOfStay) {
        this.durationOfStay = durationOfStay;
    }

    /**
     * @return String return the dateStart
     */
    public String getDateStart() {
        return dateStart;
    }

    /**
     * @param dateStart the dateStart to set
     */
    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }

    /**
     * @return String return the dateEnd
     */
    public String getDateEnd() {
        return dateEnd;
    }

    /**
     * @param dateEnd the dateEnd to set
     */
    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    /**
     * @return String return the totalAdult
     */
    public String getTotalAdult() {
        return totalAdult;
    }

    /**
     * @param totalAdult the totalAdult to set
     */
    public void setTotalAdult(String totalAdult) {
        this.totalAdult = totalAdult;
    }

    /**
     * @return String return the totalKids
     */
    public String getTotalKids() {
        return totalKids;
    }

    /**
     * @param totalKids the totalKids to set
     */
    public void setTotalKids(String totalKids) {
        this.totalKids = totalKids;
    }

    /**
     * @return String return the reserveStatus
     */
    public String getReserveStatus() {
        return reserveStatus;
    }

    /**
     * @param reserveStatus the reserveStatus to set
     */
    public void setReserveStatus(String reserveStatus) {
        this.reserveStatus = reserveStatus;
    }

    /**
     * @return String return the totalRoom
     */
    public String getTotalRoom() {
        return totalRoom;
    }

    /**
     * @param totalRoom the totalRoom to set
     */
    public void setTotalRoom(String totalRoom) {
        this.totalRoom = totalRoom;
    }

    /**
     * @return String return the totalPayment
     */
    public String getTotalPayment() {
        return totalPayment;
    }

    /**
     * @param totalPayment the totalPayment to set
     */
    public void setTotalPayment(String totalPayment) {
        this.totalPayment = totalPayment;
    }

}
