package com.example.demo.controller.requests;

import com.example.demo.dto.requests.SnsRequestDTO;
import com.example.demo.dto.requests.SnsRequestSummaryDTO;
import com.example.demo.service.requests.SnsRequestService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.controller.UserController.SESSION_USER_ID;

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
public class SnsRequestController {

    private final SnsRequestService snsRequestService;

    @GetMapping("/dashboard")
    public String requestsDashboardPage(Model model, HttpSession session)
    {
         Long userId = (Long) session.getAttribute("LOGIN_USER_ID");

        if (userId == null) {
            return "redirect:/user/login";
        }

        List<SnsRequestSummaryDTO> requests = snsRequestService.findMyRequests(userId);
        model.addAttribute("requests", requests);

        return "requests_dashboard";
    }

    @GetMapping("/apply")
    public String requestsApplyPage(){
        return "apply";
    }


    @PostMapping("/apply")
    @ResponseBody
    public ResponseEntity<Void> applySnsRequest(
            @ModelAttribute SnsRequestDTO dto,
            HttpSession session) {

        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            snsRequestService.createSnsRequests(userId, dto);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}