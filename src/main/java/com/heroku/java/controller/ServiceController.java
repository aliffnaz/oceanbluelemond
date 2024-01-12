package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.heroku.java.model.*;
import com.heroku.java.model.eventService;
import com.heroku.java.model.roomService;
import com.heroku.java.model.service;
import com.heroku.java.model.room;

import jakarta.servlet.http.HttpSession;

import java.sql.*;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Map;

import java.util.List; 

@Controller
public class ServiceController {

    private final DataSource dataSource;

    @Autowired
    public ServiceController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/managerAddService")
    public String managerAddService() {
        return "manager/managerAddService";
    }

    @PostMapping("/managerAddService")
    public String managerAddService(Model model, @ModelAttribute("managerAddService") service service, roomService roomService, eventService eventService) {

        try {
            Connection connection = dataSource.getConnection();
            String sql = "INSERT INTO service(serviceID, serviceName, serviceType, servicePrice, serviceStatus) VALUES(?, ?, ?, ?, ?)";
            final var statement = connection.prepareStatement(sql);

            String serviceID=service.getServiceID();
            String serviceName=service.getServiceName();
            String serviceType=service.getServiceType();
            String servicePrice=service.getServicePrice();
            String serviceStatus=service.getServiceStatus();

            if (serviceType.equalsIgnoreCase("roomService")){
            serviceType = "Room Service";}
            else {
            serviceType = "Event Service";}

            
            statement.setString(1, serviceID);
            statement.setString(2, serviceName);
            statement.setString(3, serviceType);
            statement.setString(4, servicePrice);
            statement.setString(5, serviceStatus);

            statement.executeUpdate();

            // Get id from database for sql 2 from sql 1
            String sql1 = "SELECT serviceid, servicename, servicetype, serviceprice, servicestatus FROM service where serviceid=?";
            final var stmt = connection.prepareStatement(sql1);
            stmt.setString(1, serviceID);
            final var resultSet = stmt.executeQuery();

                // Update fields specific to "roomService" or "eventService" based on the service type
                if (serviceType.equalsIgnoreCase("Room Service")) {
                    String roomServiceSql = "INSERT INTO roomService(serviceid, balance) VALUES (?, ?)";
                    final var roomServiceStatement = connection.prepareStatement(roomServiceSql);
                    roomServiceStatement.setString(1, serviceID);
                    roomServiceStatement.setString(2, roomService.getBalance());

                    roomServiceStatement.executeUpdate();
                } else if (serviceType.equalsIgnoreCase("Event Service")) {
                    String eventServiceSql = "INSERT INTO eventService(serviceid, eventcapacity) VALUES (?, ?)";
                    final var eventServiceStatement = connection.prepareStatement(eventServiceSql);
                    eventServiceStatement.setString(1, serviceID);
                    eventServiceStatement.setString(2, eventService.getEventCapacity());

                    eventServiceStatement.executeUpdate();
                }
                model.addAttribute("success", true);

            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return "redirect:/managerAddService?success=false";
        }

        return "redirect:/managerServiceList?success=true";
    }

    @GetMapping("/managerServiceList")
    public String managerServiceList(Model model) {
        List<service> services = new ArrayList<>();

        try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT serviceid, servicename, servicetype, serviceprice, servicestatus FROM service ORDER BY serviceName";
            final var statement = connection.createStatement();
            final var resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                String serviceID = resultSet.getString("serviceid");
                String serviceName = resultSet.getString("servicename");
                String serviceType = resultSet.getString("servicetype");
                String servicePrice = resultSet.getString("serviceprice");
                String serviceStatus = resultSet.getString("servicestatus");

                service service;
                if ("roomService".equalsIgnoreCase(serviceType)) {
                    String roomServiceSql = "SELECT serviceid, balance FROM roomService WHERE serviceid=?";
                    final var roomServiceStatement = connection.prepareStatement(roomServiceSql);
                    roomServiceStatement.setString(1, serviceID);
                    final var roomServiceResultSet = roomServiceStatement.executeQuery();
                    if (roomServiceResultSet.next()) {
                        String balance = roomServiceResultSet.getString("balance");
                        service = new roomService(serviceID, serviceName, serviceType, servicePrice, serviceStatus, balance);
                    } else {
                        service = new service(serviceID, serviceName, serviceType, servicePrice, serviceStatus);
                    }
                } else if ("eventService".equalsIgnoreCase(serviceType)) {
                    String eventServiceSql = "SELECT serviceid, eventcapacity FROM eventService WHERE serviceid=?";
                    final var eventServiceStatement = connection.prepareStatement(eventServiceSql);
                    eventServiceStatement.setString(1, serviceID);
                    final var eventServiceResultSet = eventServiceStatement.executeQuery();
                    if (eventServiceResultSet.next()) {
                        String eventCapacity = eventServiceResultSet.getString("eventCapacity");
                        service = new eventService(serviceID, serviceName, serviceType, servicePrice, serviceStatus, eventCapacity);
                    } else {
                        service = new service(serviceID, serviceName, serviceType, servicePrice, serviceStatus);
                    }
                } else {
                    service = new service(serviceID, serviceName, serviceType, servicePrice, serviceStatus);
                }

                services.add(service);
            }

            model.addAttribute("services", services);

            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "manager/managerServiceList";
    }

    @GetMapping("/managerViewService")
    public String managerViewService(@RequestParam("serviceID") String serviceID, Model model) {
        try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT service.serviceid, service.servicename, service.servicetype, service.serviceprice, service.servicestatus, roomservice.balance, eventservice.eventcapacity "
            + "FROM service "
            + "LEFT JOIN roomservice ON service.serviceid = roomservice.serviceid "
            + "LEFT JOIN eventservice ON eventservice.serviceid = service.serviceid "
            + "WHERE serviceid = ?";
            final var statement = connection.prepareStatement(sql);
            statement.setString(1, serviceID);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String serviceName = resultSet.getString("servicename");
                String serviceType = resultSet.getString("servicetype");
                String servicePrice = resultSet.getString("serviceprice");
                String serviceStatus = resultSet.getString("servicestatus");

                service service;
                if (serviceType.equalsIgnoreCase("Room Service")) {
                    String balance = resultSet.getString("balance");
                    service = new roomService(serviceID, serviceName, serviceType, servicePrice, serviceStatus, balance);
                } else if (serviceType.equalsIgnoreCase("Event Service")) {
                    String eventCapacity = resultSet.getString("eventCapacity");
                    service = new eventService(serviceID, serviceName, serviceType, servicePrice, serviceStatus, eventCapacity);
                } else {
                    // Handle the case when serviceType is neither "roomService" nor "eventService"
                    service = new service(serviceID, serviceName, serviceType, servicePrice, serviceStatus);
                }

                model.addAttribute("service", service); // Use "service" as the model attribute name

                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "manager/managerViewService";
    }

//     @PostMapping("/updateservice")
//     public String UpdateService(@ModelAttribute("service") service service, roomService roomService, eventService eventService) {
//         try {
//             Connection connection = dataSource.getConnection();
//             String sql = "UPDATE service SET serviceName=?, serviceType=?, servicePrice=? WHERE serviceID=?";
//             final var statement = connection.prepareStatement(sql);
//             statement.setString(1, service.getServiceName());
//             statement.setString(2, service.getServiceType());
//             statement.setString(3, service.getServicePrice());
//             statement.setString(4, service.getServiceID());

//             statement.executeUpdate();

//             // Update fields specific to "roomService" or "eventService" based on the service type
//             if ("roomService".equalsIgnoreCase(service.getServiceType())) {
//                 String roomServiceSql = "UPDATE roomServices SET balance=? WHERE serviceID=?";
//                 final var roomServiceStatement = connection.prepareStatement(roomServiceSql);
//                 roomServiceStatement.setString(1, roomService.getBalance());
//                 roomServiceStatement.setString(2, service.getServiceID());

//                 roomServiceStatement.executeUpdate();
//             } else if ("eventService".equalsIgnoreCase(service.getServiceType())) {
//                 String eventServiceSql = "UPDATE eventServices SET eventCapacity=? WHERE serviceID=?";
//                 final var eventServiceStatement = connection.prepareStatement(eventServiceSql);
//                 eventServiceStatement.setString(1, eventService.getEventCapacity());
//                 eventServiceStatement.setString(2, service.getServiceID());

//                 eventServiceStatement.executeUpdate();
//             }

//             connection.close();

//         } catch (SQLException e) {
//             e.printStackTrace();
//         }
//         return "redirect:/staffmenu?success=true";
//     }
 }
