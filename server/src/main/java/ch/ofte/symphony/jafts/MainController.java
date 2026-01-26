package ch.ofte.symphony.jafts;

import ch.ofte.symphony.jafts.idempotency.IdempotencyKeyGenerator;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
@RequiredArgsConstructor
public class MainController {

    private final IdempotencyKeyGenerator idempotencyKeyGenerator;

    @GetMapping("")
    public String doGet(Model model, HttpSession session) {
        // Generate fresh idempotency key for each page load (like CSRF token)
        String idempotencyKey = idempotencyKeyGenerator.refreshKey(session);
        model.addAttribute("idempotencyKey", idempotencyKey);
        return "home";
    }
}
