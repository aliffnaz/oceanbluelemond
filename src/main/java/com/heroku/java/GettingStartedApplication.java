package com.heroku.java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;

@SpringBootApplication
@Controller
public class GettingStartedApplication {
    private final DataSource dataSource;

    @Autowired
    public GettingStartedApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

// for guest

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/index")
    public String index1() {
        return "index";
    }

    @GetMapping("/guestLogin")
    public String guestLogin() {
        return "guest/guestLogin";
    }


    @GetMapping("/index_logout")
    public String index_logout() {
        return "index_logout";
    }

        @GetMapping("/guestViewRoom")
    public String guestViewRoom() {
        return "guest/guestViewRoom";
    }


     @GetMapping("/guestMakeRoomReservation")
    public String guestMakeRoomReservation() {
        return "guest/guestMakeRoomReservation";
    }

    @GetMapping("/guestProfile")
    public String guestProfile() {
        return "guest/guestProfile";
    }

    @GetMapping("/guestRoomReservation")
    public String guestRoomReservation() {
        return "guest/guestRoomReservation";
    }

    @GetMapping("/guestRegister")
    public String guestRegister() {
        return "guest/guestRegister";
    }

    @GetMapping("/guestUpdate")
    public String guestUpdate() {
        return "guest/guestUpdate";
    }

    @GetMapping("/guestViewRoomReservation")
    public String guestViewRoomReservation() {
        return "guest/guestViewRoomReservation";
    }

    @GetMapping("/guestViewService")
    public String guestViewService() {
        return "guest/guestViewService";
    }

    @GetMapping("/guestGenerateReceipt")
    public String guestGenerateReceipt() {
        return "guest/guestGenerateReceipt";
    }

    // for staff

        @GetMapping("/staffHome")
    public String staffHome() {
        return "staff/staffHome";
    }

    @GetMapping("/staffLogin")
    public String staffLogin() {
        return "staff/staffLogin";
    }

    @GetMapping("/staffProfile")
    public String staffProfile() {
        return "staff/staffProfile";
    }

    @GetMapping("/staffRoomList")
    public String staffRoomList() {
        return "staff/staffRoomList";
    }

    @GetMapping("/staffUpdate")
    public String staffUpdate() {
        return "staff/staffUpdate";
    }

    @GetMapping("/staffUpdateRoom")
    public String staffUpdateRoom() {
        return "staff/staffUpdateRoom";
    }

    @GetMapping("/staffViewGuest")
    public String staffViewGuest() {
        return "staff/staffViewGuest";
    }

    @GetMapping("/staffViewRoom")
    public String staffViewRoom() {
        return "staff/staffViewRoom";
    }

    @GetMapping("/staffReservationList")
    public String staffReservationList() {
        return "staff/staffReservationList";
    }

    @GetMapping("/staffAddRoom")
public String staffAddRoom() {
    return "staff/staffAddRoom";
}

@GetMapping("/staffGenerateReport")
public String staffGenerateReport() {
    return "staff/staffGenerateReport";
}

@GetMapping("/staffGuestList")
public String staffGuestList() {
    return "staff/staffGuestList";
}

// for manager

@GetMapping("/managerHome")
public String managerHome() {
    return "manager/managerHome";
}

@GetMapping("/managerAddRoom")
public String managerAddRoom() {
    return "manager/managerAddRoom";
}

@GetMapping("/managerAddStaff")
public String managerAddStaff() {
    return "manager/managerAddStaff";
}

@GetMapping("/managerAddRoomService")
public String managerAddRoomService() {
    return "manager/managerAddRoomService";
}

@GetMapping("/managerRoomList")
public String managerRoomList() {
    return "manager/managerRoomList";
}

@GetMapping("/managerRoomServiceList")
public String managerRoomServiceList() {
    return "manager/managerRoomServiceList";
}

@GetMapping("/managerStaffList")
public String managerStaffList() {
    return "manager/managerStaffList";
}

@GetMapping("/managerGuestList")
public String managerGuestList() {
    return "manager/managerGuestList";
}

@GetMapping("/managerStaffUpdate")
public String managerStaffUpdate() {
    return "manager/managerStaffUpdate";
}

@GetMapping("/managerUpdateRoom")
public String managerUpdateRoom() {
    return "manager/managerUpdateRoom";
}

@GetMapping("/managerUpdateRoomService")
public String managerUpdateRoomService() {
    return "manager/managerUpdateRoomService";
}

@GetMapping("/managerUpdateStatus")
public String managerUpdateStatus() {
    return "manager/managerUpdateStatus";
}
    
@GetMapping("/managerViewGuest")
public String managerViewGuest() {
    return "manager/managerViewGuest";
}

@GetMapping("/managerViewRoom")
public String managerViewRoom() {
    return "manager/managerViewRoom";
}

@GetMapping("/managerViewStaff")
public String managerViewStaff() {
    return "manager/managerViewStaff";
}

@GetMapping("/managerGenerateReport")
public String managerGenerateReport() {
    return "manager/managerGenerateReport";
}

@GetMapping("/reservationReport")
public String reservationReport() {
    return "manager/reservationReport";
}

@GetMapping("/salesReport")
public String salesReport() {
    return "manager/salesReport";
}



  

    @GetMapping("/database")
    String database(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            final var statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
            statement.executeUpdate("INSERT INTO ticks VALUES (now())");

            final var resultSet = statement.executeQuery("SELECT tick FROM ticks");
            final var output = new ArrayList<>();
            while (resultSet.next()) {
                output.add("Read from DB: " + resultSet.getTimestamp("tick"));
            }

            model.put("records", output);
            return "database";

        } catch (Throwable t) {
            model.put("message", t.getMessage());
            return "error";
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(GettingStartedApplication.class, args);
    }
}
