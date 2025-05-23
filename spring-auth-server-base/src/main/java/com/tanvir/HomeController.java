package com.tanvir;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/auth/")
    public String redirectToLogin() {
        return "redirect:/auth/login";
    }
}
