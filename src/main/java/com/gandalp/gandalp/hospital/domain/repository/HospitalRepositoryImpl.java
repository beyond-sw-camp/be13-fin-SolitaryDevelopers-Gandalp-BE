package com.gandalp.gandalp.hospital.domain.repository;

import com.gandalp.gandalp.hospital.domain.dto.HospitalDto;
import com.gandalp.gandalp.hospital.domain.entity.SortOption;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.gandalp.gandalp.hospital.domain.entity.QHospital.hospital;

@Repository
public class HospitalRepositoryImpl implements HospitalRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public HospitalRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public Page<HospitalDto> searchNearbyHospitals(List<Long> hospitalIds, String keyword, SortOption sortOption, Pageable pageable) {


        BooleanBuilder search = new BooleanBuilder();


        if (keyword != null && !keyword.isBlank()) {
            search.and(
                    hospital.name.containsIgnoreCase(keyword)
                            .or(hospital.address.containsIgnoreCase(keyword))
            );
        }

        // hospitalIds가 null/빈 리스트가 아닐 때만 IN 조건 추가
        else if (hospitalIds != null && !hospitalIds.isEmpty()) {
            search.and(hospital.id.in(hospitalIds));
        }



        List<HospitalDto> content = queryFactory
                .select(Projections.constructor(HospitalDto.class,
                        hospital.id,
                        hospital.name,
                        hospital.address,
                        hospital.phoneNumber,
                        hospital.totalErCount,
                        hospital.availableErCount,
                        hospital.latitude,
                        hospital.longitude
                        ))
                .from(hospital)
                .where(search )
                .orderBy(sortOption == SortOption.ER_COUNT
                        ? hospital.availableErCount.desc()
                        : hospital.id.asc()  // 일단 기본으로 함
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(hospital.count())
                .from(hospital)
                .where(search);


        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
