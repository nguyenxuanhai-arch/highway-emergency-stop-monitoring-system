package com.example.highwayemergencystopmonitoringsystem.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Web page controller for serving HTML templates using AdminLTE layout
 */
@Controller
public class WebPageController {

    // ==================== Authentication Pages ====================
    
    @GetMapping("/")
    public String index() {
        return "redirect:/login.html";
    }

    @GetMapping("/login.html")
    public String login() {
        return "login";
    }

    @GetMapping("/register.html")
    public String register() {
        return "register";
    }

    // ==================== Dashboard ====================
    
    @GetMapping({"/dashboard", "/dashboard.html"})
    public String dashboard(Model model) {
        model.addAttribute("activeMenu", "dashboard");
        model.addAttribute("pageTitle", "Dashboard");
        return "dashboard";
    }

    // ==================== Incidents Management ====================
    
    @GetMapping("/incidents/list")
    public String incidentsList(Model model) {
        model.addAttribute("activeMenu", "incidents");
        model.addAttribute("pageTitle", "Danh sách sự cố");
        return "incidents/list";
    }

    @GetMapping("/incidents/active")
    public String incidentsActive(Model model) {
        model.addAttribute("activeMenu", "incidents");
        model.addAttribute("pageTitle", "Sự cố đang xử lý");
        return "incidents/active";
    }

    @GetMapping("/incidents/create")
    public String incidentsCreate(Model model) {
        model.addAttribute("activeMenu", "incidents");
        model.addAttribute("pageTitle", "Báo cáo sự cố mới");
        return "incidents/create";
    }

    @GetMapping("/incidents/{id}")
    public String incidentDetail(@PathVariable Long id, Model model) {
        model.addAttribute("activeMenu", "incidents");
        model.addAttribute("pageTitle", "Chi tiết sự cố #" + id);
        model.addAttribute("incidentId", id);
        return "incidents/detail";
    }

    // ==================== Map View ====================
    
    @GetMapping("/map")
    public String map(Model model) {
        model.addAttribute("activeMenu", "map");
        model.addAttribute("pageTitle", "Bản đồ sự cố");
        return "map";
    }

    // ==================== Reports & Statistics ====================
    
    @GetMapping("/reports/statistics")
    public String reportsStatistics(Model model) {
        model.addAttribute("activeMenu", "reports");
        model.addAttribute("pageTitle", "Thống kê tổng quan");
        return "reports/statistics";
    }

    @GetMapping("/reports/daily")
    public String reportsDaily(Model model) {
        model.addAttribute("activeMenu", "reports");
        model.addAttribute("pageTitle", "Báo cáo theo ngày");
        return "reports/daily";
    }

    // ==================== Settings ====================
    
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("activeMenu", "settings");
        model.addAttribute("pageTitle", "Cài đặt hệ thống");
        return "settings";
    }
}
