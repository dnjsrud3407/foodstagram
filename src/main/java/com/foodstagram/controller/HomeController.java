package com.foodstagram.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    @GetMapping({"", "/"})
    public String home(){
        return "index";
    }

    @GetMapping("/loginForm")
    public String loginForm() {
        return "user/loginForm";
    }

}
