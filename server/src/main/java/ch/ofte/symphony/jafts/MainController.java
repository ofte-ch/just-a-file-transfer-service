package ch.ofte.symphony.jafts;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ch.ofte.symphony.jafts.service.SeoService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("")
@RequiredArgsConstructor
public class MainController {

    private final SeoService seoService;

    @GetMapping("")
    public String doGet(Model model, HttpServletRequest request) {
        // Set SEO metadata for the home page with canonical URL
        seoService.setDefaultSeo(model, request);
        return "home";
    }
}
