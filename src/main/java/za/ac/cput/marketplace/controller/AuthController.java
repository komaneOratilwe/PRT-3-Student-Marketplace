package za.ac.cput.marketplace.controller;

import za.ac.cput.marketplace.domain.User;
import za.ac.cput.marketplace.service.UserService;
import za.ac.cput.marketplace.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/register-admin")
    public User registerAdmin(@RequestBody User user, @RequestParam String secretKey) {
        if (!"cput-marketplace-admin-2026".equals(secretKey)) {
            throw new RuntimeException("Invalid secret key");
        }
        user.setRole(User.Role.ADMIN);
        return userService.register(user);
    }

    @PostMapping("/login")
    public java.util.Map<String, Object> login(@RequestBody LoginRequest request) {
        User user = userService.login(request.getEmail(), request.getPassword());

        String loginAs;
        if (user.getRole() == User.Role.ADMIN) {
            loginAs = "ADMIN";
        } else {
            if (!"BUYER".equalsIgnoreCase(request.getLoginAs()) && !"SELLER".equalsIgnoreCase(request.getLoginAs())) {
                throw new RuntimeException("loginAs must be either BUYER or SELLER");
            }
            loginAs = request.getLoginAs().toUpperCase();
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().name(), loginAs);

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("user", user);
        response.put("loginAs", loginAs);
        response.put("token", token);
        return response;
    }

    public static class LoginRequest {
        private String email;
        private String password;
        private String loginAs;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getLoginAs() { return loginAs; }
        public void setLoginAs(String loginAs) { this.loginAs = loginAs; }
    }
}