package com.ujs.devops.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

  @GetMapping("/message")
  public String getMessage() {
    return "this is a default message";
  }
}
