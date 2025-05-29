package com.gandalp.gandalp.hospital.domain.repository;

import com.gandalp.gandalp.hospital.domain.dto.HospitalDto;
import com.gandalp.gandalp.hospital.domain.entity.SortOption;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
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
import java.util.stream.Collectors;

import static com.gandalp.gandalp.hospital.domain.entity.QHospital.hospital;

@Repository
public class HospitalRepositoryImpl implements HospitalRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public HospitalRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public List<HospitalDto> searchNearbyHospitals(List<Long> hospitalIds, String keyword, SortOption sortOption) {


        BooleanBuilder search = new BooleanBuilder();

        // keyword가 null인 경우
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



        JPAQuery<HospitalDto> query = queryFactory
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
                .where(search);

            if (sortOption == SortOption.ER_COUNT) {
                query.orderBy(hospital.availableErCount.desc());
            } else if (hospitalIds != null && !hospitalIds.isEmpty()) {
                query.orderBy(builderFildOrder(hospitalIds).asc());
            }


        // 페이징 없이 리스트로 전체 결과 반환
        return query.fetch();
    }



    // 앞에서 거리순으로 받은 id 순서 유지해서 조회
    private NumberExpression<Integer> builderFildOrder(List<Long> hospitalIds) {

        String csv = hospitalIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return Expressions.numberTemplate(
                Integer.class,
                "FIELD({0}, " + csv + ")",
                hospital.id

        );
    }
}
