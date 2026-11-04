package com.example.demo.model.repository;

import java.util.List; // 1. List 임포트 추가
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.model.domain.Post;

@Repository // 게시글 리포지토리 : save, findAll, findById, delete 는 이미 있다
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 💡 내가 쓴 글 모아 보기 (로그인한 사용자의 username으로 글을 찾아 최신순(id 내림차순)으로 정렬)
    List<Post> findByAuthorUsernameOrderByIdDesc(String username);
}