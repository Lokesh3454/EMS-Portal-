package com.ems.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
        "/login",
        "/dashboard",
        "/attendance",
        "/leaves",
        "/manager",
        "/payroll",
        "/performance",
        "/documents",
        "/recruitment",
        "/employees",
        "/employees/**",
        "/departments",
        "/departments/**",
        "/my-profile",
        "/access-denied"
    })
    public String forwardToSpa() {
        return "forward:/index.html";
    }
}
