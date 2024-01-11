package com.heroku.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.heroku.java.model.eventService;
import com.heroku.java.model.roomService;
import com.heroku.java.model.service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Base64;
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
            String sql = "INSERT INTO service(serviceName, serviceType, servicePrice, serviceStatus) VALUES(?, ?, ?, ?)";
            final var statement = connection.prepareStatement(sql);

            String serviceName=getServiceName();
            String serviceType=getServiceType();
            String servicePrice=getServicePrice();
            String serviceStatus=getServiceStatus();
            
            statement.setString(1, serviceName);
            statement.setString(2, serviceType);
            statement.setString(3, servicePrice);
            statement.setString(4, serviceStatus);

            statement.executeUpdate();

            // Get the generated serviceID
            var generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                String generatedServiceID = generatedKeys.getString(1);

                // Update fields specific to "roomService" or "eventService" based on the service type
                if ("roomService".equalsIgnoreCase(serviceType)) {
                    serviceType = "Room Service";
                    String roomServiceSql = "INSERT INTO roomServices(serviceID, balance) VALUES (?, ?)";
                    final var roomServiceStatement = connection.prepareStatement(roomServiceSql);
                    roomServiceStatement.setString(1, generatedServiceID);
                    roomServiceStatement.setString(2, roomService.getBalance());

                    roomServiceStatement.executeUpdate();
                } else if ("eventService".equalsIgnoreCase(serviceType)) {
                    serviceType = "Event Service";
                    String eventServiceSql = "INSERT INTO eventServices(serviceID, eventCapacity) VALUES (?, ?)";
                    final var eventServiceStatement = connection.prepareStatement(eventServiceSql);
                    eventServiceStatement.setString(1, generatedServiceID);
                    eventServiceStatement.setString(2, eventService.getEventCapacity());

                    eventServiceStatement.executeUpdate();
                }

                model.addAttribute("success", true);
            }

            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return "redirect:manager/managerAddService?success=false";
        }

        return "redirect:manager/managerServiceList?success=true";
    }

    @GetMapping("/managerServiceList")
    public String managerServiceList(Model model) {
        List<service> services = new ArrayList<>();

        try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT * FROM service ORDER BY serviceName";
            final var statement = connection.createStatement();
            final var resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                String serviceID = resultSet.getString("serviceID");
                String serviceName = resultSet.getString("serviceName");
                String serviceType = resultSet.getString("serviceType");
                String servicePrice = resultSet.getString("servicePrice");

                service service;
                if ("roomService".equalsIgnoreCase(serviceType)) {
                    String roomServiceSql = "SELECT * FROM roomServices WHERE serviceID=?";
                    final var roomServiceStatement = connection.prepareStatement(roomServiceSql);
                    roomServiceStatement.setString(1, serviceID);
                    final var roomServiceResultSet = roomServiceStatement.executeQuery();
                    if (roomServiceResultSet.next()) {
                        String balance = roomServiceResultSet.getString("balance");
                        service = new roomService(serviceID, serviceName, serviceType, servicePrice, null, balance);
                    } else {
                        service = new service(serviceID, serviceName, serviceType, servicePrice, null);
                    }
                } else if ("eventService".equalsIgnoreCase(serviceType)) {
                    String eventServiceSql = "SELECT * FROM eventServices WHERE serviceID=?";
                    final var eventServiceStatement = connection.prepareStatement(eventServiceSql);
                    eventServiceStatement.setString(1, serviceID);
                    final var eventServiceResultSet = eventServiceStatement.executeQuery();
                    if (eventServiceResultSet.next()) {
                        String eventCapacity = eventServiceResultSet.getString("eventCapacity");
                        service = new eventService(serviceID, serviceName, serviceType, servicePrice, null, eventCapacity);
                    } else {
                        service = new service(serviceID, serviceName, serviceType, servicePrice, null);
                    }
                } else {
                    service = new service(serviceID, serviceName, serviceType, servicePrice, null);
                }

                services.add(service);
            }

            model.addAttribute("services", services);

            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "admin/servicelist";
    }

    @GetMapping("/servicedetail")
    public String servicedetail(@RequestParam("serviceID") String serviceID, Model model) {
        try {
            Connection connection = dataSource.getConnection();
            String sql = "SELECT * FROM service WHERE serviceID = ?";
            final var statement = connection.prepareStatement(sql);
            statement.setString(1, serviceID);
            final var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String serviceName = resultSet.getString("serviceName");
                String serviceType = resultSet.getString("serviceType");
                String servicePrice = resultSet.getString("servicePrice");

                service service;
                if (serviceType.equalsIgnoreCase("roomService")) {
                    String balance = resultSet.getString("balance");
                    service = new roomService(serviceID, serviceName, serviceType, servicePrice, null, balance);
                } else if (serviceType.equalsIgnoreCase("eventService")) {
                    String eventCapacity = resultSet.getString("eventCapacity");
                    service = new eventService(serviceID, serviceName, serviceType, servicePrice, null, eventCapacity);
                } else {
                    // Handle the case when serviceType is neither "roomService" nor "eventService"
                    service = new service(serviceID, serviceName, serviceType, servicePrice, null);
                }

                model.addAttribute("service", service); // Use "service" as the model attribute name

                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "admin/servicedetail";
    }

    @PostMapping("/updateservice")
    public String UpdateService(@ModelAttribute("service") service service, roomService roomService, eventService eventService) {
        try {
            Connection connection = dataSource.getConnection();
            String sql = "UPDATE service SET serviceName=?, serviceType=?, servicePrice=? WHERE serviceID=?";
            final var statement = connection.prepareStatement(sql);
            statement.setString(1, service.getServiceName());
            statement.setString(2, service.getServiceType());
            statement.setString(3, service.getServicePrice());
            statement.setString(4, service.getServiceID());

            statement.executeUpdate();

            // Update fields specific to "roomService" or "eventService" based on the service type
            if ("roomService".equalsIgnoreCase(service.getServiceType())) {
                String roomServiceSql = "UPDATE roomServices SET balance=? WHERE serviceID=?";
                final var roomServiceStatement = connection.prepareStatement(roomServiceSql);
                roomServiceStatement.setString(1, roomService.getBalance());
                roomServiceStatement.setString(2, service.getServiceID());

                roomServiceStatement.executeUpdate();
            } else if ("eventService".equalsIgnoreCase(service.getServiceType())) {
                String eventServiceSql = "UPDATE eventServices SET eventCapacity=? WHERE serviceID=?";
                final var eventServiceStatement = connection.prepareStatement(eventServiceSql);
                eventServiceStatement.setString(1, eventService.getEventCapacity());
                eventServiceStatement.setString(2, service.getServiceID());

                eventServiceStatement.executeUpdate();
            }

            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "redirect:/staffmenu?success=true";
    }
}
