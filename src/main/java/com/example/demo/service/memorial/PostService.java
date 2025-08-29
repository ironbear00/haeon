package com.example.demo.service.memorial;

import com.example.demo.domain.memorial.Post;
import com.example.demo.repository.memorial.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllWithDeceased();
    }

    public Post findByUuidLink(String uuidLink) {
        return postRepository.findByUuidLinkWithDetails(uuidLink).orElse(null);
    }

    @Transactional
    public void deletePost(String uuidLink, String userEmail) {
        Post post = postRepository.findByUuidLinkWithDetails(uuidLink)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }
}