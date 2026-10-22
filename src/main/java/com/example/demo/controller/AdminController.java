package com.example.demo.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; 
import com.example.demo.model.domain.Member;
import com.example.demo.model.service.MemberService;

@Controller
@RequestMapping("/admin") // 모든 URL 앞에 /admin (관리자 페이지 분리)
public class AdminController {

    @Autowired
    MemberService memberService;

    @GetMapping("/members") // GET /admin/members
    public String members(Model model) {
        List<Member> members = memberService.findAll();
        model.addAttribute("members", members);
        return "admin/members"; // templates/admin/members.html (폴더 포함)
    }

    // ★ [추가] JSON API를 fetch로 사용하는 회원 관리 화면 연결 메서드
    @GetMapping("/members-api") // GET /admin/members-api
    public String membersApi() {
        return "admin/members-api"; // templates/admin/members-api.html 연결 (데이터는 API로 가져옴)
    }

    @PostMapping("/members/{id}/role") // POST /admin/members/3/role
    public String changeRole(@PathVariable Long id, @RequestParam String role,
                             RedirectAttributes redirect) {
        try {
            memberService.changeRole(id, role);
            redirect.addFlashAttribute("message", "권한을 " + role + " (으)로 변경했습니다.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/members"; // 새로고침 중복 전송 방지 (PRG 패턴)
    }

    @PostMapping("/members/{id}/delete") // POST /admin/members/3/delete
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            memberService.deleteMember(id);
            redirect.addFlashAttribute("message", "회원을 삭제했습니다.");
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/members";
    }
}