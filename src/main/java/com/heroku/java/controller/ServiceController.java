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
    public String managerAddService(Model model, @ModelAttribute("managerAddService") service service, roomService roomService, eventService eventService, HttpSession session) {
     if (session.getAttribute("messege") != null) {
          session.removeAttribute("messege");
        }
        String staffICNumber = (String) session.getAttribute("staffICNumber") ;
        try {
            Connection connection = dataSource.getConnection();
            String sql = "INSERT INTO service(serviceName, serviceType, servicePrice, serviceStatus) VALUES(?, ?, ?, ?)";
            final var statement = connection.prepareStatement(sql);

            String serviceName=service.getServiceName();
            String serviceType=service.getServiceType();
            double servicePrice=service.getServicePrice();
            String serviceStatus="Available";

            if (serviceType.equalsIgnoreCase("roomService")){
            serviceType = "Room Service";}
            else {
            serviceType = "Event Service";}

            statement.setString(1, serviceName);
            statement.setString(2, serviceType);
            statement.setDouble(3, servicePrice);
            statement.setString(4, serviceStatus);

            statement.executeUpdate();

            // Get id from database for sql 2 from sql 1
            String sql1 = "SELECT * FROM service where servicename=?";
            final var stmt = connection.prepareStatement(sql1);
            stmt.setString(1, serviceName);
            final var resultSet = stmt.executeQuery();
            int serviceID = 0;
            while (resultSet.next()){
              serviceID = resultSet.getInt("serviceid");
            }
            System.out.println("service id from database: " + serviceID);
                // Update fields specific to "roomService" or "eventService" based on the service type
                if (serviceType.equalsIgnoreCase("Room Service")) {
                    String roomServiceSql = "INSERT INTO roomService(serviceid, maxQuantity) VALUES (?, ?)";
                    final var roomServiceStatement = connection.prepareStatement(roomServiceSql);
                    roomServiceStatement.setInt(1, serviceID);
                    roomServiceStatement.setInt(2, roomService.getMaxQuantity());

                    roomServiceStatement.executeUpdate();
                } else if (serviceType.equalsIgnoreCase("Event Service")) {
                    String eventServiceSql = "INSERT INTO eventService(serviceid, eventcapacity) VALUES (?, ?)";
                    final var eventServiceStatement = connection.prepareStatement(eventServiceSql);
                    int eventCapacity = eventService.getEventCapacity();
                    eventServiceStatement.setInt(1, serviceID);
                    eventServiceStatement.setInt(2, eventCapacity);

                    eventServiceStatement.executeUpdate();
                }
                model.addAttribute("success", true);

            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
             String messege = "Service Add Fail";
                  session.setAttribute("messege", messege);
            return "redirect:/managerAddService?success=false";
        }
 String messege = "Service Added Successfully ";
                  session.setAttribute("messege", messege);
        return "redirect:/managerServiceList?success=true";
    }

    @GetMapping("/managerServiceList")
    public String managerServiceList(Model model, HttpSession session) {
        String staffICNumber = (String) session.getAttribute("staffICNumber") ;
        List<service> services = new ArrayList<>();

        try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT serviceid, servicename, servicetype, serviceprice, servicestatus FROM service ORDER BY serviceid";
            final var statement = connection.createStatement();
            final var resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int serviceID = resultSet.getInt("serviceid");
                String serviceName = resultSet.getString("servicename");
                String serviceType = resultSet.getString("servicetype");
                double servicePrice = resultSet.getDouble("serviceprice");
                String serviceStatus = resultSet.getString("servicestatus");

                service service;
                if ("roomService".equalsIgnoreCase(serviceType)) {
                    String roomServiceSql = "SELECT serviceid, maxQuantity FROM roomService WHERE serviceid=?";
                    final var roomServiceStatement = connection.prepareStatement(roomServiceSql);
                    roomServiceStatement.setInt(1, serviceID);
                    final var roomServiceResultSet = roomServiceStatement.executeQuery();
                    if (roomServiceResultSet.next()) {
                        int maxQuantity = roomServiceResultSet.getInt("maxQuantity");
                        service = new roomService(serviceID, serviceName, serviceType, servicePrice, serviceStatus, maxQuantity);
                    } else {
                        service = new service(serviceID, serviceName, serviceType, servicePrice, serviceStatus);
                    }
                } else if ("eventService".equalsIgnoreCase(serviceType)) {
                    String eventServiceSql = "SELECT serviceid, eventcapacity FROM eventService WHERE serviceid=?";
                    final var eventServiceStatement = connection.prepareStatement(eventServiceSql);
                    eventServiceStatement.setInt(1, serviceID);
                    final var eventServiceResultSet = eventServiceStatement.executeQuery();
                    if (eventServiceResultSet.next()) {
                        int eventCapacity = eventServiceResultSet.getInt("eventCapacity");
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
    public String managerViewService(@RequestParam("serviceID") int serviceID, Model model,HttpSession session) {
        String staffICNumber = (String) session.getAttribute("staffICNumber") ;
        try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT service.serviceid, service.servicename, service.servicetype, service.serviceprice, service.servicestatus, roomservice.maxQuantity, eventservice.eventcapacity "
            + "FROM service "
            + "LEFT JOIN roomservice ON service.serviceid = roomservice.serviceid "
            + "LEFT JOIN eventservice ON eventservice.serviceid = service.serviceid "
            + "WHERE service.serviceid = ?";
            final var statement = connection.prepareStatement(sql);
            statement.setInt(1, serviceID);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String serviceName = resultSet.getString("servicename");
                String serviceType = resultSet.getString("servicetype");
                double servicePrice = resultSet.getDouble("serviceprice");
                String serviceStatus = resultSet.getString("servicestatus");

                service service;
                if (serviceType.equalsIgnoreCase("Room Service")) {
                    int maxQuantity = resultSet.getInt("maxQuantity");
                    service = new roomService(serviceID, serviceName, serviceType, servicePrice, serviceStatus, maxQuantity);
                } else if (serviceType.equalsIgnoreCase("Event Service")) {
                    int eventCapacity = resultSet.getInt("eventCapacity");
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

    @GetMapping("/managerUpdateService")
    public String managerUpdateService(@RequestParam("serviceID") int serviceID, Model model, HttpSession session) {
        String staffICNumber = (String) session.getAttribute("staffICNumber") ;
        try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT service.serviceid, service.servicename, service.servicetype, service.serviceprice, service.servicestatus, roomservice.maxQuantity, eventservice.eventcapacity "
            + "FROM service "
            + "LEFT JOIN roomservice ON service.serviceid = roomservice.serviceid "
            + "LEFT JOIN eventservice ON eventservice.serviceid = service.serviceid "
            + "WHERE service.serviceid = ?";
            final var statement = connection.prepareStatement(sql);
            statement.setInt(1, serviceID);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String serviceName = resultSet.getString("servicename");
                String serviceType = resultSet.getString("servicetype");
                double servicePrice = resultSet.getDouble("serviceprice");
                String serviceStatus = resultSet.getString("servicestatus");

                service service;
                if (serviceType.equalsIgnoreCase("Room Service")) {
                    int maxQuantity = resultSet.getInt("maxQuantity");
                    service = new roomService(serviceID, serviceName, serviceType, servicePrice, serviceStatus, maxQuantity);
                } else if (serviceType.equalsIgnoreCase("Event Service")) {
                    int eventCapacity = resultSet.getInt("eventCapacity");
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

        return "manager/managerUpdateService";
    }

    @PostMapping("/managerUpdateService")
    public String managerUpdateService(@ModelAttribute("managerUpdateService") service service, roomService roomService, eventService eventService, HttpSession session) {
         if (session.getAttribute("messege") != null) {
          session.removeAttribute("messege");
        }
        String staffICNumber = (String) session.getAttribute("staffICNumber");
        String serviceType = service.getServiceType();
        if (serviceType.equalsIgnoreCase("roomService")){
            serviceType = "Room Service";}
            else {
            serviceType = "Event Service";}
        
        try {
            Connection connection = dataSource.getConnection();
            String sql = "UPDATE service SET serviceName=?, serviceType=?, servicePrice=?, servicestatus=? WHERE serviceID=?";
            final var statement = connection.prepareStatement(sql);
            statement.setString(1, service.getServiceName());
            statement.setString(2, serviceType);
            statement.setDouble(3, service.getServicePrice());
            statement.setString(4, service.getServiceStatus());
            statement.setInt(5, service.getServiceID());

            statement.executeUpdate();

            // Update fields specific to "roomService" or "eventService" based on the service type
            if ("Room Service".equalsIgnoreCase(serviceType)) {
                String roomServiceSql = "UPDATE roomService SET maxQuantity=? WHERE serviceID=?";
                final var roomServiceStatement = connection.prepareStatement(roomServiceSql);
                roomServiceStatement.setInt(1, roomService.getMaxQuantity());
                roomServiceStatement.setInt(2, service.getServiceID());

                roomServiceStatement.executeUpdate();
            } else if ("Event Service".equalsIgnoreCase(serviceType)) {
                String eventServiceSql = "UPDATE eventService SET eventCapacity=? WHERE serviceID=?";
                final var eventServiceStatement = connection.prepareStatement(eventServiceSql);
                eventServiceStatement.setInt(1, eventService.getEventCapacity());
                eventServiceStatement.setInt(2, service.getServiceID());

                eventServiceStatement.executeUpdate();
            }

            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
             String messege = "Update Service Failed";
                  session.setAttribute("messege", messege);
            return "redirect:/managerUpdateService?success=false";
        }
        
 String messege = "Successfully Updated";
                  session.setAttribute("messege", messege);
        return "redirect:/managerServiceList?success=true";
    }
 }
