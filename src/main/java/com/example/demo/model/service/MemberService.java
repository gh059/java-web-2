package com.example.demo.model.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize; 
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.model.domain.Member;
import com.example.demo.model.dto.MemberForm;
import com.example.demo.model.repository.MemberRepository;
import org.springframework.security.core.userdetails.*;

@Service
public class MemberService implements UserDetailsService { // 로그인을 위한 인터페이스 제공
    @Autowired
    private MemberRepository memberRepository;

    @Autowired // SecurityConfig 에 등록한 BCryptPasswordEncoder 주입
    private PasswordEncoder passwordEncoder;

    public Member signup(MemberForm form) { // 회원가입
        // ── [추가] Postman 등 API 요청 시 빈 값 검사 (required 대체) ──
        if (isBlank(form.getUsername()) || isBlank(form.getPassword()) || isBlank(form.getName())) {
            throw new IllegalArgumentException("아이디, 비밀번호, 이름은 필수입니다.");
        }

        if (memberRepository.existsByUsername(form.getUsername())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        Member member = new Member();
        member.setUsername(form.getUsername());
        member.setPassword(passwordEncoder.encode(form.getPassword())); // ★ 암호화
        member.setName(form.getName());
        member.setRole("USER"); // 기본 권한
        return memberRepository.save(member); // INSERT
    }

    // ── [추가] 문자열 빈 값/null 체크용 도우미 메서드 ──
    private boolean isBlank(String value) { 
        return value == null || value.isBlank(); 
    }

    @Override // 로그인 시 시큐리티가 자동 호출
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("회원 없음 : " + username));

        return User.builder() // 시큐리티용 사용자 객체로 변환
                .username(member.getUsername())
                .password(member.getPassword()) // 암호화 값 → matches()로 비교
                .roles(member.getRole())        // USER → ROLE_USER
                .build();
    }
    
    // MemberService : 로그인한 아이디로 회원 조회
    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "회원이 존재하지 않습니다 : " + username));
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    // 관리자 페이지용: 전체 회원 조회
    public List<Member> findAll() { 
        return memberRepository.findAll(Sort.by("id"));
    }

    // 관리자가 부여할 수 있는 권한 (화면에서 넘어온 값을 그대로 믿지 않는다)
    private static final List<String> ROLES = List.of("USER", "MANAGER", "ADMIN");

    @PreAuthorize("hasRole('ADMIN')") // 관리자만 실행 가능 (2차 잠금)
    public Member changeRole(Long id, String role) { // 권한 변경 (void → Member)
        if (!ROLES.contains(role)) { // 허용된 권한만
            throw new IllegalArgumentException("허용되지 않는 권한입니다 : " + role);
        }
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // ── [추가] 자기 자신의 권한 변경 방지 ──
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (member.getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("자기 자신의 권한은 변경할 수 없습니다.");
        }

        member.setRole(role);
        return memberRepository.save(member); // UPDATE 후 바뀐 회원 객체 리턴
    }

    @PreAuthorize("hasRole('ADMIN')") // 관리자만 실행 가능 (2차 잠금)
    public void deleteMember(Long id) { // 회원 삭제
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // ── [추가] 자기 자신의 계정 삭제 방지 ──
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (member.getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("자기 자신의 계정은 삭제할 수 없습니다.");
        }

        memberRepository.delete(member); // DELETE
    }
}