package com.example.demo.controller;

import com.example.demo.dto.DeceasedRequest;
import com.example.demo.dto.DeceasedResponse;
import com.example.demo.service.DeceasedService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.demo.controller.UserController.SESSION_USER_ID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/deceased")
public class DeceasedController {

    private final DeceasedService deceasedService;

    @GetMapping
    public String list(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/user/login?next=/deceased";
        }
        List<DeceasedResponse> list = deceasedService.getDeceasedList(userId); // ⬅ 내 소유만 반환하도록 서비스 구현
        model.addAttribute("deceasedList", list);
        return "deceased_list";
    }

    @GetMapping("/create")
    public String createForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/user/login?next=/deceased/create";
        }
        model.addAttribute("deceased", new DeceasedRequest());
        return "deceased_create";
    }

    @PostMapping("/create")
    public String create(HttpSession session,
                         @ModelAttribute("deceased") DeceasedRequest req,
                         @RequestParam(value = "from", required = false) String from) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) throw new IllegalStateException("로그인 상태가 아닙니다.");

        DeceasedResponse saved = deceasedService.createDeceased(userId, req);

        if ("apply".equals(from)) {

            return "redirect:/requests/apply?step=upload&targetId=" + saved.getId();
        }

        return "redirect:/deceased";
    }

    @GetMapping("/edit")
    public String editForm(@RequestParam("id") Long id,
                           HttpSession session,
                           Model model) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/user/login?next=/deceased/edit?id=" + id;
        }
        DeceasedResponse response = deceasedService.getDeceasedByIdForOwner(id, userId);
        model.addAttribute("deceased", response);
        return "deceased_edit";
    }

    @PostMapping("/edit")
    public String edit(@RequestParam("id") Long id,
                       @ModelAttribute("deceased") DeceasedRequest req,
                       HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/user/login?next=/deceased/edit?id=" + id;
        }
        deceasedService.updateDeceasedForOwner(id, userId, req);
        return "redirect:/deceased";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("id") Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/user/login?next=/deceased";
        }
        deceasedService.deleteDeceasedForOwner(id, userId);
        return "redirect:/deceased";
    }
}