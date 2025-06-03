package com.gandalp.gandalp.member.domain.repository;

import com.gandalp.gandalp.hospital.domain.entity.Hospital;
import com.gandalp.gandalp.member.domain.dto.MemberResponseDto;
import com.gandalp.gandalp.member.domain.entity.Member;
import com.gandalp.gandalp.member.domain.entity.MemberSearchOption;
import com.gandalp.gandalp.member.domain.entity.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

    Page<Member> searchMembers(Hospital hospital, String keyword, Type type, MemberSearchOption option, Pageable pageable);

}
