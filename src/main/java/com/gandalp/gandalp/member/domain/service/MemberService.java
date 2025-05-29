package com.gandalp.gandalp.member.domain.service;

import java.util.Optional;

import com.gandalp.gandalp.auth.model.service.AuthService;
import com.gandalp.gandalp.common.repository.CommonCodeRepository;
import com.gandalp.gandalp.member.domain.dto.MemberInfoDto;
import com.gandalp.gandalp.member.domain.dto.MemberResponseDto;
import com.gandalp.gandalp.member.domain.dto.MemberUpdateDto;
import com.gandalp.gandalp.member.domain.entity.Member;
import com.gandalp.gandalp.member.domain.entity.MemberSearchOption;
import com.gandalp.gandalp.member.domain.entity.Type;
import com.gandalp.gandalp.member.domain.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final CommonCodeRepository commonCodeRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public Page<MemberResponseDto> getAllMembers(MemberSearchOption option, String keyword, Type type, Pageable pageable){


        // 기본 전체 조회

        // 검색어가 있는데 검색 옵션이 없는 경우 검색이 안됨
        if (keyword != null && option == null) {
            throw new IllegalArgumentException("검색 옵션을 선택해 주십시오.");
        }

        Page<Member> searchResults = memberRepository.searchMembers( keyword, type, option, pageable);

        // 검색 결과가 없는 경우 예외 처리
        if (searchResults.isEmpty()) {
            throw new EntityNotFoundException("멤버가 존재하지 않습니다.");
        }




        return searchResults.map( m-> {

            String label = commonCodeRepository.findCodeLabelByCodeGroupAndCodeValue("member", String.valueOf(m.getType())
                ).orElse(m.getType().name());
            return MemberResponseDto.builder()
                .id(m.getId())
                .hospitalName(m.getHospital() != null ? m.getHospital().getName() : null)
                .departmentName(m.getDepartment() != null ? m.getDepartment().getName() : null)
                .accountId(m.getAccountId())
                .codeLabel(label)
                .build();
        });
    }

    @Transactional
    public MemberResponseDto updateMember(Long memberId, MemberUpdateDto updateDto){

        String password = updateDto.getPassword();

        // 1. member 조회
        Member member = memberRepository.findById(memberId).orElseThrow(
                ()-> new EntityNotFoundException("해당하는 회원이 존재하지 않습니다.")
        );

        Optional<String> codeLabel = commonCodeRepository.findCodeLabelByCodeGroupAndCodeValue("member",String.valueOf(member.getType()));
        if(codeLabel.isEmpty()) throw new RuntimeException("codeLabel is empty");

        String encodedPassword = passwordEncoder.encode(password);

        updateDto.setPassword(encodedPassword);

        // 2. update
        member.update(updateDto);

        return new MemberResponseDto(member,codeLabel.get());
    }

    @Transactional
    public void deleteMember(Long memberId){

        // 1. 회원이 존재하는지
        Member member = memberRepository.findById(memberId).orElseThrow(
                ()-> new EntityNotFoundException("해당하는 회원이 존재하지 않습니다.")
        );


        memberRepository.deleteById(member.getId());

    }

    public MemberInfoDto getMyInfo(){

        Member loginMember = authService.getLoginMember();

        String label = commonCodeRepository.findCodeLabelByCodeGroupAndCodeValue("member",
            String.valueOf(loginMember.getType())).orElse(loginMember.getType().name());

        return MemberInfoDto.builder()
            .id(loginMember.getId())
            .hospitalName(loginMember.getHospital().getName())
            .deptName(loginMember.getDepartment().getName())
            .type(label)
            .build();
    }




}
