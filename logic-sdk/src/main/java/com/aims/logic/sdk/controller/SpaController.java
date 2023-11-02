package com.aims.logic.sdk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SpaController {
    @RequestMapping(value = {"/{path:[^\\.]*}", "/api/**"}, method = RequestMethod.GET)
    public String redirect() {
        return "forward:/index.html";
    }
    @RequestMapping(value = "/{path1:[^\\.]*}/{path:2[^\\.]*}", method = RequestMethod.GET)
    public String redirect2() {
        return "forward:/index.html";
    }
    @RequestMapping(value = "/{path1:[^\\.]*}/{path:2[^\\.]*}/{path3:[^\\.]*}", method = RequestMethod.GET)
    public String redirect3() {
        return "forward:/index.html";
    }
    @RequestMapping(value = "/{path1:[^\\.]*}/{path:2[^\\.]*}/{path3:[^\\.]*}/{path4:[^\\.]*}", method = RequestMethod.GET)
    public String redirect4() {
        return "forward:/index.html";
    }
    @RequestMapping(value = "/{path1:[^\\.]*}/{path:2[^\\.]*}/{path3:[^\\.]*}/{path4:[^\\.]*}/{path5:[^\\.]*}", method = RequestMethod.GET)
    public String redirect5() {
        return "forward:/index.html";
    }
//    @RequestMapping(value = {"/{path:[^\\.]*}/{path:[^\\.]*}/{path:[^\\.]*}/{path:[^\\.]*}", "/api/**"}, method = RequestMethod.GET)
//    public String redirect4() {
//        return "forward:/index.html";
//    }
//    @RequestMapping(value = {"/{path:[^\\.]*}/{path:[^\\.]*}/{path:[^\\.]*}/{path:[^\\.]*}/{path:[^\\.]*}", "/api/**"}, method = RequestMethod.GET)
//    public String redirect5() {
//        return "forward:/index.html";
//    }
//
//    @RequestMapping(value = {"/{path:[^\\.]*}/{path:[^\\.]*}/{path:[^\\.]*}/{path:[^\\.]*}/{path:[^\\.]*}/{path:[^\\.]*}", "/api/**"}, method = RequestMethod.GET)
//    public String redirect6() {
//        return "forward:/index.html";
//    }
}
