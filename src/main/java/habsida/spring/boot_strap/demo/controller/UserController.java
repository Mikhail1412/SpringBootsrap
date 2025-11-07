package habsida.spring.boot_strap.demo.controller;

import habsida.spring.boot_strap.demo.model.User;
import habsida.spring.boot_strap.demo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public String userPage(Model model, Principal principal) {
        User u = userService.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("User not found: " + principal.getName()));

        model.addAttribute("user", u);
        return "user/info";
    }
}