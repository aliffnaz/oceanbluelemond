package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.sql.Date;

import com.heroku.java.model.*;

import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

// import org.jscience.physics.amount.Amount;
// import org.jscience.physics.model.RelativisticModel;
// import javax.measure.unit.SI;
@SpringBootApplication
@Controller

public class ReservationController {
  private final DataSource dataSource;

  @Autowired
  public ReservationController(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @GetMapping("/guestMakeRoomReservation")
  public String guestMakeRoomReservation(HttpSession session) {
    String guestICNumber = (String) session.getAttribute("guestICNumber");
    return "guest/guestMakeRoomReservation";
  }

  // Method to get a list of random room numbers from the available list
    private List<String> getRandomRoomNumbers(List<String> availableRoomNumbers, int totalRooms) {
        if (availableRoomNumbers.isEmpty() || totalRooms <= 0) {
            throw new IllegalStateException("No available rooms or invalid total rooms.");
    }

        List<String> selectedRoomNumbers = new ArrayList<>();
        int numberOfRoomsToSelect = Math.min(totalRooms, availableRoomNumbers.size());

        for (int i = 0; i < numberOfRoomsToSelect; i++) {
            int randomIndex = (int) (Math.random() * availableRoomNumbers.size());
            selectedRoomNumbers.add(availableRoomNumbers.remove(randomIndex));
        }

        return selectedRoomNumbers;
    }

    //Method to check availability of rooms based on room type
    // private boolean checkRoomAvailability(String roomType, String dateStart, String dateEnd, Connection connection) throws SQLException {
    //     // Check if there are any overlapping reservations for the selected room type and date range
    //     String sql = "SELECT COUNT(*) FROM roomreservation rr " +
    //                  "JOIN reservation r ON rr.reservationid = r.reservationid " +
    //                  "JOIN room room ON room.roomnum = rr.roomnum " +
    //                  "WHERE room.roomtype = ? " +
    //                  "  AND r.dateStart <= ? AND r.dateEnd >= ?";
                     
    //     try (PreparedStatement statement = connection.prepareStatement(sql)) {
            
    //         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
    //         try{
    //             java.util.Date utilStartDate = dateFormat.parse(dateStart);
    //             java.util.Date utilEndDate = dateFormat.parse(dateEnd);
    //             Date dateStartDate = new Date (utilStartDate.getTime());
    //             Date dateEndDate = new Date (utilEndDate.getTime());
    //             statement.setString(1, roomType);
    //             statement.setDate(2, dateEndDate);  // Check if the reservation end date is after the selected start date
    //             statement.setDate(3, dateStartDate); // Check if the reservation start date is before the selected end date
    //         }
    //         catch (ParseException e) {
    //             e.printStackTrace();
    //         }
    //         try (ResultSet resultSet = statement.executeQuery()) {
    //             if (resultSet.next()) {
    //                 int overlappingReservationsCount = resultSet.getInt(1);
    //                 // Adjust the logic based on your requirements
    //                 return overlappingReservationsCount == 0;
    //             }
    //         }
    //     }
    //     return false;
    // }

    public static boolean checkRoomAvailability(String roomType, int totalRooms, Date startDate, Date endDate, Connection connection) throws SQLException {
        // Check if there are any overlapping reservations for the selected room type and date range
        String sql = "SELECT COUNT(*) FROM roomreservation rr " +
                "JOIN reservation r ON rr.reservationid = r.reservationid " +
                "WHERE rr.roomnum IN (SELECT roomnum FROM room WHERE roomtype = ? AND roomstatus = 'Available') " +
                "  AND ((r.datestart <= ? AND r.dateend >= ?) OR (r.datestart <= ? AND r.dateend >= ?))";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roomType);
            statement.setDate(2, new java.sql.Date(endDate.getTime()));  // Check if the reservation end date is after the selected start date
            statement.setDate(3, new java.sql.Date(startDate.getTime())); // Check if the reservation start date is before the selected end date
            statement.setDate(4, new java.sql.Date(startDate.getTime())); // Check if the reservation start date is before the selected end date
            statement.setDate(5, new java.sql.Date(endDate.getTime()));   // Check if the reservation end date is after the selected start date

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int overlappingReservationsCount = resultSet.getInt(1);
                    return overlappingReservationsCount == 0;
                }
            }
        }
        return false;
    }

    public static boolean updateRoomStatus(String roomType, int totalRooms, Connection connection) throws SQLException {
        String updateSql = "UPDATE room SET roomstatus = 'Booked' WHERE roomtype = ? LIMIT ?";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
            updateStatement.setString(1, roomType);
            updateStatement.setInt(2, totalRooms);

            int updatedRows = updateStatement.executeUpdate();
            return updatedRows > 0;
        }
    }

  @PostMapping("/guestMakeRoomReservation")
  public String guestMakeRoomReservation(HttpSession session, @ModelAttribute("guestMakeRoomReservation") reservation reservation, 
  room room, roomReservation roomReservation, staff staff, Model model, @RequestParam("addon") String addon,
  @RequestParam("roomType") String roomType, @RequestParam("date") String date){

    try{
        Connection connection = dataSource.getConnection();

        //assign manager ic number to reservation
        String sqlStaff = "SELECT stafficnumber FROM staff where staffrole = ?";
        final var statementStaff = connection.prepareStatement(sqlStaff);
        statementStaff.setString(1, "Manager");
        final var resultSetStaff = statementStaff.executeQuery();

        if (resultSetStaff.next()) {
            String staffICNumber = resultSetStaff.getString("staffICNumber");

        // String sql = "INSERT INTO reservation() VALUES (?)";
        // final var statement = connection.prepareStatement(sql);

        String[] dateParts = date.split(" to ");
        
        // Extract start date and end date
        String dateStart = dateParts[0];
        String dateEnd = dateParts[1];
        String reservationID = reservation.getReservationID();
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        int totalAdult = reservation.getTotalAdult();
        int totalKids = reservation.getTotalKids();
        String reserveStatus = "Pending";
        int totalRoom = reservation.getTotalRoom();
        String totalPayment = "0.00";
        int guestQuantity = totalAdult + totalKids;
        
        //debugging
        System.out.println("dateStart: " + dateStart);
        System.out.println("dateEnd: " + dateEnd);
        System.out.println("reservationID: " + reservationID);
        System.out.println("guestICNumber: " + guestICNumber);
        System.out.println("totalAdult: " + totalAdult);
        System.out.println("totalKids: " + totalKids);
        System.out.println("totalRoom: " + totalRoom);
        System.out.println("guestQuantity: " + guestQuantity);
        System.out.println("roomType: " + roomType);

        //get rooms according to roomtype
        // String sqlRoom = "SELECT roomNum, maxGuest from room where roomType=?";
        // final var statementRoom = connection.prepareStatement(sqlRoom);
        // statementRoom.setString(1, roomType);
        
        // ResultSet availableRoomsResult = statementRoom.executeQuery();
        // List<String> availableRoomNumbers = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
        try{
            java.util.Date utilStartDate = dateFormat.parse(dateStart);
            java.util.Date utilEndDate = dateFormat.parse(dateEnd);
            Date dateStartDate = new Date (utilStartDate.getTime());
            Date dateEndDate = new Date (utilEndDate.getTime());
            System.out.println("date start in new format: " + dateStartDate);
            System.out.println("date end in new format: " + dateEndDate);
            // statement.setString(1, roomType);
            // statement.setDate(2, dateEndDate);  // Check if the reservation end date is after the selected start date
            // statement.setDate(3, dateStartDate); // Check if the reservation start date is before the selected end date
            
            boolean available = checkRoomAvailability(roomType, totalRoom, dateStartDate, dateEndDate, connection);
            System.out.println(available);
        
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        
        // if (available){
        //     int totalMaxGuests = 0;
        //     while (availableRoomsResult.next()) {
        //         availableRoomNumbers.add(availableRoomsResult.getString("roomNumber"));
        //         totalMaxGuests += availableRoomsResult.getInt("maxGuest");
        //     }
        //     //List<String> selectedRoomNumbers = getRandomRoomNumbers(availableRoomNumbers, totalRoom);
        // if (totalMaxGuests < guestQuantity){
        //     System.out.println("lebih ni");
        // }
            
        // System.out.println("totalMaxGuests: " + totalMaxGuests);
        // }
        System.out.println("reservation date: " + date);
        
        //set reservation id into session
        session.setAttribute("reservationID", reservationID);

        }
    }
    catch (Exception e) {
        e.printStackTrace();
        System.out.println("reservation date: " + date);
        return "redirect:/index";
    }
    return "guest/guestMakeRoomService";  
}
  
@GetMapping("/guestMakeRoomService")
public String guestMakeRoomService(HttpSession session) {
  String guestICNumber = (String) session.getAttribute("guestICNumber");
  String reservationID = (String) session.getAttribute("reservationID");
  return "guest/guestMakeRoomService";
}

@GetMapping("/guestMakeEventService")
public String guestMakeEventService(HttpSession session) {
  String guestICNumber = (String) session.getAttribute("guestICNumber");
  String reservationID = (String) session.getAttribute("reservationID");
  return "guest/guestMakeEventService";
}

}
