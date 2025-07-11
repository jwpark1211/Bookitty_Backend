package capstone.bookitty.global.application;

import capstone.bookitty.domain.member.domain.Member;
import capstone.bookitty.domain.member.repository.MemberRepository;
import capstone.bookitty.global.authentication.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return memberRepository.findByEmail(username)
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException(username + " -> 데이터베이스에서 찾을 수 없습니다."));
    }

    private UserDetails createUserDetails(Member member) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        String roles = member.getAuthority().toString();
        for (String role : roles.split(",")) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        return new CustomUserDetails(member);
    }
}