package com.tanvir;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoutController {

    @GetMapping("/auth/logout-success")
    public String logoutSuccess() {
        return "logout";
    }
}

