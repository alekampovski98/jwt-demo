package io.intertec.jwtdemo.controller;

import io.intertec.jwtdemo.dto.AuthenticationRequest;
import io.intertec.jwtdemo.dto.AuthenticationResponse;
import io.intertec.jwtdemo.dto.RegisterRequest;
import io.intertec.jwtdemo.exception.InvalidPasswordException;
import io.intertec.jwtdemo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
    return ResponseEntity.ok(userService.register(request));
  }

  @PostMapping("/authenticate")
  public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
    try {
      return ResponseEntity.ok(userService.authenticate(request));
    } catch (InvalidPasswordException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthenticationResponse());
    }
  }
}
