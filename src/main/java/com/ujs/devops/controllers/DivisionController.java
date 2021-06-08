package com.ujs.devops.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DivisionController {
  @GetMapping("/divide")
  public String getDivide(@RequestParam("numerator") int numerator,
                          @RequestParam("denominator") int denominator) {
    double result = (double) numerator / denominator;
    throw new UnsupportedOperationException("I'm lazy and didn't feel like finishing this.");
  }
}
