package ch.ofte.symphony.jafts;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
@RequiredArgsConstructor
public class MainController {

    @GetMapping("")
    public String doGet(Model model) {
        // CSRF token is automatically added to model by Spring Security
        // Session ID is managed by Spring container
        // No need to manually generate idempotency key
        return "home";
    }
}
