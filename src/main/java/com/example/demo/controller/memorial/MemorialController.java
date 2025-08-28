package com.example.demo.controller.memorial;

import com.example.demo.domain.User;
import com.example.demo.domain.Deceased;
import com.example.demo.domain.memorial.Comment;
import com.example.demo.domain.memorial.Post;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.DeceasedRepository;
import com.example.demo.repository.memorial.CommentRepository;
import com.example.demo.repository.memorial.PostRepository;
import com.example.demo.service.PostService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/memorial")
public class MemorialController {

    private final PostService postService;
    private final PostRepository postRepository;
    private final DeceasedRepository deceasedRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public MemorialController(PostService postService,
                              PostRepository postRepository,
                              DeceasedRepository deceasedRepository,
                              CommentRepository commentRepository,
                              UserRepository userRepository) {
        this.postService = postService;
        this.postRepository = postRepository;
        this.deceasedRepository = deceasedRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    /** 목록 */
    @GetMapping("")
    public String memorial(Model model) {
        List<Post> postList = postService.getAllPosts();
        model.addAttribute("postList", postList);
        return "memorial";
    }

    /**
     * 작성 페이지
     * - 로그인 안되어 있으면 로그인 페이지로
     * - 로그인 O → 로그인한 사용자가 등록한 고인만 보여줌
     */
    @GetMapping("/memorial_write")
    public String memorialWrite(@RequestParam(required = false) Long deceasedId,
                                Authentication authentication,
                                Principal principal,
                                Model model) {
        boolean notLoggedIn =
                (authentication == null)
                        || !authentication.isAuthenticated()
                        || (authentication instanceof AnonymousAuthenticationToken);

        if (notLoggedIn || principal == null) {
            String next = "/memorial/memorial_write" + (deceasedId != null ? ("?deceasedId=" + deceasedId) : "");
            return "redirect:/user/login?next=" + next;
        }

        // 현재 로그인 사용자 조회
        User me = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getName()));

        // ✅ 내가 등록한 고인만 조회
        List<Deceased> deceasedList = deceasedRepository.findByManagerUser_Id(me.getId());
        model.addAttribute("deceasedList", deceasedList);

        // 선택된 고인 ID (있으면 드롭다운에서 선택됨)
        model.addAttribute("preselectedId", deceasedId);

        return "memorial_write";
    }

    /** 글 작성 처리: 고인은 기존 데이터에서 찾음 */
    @PostMapping("/write")
    @Transactional
    public String writeMemorial(@RequestParam Long deceasedId,
                                @RequestParam String title,
                                @RequestParam String content,
                                @RequestParam String accessPassword,
                                Principal principal,
                                HttpSession session,
                                RedirectAttributes ra) {

        if (principal == null) {
            return "redirect:/user/login?next=/memorial/memorial_write" + (deceasedId != null ? ("?deceasedId=" + deceasedId) : "");
        }

        User author = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getName()));

        Deceased deceased = deceasedRepository.findById(deceasedId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid deceasedId: " + deceasedId));

        String uuid = UUID.randomUUID().toString();

        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setAuthor(author);
        post.setDeceased(deceased);
        post.setUuidLink(uuid);

        if (accessPassword != null && !accessPassword.isBlank()) {
            post.setAccessPassword(accessPassword);
        }

        postRepository.save(post);
        ra.addFlashAttribute("justCreated", true);

        if (post.getAccessPassword() == null || post.getAccessPassword().isBlank()) {
            session.setAttribute("memorial_access_" + uuid, true);
            return "redirect:/memorial/detail/" + uuid;
        }
        return "redirect:/memorial/password_check/" + uuid;
    }

    /** 비밀번호 확인 페이지 */
    @GetMapping("/password_check/{uuid}")
    public String memorialPasswordCheck(@PathVariable("uuid") String uuid, Model model) {
        Post post = postService.findByUuidLink(uuid);
        if (post == null) return "redirect:/memorial";
        if (post.getAccessPassword() == null || post.getAccessPassword().isBlank()) {
            return "redirect:/memorial/detail/" + uuid;
        }
        model.addAttribute("uuid", uuid);
        return "memorial_password_check";
    }

    /** 비밀번호 제출 처리 */
    @PostMapping("/detail/{uuid}")
    public String checkPassword(@PathVariable("uuid") String uuid,
                                @RequestParam("password") String submittedPassword,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        Post post = postService.findByUuidLink(uuid);
        if (post == null) return "redirect:/memorial";

        String actual = post.getAccessPassword();
        boolean ok = (actual == null || actual.isBlank()) || actual.equals(submittedPassword);
        if (ok) {
            session.setAttribute("memorial_access_" + uuid, true);
            return "redirect:/memorial/detail/" + uuid;
        } else {
            redirectAttributes.addFlashAttribute("error", true);
            return "redirect:/memorial/password_check/" + uuid;
        }
    }

    /** 상세 */
    @GetMapping("/detail/{uuid}")
    public String memorialDetail(@PathVariable("uuid") String uuid, Model model, HttpSession session) {
        Post post = postService.findByUuidLink(uuid);
        if (post == null) return "redirect:/memorial";

        boolean needPw = !(post.getAccessPassword() == null || post.getAccessPassword().isBlank());
        boolean hasToken = session.getAttribute("memorial_access_" + uuid) != null;

        if (needPw && !hasToken) {
            return "redirect:/memorial/password_check/" + uuid;
        }

        model.addAttribute("post", post);
        return "memorial_detail";
    }

    /** 댓글 등록 */
    @Transactional
    @PostMapping("/comment/{uuid}")
    public String addComment(@PathVariable String uuid,
                             @RequestParam String content,
                             @RequestParam(required = false) String authorName,
                             Principal principal) {

        Post post = postService.findByUuidLink(uuid);
        if (post == null) throw new IllegalArgumentException("Invalid post uuid: " + uuid);

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);

        if (principal != null) {
            User author = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getName()));
            comment.setAuthor(author);
        } else {
            comment.setAuthorName(authorName);
        }

        commentRepository.save(comment);
        return "redirect:/memorial/detail/" + uuid;
    }
}