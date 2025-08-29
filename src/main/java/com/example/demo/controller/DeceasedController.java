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

    /** 고인 목록 페이지 */
    @GetMapping
    public String list(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) throw new IllegalStateException("로그인 상태가 아닙니다.");

        List<DeceasedResponse> list = deceasedService.getDeceasedList(userId);
        model.addAttribute("deceasedList", list);
        return "deceased_list";
    }

    /** 등록 폼 페이지 */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("deceased", new DeceasedRequest());
        return "deceased_create";
    }



    // DeceasedController.java (수정된 부분)
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

    /** 수정 폼 페이지 */
    @GetMapping("/edit")
    public String editForm(@RequestParam("id") Long id, Model model) {
        DeceasedResponse response = deceasedService.getDeceasedById(id);
        model.addAttribute("deceased", response);
        return "deceased_edit";
    }

    /** 수정 처리 */
    @PostMapping("/edit")
    public String edit(@RequestParam("id") Long id,
                       @ModelAttribute("deceased") DeceasedRequest req) {
        deceasedService.updateDeceased(id, req);
        return "redirect:/deceased";
    }

    /** 삭제 처리 */
    @PostMapping("/delete")
    public String delete(@RequestParam("id") Long id) {
        deceasedService.deleteDeceased(id);
        return "redirect:/deceased";
    }
}
