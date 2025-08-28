package com.example.demo.controller.memorial;

import com.example.demo.domain.memorial.Comment;
import com.example.demo.domain.memorial.Post;
import com.example.demo.domain.User;
import com.example.demo.repository.memorial.CommentRepository;
import com.example.demo.repository.UserRepository;
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

@Controller
@RequestMapping("/memorial")
public class MemorialController {

    private final PostService postService;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public MemorialController(PostService postService, CommentRepository commentRepository, UserRepository userRepository) {
        this.postService = postService;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("")
    public String memorial(Model model){
        List<Post> postList = postService.getAllPosts();
        model.addAttribute("postList", postList);
        return "memorial";
    }

    /** 로그인 여부에 따라 분기: 로그인 O → 작성 페이지, 로그인 X → 로그인 페이지로 리다이렉트 */
    @GetMapping("/memorial_write")
    public String memorialWrite(Authentication authentication) {
        boolean notLoggedIn =
                (authentication == null)
                        || !authentication.isAuthenticated()
                        || (authentication instanceof AnonymousAuthenticationToken);

        if (notLoggedIn) {
            return "redirect:/user/login?next=/memorial/memorial_write";
        }
        return "memorial_write";
    }

    @GetMapping("/password_check/{uuid}")
    public String memorialPasswordCheck(@PathVariable("uuid") String uuid, Model model) {
        model.addAttribute("uuid", uuid);
        return "memorial_password_check";
    }

    @PostMapping("/detail/{uuid}")
    public String checkPassword(@PathVariable("uuid") String uuid,
                                @RequestParam("password") String submittedPassword,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        Post post = postService.findByUuidLink(uuid);

        if (post != null && post.getAccessPassword().equals(submittedPassword)) {
            session.setAttribute("memorial_access_" + uuid, true);
            return "redirect:/memorial/detail/" + uuid;
        } else {
            redirectAttributes.addFlashAttribute("error", true);
            return "redirect:/memorial/password_check/" + uuid;
        }
    }

    @GetMapping("/detail/{uuid}")
    public String memorialDetail(@PathVariable("uuid") String uuid, Model model, HttpSession session) {
        if (session.getAttribute("memorial_access_" + uuid) == null) {
            return "redirect:/memorial/password_check/" + uuid;
        }

        Post post = postService.findByUuidLink(uuid);
        if (post == null) {
            return "redirect:/memorial";
        }

        model.addAttribute("post", post);
        return "memorial_detail";
    }

    @Transactional
    @PostMapping("/comment/{uuid}")
    public String addComment(@PathVariable String uuid,
                             @RequestParam String content,
                             @RequestParam(required = false) String authorName,
                             Principal principal) {

        Post post = postService.findByUuidLink(uuid);
        if (post == null) {
            throw new IllegalArgumentException("Invalid post uuid: " + uuid);
        }

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