package klu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private static final String ADMIN_USERNAME = "jsn";
    private static final String ADMIN_PASSWORD = "jsn";
    private static final String ADMIN_EMAIL = "jsnstore@gmail.com";

    @Autowired
    private CustomerRepo customerRepo;

    private boolean isAdmin(String email, String username, String password) {
        return ADMIN_EMAIL.equals(email) &&
               ADMIN_USERNAME.equals(username) &&
               ADMIN_PASSWORD.equals(password);
    }

    @PostMapping("/all-users")
    public ResponseEntity<?> getAllUsers(@RequestBody AdminAuthRequest adminRequest) {
        if (!isAdmin(adminRequest.getEmail(), adminRequest.getUsername(), adminRequest.getPassword())) {
            return ResponseEntity.status(403).body("Unauthorized: Only admin can view all users");
        }
        return ResponseEntity.ok(customerRepo.findAll());
    }

    @PutMapping("/update-customer-role/{email}")
    public ResponseEntity<?> updateCustomerRole(
            @PathVariable String email,
            @RequestBody RoleUpdateRequest request,
            @RequestHeader("X-Admin-Email") String adminEmail,
            @RequestHeader("X-Admin-Username") String adminUsername,
            @RequestHeader("X-Admin-Password") String adminPassword) {

        if (!isAdmin(adminEmail, adminUsername, adminPassword)) {
            return ResponseEntity.status(403).body("Unauthorized: Invalid admin credentials");
        }

        Optional<Customer> optionalCustomer = Optional.ofNullable(customerRepo.findByEmail(email));
        if (!optionalCustomer.isPresent()) {
            return ResponseEntity.status(404).body("User not found.");
        }

        Customer customer = optionalCustomer.get();
        if (customer.getRole().equalsIgnoreCase(request.getRole())) {
            return ResponseEntity.status(400).body("User already has the role: " + request.getRole());
        }

        customer.setRole(request.getRole());
        customerRepo.save(customer);
        return ResponseEntity.ok("User role updated successfully to " + request.getRole());
    }
}
