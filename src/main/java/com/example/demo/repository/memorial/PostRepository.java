package com.example.demo.repository.memorial;

import com.example.demo.domain.memorial.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.deceased LEFT JOIN FETCH p.author LEFT JOIN FETCH p.comments c LEFT JOIN FETCH c.author WHERE p.uuidLink = :uuidLink")
    Optional<Post> findByUuidLinkWithDetails(@Param("uuidLink") String uuidLink);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.deceased")
    List<Post> findAllWithDeceased();
}