package com.example.demo.controller;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.domain.Post;
import com.example.demo.model.dto.PostForm;
import com.example.demo.model.service.CommentService;
import com.example.demo.model.service.PostService;

@Controller // 게시판 화면 컨트롤러
@RequestMapping("/board") // 모든 URL 앞에 /board
public class BoardController {
    @Autowired
    PostService postService;
    @Autowired
    CommentService commentService;

    @GetMapping // 목록 : GET /board?page=0&type=titleContent&keyword=스프링 (누구나)
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String type,
                       @RequestParam(required = false) String keyword,
                       Model model) {

        // 서비스에 type과 keyword 전달
        Page<Post> posts = postService.findPage(type, keyword, page);

        model.addAttribute("posts", posts.getContent());
        model.addAttribute("page", posts);
        model.addAttribute("type", type);       // 검색 타입 유지용
        model.addAttribute("keyword", keyword); // 검색어 유지용
        model.addAttribute("pageUrl", "/board");

        return "board/list";
    }

    @GetMapping("/my")
    public String myPosts(
            @RequestParam(defaultValue = "0") int page,
            Principal principal,
            Model model) {
        Page<Post> posts = postService.findMyPosts(principal.getName(), page);
        model.addAttribute("posts", posts.getContent());
        model.addAttribute("page", posts);
        model.addAttribute("listTitle", "내가 쓴 글"); // 목록 화면 제목 변경용
        model.addAttribute("pageUrl", "/board/my"); // 페이징 네비게이션 주소 지정
        return "board/list";
    }

    @GetMapping("/{id}") // 상세 : GET /board/3 (누구나)
    public String detail(@PathVariable Long id, Model model) {
        Post post = postService.read(id); // 조회수 증가 기능이 포함된 read() 사용
        model.addAttribute("post", post);
        model.addAttribute("comments", commentService.findByPost(post)); // 댓글 목록 → 화면에 댓글 영역
        return "board/detail";
    }

    @GetMapping("/write") // 글쓰기 화면 : GET /board/write (로그인 필요)
    public String writeForm() {
        return "board/form";
    }

    @PostMapping("/write") // 글쓰기 처리 : POST /board/write
    public String write(PostForm postForm, Principal principal, Model model) {
        try {
            Post post = postService.write(postForm, principal.getName()); // 작성자 = 로그인한 사람
            return "redirect:/board/" + post.getId(); // 저장 후 상세 화면으로
        } catch (IllegalArgumentException e) { // 빈 제목·내용
            model.addAttribute("error", e.getMessage());
            return "board/form"; // 입력값 유지한 채 다시 글쓰기 화면
        }
    }

    @GetMapping("/{id}/edit") // 수정 화면 : GET /board/3/edit
    public String editForm(@PathVariable Long id, Model model) {
        Post post = postService.findForEdit(id);
        PostForm postForm = new PostForm(); // 기존 내용을 폼에 채워서 보여 준다
        postForm.setTitle(post.getTitle());
        postForm.setContent(post.getContent());
        model.addAttribute("postForm", postForm);
        model.addAttribute("postId", id); // 화면이 "수정" 모드로 바뀐다
        return "board/form";
    }

    @PostMapping("/{id}/edit") // 수정 처리 : POST /board/3/edit
    public String edit(@PathVariable Long id, PostForm postForm, Model model) {
        try {
            postService.edit(id, postForm);
            return "redirect:/board/" + id;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("postId", id);
            return "board/form";
        }
    }

    @PostMapping("/{id}/delete") // 삭제 처리 : POST /board/3/delete
    public String delete(@PathVariable Long id) {
        try {
            postService.delete(id);
        } catch (IllegalStateException e) { // [11주차] 연습 ② : 신청자가 있는 모집글
            return "redirect:/board/" + id + "?deleteError"; // 상세 화면 + 빨간 안내
        }
        return "redirect:/board?deleted"; // 목록 + "삭제되었습니다"
    }
}