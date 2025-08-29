package com.example.demo;

import com.example.demo.domain.Deceased;
import com.example.demo.domain.memorial.Comment;
import com.example.demo.domain.memorial.Post;
import com.example.demo.domain.requests.RequestFile;
import com.example.demo.domain.requests.SnsRequest;
import com.example.demo.domain.utils.AuthProvider;
import com.example.demo.domain.User;
import com.example.demo.domain.utils.Gender;
import com.example.demo.domain.utils.SnsPlatform;
import com.example.demo.domain.utils.Status;
import com.example.demo.repository.*;
import com.example.demo.repository.memorial.CommentRepository;
import com.example.demo.repository.memorial.PostRepository;
import com.example.demo.repository.requests.SnsRequestRepository;
import com.example.demo.repository.utils.FileTypeRepository;
import com.example.demo.repository.utils.SnsPlatformRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.utils.StatusRepository;
import com.example.demo.service.requests.processors.GoogleProcessor;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@SpringBootTest
class HaeonApplicationTests {

    @Autowired private UserRepository userRepository;
    @Autowired private DeceasedRepository deceasedRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private StatusRepository statusRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private SnsRequestRepository snsRequestRepository;
    @Autowired private SnsPlatformRepository snsPlatformRepository;
    @Autowired private FileTypeRepository fileTypeRepository;

    @Autowired private GoogleProcessor googleProcessor;

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

            user.setEmail("usera" + i + "@example.com");
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

    //region request CRUD
    //
    //
    //
    //create request
    @Test
    public void createRequest(){
//        User user = userRepository.findById(1L)
//                .orElseThrow(() -> new RuntimeException("User with id=1 not found"));
//        Deceased deceased = deceasedRepository.findById(1L)
//                .orElseThrow(() -> new RuntimeException("Deceased with id=1 not found"));
//        SnsPlatform platform = snsPlatformRepository.findById(1L)
//                .orElseThrow(() -> new RuntimeException("SnsPlatform with id=1 not found"));
//
//        SnsRequest request = new SnsRequest();
//        request.setRequester(user);
//        request.setDeceased(deceased);
//        request.setSnsPlatform(platform);
//        Status submittedStatus = statusRepository.findByCode("SUBMITTED")
//                .orElseThrow(() -> new RuntimeException("Status SUBMITTED not found"));
//
//        request.setStatus(submittedStatus.getCode());
//        request.setReason("계정 삭제 요청");
//
//        RequestFile file1 = new RequestFile();
//        file1.setFilePath("/uploads/file1.pdf");
//        file1.setOriginalFileName("file1.pdf");
//        file1.setFileType(null); // 필요 시 FileType 지정
//        request.addFile(file1);
//
//        RequestFile file2 = new RequestFile();
//        file2.setFilePath("/uploads/file2.png");
//        file2.setOriginalFileName("file2.png");
//        file2.setFileType(null);
//        request.addFile(file2);
//
//        SnsRequest saved = snsRequestRepository.save(request);
//
//        System.out.println("Saved Request ID: " + saved.getId() + ", Files count: " + saved.getFiles().size());

        for (long i = 1; i <= 20; i++) {
            final long userId = i; // 람다에서 참조할 수 있도록 final 변수로 복사

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User with id=" + userId + " not found"));

            Deceased deceased = deceasedRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Deceased with id=" + userId + " not found"));

            SnsPlatform platform = snsPlatformRepository.findById(userId % 4 + 1L)
                    .orElseThrow(() -> new RuntimeException("SnsPlatform with id=" + (userId % 4 + 1) + " not found"));

            Status submittedStatus = statusRepository.findByCode("SUBMITTED")
                    .orElseThrow(() -> new RuntimeException("Status SUBMITTED not found"));

            SnsRequest request = new SnsRequest();
            request.setRequester(user);
            request.setDeceased(deceased);
            request.setSnsPlatform(platform);
            request.setStatus(submittedStatus.getCode());
            request.setReason("계정 삭제 요청 (" + userId + "번)");

            RequestFile file1 = new RequestFile();
            file1.setFilePath("/uploads/file" + userId + "_1.pdf");
            file1.setOriginalFileName("file" + userId + "_1.pdf");
            request.addFile(file1);

            RequestFile file2 = new RequestFile();
            file2.setFilePath("/uploads/file" + userId + "_2.png");
            file2.setOriginalFileName("file" + userId + "_2.png");
            request.addFile(file2);

            SnsRequest saved = snsRequestRepository.save(request);

            System.out.println("Saved Request ID: " + saved.getId()
                    + " (user=" + user.getId()
                    + ", deceased=" + deceased.getId()
                    + ", platform=" + platform.getId() + ")");
        }
    }

    @Test
    @Transactional
    public void readRequest() {
        SnsRequest request = snsRequestRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("SnsRequest not found"));

        System.out.println("Request ID: " + request.getId());
        System.out.println("Requester: " + request.getRequester().getName());
        System.out.println("Deceased: " + request.getDeceased().getName());
        System.out.println("Platform: " + request.getSnsPlatform().getName());
        System.out.println("Status: " + request.getStatus());
        System.out.println("Reason: " + request.getReason());

        request.getFiles().forEach(f ->
                System.out.println("  File: " + f.getOriginalFileName() + ", Path: " + f.getFilePath())
        );
    }

    @Test
    @Transactional
    public void readAllRequests() {
        List<SnsRequest> requests = snsRequestRepository.findAll();

        requests.forEach(r -> {
            System.out.println("Request ID: " + r.getId() +
                    ", Requester: " + r.getRequester().getName() +
                    ", Deceased: " + r.getDeceased().getName() +
                    ", Platform: " + r.getSnsPlatform().getName() +
                    ", Status: " + r.getStatus() +
                    ", Reason: " + r.getReason());
            r.getFiles().forEach(f ->
                    System.out.println("    File: " + f.getOriginalFileName() + ", Path: " + f.getFilePath())
            );
        });
    }

    //update request
    @Test
    public void updateRequest(){
        SnsRequest request = snsRequestRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("SnsRequest not found"));

        request.setReason("계정 삭제 요청 - 업데이트 완료");
        SnsRequest updated = snsRequestRepository.save(request);

        System.out.println("Updated Request ID: " + updated.getId() + ", New reason: " + updated.getReason());
    }

    //delete request
    @Test
    public void deleteRequest(){
        SnsRequest request = snsRequestRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("SnsRequest not found"));

        Status newStatus = statusRepository.findByCode("DELETED")
                .orElseThrow(() -> new RuntimeException("Status PROCESSING not found"));

        request.setStatus(newStatus.getCode());
        SnsRequest updated = snsRequestRepository.save(request);

        System.out.println("Updated Request ID: " + updated.getId() + ", New reason: " + updated.getReason());
    }
    //endregion

    //region Google process test
    //
    //
    @Test
    @DisplayName("GoogleProcessor는 RPA 작업을 성공적으로 완료하고 상태를 COMPLETED로 변경한다")
    void processGoogleRequest_Success() {
        Long testRequestId = 1L;

        SnsRequest existingRequest = snsRequestRepository.findById(testRequestId)
                .orElseThrow(() -> new IllegalArgumentException("ID " + testRequestId + "에 해당하는 요청이 DB에 없습니다."));

        googleProcessor.process(existingRequest);
        SnsRequest updatedRequest = snsRequestRepository.findById(testRequestId).get();
        Assertions.assertEquals("COMPLETED", updatedRequest.getStatus());
    }
    //endregion
}