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

import com.heroku.java.model.room;

import jakarta.servlet.http.HttpSession;

import java.sql.*;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Map;

import java.util.List;

@Controller
public class RoomController {
    private final DataSource dataSource;

    @Autowired
    public RoomController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    //original syahir punyer, guna session
    // @GetMapping("/managerAddRoom")
    // public String managerAddRoom(HttpSession session) {
    //     // int roomNumber = (int) session.getAttribute("roomNumber");
    //     // System.out.println("roomNumber id :" + roomNumber);
    //     return "manager/managerAddRoom";
    // }

    // @GetMapping("/managerAddRoom")
    // public String managerAddRoom() {
    // // int roomNumber = (int) session.getAttribute("roomNumber");
    // // System.out.println("roomNumber id :" + roomNumber);
    // return "manager/managerAddRoom";
    // }

    @GetMapping("/managerRoomList")
    public String managerRoomList(Model model) {

        List<room> rooms = new ArrayList<room>();
        // Retrieve the logged-in room's role from the session (syahir punya nih)
        //String staffsrole = (String) session.getAttribute("staffsrole");
        //System.out.println("staffrole managerRoomList : " + staffsrole);
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT roomNum, roomType, maxGuest, roomRate, roomSize, roomStatus FROM room order by roomNum"; //ni originally WHERE staffsrole=?
            final var statement = connection.prepareStatement(sql);
            //statement.setString(1, "baker"); (syahir punya nih)
            final var resultSet = statement.executeQuery();
            System.out.println("pass try managerRoomList >>>>>");

            while (resultSet.next()) {
                String roomNum = resultSet.getString("roomNum");
                String roomType = resultSet.getString("roomType");
                String maxGuest = resultSet.getString("maxGuest");
                String roomRate = resultSet.getString("roomRate");
                String roomSize = resultSet.getString("roomSize");
                String roomStatus = resultSet.getString("roomStatus");
                //System.out.println("room number" + roomNum);
                
                room r = new room();
                r.setRoomNum(roomNum);
                r.setRoomType(roomType);
                r.setMaxGuest(maxGuest);
                r.setRoomRate(roomRate);
                r.setRoomSize(roomSize);
                r.setRoomStatus(roomStatus);                

                rooms.add(r);
                model.addAttribute("rooms", rooms);
                //model.addAttribute("isAdmin", staffsrole != null && staffsrole.equals("admin")); // Add isAdmin flag to the modelF (syahir punya gak)

            }

            connection.close();

         return "manager/managerRoomList";
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception as desired (e.g., show an error message)
            return "error";
        }
        
    }

    /* syahir punya delete, kita tak buat kot
    @GetMapping("/deletestaff/")
    public String deleteStaff(@RequestParam("roomNum") int roomNum, HttpSession session) {
        // Retrieve the logged-in staff's role from the session
        String staffsrole = (String) session.getAttribute("staffsrole");
        System.out.println("delete staff : " + staffsrole);
        System.out.println(roomNum);
        if (staffsrole != null && staffsrole.equals("admin")) {
            try (Connection connection = dataSource.getConnection()) {
                String sql = "DELETE FROM staffs WHERE roomNum = ?";
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, roomNum);
                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    // Deletion successful
                    connection.close();
                    return "redirect:/managerRoomList"; // Redirect back to the staff list
                } else {
                    // Deletion failed
                    connection.close();
                    return "redirect:/managerRoomList"; // Redirect to an error page or show an error message
                }


            } catch (SQLException e) {
                e.printStackTrace();
                // Handle the exception as desired (e.g., show an error message)
                return "admin/managerRoomList"; // Redirect to an error page or show an error message
            }
        }

        // Redirect to an error page or back to the staff list
        return "redirect:/managerRoomList";
    }*/  

    /*
    @PostMapping("/staffregister")
    public String addAccountStaff(HttpSession session, @ModelAttribute("staffregister") staff staff) {
        String fullname = (String) session.getAttribute("roomType");
        int userid = (int) session.getAttribute("roomNum");

        //debug
        System.out.println("fullname : "+fullname);
        System.out.println("userid : "+ userid);
        try {
            Connection connection = dataSource.getConnection();
            String sql1 = "INSERT INTO staffs (roomType, maxGuest, roomRate, staffsrole,managersid) VALUES (?,?,?,?,?)";
            final var statement1 = connection.prepareStatement(sql1);

            String fname = staff.getFullname();
            String email = staff.getEmail();
            String password = staff.getPassword();
            System.out.println("password : " + password);
            System.out.println("fullname : " + fname);
            System.out.println("email : " + email);

            statement1.setString(1, fname);
            statement1.setString(2, email);
            statement1.setString(3, password);
            statement1.setString(4, "baker");
            statement1.setInt(5, (int) session.getAttribute("roomNum"));

            statement1.executeUpdate();

            connection.close();
            return "redirect:/login";

        } catch (SQLException sqe) {
            System.out.println("Error Code = " + sqe.getErrorCode());
            System.out.println("SQL state = " + sqe.getSQLState());
            System.out.println("Message = " + sqe.getMessage());
            System.out.println("printTrace /n");
            sqe.printStackTrace();

            return "redirect:/staffregister";
        } catch (Exception e) {
            System.out.println("E message : " + e.getMessage());
            return "redirect:/staffregister";
        }
    }

    @GetMapping("/staffprofile")
    public String viewprofilestaff(HttpSession session, Model model) {
        String fullname = (String) session.getAttribute("roomType");
        int userid = (int) session.getAttribute("roomNum");
        String staffrole = (String) session.getAttribute("staffsrole");
        System.out.println("staff fullname : " + fullname);
        System.out.println("staff id : " + userid);
        System.out.println("staff role : " + staffrole);

        if (fullname != null) {
            try {
                Connection connection = dataSource.getConnection();
                final var statement = connection.prepareStatement(
                        "SELECT  roomType, maxGuest, roomRate,staffsrole FROM staffs WHERE roomNum = ?");
                statement.setInt(1, userid);
                final var resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    String fname = resultSet.getString("roomType");
                    String email = resultSet.getString("maxGuest");
                    String password = resultSet.getString("roomRate");
                    String staffsrole = resultSet.getString("staffsrole");
                    // debug
                    System.out.println("fullname from db = " + fname);

                    staff staffprofile = new staff(userid, fname, email, password, staffsrole);

                    model.addAttribute("staffprofile", staffprofile);
                    System.out.println("fullname :" + staffprofile.getFullname());
                    // Return the view name for displaying staff details --debug
                    System.out.println("Session staffprofile : " + model.getAttribute("staffprofile"));

                }
                connection.close();
                return "staffprofile";
            } catch (SQLException e) {
                e.printStackTrace();
                return "login";
            }
        } else {
            return "login";
        }
        

    }

    // Update Profile staff
    @PostMapping("/staffupdate")
    public String updatestaff(HttpSession session, @ModelAttribute("staffprofile") staff staff, Model model) {
        int roomNum = (int) session.getAttribute("roomNum");
        String staffsrole = (String) session.getAttribute("staffsrole");

        String roomType = staff.getFullname();
        String maxGuest = staff.getEmail();
        String roomRate = staff.getPassword();

        // debug
        System.out.println("id update = " + roomNum);
        System.out.println("role update = " + staffsrole);
        
        try {
            Connection connection = dataSource.getConnection();
            String sql1 = "UPDATE staffs SET roomType=? ,maxGuest=?, staffsrole=?, roomRate=? WHERE roomNum=?";
            final var statement = connection.prepareStatement(sql1);

                statement.setString(1, roomType);
                statement.setString(2, maxGuest);
                statement.setString(3, staffsrole);
                statement.setString(4, roomRate);
                statement.setInt(5, roomNum);
                statement.executeUpdate();
            System.out.println("debug= " + roomNum + " " + roomType + " " + staffsrole + " " + maxGuest + " " + roomRate);

            connection.close();

            String returnPage = "staffprofile";
            return returnPage;

        } catch (Throwable t) {
            System.out.println("message : " + t.getMessage());
            System.out.println("error");
            return "redirect:/staffprofile";
        }
    }

    /* delete controller
    @GetMapping("/deletestaff")
    public String deleteProfileCust(HttpSession session, Model model) {
        String fullname = (String) session.getAttribute("roomType");
        int userid = (int) session.getAttribute("roomNum");

        if (fullname != null) {
            try (Connection connection = dataSource.getConnection()) {

                // Delete user record
                final var deleteStaffStatement = connection.prepareStatement("DELETE FROM staffs WHERE roomNum=?");
                deleteStaffStatement.setInt(1, userid);
                int userRowsAffected = deleteStaffStatement.executeUpdate();

                if (userRowsAffected > 0) {
                    // Deletion successful
                    // You can redirect to a success page or perform any other desired actions
                    
                    session.invalidate();
                    connection.close();
                    return "redirect:/";
                } else {
                    // Deletion failed
                    connection.close();
                     System.out.println("Delete Failed");
                    return "admin/deletestaff";
                   
                }
            } catch (SQLException e) {
                // Handle any potential exceptions (e.g., log the error, display an error page)
                e.printStackTrace();

                // Deletion failed
                // You can redirect to an error page or perform any other desired actions
                System.out.println("Error");
            }
        }
        // Username is null or deletion failed, handle accordingly (e.g., redirect to an
        // error page)
        return "staff/stafforder";
    }*/

}
