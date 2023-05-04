package io.intertec.jwtdemo.service;

import io.intertec.jwtdemo.dto.Request.AuthenticationRequest;
import io.intertec.jwtdemo.dto.Response.AuthenticationResponse;
import io.intertec.jwtdemo.dto.Request.RegisterRequest;
import io.intertec.jwtdemo.exception.InvalidPasswordException;
import io.intertec.jwtdemo.model.Role;
import io.intertec.jwtdemo.model.User;
import io.intertec.jwtdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  /**
   * @param username user's email
   */
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return findByEmail(username);
  }

  public AuthenticationResponse register(RegisterRequest request) {
    var user = User.builder()
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role(Role.USER)
        .build();
    userRepository.save(user);
    return generateResponse(user);
  }

  public AuthenticationResponse authenticate(AuthenticationRequest request) throws InvalidPasswordException {
    String email = request.getEmail();
    var user = findByEmail(email);
    checkPassword(user, request.getPassword());
    return generateResponse(user);
  }

  private void checkPassword(User user, String reqPassword) throws InvalidPasswordException {
    if (!passwordEncoder.matches(reqPassword, user.getPassword()))
      throw new InvalidPasswordException("User's password is incorrect! Please provide appropriate one!");
  }

  private User findByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException(String.format("User with email %s not found!", email)));
  }

  private AuthenticationResponse generateResponse(User user) {
    //Map<String, Object> claims = new HashMap<>();
    //claims.put("description", "this user should be trusted");
    //var jwtToken =  jwtService.generateToken(claims, user);
    var jwtToken = jwtService.generateToken(user);
    return AuthenticationResponse.builder()
        .token(jwtToken)
        .build();
  }
}
