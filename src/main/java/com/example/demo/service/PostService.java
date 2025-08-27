package com.example.demo.service;

import com.example.demo.domain.memorial.Post;
import com.example.demo.repository.memorial.PostRepository;
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
}