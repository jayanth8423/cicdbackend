package klu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private MailService mailService;

    private static final String ADMIN_USERNAME = "jsn";
    private static final String ADMIN_PASSWORD = "jsn";
    private static final String ADMIN_EMAIL = "jsnstore@gmail.com";
    private static final String ADMIN_ROLE = "admin";

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody Customer customer) {
        try {
            customerService.registerCustomer(customer);
            return ResponseEntity.ok("User registered successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Customer loginRequest) {
        if (ADMIN_EMAIL.equalsIgnoreCase(loginRequest.getEmail()) &&
            ADMIN_PASSWORD.equals(loginRequest.getPassword())) {

            Customer adminUser = new Customer();
            adminUser.setEmail(ADMIN_EMAIL);
            adminUser.setUsername(ADMIN_USERNAME);
            adminUser.setPassword(ADMIN_PASSWORD); // Storing plain text password
            adminUser.setRole(ADMIN_ROLE);

            return ResponseEntity.ok(adminUser);
        }

        Customer user = customerRepo.findById(loginRequest.getEmail()).orElse(null);
        if (user == null || !loginRequest.getPassword().equals(user.getPassword())) { // Validate plain text password
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        return ResponseEntity.ok(user);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        Customer customer = customerRepo.findById(email).orElse(null);

        if (customer == null) {
            return ResponseEntity.status(404).body("Email not found.");
        }

        // Generate a unique token for password reset
        String resetToken = UUID.randomUUID().toString();
        customer.setResetToken(resetToken);
        customerRepo.save(customer);

        String resetLink = "http://localhost:8080/api/reset-password?token=" + resetToken;
        String subject = "Password Recovery - JSN Store";
        String body = "Hello " + customer.getUsername() + ",\n\n"
                    + "Click the link below to reset your password:\n"
                    + resetLink + "\n\n"
                    + "If you did not request a password reset, please ignore this email.\n\n"
                    + "Regards,\nJSN Store Team";

        try {
            mailService.sendMail(email, subject, body);
            return ResponseEntity.ok("Password reset link sent to your email.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send email: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestBody Map<String, String> payload) {
        Customer customer = customerRepo.findByResetToken(token);
        if (customer == null) {
            return ResponseEntity.status(404).body("Invalid or expired token.");
        }

        String newPassword = payload.get("newPassword");
        customer.setPassword(newPassword); 
        customer.setResetToken(null); 
        customerRepo.save(customer);
        return ResponseEntity.ok("Password has been successfully updated.");
    }
}
