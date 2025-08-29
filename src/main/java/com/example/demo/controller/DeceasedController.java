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

    /** 고인 목록 페이지 (로그인 사용자 소유분만) */
    @GetMapping
    public String list(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            // 로그인 페이지로 보낼 때, 돌아올 곳 지정
            return "redirect:/user/login?next=/deceased";
        }
        List<DeceasedResponse> list = deceasedService.getDeceasedList(userId); // ⬅ 내 소유만 반환하도록 서비스 구현
        model.addAttribute("deceasedList", list);
        return "deceased_list";
    }

    /** 등록 폼 페이지 */
    @GetMapping("/create")
    public String createForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/user/login?next=/deceased/create";
        }
        model.addAttribute("deceased", new DeceasedRequest());
        return "deceased_create";
    }

    /** 등록 처리 */
    @PostMapping("/create")
    public String create(HttpSession session,
                         @ModelAttribute("deceased") DeceasedRequest req) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/user/login?next=/deceased/create";
        }
        deceasedService.createDeceased(userId, req); // ⬅ 생성 시 managerUser를 userId로 설정
        return "redirect:/deceased";
    }

    /** 수정 폼 페이지 (소유권 검증 + 모델 주입) */
    @GetMapping("/edit")
    public String editForm(@RequestParam("id") Long id,
                           HttpSession session,
                           Model model) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/user/login?next=/deceased/edit?id=" + id;
        }
        DeceasedResponse response = deceasedService.getDeceasedByIdForOwner(id, userId);
        // ⬆️ 서비스에서 해당 id가 userId 소유인지 검증(아니면 예외)
        model.addAttribute("deceased", response);
        return "deceased_edit";
    }

    /** 수정 처리 (소유권 검증) */
    @PostMapping("/edit")
    public String edit(@RequestParam("id") Long id,
                       @ModelAttribute("deceased") DeceasedRequest req,
                       HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/user/login?next=/deceased/edit?id=" + id;
        }
        deceasedService.updateDeceasedForOwner(id, userId, req); // ⬅ 소유권 검증 포함
        return "redirect:/deceased";
    }

    /** 삭제 처리 (소유권 검증) */
    @PostMapping("/delete")
    public String delete(@RequestParam("id") Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            return "redirect:/user/login?next=/deceased";
        }
        deceasedService.deleteDeceasedForOwner(id, userId); // ⬅ 소유권 검증 포함
        return "redirect:/deceased";
    }
}