package com.gandalp.gandalp.schedule.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.gandalp.gandalp.member.domain.entity.Nurse;
import com.gandalp.gandalp.member.domain.entity.Status;
import com.gandalp.gandalp.schedule.domain.dto.StaticsResponseDto;
import com.gandalp.gandalp.schedule.domain.entity.Schedule;
import com.gandalp.gandalp.schedule.domain.entity.SelectOption;

public interface ScheduleRepositoryCustom {

	boolean findCurrentSchedule(Long nurseId, LocalDateTime now );

	StaticsResponseDto getNursesWorkingStatistics(Nurse nurse, SelectOption selectOption,  int year, int month, Integer quarter );


}


