package com.example.demo.controller.memorial;

import com.example.demo.domain.Deceased;
import com.example.demo.domain.User;
import com.example.demo.domain.memorial.Comment;
import com.example.demo.domain.memorial.Post;
import com.example.demo.repository.DeceasedRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.memorial.CommentRepository;
import com.example.demo.repository.memorial.PostRepository;
import com.example.demo.service.PostService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity; // 추가
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map; // 추가
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

    @GetMapping("")
    public String memorial(Model model) {
        List<Post> postList = postService.getAllPosts();
        model.addAttribute("postList", postList);
        return "memorial";
    }

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

        User me = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + principal.getName()));

        List<Deceased> deceasedList = deceasedRepository.findByManagerUser_Id(me.getId());
        model.addAttribute("deceasedList", deceasedList);
        model.addAttribute("preselectedId", deceasedId);

        return "memorial_write";
    }

    @PostMapping("/write")
    @Transactional
    public String writeMemorial(@RequestParam Long deceasedId,
                                @RequestParam String title,
                                @RequestParam String content,
                                @RequestParam String accessPassword,
                                @RequestParam(value = "photo", required = false) MultipartFile photo,
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
        post.setAccessPassword(accessPassword);

        if (photo != null && !photo.isEmpty()) {
            try {
                Path uploadDir = Paths.get(System.getProperty("user.home"), "uploads", "memorial");
                Files.createDirectories(uploadDir);
                String original = photo.getOriginalFilename();
                String safeName = original == null ? "image" : Paths.get(original).getFileName().toString();
                String newName = UUID.randomUUID() + "_" + safeName;
                Path dest = uploadDir.resolve(newName);
                Files.copy(photo.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
                String webPath = "/uploads/memorial/" + newName;
                post.setDeceasedPhotoPath(webPath);
            } catch (IOException e) {
                throw new RuntimeException("파일 업로드에 실패했습니다.", e);
            }
        }

        postRepository.save(post);
        ra.addFlashAttribute("justCreated", true);

        if (post.getAccessPassword() == null || post.getAccessPassword().isBlank()) {
            session.setAttribute("memorial_access_" + uuid, true);
            return "redirect:/memorial/detail/" + uuid;
        }
        return "redirect:/memorial/password_check/" + uuid;
    }

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

    // 게시글 삭제 기능 추가
    @DeleteMapping("/delete/{uuid}")
    public ResponseEntity<String> deletePost(@PathVariable String uuid, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            postService.deletePost(uuid, principal.getName());
            return ResponseEntity.ok().body("게시글이 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("게시글 삭제 중 오류가 발생했습니다.");
        }
    }

    // 게시글 수정 기능 추가
    @PutMapping("/edit/{uuid}")
    @Transactional
    public ResponseEntity<String> editPost(@PathVariable String uuid,
                                           @RequestBody Map<String, String> payload,
                                           Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        Post post = postService.findByUuidLink(uuid);
        if (post == null) {
            return ResponseEntity.status(404).body("게시글을 찾을 수 없습니다.");
        }

        // 권한 확인: 현재 로그인한 사용자가 게시글의 작성자인지 확인
        if (!post.getAuthor().getEmail().equals(principal.getName())) {
            return ResponseEntity.status(403).body("수정 권한이 없습니다.");
        }

        // 추모글 내용 업데이트
        String newContent = payload.get("content");
        if (newContent != null) {
            post.setContent(newContent);
            postRepository.save(post);
            return ResponseEntity.ok().body("게시글이 성공적으로 수정되었습니다.");
        } else {
            return ResponseEntity.status(400).body("수정할 내용이 없습니다.");
        }
    }
}