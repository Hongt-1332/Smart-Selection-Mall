package edu.fafu.controller.redirectcontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RedirectController {

    @RequestMapping("/")
    public String index() {
        return "redirect:/index.html";
    }

    @RequestMapping("/login")
    public String login() {
        return "redirect:/index.html#/login";
    }

    @RequestMapping("/register")
    public String register() {
        return "redirect:/index.html#/register";
    }

    @RequestMapping({"/user/dashboard", "/user/address", "/user/cart", "/user/goods", "/user/trade"})
    public String userPages() {
        return "redirect:/index.html";
    }

    @RequestMapping({"/merchant/goods", "/merchant/trade"})
    public String merchantPages() {
        return "redirect:/index.html";
    }

    @RequestMapping({"/manager/user", "/manager/address", "/manager/ai", "/manager/background",
            "/manager/cart", "/manager/config", "/manager/goods", "/manager/permission",
            "/manager/trade", "/manager/vipConfig", "/manager/vipTrade"})
    public String managerPages() {
        return "redirect:/index.html";
    }
}