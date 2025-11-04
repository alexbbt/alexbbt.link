package com.hna.webserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to handle /admin requests and forward to static index.html.
 * This ensures /admin is handled before RedirectController's catch-all pattern.
 */
@Controller
public class AdminController {

    /**
     * Forward /admin and /admin/ requests to the static index.html file.
     * This takes precedence over the RedirectController's catch-all pattern.
     */
    @GetMapping({"/admin", "/admin/"})
    public String admin() {
        return "forward:/admin/index.html";
    }
}
