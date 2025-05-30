package com.gandalp.gandalp.hospital.domain.repository;

import com.gandalp.gandalp.hospital.domain.dto.ErStatisticsRequestDto;
import com.gandalp.gandalp.hospital.domain.dto.ErStatisticsResponseDto;
import com.gandalp.gandalp.hospital.domain.entity.ErOption;
import com.gandalp.gandalp.hospital.domain.entity.Hospital;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import static com.gandalp.gandalp.hospital.domain.entity.QErStatistics.erStatistics;


@Repository
public class ErStatisticsRepositoryCustomImpl implements ErStatisticsRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public ErStatisticsRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<ErStatisticsResponseDto> findByErStatistics(ErStatisticsRequestDto requestDto, Hospital hospital){

        ErOption option = requestDto.getErOption();
        Long hospitalId = hospital.getId();

        Integer year = null;
        Integer month = null;
        Integer day = null;

        if(option == ErOption.YEAR){
            year = requestDto.getYear();
        }else if(option == ErOption.MONTH){
            year = requestDto.getYear();
            month = requestDto.getMonth();
        }else if(option == ErOption.DAY){
            year = requestDto.getYear();
            month = requestDto.getMonth();
            day = requestDto.getDay();
        }else if(option == null){
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            year = yesterday.getYear();
            month = yesterday.getMonthValue();
            day = yesterday.getDayOfMonth();
        }

        List<ErStatisticsResponseDto> result = queryFactory.select(Projections.constructor(ErStatisticsResponseDto.class,

                erStatistics.hospital.id,
                erStatistics.year,
                erStatistics.month,
                erStatistics.day,
                erStatistics.hour,
                erStatistics.patients

        )).from(erStatistics)
          .where(
                  erStatistics.hospital.id.eq(hospitalId),
                  year != null ? erStatistics.year.eq(year) : null,
                  month != null ? erStatistics.month.eq(month) : null,
                  day != null ? erStatistics.day.eq(day) : null

          ).orderBy(
                    erStatistics.year.asc(),
                    erStatistics.month.asc(),
                    erStatistics.day.asc(),
                    erStatistics.hour.asc()
            )
            .fetch();

        return result;
    }
}
