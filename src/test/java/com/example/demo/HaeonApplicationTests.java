package com.example.demo;

import com.example.demo.domain.Deceased;
import com.example.demo.domain.memorial.Comment;
import com.example.demo.domain.memorial.Post;
import com.example.demo.domain.utils.AuthProvider;
import com.example.demo.domain.User;
import com.example.demo.domain.utils.Gender;
import com.example.demo.domain.utils.Status;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@SpringBootTest
@ActiveProfiles("ironbear")
class HaeonApplicationTests {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DeceasedRepository deceasedRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private CommentRepository commentRepository;

    private final Random random = new Random();

    //region user CRUD
    //
    //
    //
    //Create(insert)
    @Test
    public void insertDummyUsersWithDeceased() {
        Random random = new Random();

        for (int i = 1; i <= 20; i++) {
            User user = new User();

            user.setName("User" + i);
            user.setBirthDate(LocalDate.of(1970 + random.nextInt(30), 1 + random.nextInt(12), 1 + random.nextInt(28)));
            user.setGender(Gender.values()[random.nextInt(Gender.values().length)]);

            user.setEmail("user" + i + "@example.com");
            user.setPassword("password" + i); // 실제 서비스면 암호화 필요
            AuthProvider provider = AuthProvider.values()[random.nextInt(AuthProvider.values().length)];
            user.setProvider(provider);
            user.setProviderId(provider == AuthProvider.LOCAL ? null : provider.name() + "_id_" + i);

            int deceasedCount = random.nextInt(4);
            for (int j = 1; j <= deceasedCount; j++) {
                Deceased deceased = new Deceased();
                deceased.setName("Deceased" + i + "_" + j);
                deceased.setBirthDate(LocalDate.of(1930 + random.nextInt(30), 1 + random.nextInt(12), 1 + random.nextInt(28)));
                deceased.setDeathDate(LocalDate.of(2020 + random.nextInt(3), 1 + random.nextInt(12), 1 + random.nextInt(28)));
                deceased.setManagerUser(user); // 유족과 연결
                user.getDeceasedList().add(deceased);
            }

            userRepository.save(user);
            System.out.println("Created user: " + user.getEmail() + ", deceased count: " + user.getDeceasedList().size());
        }
    }

    //Read(get)
    @Test
    @Transactional
    public void testReadUser() {
        // 아무 사용자 하나 조회
        User user = userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No users found"));

        System.out.println("User: " + user.getName() + ", email=" + user.getEmail());
        user.getDeceasedList().forEach(d ->
                System.out.println("  Deceased: " + d.getName() + " (death: " + d.getDeathDate() + ")"));
    }

    @Test
    @Transactional
    public void testReadAllUsers() {
        List<User> users = userRepository.findAll();

        if (users.isEmpty()) {
            System.out.println("No users found");
            return;
        }

        for (User user : users) {
            System.out.println("User: " + user.getName() + ", email=" + user.getEmail()
                    + ", deceased count: " + user.getDeceasedList().size());

            user.getDeceasedList().forEach(d ->
                    System.out.println("   Deceased: " + d.getName()
                            + " (birth: " + d.getBirthDate() + ", death: " + d.getDeathDate() + ")"));
        }
    }

    //Update
    @Test
    public void testUpdateUser() {
        User user = userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No users found"));

        String oldEmail = user.getEmail();
        user.setEmail("updated_" + oldEmail);
        user.setName(user.getName() + "_updated");

        userRepository.save(user);

        User updated = userRepository.findById(user.getId()).orElseThrow();
        System.out.println("Updated user email: " + updated.getEmail());
        System.out.println("Updated user name: " + updated.getName());
    }

    //Delete
    @Test
    public void testDeleteUserWithCascade() {
        User user = userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No users found"));

        Long userId = user.getId();
        System.out.println("Trying to delete user: " + user.getEmail());

        for (Deceased d : user.getDeceasedList()) {
            deceasedRepository.delete(d);
        }
        user.getDeceasedList().clear();
        userRepository.delete(user);

        boolean exists = userRepository.findById(userId).isPresent();
        System.out.println("User exists after delete? " + exists);
    }
    //endregion

    //region post CRUD
    //
    //
    //
    //create(insert)
    @Test
    public void insertDummyPost()
    {
        User user = userRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No users found"));

        Deceased deceased = new Deceased();
        deceased.setName("Deceased1");
        deceased.setManagerUser(user);
        deceasedRepository.save(deceased);

        Post post = new Post();
        post.setAuthor(user);
        post.setDeceased(deceased);
        post.setTitle("첫 번째 추모 글");
        post.setContent("고인을 기리며...");
        post.setAccessPassword("1234");
        Status submittedStatus = statusRepository.findByCode("SUBMITTED")
                .orElseThrow(() -> new RuntimeException("Status SUBMITTED not found"));

        post.setStatus(submittedStatus.getCode());

        Post saved = postRepository.save(post);

        System.out.println("Created Post ID: " + saved.getId() + ", UUID: " + saved.getUuidLink());
    }

    //Read(get)
    @Test
    @Transactional
    public void testReadPost() {
        Post post = postRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No posts found"));

        System.out.println("Post ID: " + post.getId());
        System.out.println("Title: " + post.getTitle());
        System.out.println("Author: " + post.getAuthor().getName());
        System.out.println("Deceased: " + post.getDeceased().getName());
        System.out.println("Status: " + post.getStatus());
        System.out.println("UUID: " + post.getUuidLink());
    }
    @Test
    @Transactional
    public void testReadAllPosts() {
        List<Post> posts = postRepository.findAll();

        if (posts.isEmpty()) {
            System.out.println("No posts found");
            return;
        }

        for (Post post : posts) {
            System.out.println("Post ID: " + post.getId());
            System.out.println("Title: " + post.getTitle());
            System.out.println("Author: " + post.getAuthor().getName());
            System.out.println("Deceased: " + post.getDeceased().getName());
            System.out.println("Status: " + post.getStatus());
            System.out.println("UUID: " + post.getUuidLink());
            System.out.println("--------------------------------");
        }
    }

    //Update(status update)
    @Test
    public void testUpdatePost() {
        Post post = postRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No posts found"));

        System.out.println("Before update: Post ID=" + post.getId() + ", Status=" + post.getStatus());

        Status newStatus = statusRepository.findByCode("PROCESSING")
                .orElseThrow(() -> new RuntimeException("Status PROCESSING not found"));

        post.setStatus(newStatus.getCode());
        postRepository.save(post);

        Post updated = postRepository.findById(post.getId())
                .orElseThrow(() -> new RuntimeException("Post not found after update"));

        System.out.println("After update: Post ID=" + updated.getId() + ", Status=" + updated.getStatus());
    }

    //Delete(status update)
    @Test
    public void testDeletePost() {
        Post post = postRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No posts found"));

        System.out.println("Before update: Post ID=" + post.getId() + ", Status=" + post.getStatus());

        Status newStatus = statusRepository.findByCode("DELETED")
                .orElseThrow(() -> new RuntimeException("Status PROCESSING not found"));

        post.setStatus(newStatus.getCode());
        postRepository.save(post);

        Post updated = postRepository.findById(post.getId())
                .orElseThrow(() -> new RuntimeException("Post not found after update"));

        System.out.println("After update: Post ID=" + updated.getId() + ", Status=" + updated.getStatus());
    }
    //endregion

    //region comment CRUD
    //
    //
    //Create(insert)
    @Test
    public void insertDummyComment()
    {
        User user = userRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No users found"));

        Post post = postRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No posts found"));

        Comment comment = new Comment();
        comment.setAuthor(user);
        comment.setPost(post);
        comment.setContent("추모의 말씀을 올립니다.");
        comment.setStatus("SUBMITTED");

        Comment saved = commentRepository.save(comment);
        System.out.println("Created Comment ID: " + saved.getId());
    }

    //Read(get)
    @Test
    public void readComment() {
        Comment comment = commentRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        System.out.println("Comment ID: " + comment.getId() + ", Content: " + comment.getContent());
    }

    @Test
    @Transactional
    public void readAllComments() {
        List<Comment> comments = commentRepository.findAll();
        comments.forEach(c ->
                System.out.println("Comment ID: " + c.getId() +
                        ", Author: " + c.getAuthor().getName() +
                        ", Content: " + c.getContent() +
                        ", Status: " + c.getStatus())
        );
    }

    //Update
    @Test
    public void updateComment() {
        Comment comment = commentRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        System.out.println("Before Update: " + comment.getContent());

        comment.setContent("수정된 댓글 내용입니다.");
        Comment updated = commentRepository.save(comment);

        System.out.println("After Update: Comment ID=" + updated.getId() +
                ", Content=" + updated.getContent());
    }

    //Delete(update status)
    @Test
    public void deleteComment() {
        Comment comment = commentRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setStatus("DELETED");
        Comment updated = commentRepository.save(comment);
        System.out.println("Updated Comment ID=" + updated.getId() + ", Status=" + updated.getStatus());
    }
    //endregion

    //region request file CRUD
    //
    //
    //
    //create request file
    public void createRequestFile(){

    }
    //endregion

    //region request CRUD
    //
    //
    //
    //create request
    public void createRequest(){

    }
    //endregion
}