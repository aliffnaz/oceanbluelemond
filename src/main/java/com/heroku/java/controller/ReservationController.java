
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
import java.time.Duration;
import java.time.LocalDate;

import com.heroku.java.model.*;

import jakarta.servlet.http.HttpSession;

import javax.net.ssl.HttpsURLConnection;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.util.HashMap;

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

    public static List<String> getAvailableRoomNumbers(String roomType, int totalRooms, Date startDate, Date endDate, Connection connection, HttpSession session) throws SQLException {
        // Query to get available room numbers
        String sql = "SELECT roomnum FROM room WHERE roomtype = ? AND roomstatus = 'Available' " +
                "AND roomnum NOT IN (SELECT roomnum FROM roomreservation rr " +
                "JOIN reservation r ON rr.reservationid = r.reservationid " +
                "WHERE (r.datestart <= ? AND r.dateend >= ?) OR (r.datestart <= ? AND r.dateend >= ?)) " +
                "LIMIT ?";
    
        List<String> availableRoomNumbers = new ArrayList<>();
    
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roomType);
            statement.setDate(2, new java.sql.Date(endDate.getTime()));
            statement.setDate(3, new java.sql.Date(startDate.getTime()));
            statement.setDate(4, new java.sql.Date(startDate.getTime()));
            statement.setDate(5, new java.sql.Date(endDate.getTime()));
            statement.setInt(6, totalRooms);
    
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    availableRoomNumbers.add(resultSet.getString("roomnum"));
                }
            }
            catch (SQLException e){
                System.out.println("failure at getAvailableRoomNumbers");
                e.printStackTrace();
            }
        }
        if (availableRoomNumbers.size() < 1){
            session.setAttribute("messege", "No available room for that combination of Room Type and Date");
        }
    
        return availableRoomNumbers;
    }

    public static Date convertToPostgresDate(String originalDateString) {
        SimpleDateFormat originalFormat = new SimpleDateFormat("MM-dd-yy");

        try {
            // Parse the original string
            java.util.Date utilDate = originalFormat.parse(originalDateString);

            // Format it for PostgreSQL (YYYY-MM-DD)
            SimpleDateFormat postgresqlFormat = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDateString = postgresqlFormat.format(utilDate);

            // Convert the formatted string to java.sql.Date
            return Date.valueOf(formattedDateString);
        } catch (ParseException e) {
            e.printStackTrace();
            // Handle the exception according to your needs
            return null; // or throw an exception
        }
    }

    public static int calculateDurationOfStay(String startDateString, String endDateString) {
        try {
            // Convert start date to java.sql.Date
            Date startDate = convertToPostgresDate(startDateString);
    
            // Convert end date to java.sql.Date
            Date endDate = convertToPostgresDate(endDateString);
    
            // Calculate the duration in milliseconds
            long durationMillis = endDate.getTime() - startDate.getTime();
    
            // Convert duration to days and cast to int
            return (int) Duration.ofMillis(durationMillis).toDays();
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception according to your needs
            return -1; // or throw an exception
        }
    }

    private int getMaxGuestsForRoom(String roomNumber, Connection connection) throws SQLException {
        // Query to get the max guests for the specified room
        String sql = "SELECT maxGuest FROM room WHERE roomnum = ?";
    
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roomNumber);
    
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("maxGuest");
                }
            }
        }
        throw new SQLException("Failed to get max guests for room: " + roomNumber);
    }

    private double calculateTotalPayment(List<String> availableRoomNumbers, int durationOfStay, Connection connection) throws SQLException {
        double totalPayment = 0.0;
    
        // Iterate through each room and fetch its roomrate
        for (String roomNumber : availableRoomNumbers) {
            String sqlRoomRate = "SELECT roomrate FROM room WHERE roomnum = ?";
            try (PreparedStatement statementRoomRate = connection.prepareStatement(sqlRoomRate)) {
                statementRoomRate.setString(1, roomNumber);
    
                try (ResultSet resultSetRoomRate = statementRoomRate.executeQuery()) {
                    if (resultSetRoomRate.next()) {
                        double roomRate = resultSetRoomRate.getDouble("roomrate");
                        double roomPrice = roomRate * durationOfStay;
                        totalPayment += roomPrice;
                    }
                }
            }
        }
    
        return totalPayment;
    }

    private double calculateTotalServicePrice(int reservationid, Connection connection) throws SQLException {
        double totalServicePrice = 0.0;
    
            String sqlServicePrice = "SELECT s.serviceprice, rs.serviceduration, rs.servicequantity FROM service s " +
            "JOIN reservationservice rs ON rs.serviceid = s.serviceid " +
            "JOIN reservation r ON r.reservationid = rs.reservationid " +
            "WHERE r.reservationid = ?";
            try (PreparedStatement statementServicePrice = connection.prepareStatement(sqlServicePrice)) {
                statementServicePrice.setInt(1, reservationid);
    
                try (ResultSet resultSet = statementServicePrice.executeQuery()) {
                    while (resultSet.next()) {
                        double servicePrice = resultSet.getDouble("serviceprice");
                        int serviceDuration = resultSet.getInt("serviceduration");
                        int serviceQuantity = resultSet.getInt("servicequantity");
                        totalServicePrice += (servicePrice * serviceDuration * serviceQuantity);
                    }
                }
                catch(SQLException e){
                    e.printStackTrace();
                    System.out.println("failed at calculateTotalServicePrice");
                }
            }
    
        return totalServicePrice;
    }

    public static boolean isRoomServiceAvailable(int serviceId, Date startDate, Date endDate, int totalRooms, Connection connection) throws SQLException {
        // Query to check if the room service is available for the given date range and maximum quantity constraint
        String sql = "SELECT COUNT(*) FROM reservationservice rs " +
                     "JOIN reservation r ON rs.reservationid = r.reservationid " +
                     "JOIN roomservice ON roomservice.serviceid = rs.serviceid " +
                     "WHERE rs.serviceid = ? " +
                     "  AND (r.datestart <= ? AND r.dateend >= ? OR r.datestart <= ? AND r.dateend >= ?) " +
                     "  AND roomservice.maxquantity >= ?";
    
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceId);
            statement.setDate(2, startDate);
            statement.setDate(3, endDate);
            statement.setDate(4, startDate);
            statement.setDate(5, endDate);
            statement.setInt(6, totalRooms);
    
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int existingReservationsCount = resultSet.getInt(1);
                    return existingReservationsCount == 0;
                }
            }
        }
        catch(SQLException e) {
            System.out.println("fail at method isRoomServiceAvailable()");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isEventServiceAvailable(int serviceId, Date startDate, Date endDate, Connection connection) throws SQLException {
        // Query to check if the room service is available for the given date range and maximum quantity constraint
        String sql = "SELECT COUNT(*) FROM reservationservice rs " +
                     "JOIN reservation r ON rs.reservationid = r.reservationid " +
                     "JOIN eventservice ON eventservice.serviceid = rs.serviceid " +
                     "WHERE rs.serviceid = ? " +
                     "  AND (r.datestart <= ? AND r.dateend >= ? OR r.datestart <= ? AND r.dateend >= ?) ";
    
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, serviceId);
            statement.setDate(2, startDate);
            statement.setDate(3, endDate);
            statement.setDate(4, startDate);
            statement.setDate(5, endDate);
    
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int existingReservationsCount = resultSet.getInt(1);
                    return existingReservationsCount == 0;
                }
            }
        }
        catch(SQLException e) {
            System.out.println("fail at method isEventServiceAvailable()");
            e.printStackTrace();
        }
        return false;
    }


  @PostMapping("/guestMakeRoomReservation")
  public String guestMakeRoomReservation(HttpSession session, @ModelAttribute("guestMakeRoomReservation") reservation reservation, 
  room room, roomReservation roomReservation, staff staff, Model model, @RequestParam("addon") String addon,
  @RequestParam("roomType") String roomType, @RequestParam("date") String date){

    // Check if the errorMessage attribute is not null and remove it
    if (session.getAttribute("messege") != null) {
        session.removeAttribute("messege");
      }
    try{
        Connection connection = dataSource.getConnection();

        //assign manager ic number to reservation
        String sqlStaff = "SELECT stafficnumber FROM staff where staffrole = ?";
        final var statementStaff = connection.prepareStatement(sqlStaff);
        statementStaff.setString(1, "Manager");
        final var resultSetStaff = statementStaff.executeQuery();

        if (resultSetStaff.next()) {
            String staffICNumber = resultSetStaff.getString("staffICNumber");

        String[] dateParts = date.split(" to ");
        
        // Extract start date and end date
        String dateStart = dateParts[0];
        String dateEnd = dateParts[1];
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        int totalAdult = reservation.getTotalAdult();
        int totalKids = reservation.getTotalKids();
        String reserveStatus = "Pending";
        int totalRoom = reservation.getTotalRoom();
        double totalPayment = 0.00;
        int guestQuantity = totalAdult + totalKids;
        
        //debugging
        System.out.println("dateStart: " + dateStart);
        System.out.println("dateEnd: " + dateEnd);
        System.out.println("guestICNumber: " + guestICNumber);
        System.out.println("totalAdult: " + totalAdult);
        System.out.println("totalKids: " + totalKids);
        System.out.println("totalRoom: " + totalRoom);
        System.out.println("guestQuantity: " + guestQuantity);
        System.out.println("roomType: " + roomType);

        Date dateStartDate = convertToPostgresDate(dateStart);
        Date dateEndDate = convertToPostgresDate(dateEnd);
        System.out.println("date start: " + dateStartDate);
        System.out.println("date end: " + dateEndDate);
        int durationOfStay = calculateDurationOfStay(dateStart, dateEnd);
        
        // boolean available = checkRoomAvailability(roomType, totalRoom, dateStartDate, dateEndDate, connection);
        // System.out.println(available);

        // Convert java.sql.Date to LocalDate
        LocalDate userDate = dateStartDate.toLocalDate();

        // Get today's date
        LocalDate currentDate = LocalDate.now();

        // Check if the user's date is not earlier than today
        if (userDate.isBefore(currentDate)) {
            session.setAttribute("messege", "Selected date cannot be earlier than today");
            // You might want to return to the same page or redirect with an error message
            return "redirect:/guestMakeRoomReservation";
        }

             // Get available room numbers
             List<String> availableRoomNumbers = getAvailableRoomNumbers(roomType, totalRoom, dateStartDate, dateEndDate, connection, session);
            int totalMaxGuests = availableRoomNumbers.stream()
            .mapToInt(roomNumber -> {
                try{ 
                    return getMaxGuestsForRoom(roomNumber, connection);
            }
            catch (SQLException e){
                e.printStackTrace();
                return 0;
            }
            }).sum();
            // Check if the total guest quantity exceeds the total maximum allowed guests
            boolean exceedsMaxGuests = guestQuantity > totalMaxGuests;
            if (!exceedsMaxGuests){

            //insert into table reservation    
            String sqlReservation = "INSERT INTO reservation(guestICNumber, guestQuantity, durationOfStay, datestart, dateend, totaladult, totalkids, reservestatus, totalroom, totalpayment, stafficnumber) VALUES (?,?,?,?,?,?,?,?,?,?,?) RETURNING reservationid";
            final var statementReservation = connection.prepareStatement(sqlReservation);
            
            statementReservation.setString(1,guestICNumber);
            statementReservation.setInt(2,guestQuantity);
            statementReservation.setInt(3,durationOfStay);
            statementReservation.setDate(4,dateStartDate);
            statementReservation.setDate(5,dateEndDate);
            statementReservation.setInt(6,totalAdult);
            statementReservation.setInt(7,totalKids);
            statementReservation.setString(8,reserveStatus);
            statementReservation.setInt(9,totalRoom);
            statementReservation.setDouble(10,totalPayment);
            statementReservation.setString(11,staffICNumber);

            final var resultSetReservation = statementReservation.executeQuery();

            int reservationID = 0;
            // Retrieve the auto-generated reservationID
            if (resultSetReservation.next()) {
                reservationID = resultSetReservation.getInt("reservationID");
            }
            System.out.println("id from db reservation : " + reservationID);
            session.setAttribute("reservationID", reservationID);
            session.setAttribute("durationOfStay", durationOfStay);
            session.setAttribute("dateStart", dateStartDate);
            session.setAttribute("dateEnd", dateEndDate);
            session.setAttribute("totalRoom", totalRoom);


            // Insert room numbers into roomreservation table
            for (String roomNumber : availableRoomNumbers) {
                String sqlRoomReservation = "INSERT INTO roomreservation(roomnum, reservationid) VALUES (?, ?)";
                try (PreparedStatement statementRoomReservation = connection.prepareStatement(sqlRoomReservation)) {
                    statementRoomReservation.setString(1, roomNumber);
                    statementRoomReservation.setInt(2, reservationID);
                    statementRoomReservation.executeUpdate();
                    System.out.println("room number: "+roomNumber);
                    }
                    catch (SQLException e){
                        e.printStackTrace();
                        System.out.println("fail to insert into roomreservation table");
                    }
                }
            } else {
                session.setAttribute("messege", "Something went wrong :(  Please ensure that you entered valid data");
                System.out.println("Guest quantity exceeds max guest allowed");
                return "redirect:/guestMakeRoomReservation";
            }

            int reservationID = (int) session.getAttribute("reservationID");
            totalPayment = calculateTotalPayment(availableRoomNumbers, durationOfStay, connection);
            String sqlUpdateTotalPayment = "UPDATE reservation SET totalpayment = ? WHERE reservationid = ?";
            try (PreparedStatement statementUpdateTotalPayment = connection.prepareStatement(sqlUpdateTotalPayment)) {
                statementUpdateTotalPayment.setDouble(1, totalPayment);
                statementUpdateTotalPayment.setInt(2, reservationID);
                statementUpdateTotalPayment.executeUpdate();
            }

        connection.close();

        //set payment into session
        session.setAttribute("totalPayment", totalPayment);
        }
        System.out.println("reservation date: " + date);

        }
        catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("messege", "Room not available");
            System.out.println("Failed to make room reservation");
            return "redirect:/guestMakeRoomReservation";
        }

        if (addon.equalsIgnoreCase("Yes")){
            System.out.println("yes for addon");
            return "redirect:/guestMakeRoomService";
        }
        else{
            System.out.println("no for addon");
            return "redirect:/guestRoomReservation";
        }
    }
  
    @GetMapping("/guestMakeRoomService")
    public String guestMakeRoomService(HttpSession session, Model model) {
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        int reservationID = (int) session.getAttribute("reservationID");
        double totalPayment = (double) session.getAttribute("totalPayment");
        System.out.println("guestICNumber: " + guestICNumber);
        System.out.println("reservationID: " + reservationID);

        //first part, getting all room services and putting them into the dropdown menu
        List <service> services = new ArrayList<service>();
        try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT serviceid, servicename, serviceprice from service where servicetype = ? and servicestatus = ?";
            final var statement = connection.prepareStatement(sql);
            statement.setString(1, "Room Service");
            statement.setString(2, "Available");
            final var resultSet = statement.executeQuery();
            while (resultSet.next()){
                int serviceID = resultSet.getInt("serviceid");
                String serviceName = resultSet.getString("servicename");
                double servicePrice = resultSet.getDouble("serviceprice");

                service service = new service();
                service.setServiceID(serviceID);
                service.setServiceName(serviceName);
                service.setServicePrice(servicePrice);
                
                //debug
                System.out.println("add into array for dropdown");

                services.add(service);
                model.addAttribute("services", services);
            }

            connection.close();

        }
        catch(SQLException e){
            e.printStackTrace();
            // Handle the exception as desired (e.g., show an error message)
            System.out.println("error to execute dropdown menu for service type");

        }

        //second part, table of service that the guest added
        List <service> guestServices = new ArrayList<service>();
        List <reservationService> guestReservationServices = new ArrayList<reservationService>();
        try (Connection connection = dataSource.getConnection()){
            String sqlGuestService = "SELECT service.serviceid, service.servicename, service.servicetype, service.serviceprice, reservationservice.serviceduration, reservationservice.servicequantity "
            + "from service "
            + "JOIN reservationservice ON reservationservice.serviceid = service.serviceid "
            + "JOIN reservation ON reservationservice.reservationid = reservation.reservationid "
            + "WHERE reservation.reservationid = ?";
            final var statementGuestService = connection.prepareStatement(sqlGuestService);
            statementGuestService.setInt(1, reservationID);
            final var resultSetGuestService = statementGuestService.executeQuery();
            System.out.println("pass for getting guest services for this reservationid");

            while (resultSetGuestService.next()){
                int guestServiceID = resultSetGuestService.getInt("serviceid");
                String guestServiceName = resultSetGuestService.getString("servicename");
                String guestServiceType = resultSetGuestService.getString("servicetype");
                double guestServicePrice = resultSetGuestService.getDouble("serviceprice");
                int guestServiceDuration = resultSetGuestService.getInt("serviceduration");
                int guestServiceQuantity = resultSetGuestService.getInt("servicequantity");

                service guestService = new service();
                reservationService guestReservationService = new reservationService();
                guestService.setServiceID(guestServiceID);
                guestService.setServiceName(guestServiceName);
                guestService.setServiceType(guestServiceType);
                guestService.setServicePrice(guestServicePrice);
                guestReservationService.setServiceDuration(guestServiceDuration);
                guestReservationService.setServiceQuantity(guestServiceQuantity);

                guestServices.add(guestService);
                guestReservationServices.add(guestReservationService);
                model.addAttribute("guestServices", guestServices);
                model.addAttribute("guestReservationServices", guestReservationServices);
                System.out.println("service added into list");
            }

            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception as desired (e.g., show an error message)
            System.out.println("error to display list of service that the guest reserve");
        }

        return "guest/guestMakeRoomService";
    }

    @PostMapping("/guestMakeRoomService")
    public String guestMakeRoomService(HttpSession session, @ModelAttribute("guestMakeRoomService")service service, @RequestParam("serviceID") String serviceIDString, @RequestParam("serviceQuantity") int serviceQuantity, @RequestParam("serviceDuration") int serviceDuration){
        
        try {
        Connection connection = dataSource.getConnection();
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        int reservationID = (int) session.getAttribute("reservationID");
        int durationOfStay = (int) session.getAttribute("durationOfStay");
        Date dateStart = (Date) session.getAttribute("dateStart");
        Date dateEnd = (Date) session.getAttribute("dateEnd");
        double totalPayment = (double) session.getAttribute("totalPayment");
        int totalRoom = (int) session.getAttribute("totalRoom");
        int serviceID = Integer.parseInt(serviceIDString);

        System.out.println("guestICNumber: " + guestICNumber);
        System.out.println("reservationID: " + reservationID);
        System.out.println("durationOfStay: " + durationOfStay);
        System.out.println("dateStart: " + dateStart);
        System.out.println("dateEnd: " + dateEnd);
        System.out.println("totalPayment: " + totalPayment);
        System.out.println("serviceID: " + serviceID);
        System.out.println("total room: " + totalRoom);

        if (serviceDuration > durationOfStay){
            System.out.println("Service duration cannot exceed Duration of stay");
            return "redirect:/guestMakeRoomService";
        }
        
        boolean serviceAvailability = isRoomServiceAvailable(serviceID, dateStart, dateEnd, totalRoom, connection);
        if (serviceAvailability){
            String sql = "INSERT INTO reservationservice(reservationid, serviceduration, servicequantity, serviceid) VALUES (?,?,?,?)";
            final var statement = connection.prepareStatement(sql);
            
            statement.setInt(1, reservationID);
            statement.setInt(2, serviceDuration);
            statement.setInt(3, serviceQuantity);
            statement.setInt(4, serviceID);

            statement.executeUpdate();
            System.out.println("sukses insert into table reservationservice");
            
        }
        else{
            System.out.println("gagal insert into table reservationservice sebab tak available");
        }
        connection.close();

        } catch(SQLException e){
            System.out.println("failed to insert into reservationservice");
            e.printStackTrace();
            return "redirect:/index";
        }
        return "redirect:/guestMakeRoomService";
    }

    @GetMapping("/guestMakeEventService")
    public String guestMakeEventService(HttpSession session, Model model) {

        String guestICNumber = (String) session.getAttribute("guestICNumber");
        int reservationID = (int) session.getAttribute("reservationID");
        int durationOfStay = (int) session.getAttribute("durationOfStay");
        Date dateStart = (Date) session.getAttribute("dateStart");
        Date dateEnd = (Date) session.getAttribute("dateEnd");
        double totalPayment = (double) session.getAttribute("totalPayment");
        int totalRoom = (int) session.getAttribute("totalRoom");

        //for debugging purposes only
        System.out.println("guestICNumber: " + guestICNumber);
        System.out.println("reservationID: " + reservationID);
        System.out.println("durationOfStay: " + durationOfStay);
        System.out.println("dateStart: " + dateStart);
        System.out.println("dateEnd: " + dateEnd);
        System.out.println("totalPayment: " + totalPayment);
        System.out.println("total room: " + totalRoom);

        //first part, getting all services and putting them into the dropdown menu
        List <service> services = new ArrayList<service>();
        try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT serviceid, servicename, serviceprice from service where servicetype = ? and servicestatus = ?";
            final var statement = connection.prepareStatement(sql);
            statement.setString(1, "Event Service");
            statement.setString(2, "Available");
            final var resultSet = statement.executeQuery();
            while (resultSet.next()){
                int serviceID = resultSet.getInt("serviceid");
                String serviceName = resultSet.getString("servicename");
                double servicePrice = resultSet.getDouble("serviceprice");

                service service = new service();
                service.setServiceID(serviceID);
                service.setServiceName(serviceName);
                service.setServicePrice(servicePrice);
                
                //debug
                System.out.println("add into array for dropdown");

                services.add(service);
                model.addAttribute("services", services);
            }
            connection.close();
        }
        catch(SQLException e){
            e.printStackTrace();
            // Handle the exception as desired (e.g., show an error message)
            System.out.println("error to execute dropdown menu for service type");

        }

        //second part, table of service that the guest added
        List <service> guestServices = new ArrayList<service>();
        List <reservationService> guestReservationServices = new ArrayList<reservationService>();
        try (Connection connection = dataSource.getConnection()){
            String sqlGuestService = "SELECT service.serviceid, service.servicename, service.servicetype, service.serviceprice, reservationservice.serviceduration, reservationservice.servicequantity "
            + "from service "
            + "JOIN reservationservice ON reservationservice.serviceid = service.serviceid "
            + "JOIN reservation ON reservationservice.reservationid = reservation.reservationid "
            + "WHERE reservation.reservationid = ?";
            final var statementGuestService = connection.prepareStatement(sqlGuestService);
            statementGuestService.setInt(1, reservationID);
            final var resultSetGuestService = statementGuestService.executeQuery();
            System.out.println("pass for getting guest services for this reservationid");

            while (resultSetGuestService.next()){
                int guestServiceID = resultSetGuestService.getInt("serviceid");
                String guestServiceName = resultSetGuestService.getString("servicename");
                String guestServiceType = resultSetGuestService.getString("servicetype");
                double guestServicePrice = resultSetGuestService.getDouble("serviceprice");
                int guestServiceDuration = resultSetGuestService.getInt("serviceduration");
                int guestServiceQuantity = resultSetGuestService.getInt("servicequantity");

                service guestService = new service();
                reservationService guestReservationService = new reservationService();
                guestService.setServiceID(guestServiceID);
                guestService.setServiceName(guestServiceName);
                guestService.setServiceType(guestServiceType);
                guestService.setServicePrice(guestServicePrice);
                guestReservationService.setServiceDuration(guestServiceDuration);
                guestReservationService.setServiceQuantity(guestServiceQuantity);

                guestServices.add(guestService);
                guestReservationServices.add(guestReservationService);
                model.addAttribute("guestServices", guestServices);
                model.addAttribute("guestReservationServices", guestReservationServices);
                System.out.println("service added into list");
            }

            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception as desired (e.g., show an error message)
            System.out.println("error to display list of service that the guest reserve");
        }

        return "guest/guestMakeEventService";
    }

    @PostMapping("/guestMakeEventService")
    public String guestMakeEventService(HttpSession session, @ModelAttribute("guestMakeRoomService")service service, @RequestParam("serviceID") String serviceIDString, @RequestParam("people") int people, @RequestParam("serviceDuration") int serviceDuration){
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        int reservationID = (int) session.getAttribute("reservationID");
        int durationOfStay = (int) session.getAttribute("durationOfStay");
        Date dateStart = (Date) session.getAttribute("dateStart");
        Date dateEnd = (Date) session.getAttribute("dateEnd");
        double totalPayment = (double) session.getAttribute("totalPayment");
        int totalRoom = (int) session.getAttribute("totalRoom");
        int serviceID = Integer.parseInt(serviceIDString);

        //for debugging purposes only
        System.out.println("postmappping guestMakeEventService");
        System.out.println("guestICNumber: " + guestICNumber);
        System.out.println("reservationID: " + reservationID);
        System.out.println("durationOfStay: " + durationOfStay);
        System.out.println("dateStart: " + dateStart);
        System.out.println("dateEnd: " + dateEnd);
        System.out.println("totalPayment: " + totalPayment);
        System.out.println("serviceID: " + serviceID);
        System.out.println("total room: " + totalRoom);
        System.out.println("service id: " + serviceID);

        if (serviceDuration > durationOfStay){
            System.out.println("Service duration cannot exceed Duration of stay");
            return "redirect:/guestMakeEventService";
        }

        try {
            Connection connection = dataSource.getConnection();
            boolean available = isEventServiceAvailable(serviceID, dateStart, dateEnd, connection);

            if (available){
                String sql = "INSERT INTO reservationservice(reservationid, serviceduration, servicequantity, serviceid) VALUES (?,?,?,?)";
                final var statement = connection.prepareStatement(sql);
                
                int serviceQuantity = 1;
                statement.setInt(1, reservationID);
                statement.setInt(2, serviceDuration);
                statement.setInt(3, serviceQuantity);
                statement.setInt(4, serviceID);

                statement.executeUpdate();
                System.out.println("sukses insert into table reservationservice");
            }
            else{
                System.out.println("gagal insert into table reservationservice sebab tak available");
            }
            connection.close();
        }
        catch (Exception e) {
            System.out.println("failed postmapping guestMakeEventService");
            e.printStackTrace();
            return "redirect:/index";
        }

        return "redirect:/guestMakeEventService";
    }

    @GetMapping("/confirmService")
    public String confirmService(HttpSession session){
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        int reservationID = (int) session.getAttribute("reservationID");
        double totalPayment = (double) session.getAttribute("totalPayment");
        try{
            Connection connection = dataSource.getConnection();
            Double totalServicePrice = calculateTotalServicePrice(reservationID, connection);
            totalPayment = totalPayment + totalServicePrice;
            System.out.println("totalServicePrice: " + totalServicePrice);
            System.out.println("totalPayment: " + totalPayment);
            String sqlUpdateTotalPayment = "UPDATE reservation SET totalpayment = ? WHERE reservationid = ?";
            PreparedStatement statementUpdateTotalPayment = connection.prepareStatement(sqlUpdateTotalPayment); 
            statementUpdateTotalPayment.setDouble(1, totalPayment);
            statementUpdateTotalPayment.setInt(2, reservationID);
            statementUpdateTotalPayment.executeUpdate();

        }
        catch (SQLException e){
            e.printStackTrace();
            System.out.println("failed to update total payment");
        }
        return "redirect:/guestRoomReservation";

    }

    @GetMapping("/deleteGuestRoomService")
    public String deleteGuestRoomService(HttpSession session, Model model, @RequestParam("serviceID") int serviceID){
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        int reservationID = (int) session.getAttribute("reservationID");
        int durationOfStay = (int) session.getAttribute("durationOfStay");
        Date dateStart = (Date) session.getAttribute("dateStart");
        Date dateEnd = (Date) session.getAttribute("dateEnd");
        double totalPayment = (double) session.getAttribute("totalPayment");
        int totalRoom = (int) session.getAttribute("totalRoom");

        //for debugging purposes only
        System.out.println("guestICNumber: " + guestICNumber);
        System.out.println("reservationID: " + reservationID);
        System.out.println("durationOfStay: " + durationOfStay);
        System.out.println("dateStart: " + dateStart);
        System.out.println("dateEnd: " + dateEnd);
        System.out.println("totalPayment: " + totalPayment);
        System.out.println("total room: " + totalRoom);
        System.out.println("service id: " + serviceID);

        try (Connection connection = dataSource.getConnection()){
            //delete servvice from the guest reservation list
            final var deleteServiceStatement = connection.prepareStatement("DELETE from reservationservice where reservationid = ? AND serviceid = ?");
            deleteServiceStatement.setInt(1, reservationID);
            deleteServiceStatement.setInt(2, serviceID);
            deleteServiceStatement.executeUpdate();
            connection.close();
            System.out.println("succeed to delete service from the guest service list");
        }
        catch (Exception e){
            System.out.println("failed to delete service from the guest service list");
            e.printStackTrace();
        }
        return "redirect:/guestMakeRoomService";
    }

    @GetMapping("/deleteGuestEventService")
    public String deleteGuestEventService(HttpSession session, Model model, @RequestParam("serviceID") int serviceID){
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        int reservationID = (int) session.getAttribute("reservationID");
        int durationOfStay = (int) session.getAttribute("durationOfStay");
        Date dateStart = (Date) session.getAttribute("dateStart");
        Date dateEnd = (Date) session.getAttribute("dateEnd");
        double totalPayment = (double) session.getAttribute("totalPayment");
        int totalRoom = (int) session.getAttribute("totalRoom");

        //for debugging purposes only
        System.out.println("guestICNumber: " + guestICNumber);
        System.out.println("reservationID: " + reservationID);
        System.out.println("durationOfStay: " + durationOfStay);
        System.out.println("dateStart: " + dateStart);
        System.out.println("dateEnd: " + dateEnd);
        System.out.println("totalPayment: " + totalPayment);
        System.out.println("serviceID: " + serviceID);
        System.out.println("total room: " + totalRoom);
        System.out.println("service id: " + serviceID);

        try (Connection connection = dataSource.getConnection()){
            //delete servvice from the guest reservation list
            final var deleteServiceStatement = connection.prepareStatement("DELETE from reservationservice where reservationid = ? AND serviceid = ?");
            deleteServiceStatement.setInt(1, reservationID);
            deleteServiceStatement.setInt(2, serviceID);
            deleteServiceStatement.executeUpdate();
            connection.close();
            System.out.println("succeed to delete service from the guest service list");
        }
        catch (Exception e){
            System.out.println("failed to delete service from the guest service list");
            e.printStackTrace();
        }
        return "redirect:/guestMakeEventService";
    }

    @GetMapping("/guestRoomReservation")
    public String guestRoomReservation(Model model, HttpSession session){
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        List<reservation> reservations = new ArrayList<reservation>();
        try (Connection connection = dataSource.getConnection()){
            String sql = "SELECT * from reservation where guesticnumber = ? order by reservationid desc";
            final var statement = connection.prepareStatement(sql);
            statement.setString(1, guestICNumber);
            final var resultSet = statement.executeQuery();
            System.out.println("pass try guestRoomReservation >>>>>");

            while (resultSet.next()){
                int reservationID = resultSet.getInt("reservationid");
                int guestQuantity = resultSet.getInt("guestquantity");
                int durationOfStay = resultSet.getInt("durationofstay");
                Date dateStart = resultSet.getDate("datestart");
                Date dateEnd = resultSet.getDate("dateend");
                int totalAdult = resultSet.getInt("totaladult");
                int totalKids= resultSet.getInt("totalkids");
                String reserveStatus = resultSet.getString("reservestatus");
                int totalRoom = resultSet.getInt("totalroom");
                double totalPayment = resultSet.getDouble("totalpayment");

                reservation reservation = new reservation();
                reservation.setReservationID(reservationID);
                reservation.setGuestICNumber(guestICNumber);
                reservation.setGuestQuantity(guestQuantity);
                reservation.setDurationOfStay(durationOfStay);
                reservation.setDateStart(dateStart);
                reservation.setDateEnd(dateEnd);
                reservation.setTotalAdult(totalAdult);
                reservation.setTotalKids(totalKids);
                reservation.setReserveStatus(reserveStatus);
                reservation.setTotalRoom(totalRoom);
                reservation.setTotalPayment(totalPayment);

                reservations.add(reservation);
                model.addAttribute("reservations", reservations);

            }
            connection.close();

        }
        catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception as desired (e.g., show an error message)
            System.out.println("error getting guestRoomReservation");
            return "redirect:/index";
        }
        return "guest/guestRoomReservation";
    }

    @GetMapping("/guestViewRoomReservation")
    public String guestViewRoomReservation(HttpSession session, Model model, @RequestParam("reservationID") int reservationID){
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        System.out.println("guestICNumber: " + guestICNumber);
        try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT * from reservation " +
            "FULL OUTER JOIN roomreservation ON reservation.reservationid = roomreservation.reservationid " +
            "FULL OUTER JOIN reservationservice ON reservation.reservationid = reservationservice.reservationid " +
            "FULL OUTER JOIN room ON roomreservation.roomnum = room.roomnum " +
            "FULL OUTER JOIN service ON reservationservice.serviceid = service.serviceid " +
            "FULL OUTER JOIN guest ON reservation.guestICNumber = guest.guestICNumber " +
            "WHERE reservation.reservationid = ?";
            final var statement = connection.prepareStatement(sql);
            statement.setInt(1, reservationID);
            final var resultSet = statement.executeQuery();

            while(resultSet.next()){
                int guestQuantity = resultSet.getInt("guestQuantity");
                int durationOfStay = resultSet.getInt("durationOfStay");
                Date dateStart = resultSet.getDate("dateStart");
                Date dateEnd = resultSet.getDate("dateEnd");
                int totalAdult = resultSet.getInt("totalAdult");
                int totalKids = resultSet.getInt("totalKids");
                int totalRoom = resultSet.getInt("totalRoom");
                double totalPayment = resultSet.getDouble("totalPayment");
                String reserveStatus = resultSet.getString("reserveStatus");

                reservation reservation = new reservation();
                reservation.setReservationID(reservationID);
                reservation.setGuestICNumber(guestICNumber);
                reservation.setGuestQuantity(guestQuantity);
                reservation.setDurationOfStay(durationOfStay);
                reservation.setDateStart(dateStart);
                reservation.setDateEnd(dateEnd);
                reservation.setTotalAdult(totalAdult);
                reservation.setTotalKids(totalKids);
                reservation.setReserveStatus(reserveStatus);
                reservation.setTotalRoom(totalRoom);
                reservation.setTotalPayment(totalPayment);
                model.addAttribute("reservation", reservation);

            }
            connection.close();
        }
        catch (SQLException e){
            System.out.println("failed at retrieving reservation details");
            e.printStackTrace();
            return "redirect:/index";
        }

        try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT * from room " +
            "JOIN roomreservation ON room.roomnum = roomreservation.roomnum " +
            "JOIN reservation ON reservation.reservationid = roomreservation.reservationid " +
            "WHERE reservation.reservationid = ?";
            final var statement = connection.prepareStatement(sql);
            statement.setInt(1, reservationID);
            final var resultSet = statement.executeQuery();

            List<room> rooms = new ArrayList<room>();

            while (resultSet.next()){ 
                String roomNum = resultSet.getString("roomNum");
                String roomType = resultSet.getString("roomType");
                double roomRate = resultSet.getDouble("roomRate");

                room room = new room();
                room.setRoomType(roomType);
                room.setRoomRate(roomRate);
                room.setRoomNum(roomNum);
                rooms.add(room);
                model.addAttribute("rooms", rooms);
            }
            connection.close();
            }
        catch (SQLException e){
                System.out.println("failed at retrieving room details for this reservation");
                e.printStackTrace();
                return "redirect:/index";
            }

            try {
                Connection connection = dataSource.getConnection();
                String sql = "SELECT * from guest " +
                "JOIN reservation ON guest.guestICNumber = reservation.guestICNumber " +
                "WHERE reservation.reservationid = ?";
                final var statement = connection.prepareStatement(sql);
                statement.setInt(1, reservationID);
                final var resultSet = statement.executeQuery();

                if (resultSet.next()){
                guest guest = new guest();
                guest.setGuestName(resultSet.getString("guestName"));
                model.addAttribute("guest", guest);
            }
                connection.close();
            }
            catch (SQLException e){
                System.out.println("failed at retrieving guest details for this reservation");
                e.printStackTrace();
                return "redirect:/index";
            }

            try {
                Connection connection = dataSource.getConnection();
                String sql = "SELECT * from service " +
                "JOIN reservationservice ON service.serviceid = reservationservice.serviceid " +
                "JOIN reservation ON reservation.reservationid = reservationservice.reservationid " +
                "WHERE reservation.reservationid = ?";
                final var statement = connection.prepareStatement(sql);
                statement.setInt(1, reservationID);
                final var resultSet = statement.executeQuery();
                
                List <service> services = new ArrayList<service>();
                while (resultSet.next()){
                String serviceName = resultSet.getString("servicename");
                double servicePrice = resultSet.getDouble("serviceprice");
                int serviceQuantity = resultSet.getInt("serviceQuantity");
                int serviceDuration = resultSet.getInt("serviceDuration");

                service service = new service();
                service.setServiceName(serviceName);
                service.setServicePrice(servicePrice);
                service.setServiceQuantity(serviceQuantity);
                service.setServiceDuration(serviceDuration);

                System.out.println("service added into array for guestViewRoomReservation");
                services.add(service);
                model.addAttribute("services", services);
                }
                connection.close();
            }
            catch (SQLException e){
                System.out.println("failed at retrieving service details for this reservation");
                e.printStackTrace();
                return "redirect:/index";
            }

        return "guest/guestViewRoomReservation";
    }

    @GetMapping("/staffReservationList")
    public String staffReservationList(Model model, HttpSession session){
        String staffICNumber = (String) session.getAttribute("staffICNumber");
        List<reservation> reservations = new ArrayList<reservation>();
        try (Connection connection = dataSource.getConnection()){
            String sql = "SELECT * from reservation order by reservationid desc";
            final var statement = connection.prepareStatement(sql);
            final var resultSet = statement.executeQuery();
            System.out.println("pass try staffReservationList >>>>>");

            while (resultSet.next()){
                String guestICNumber = resultSet.getString("guestICNumber");
                int reservationID = resultSet.getInt("reservationid");
                int guestQuantity = resultSet.getInt("guestquantity");
                int durationOfStay = resultSet.getInt("durationofstay");
                Date dateStart = resultSet.getDate("datestart");
                Date dateEnd = resultSet.getDate("dateend");
                int totalAdult = resultSet.getInt("totaladult");
                int totalKids= resultSet.getInt("totalkids");
                String reserveStatus = resultSet.getString("reservestatus");
                int totalRoom = resultSet.getInt("totalroom");
                double totalPayment = resultSet.getDouble("totalpayment");

                reservation reservation = new reservation();
                reservation.setReservationID(reservationID);
                reservation.setGuestICNumber(guestICNumber);
                reservation.setGuestQuantity(guestQuantity);
                reservation.setDurationOfStay(durationOfStay);
                reservation.setDateStart(dateStart);
                reservation.setDateEnd(dateEnd);
                reservation.setTotalAdult(totalAdult);
                reservation.setTotalKids(totalKids);
                reservation.setReserveStatus(reserveStatus);
                reservation.setTotalRoom(totalRoom);
                reservation.setTotalPayment(totalPayment);

                reservations.add(reservation);
                model.addAttribute("reservations", reservations);

            }
            connection.close();

        }
        catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception as desired (e.g., show an error message)
            System.out.println("error getting staffReservationList");
            return "redirect:/index";
        }
        return "staff/staffReservationList";
    }

    @PostMapping("/staffReservationList")
    public String staffReservationList(Model model, HttpSession session, @RequestParam("searchInput") String searchInput){
        String staffICNumber = (String) session.getAttribute("staffICNumber");
        searchInput = searchInput.trim();
        List<reservation> reservations = new ArrayList<reservation>();
        try (Connection connection = dataSource.getConnection()){
            String sql = "SELECT * FROM reservation WHERE "
            + "reservationid = ? "
            + "OR lower(reservestatus) = lower(?) "
            + "OR datestart::text ILIKE ? "
            + "OR dateend::text ILIKE ? "
            + "ORDER BY reservationid DESC";
            final var statement = connection.prepareStatement(sql);

            // Setting parameters for reservation ID and reservation status
            int searchInputInt = 0;
            try {
                searchInputInt = Integer.parseInt(searchInput);
                statement.setInt(1, searchInputInt);
            } catch (NumberFormatException e) {
                // If searchInput is not a number, set reservation ID parameter as 0
                statement.setInt(1, 0);
            }
            statement.setString(2, searchInput);
            // Setting parameters for date start and date end
            statement.setString(3, "%" + searchInput + "%");
            statement.setString(4, "%" + searchInput + "%");

            final var resultSet = statement.executeQuery();
            System.out.println("pass try managerReservationList for search >>>>>");

            while (resultSet.next()){
                int reservationID = resultSet.getInt("reservationid");
                String guestICNumber = resultSet.getString("guestICNumber");
                int guestQuantity = resultSet.getInt("guestquantity");
                int durationOfStay = resultSet.getInt("durationofstay");
                Date dateStart = resultSet.getDate("datestart");
                Date dateEnd = resultSet.getDate("dateend");
                int totalAdult = resultSet.getInt("totaladult");
                int totalKids= resultSet.getInt("totalkids");
                String reserveStatus = resultSet.getString("reservestatus");
                int totalRoom = resultSet.getInt("totalroom");
                double totalPayment = resultSet.getDouble("totalpayment");

                reservation reservation = new reservation();
                reservation.setReservationID(reservationID);
                reservation.setGuestICNumber(guestICNumber);
                reservation.setGuestQuantity(guestQuantity);
                reservation.setDurationOfStay(durationOfStay);
                reservation.setDateStart(dateStart);
                reservation.setDateEnd(dateEnd);
                reservation.setTotalAdult(totalAdult);
                reservation.setTotalKids(totalKids);
                reservation.setReserveStatus(reserveStatus);
                reservation.setTotalRoom(totalRoom);
                reservation.setTotalPayment(totalPayment);

                reservations.add(reservation);
                model.addAttribute("reservations", reservations);
                if (reservations.isEmpty()) {
                    model.addAttribute("messageNoResult", "No results found for \"" + searchInput + "\"");
                }
            }
            connection.close();

        }
        catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception as desired (e.g., show an error message)
            System.out.println("error getting staffReservationList");
            return "redirect:/index";
        }
        return "staff/staffReservationList";
    }

    @GetMapping("/managerUpdateStatus")
    public String managerUpdateStatus(Model model, HttpSession session){
        String staffICNumber = (String) session.getAttribute("staffICNumber");
        List<reservation> reservations = new ArrayList<reservation>();
        try (Connection connection = dataSource.getConnection()){
            String sql = "SELECT * from reservation order by reservationid desc";
            final var statement = connection.prepareStatement(sql);
            final var resultSet = statement.executeQuery();
            System.out.println("pass try managerReservationList >>>>>");

            while (resultSet.next()){
                int reservationID = resultSet.getInt("reservationid");
                String guestICNumber = resultSet.getString("guestICNumber");
                int guestQuantity = resultSet.getInt("guestquantity");
                int durationOfStay = resultSet.getInt("durationofstay");
                Date dateStart = resultSet.getDate("datestart");
                Date dateEnd = resultSet.getDate("dateend");
                int totalAdult = resultSet.getInt("totaladult");
                int totalKids= resultSet.getInt("totalkids");
                String reserveStatus = resultSet.getString("reservestatus");
                int totalRoom = resultSet.getInt("totalroom");
                double totalPayment = resultSet.getDouble("totalpayment");

                reservation reservation = new reservation();
                reservation.setReservationID(reservationID);
                reservation.setGuestICNumber(guestICNumber);
                reservation.setGuestQuantity(guestQuantity);
                reservation.setDurationOfStay(durationOfStay);
                reservation.setDateStart(dateStart);
                reservation.setDateEnd(dateEnd);
                reservation.setTotalAdult(totalAdult);
                reservation.setTotalKids(totalKids);
                reservation.setReserveStatus(reserveStatus);
                reservation.setTotalRoom(totalRoom);
                reservation.setTotalPayment(totalPayment);

                reservations.add(reservation);
                model.addAttribute("reservations", reservations);
                if (reservations.isEmpty()) {
                    model.addAttribute("messageNoResult", "No results found");
                }

            }
            connection.close();

        }
        catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception as desired (e.g., show an error message)
            System.out.println("error getting managerReservationList");
            return "redirect:/index";
        }
        return "manager/managerUpdateStatus";
    }   
    
    @PostMapping("/managerUpdateStatus")
    public String managerUpdateStatus(Model model, HttpSession session, @RequestParam("searchInput") String searchInput){
        String staffICNumber = (String) session.getAttribute("staffICNumber");
        searchInput = searchInput.trim();
        List<reservation> reservations = new ArrayList<reservation>();
        try (Connection connection = dataSource.getConnection()){
            String sql = "SELECT * FROM reservation WHERE "
            + "reservationid = ? "
            + "OR lower(reservestatus) = lower(?) "
            + "OR datestart::text ILIKE ? "
            + "OR dateend::text ILIKE ? "
            + "ORDER BY reservationid DESC";
            final var statement = connection.prepareStatement(sql);

            // Setting parameters for reservation ID and reservation status
            int searchInputInt = 0;
            try {
                searchInputInt = Integer.parseInt(searchInput);
                statement.setInt(1, searchInputInt);
            } catch (NumberFormatException e) {
                // If searchInput is not a number, set reservation ID parameter as 0
                statement.setInt(1, 0);
            }
            statement.setString(2, searchInput);
            // Setting parameters for date start and date end
            statement.setString(3, "%" + searchInput + "%");
            statement.setString(4, "%" + searchInput + "%");

            final var resultSet = statement.executeQuery();
            System.out.println("pass try managerReservationList for search >>>>>");

            while (resultSet.next()){
                int reservationID = resultSet.getInt("reservationid");
                String guestICNumber = resultSet.getString("guestICNumber");
                int guestQuantity = resultSet.getInt("guestquantity");
                int durationOfStay = resultSet.getInt("durationofstay");
                Date dateStart = resultSet.getDate("datestart");
                Date dateEnd = resultSet.getDate("dateend");
                int totalAdult = resultSet.getInt("totaladult");
                int totalKids= resultSet.getInt("totalkids");
                String reserveStatus = resultSet.getString("reservestatus");
                int totalRoom = resultSet.getInt("totalroom");
                double totalPayment = resultSet.getDouble("totalpayment");

                reservation reservation = new reservation();
                reservation.setReservationID(reservationID);
                reservation.setGuestICNumber(guestICNumber);
                reservation.setGuestQuantity(guestQuantity);
                reservation.setDurationOfStay(durationOfStay);
                reservation.setDateStart(dateStart);
                reservation.setDateEnd(dateEnd);
                reservation.setTotalAdult(totalAdult);
                reservation.setTotalKids(totalKids);
                reservation.setReserveStatus(reserveStatus);
                reservation.setTotalRoom(totalRoom);
                reservation.setTotalPayment(totalPayment);

                reservations.add(reservation);
                model.addAttribute("reservations", reservations);
                if (reservations.isEmpty()) {
                    model.addAttribute("messageNoResult", "No results found for \"" + searchInput + "\"");
                }
            }
            connection.close();

        }
        catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception as desired (e.g., show an error message)
            System.out.println("error getting managerReservationList for search");
            return "redirect:/index";
        }
        return "manager/managerUpdateStatus";
    }

   @GetMapping("/managerViewReservation")
    public String managerViewReservation(HttpSession session, Model model, @RequestParam("reservationID") int reservationID){
        String staffICNumber = (String) session.getAttribute("staffICNumber");
        System.out.println("staffICNumber: " + staffICNumber);
        try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT * from reservation " +
            "FULL OUTER JOIN roomreservation ON reservation.reservationid = roomreservation.reservationid " +
            "FULL OUTER JOIN reservationservice ON reservation.reservationid = reservationservice.reservationid " +
            "FULL OUTER JOIN room ON roomreservation.roomnum = room.roomnum " +
            "FULL OUTER JOIN service ON reservationservice.serviceid = service.serviceid " +
            "FULL OUTER JOIN guest ON reservation.guestICNumber = guest.guestICNumber " +
            "WHERE reservation.reservationid = ?";
            final var statement = connection.prepareStatement(sql);
            statement.setInt(1, reservationID);
            final var resultSet = statement.executeQuery();

            while(resultSet.next()){
                int guestQuantity = resultSet.getInt("guestQuantity");
                int durationOfStay = resultSet.getInt("durationOfStay");
                Date dateStart = resultSet.getDate("dateStart");
                Date dateEnd = resultSet.getDate("dateEnd");
                int totalAdult = resultSet.getInt("totalAdult");
                int totalKids = resultSet.getInt("totalKids");
                int totalRoom = resultSet.getInt("totalRoom");
                double totalPayment = resultSet.getDouble("totalPayment");
                String reserveStatus = resultSet.getString("reserveStatus");

                reservation reservation = new reservation();
                reservation.setReservationID(reservationID);
                reservation.setGuestQuantity(guestQuantity);
                reservation.setDurationOfStay(durationOfStay);
                reservation.setDateStart(dateStart);
                reservation.setDateEnd(dateEnd);
                reservation.setTotalAdult(totalAdult);
                reservation.setTotalKids(totalKids);
                reservation.setReserveStatus(reserveStatus);
                reservation.setTotalRoom(totalRoom);
                reservation.setTotalPayment(totalPayment);
                model.addAttribute("reservation", reservation);

            }
            connection.close();
        }
        catch (SQLException e){
            System.out.println("failed at retrieving reservation details");
            e.printStackTrace();
            return "redirect:/index";
        }

        try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT * from room " +
            "JOIN roomreservation ON room.roomnum = roomreservation.roomnum " +
            "JOIN reservation ON reservation.reservationid = roomreservation.reservationid " +
            "WHERE reservation.reservationid = ?";
            final var statement = connection.prepareStatement(sql);
            statement.setInt(1, reservationID);
            final var resultSet = statement.executeQuery();

            List<room> rooms = new ArrayList<room>();

            while (resultSet.next()){ 
                String roomNum = resultSet.getString("roomNum");
                String roomType = resultSet.getString("roomType");
                double roomRate = resultSet.getDouble("roomRate");

                room room = new room();
                room.setRoomType(roomType);
                room.setRoomRate(roomRate);
                room.setRoomNum(roomNum);
                rooms.add(room);
                model.addAttribute("rooms", rooms);
            }
            connection.close();
            }
        catch (SQLException e){
                System.out.println("failed at retrieving room details for this reservation");
                e.printStackTrace();
                return "redirect:/index";
            }

            try {
                Connection connection = dataSource.getConnection();
                String sql = "SELECT * from guest " +
                "JOIN reservation ON guest.guestICNumber = reservation.guestICNumber " +
                "WHERE reservation.reservationid = ?";
                final var statement = connection.prepareStatement(sql);
                statement.setInt(1, reservationID);
                final var resultSet = statement.executeQuery();

                if (resultSet.next()){
                guest guest = new guest();
                guest.setGuestICNumber(resultSet.getString("guestICNumber"));
                guest.setGuestName(resultSet.getString("guestName"));
                model.addAttribute("guest", guest);
            }
                connection.close();
            }
            catch (SQLException e){
                System.out.println("failed at retrieving guest details for this reservation");
                e.printStackTrace();
                return "redirect:/index";
            }

            try {
                Connection connection = dataSource.getConnection();
                String sql = "SELECT * from service " +
                "JOIN reservationservice ON service.serviceid = reservationservice.serviceid " +
                "JOIN reservation ON reservation.reservationid = reservationservice.reservationid " +
                "WHERE reservation.reservationid = ?";
                final var statement = connection.prepareStatement(sql);
                statement.setInt(1, reservationID);
                final var resultSet = statement.executeQuery();
                
                List <service> services = new ArrayList<service>();
                while (resultSet.next()){
                String serviceName = resultSet.getString("servicename");
                double servicePrice = resultSet.getDouble("serviceprice");
                int serviceQuantity = resultSet.getInt("serviceQuantity");
                int serviceDuration = resultSet.getInt("serviceDuration");

                service service = new service();
                service.setServiceName(serviceName);
                service.setServicePrice(servicePrice);
                service.setServiceQuantity(serviceQuantity);
                service.setServiceDuration(serviceDuration);

                System.out.println("service added into array for guestViewRoomReservation");
                services.add(service);
                model.addAttribute("services", services);
                }
                connection.close();
            }
            catch (SQLException e){
                System.out.println("failed at retrieving service details for this reservation");
                e.printStackTrace();
                return "redirect:/index";
            }

        return "manager/managerViewReservation";
    }

    @GetMapping("/staffViewReservation")
    public String staffViewReservation(HttpSession session, Model model, @RequestParam("reservationID") int reservationID){
        String staffICNumber = (String) session.getAttribute("staffICNumber");
        System.out.println("staffICNumber: " + staffICNumber);
        try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT * from reservation " +
            "FULL OUTER JOIN roomreservation ON reservation.reservationid = roomreservation.reservationid " +
            "FULL OUTER JOIN reservationservice ON reservation.reservationid = reservationservice.reservationid " +
            "FULL OUTER JOIN room ON roomreservation.roomnum = room.roomnum " +
            "FULL OUTER JOIN service ON reservationservice.serviceid = service.serviceid " +
            "FULL OUTER JOIN guest ON reservation.guestICNumber = guest.guestICNumber " +
            "WHERE reservation.reservationid = ?";
            final var statement = connection.prepareStatement(sql);
            statement.setInt(1, reservationID);
            final var resultSet = statement.executeQuery();

            while(resultSet.next()){
                int guestQuantity = resultSet.getInt("guestQuantity");
                int durationOfStay = resultSet.getInt("durationOfStay");
                Date dateStart = resultSet.getDate("dateStart");
                Date dateEnd = resultSet.getDate("dateEnd");
                int totalAdult = resultSet.getInt("totalAdult");
                int totalKids = resultSet.getInt("totalKids");
                int totalRoom = resultSet.getInt("totalRoom");
                double totalPayment = resultSet.getDouble("totalPayment");
                String reserveStatus = resultSet.getString("reserveStatus");

                reservation reservation = new reservation();
                reservation.setReservationID(reservationID);
                reservation.setGuestQuantity(guestQuantity);
                reservation.setDurationOfStay(durationOfStay);
                reservation.setDateStart(dateStart);
                reservation.setDateEnd(dateEnd);
                reservation.setTotalAdult(totalAdult);
                reservation.setTotalKids(totalKids);
                reservation.setReserveStatus(reserveStatus);
                reservation.setTotalRoom(totalRoom);
                reservation.setTotalPayment(totalPayment);
                model.addAttribute("reservation", reservation);

            }
            connection.close();
        }
        catch (SQLException e){
            System.out.println("failed at retrieving reservation details");
            e.printStackTrace();
            return "redirect:/index";
        }

        try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT * from room " +
            "JOIN roomreservation ON room.roomnum = roomreservation.roomnum " +
            "JOIN reservation ON reservation.reservationid = roomreservation.reservationid " +
            "WHERE reservation.reservationid = ?";
            final var statement = connection.prepareStatement(sql);
            statement.setInt(1, reservationID);
            final var resultSet = statement.executeQuery();

            List<room> rooms = new ArrayList<room>();

            while (resultSet.next()){ 
                String roomNum = resultSet.getString("roomNum");
                String roomType = resultSet.getString("roomType");
                double roomRate = resultSet.getDouble("roomRate");

                room room = new room();
                room.setRoomType(roomType);
                room.setRoomRate(roomRate);
                room.setRoomNum(roomNum);
                rooms.add(room);
                model.addAttribute("rooms", rooms);
            }
            connection.close();
            }
        catch (SQLException e){
                System.out.println("failed at retrieving room details for this reservation");
                e.printStackTrace();
                return "redirect:/index";
            }

            try {
                Connection connection = dataSource.getConnection();
                String sql = "SELECT * from guest " +
                "JOIN reservation ON guest.guestICNumber = reservation.guestICNumber " +
                "WHERE reservation.reservationid = ?";
                final var statement = connection.prepareStatement(sql);
                statement.setInt(1, reservationID);
                final var resultSet = statement.executeQuery();

                if (resultSet.next()){
                guest guest = new guest();
                guest.setGuestICNumber(resultSet.getString("guestICNumber"));
                guest.setGuestName(resultSet.getString("guestName"));
                model.addAttribute("guest", guest);
            }
                connection.close();
            }
            catch (SQLException e){
                System.out.println("failed at retrieving guest details for this reservation");
                e.printStackTrace();
                return "redirect:/index";
            }

            try {
                Connection connection = dataSource.getConnection();
                String sql = "SELECT * from service " +
                "JOIN reservationservice ON service.serviceid = reservationservice.serviceid " +
                "JOIN reservation ON reservation.reservationid = reservationservice.reservationid " +
                "WHERE reservation.reservationid = ?";
                final var statement = connection.prepareStatement(sql);
                statement.setInt(1, reservationID);
                final var resultSet = statement.executeQuery();
                
                List <service> services = new ArrayList<service>();
                while (resultSet.next()){
                String serviceName = resultSet.getString("servicename");
                double servicePrice = resultSet.getDouble("serviceprice");
                int serviceQuantity = resultSet.getInt("serviceQuantity");
                int serviceDuration = resultSet.getInt("serviceDuration");

                service service = new service();
                service.setServiceName(serviceName);
                service.setServicePrice(servicePrice);
                service.setServiceQuantity(serviceQuantity);
                service.setServiceDuration(serviceDuration);

                System.out.println("service added into array for guestViewRoomReservation");
                services.add(service);
                model.addAttribute("services", services);
                }
                connection.close();
            }
            catch (SQLException e){
                System.out.println("failed at retrieving service details for this reservation");
                e.printStackTrace();
                return "redirect:/index";
            }

        return "staff/staffViewReservation";
    }

    @GetMapping("/viewReceipt")
    public String viewReceipt(HttpSession session, Model model, @RequestParam("reservationID") int reservationID){
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        String staffICNumber = (String) session.getAttribute("staffICNumber");
        System.out.println("guestICNumber: " + guestICNumber);
        System.out.println("staffICNumber: " + staffICNumber);
        try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT * from reservation " +
            "FULL OUTER JOIN roomreservation ON reservation.reservationid = roomreservation.reservationid " +
            "FULL OUTER JOIN reservationservice ON reservation.reservationid = reservationservice.reservationid " +
            "FULL OUTER JOIN room ON roomreservation.roomnum = room.roomnum " +
            "FULL OUTER JOIN service ON reservationservice.serviceid = service.serviceid " +
            "FULL OUTER JOIN guest ON reservation.guestICNumber = guest.guestICNumber " +
            "WHERE reservation.reservationid = ?";
            final var statement = connection.prepareStatement(sql);
            statement.setInt(1, reservationID);
            final var resultSet = statement.executeQuery();

            List<room> rooms = new ArrayList<room>();
            List <service> services = new ArrayList<service>();
            while(resultSet.next()){
                guestICNumber = resultSet.getString("guestICNumber");
                String guestName = resultSet.getString("guestName");
                int guestQuantity = resultSet.getInt("guestQuantity");
                int durationOfStay = resultSet.getInt("durationOfStay");
                Date dateStart = resultSet.getDate("dateStart");
                Date dateEnd = resultSet.getDate("dateEnd");
                String roomNum = resultSet.getString("roomNum");
                String roomType = resultSet.getString("roomType");
                double roomRate = resultSet.getDouble("roomRate");
                int totalAdult = resultSet.getInt("totalAdult");
                int totalKids = resultSet.getInt("totalKids");
                int totalRoom = resultSet.getInt("totalRoom");
                double totalPayment = resultSet.getDouble("totalPayment");
                String reserveStatus = resultSet.getString("reserveStatus");

                reservation reservation = new reservation();
                reservation.setReservationID(reservationID);
                reservation.setGuestICNumber(guestICNumber);
                reservation.setGuestQuantity(guestQuantity);
                reservation.setDurationOfStay(durationOfStay);
                reservation.setDateStart(dateStart);
                reservation.setDateEnd(dateEnd);
                reservation.setTotalAdult(totalAdult);
                reservation.setTotalKids(totalKids);
                reservation.setReserveStatus(reserveStatus);
                reservation.setTotalRoom(totalRoom);
                reservation.setTotalPayment(totalPayment);
                model.addAttribute("reservation", reservation);

                room room = new room();
                room.setRoomType(roomType);
                room.setRoomRate(roomRate);
                room.setRoomNum(roomNum);
                rooms.add(room);
                model.addAttribute("room", room);

                guest guest = new guest();
                guest.setGuestName(resultSet.getString("guestName"));
                model.addAttribute("guest", guest);

                String serviceName = resultSet.getString("servicename");
                double servicePrice = resultSet.getDouble("serviceprice");
                int serviceQuantity = resultSet.getInt("serviceQuantity");
                int serviceDuration = resultSet.getInt("serviceDuration");

                service service = new service();
                service.setServiceName(serviceName);
                service.setServicePrice(servicePrice);
                service.setServiceQuantity(serviceQuantity);
                service.setServiceDuration(serviceDuration);

                System.out.println("service added into array for generateReceipt");
                services.add(service);
            }
            model.addAttribute("rooms", rooms);
            model.addAttribute("services", services);

            connection.close();

        }
        catch (SQLException e){
            System.out.println("failed at generateReceipt");
            e.printStackTrace();
            return "redirect:/index";
        }

        return "viewReceipt";
    }

    @PostMapping("/managerUpdateReservationStatus")
    public String managerUpdateReservationStatus(HttpSession session, @RequestParam("reserveStatus") String reserveStatus, @RequestParam("reservationID") String reservationIDString){
        String staffICNumber = (String) session.getAttribute("staffICNumber");
        System.out.println("pass manager update status<<<<<<");
        int reservationID = Integer.parseInt(reservationIDString);

        try (Connection connection = dataSource.getConnection()){
            String sql = "UPDATE reservation set reservestatus=? where reservationid=?";
            final var statement = connection.prepareStatement(sql);
            statement.setString(1, reserveStatus);
            statement.setInt(2, reservationID);
            final var resultSet = statement.executeUpdate();
            connection.close();
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("failed to update status");
        }
        System.out.println("sukses update status");
        return "redirect:/managerUpdateStatus";

    }

    @PostMapping("/generateReport")
    public String generateReport(HttpSession session, Model model, @RequestParam("month") String monthString){
        String guestICNumber = (String) session.getAttribute("guestICNumber");
        String staffICNumber = (String) session.getAttribute("staffICNumber");
        System.out.println("guestICNumber: " + guestICNumber);
        System.out.println("staffICNumber: " + staffICNumber);
        int month = Integer.parseInt(monthString);

        try (Connection connection = dataSource.getConnection()){
            String sql = "SELECT r.reservationid, r.datestart, r.dateend, COUNT(rr.roomnum) AS totalroom, room.roomtype, r.durationofstay, r.totalpayment, r.reservestatus " +
            "FROM reservation r " +
            "JOIN roomreservation rr ON r.reservationid = rr.reservationid " +
            "JOIN room ON room.roomnum = rr.roomnum " +
            "WHERE EXTRACT(MONTH FROM r.datestart) = ? " +
            "GROUP BY r.reservationid, r.datestart, r.dateend, r.durationofstay, room.roomtype, r.totalpayment, r.reservestatus";

            final var statement = connection.prepareStatement(sql);
            statement.setInt(1, month);
            final var resultSet = statement.executeQuery();

            List<Map<String, Object>> reports = new ArrayList<>();
            double totalPaymentReport = 0;
            while(resultSet.next()){
                int reservationID = resultSet.getInt("reservationid");
                int durationOfStay = resultSet.getInt("durationOfStay");
                Date dateStart = resultSet.getDate("dateStart");
                Date dateEnd = resultSet.getDate("dateEnd");
                String roomType = resultSet.getString("roomType");
                int totalRoom = resultSet.getInt("totalRoom");
                double totalPayment = resultSet.getDouble("totalPayment");
                String reserveStatus = resultSet.getString("reserveStatus");

                Map<String, Object> report = new HashMap<>();
                report.put("reservationID", reservationID);
                report.put("durationOfStay", durationOfStay);
                report.put("dateStart", dateStart);
                report.put("dateEnd", dateEnd);
                report.put("roomType", roomType);
                report.put("totalRoom", totalRoom);
                report.put("totalPayment", totalPayment);
                report.put("reserveStatus", reserveStatus);

                reports.add(report);

                totalPaymentReport = totalPaymentReport + totalPayment;
            }
            model.addAttribute("reports", reports);
            session.setAttribute("totalPaymentReport", totalPaymentReport);
            connection.close();
        }
        catch(Exception e){
            e.printStackTrace();
            System.out.println("failed to generate report");
        }

        if (month==01){ 
            monthString = "January";
        }
        if(month==02){ 
            monthString = "February";
        } 
        if(month==03){ 
            monthString = "March";
        } 
        if(month==04){ 
            monthString = "April";
        } 
        if(month==05){ 
            monthString = "May";
        } 
        if(month==06){ 
            monthString = "June";
        } 
        if(month==07){ 
            monthString = "July";
        } 
        if(month==8){ 
            monthString = "August";
        } 
        if(month==9){ 
            monthString = "September";
        } 
        if(month==10){ 
            monthString = "October";
        } 
        if(month==11){ 
            monthString = "November";
        } 
        if(month==12){ 
            monthString = "December";
        } 

        session.setAttribute("month", monthString);
        return "reservationReport";

    }

  @GetMapping("/managerHome")
    public String managerHome(@RequestParam(name = "success", required = false) Boolean success, HttpSession session) {
    String staffICNumber = (String) session.getAttribute("staffICNumber") ;
    double totalStandard = 0;
    double totalDeluxe = 0;
      try (Connection connection = dataSource.getConnection()){
        String sqlStandard = "select totalpayment from reservation r "
        + "JOIN roomreservation rr ON r.reservationid = rr.reservationid "
        + "JOIN room ON rr.roomnum = room.roomnum "
        + "WHERE room.roomtype = ? AND r.reservestatus = ?";
        final var statement = connection.prepareStatement(sqlStandard);
        statement.setString(1, "Standard Room");
        statement.setString(2, "Paid");
        final var resultSet = statement.executeQuery();
        while (resultSet.next()){
            double totalPayment = resultSet.getDouble("totalpayment");
            totalStandard = totalStandard + totalPayment;
        }
        session.setAttribute("totalStandard", totalStandard);
        connection.close();
        
      }
      catch (SQLException e){
        e.printStackTrace();
        session.setAttribute("messege", e);
      }

      try(Connection connection = dataSource.getConnection()){
        String sqlDeluxe = "select totalpayment from reservation r "
        + "JOIN roomreservation rr ON r.reservationid = rr.reservationid "
        + "JOIN room ON rr.roomnum = room.roomnum "
        + "WHERE room.roomtype = ? AND r.reservestatus=?";
        final var statement = connection.prepareStatement(sqlDeluxe);
        statement.setString(1, "Deluxe Room");
        statement.setString(2, "Paid");
        final var resultSet = statement.executeQuery();
        while (resultSet.next()){
            double totalPayment = resultSet.getDouble("totalpayment");
            totalDeluxe = totalDeluxe + totalPayment;
        }
        session.setAttribute("totalDeluxe", totalDeluxe);
        connection.close();
        
      }
      catch (SQLException e){
        e.printStackTrace();
        session.setAttribute("messege", e);
      }
      
    double totalOverall = totalStandard + totalDeluxe;
    session.setAttribute("totalOverall", totalOverall);
    return "manager/managerHome";
}
}
