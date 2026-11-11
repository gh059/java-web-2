package com.example.demo.model.repository;

import java.util.List; // 1. List 임포트 추가
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.demo.model.domain.Post;
import com.example.demo.model.domain.Member;

@Repository // 게시글 리포지토리 : save, findAll, findById, delete 는 이미 있다
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 💡 [수정] 기존 List<Post> 에서 Page<Post> 반환 및 Pageable 파라미터 추가
    Page<Post> findByAuthorUsernameOrderByIdDesc(String username, Pageable pageable);

    // [10주차] 단계 2 : 제목 또는 내용에 검색어가 들어간 글 (JPQL)
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    Page<Post> search(@Param("keyword") String keyword, Pageable pageable);

    // 👇 [추가] 작성자 이름 또는 아이디로 검색하는 JPQL 쿼리
    @Query("SELECT p FROM Post p WHERE p.author.name LIKE %:keyword% OR p.author.username LIKE %:keyword%")
    Page<Post> searchByAuthor(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByAuthor(Member author); // 이 회원이 쓴 글이 있는지
}