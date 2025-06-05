package com.gandalp.gandalp.calender.domain.service;

import com.gandalp.gandalp.auth.model.service.AuthService;
import com.gandalp.gandalp.calender.domain.dto.OrsDeleteRequestDto;
import com.gandalp.gandalp.calender.domain.dto.OrsGetRequestDto;
import com.gandalp.gandalp.calender.domain.dto.OrsGetResponseDto;
import com.gandalp.gandalp.calender.domain.dto.OrsRequestDto;
import com.gandalp.gandalp.calender.domain.dto.OrsResponseDto;
import com.gandalp.gandalp.calender.domain.dto.PersonalScheduleDeleteRequestDto;
import com.gandalp.gandalp.calender.domain.dto.PersonalScheduleRequestDto;
import com.gandalp.gandalp.calender.domain.dto.PersonalScheduleResponseDto;
import com.gandalp.gandalp.calender.domain.dto.PersonalScheduleUpdateRequestDto;
import com.gandalp.gandalp.calender.domain.dto.RoomResponseDto;
import com.gandalp.gandalp.hospital.domain.entity.Department;
import com.gandalp.gandalp.hospital.domain.entity.Room;
import com.gandalp.gandalp.hospital.domain.entity.Status;
import com.gandalp.gandalp.hospital.domain.repository.RoomRepository;
import com.gandalp.gandalp.member.domain.entity.Nurse;
import com.gandalp.gandalp.member.domain.entity.SurgeryNurse;
import com.gandalp.gandalp.member.domain.repository.NurseRepository;
import com.gandalp.gandalp.member.domain.repository.SurgeryNurseRepository;
import com.gandalp.gandalp.schedule.domain.entity.Category;
import com.gandalp.gandalp.schedule.domain.entity.Schedule;
import com.gandalp.gandalp.schedule.domain.entity.SurgerySchedule;
import com.gandalp.gandalp.schedule.domain.repository.ScheduleRepository;
import com.gandalp.gandalp.schedule.domain.repository.SurgeryScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CalenderService {
    private final ScheduleRepository scheduleRepository;
    private final NurseRepository nurseRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoomRepository roomRepository;
    private final SurgeryScheduleRepository surgeryScheduleRepository;
    private final SurgeryNurseRepository surgeryNurseRepository;
    private final AuthService authService;

    @Transactional
    public PersonalScheduleResponseDto createPersonalSchedule(/*final*/ PersonalScheduleRequestDto personalScheduleRequestDto) {
        Nurse nurse = nurseRepository.findById(personalScheduleRequestDto.getNurseId()).orElseThrow(() -> new RuntimeException("간호사를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(personalScheduleRequestDto.getPassword(), nurse.getPassword())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        Schedule schedule = Schedule.builder()
                .nurse(nurse)
                .category(Category.PERSONAL)
                .content(personalScheduleRequestDto.getContent())
                .startTime(personalScheduleRequestDto.getStartTime())
                .endTime(personalScheduleRequestDto.getEndTime())
                .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);

        return PersonalScheduleResponseDto.fromSchedule(savedSchedule);
    }

    @Transactional
    public void deletePersonalSchedule(PersonalScheduleDeleteRequestDto personalScheduleDeleteRequestDto) {

        Schedule schedule = scheduleRepository.findById(personalScheduleDeleteRequestDto.getScheduleId()).orElseThrow(() -> new IllegalStateException("일정을 찾을 수 없습니다."));

        if (!passwordEncoder.matches(personalScheduleDeleteRequestDto.getPassword(), schedule.getNurse().getPassword())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        scheduleRepository.delete(schedule);
    }

    @Transactional
    public PersonalScheduleResponseDto updatePersonalSchedule(PersonalScheduleUpdateRequestDto personalScheduleUpdateRequestDto) {

        Schedule schedule = scheduleRepository.findById(personalScheduleUpdateRequestDto.getScheduleId()).orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));

        Nurse nurse = schedule.getNurse();

        if (!passwordEncoder.matches(personalScheduleUpdateRequestDto.getPassword(), nurse.getPassword())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        schedule.updateSchedule(personalScheduleUpdateRequestDto);

        return PersonalScheduleResponseDto.fromSchedule(schedule);
    }

    public List<PersonalScheduleResponseDto> getPersonalSchedules(Long nurseId) {
        Nurse nurse = nurseRepository.findById(nurseId).orElseThrow(() -> new RuntimeException("간호사를 찾을 수 없습니다."));
        List<Nurse> nurses = new ArrayList<>();
        nurses.add(nurse);

        List<Schedule> schedules = scheduleRepository.findInNurseAndCategory(nurses, Category.PERSONAL);

        return schedules.stream().map(PersonalScheduleResponseDto::fromSchedule).collect(Collectors.toList());
    }

    public List<PersonalScheduleResponseDto> getSchedules() {

        Department department = authService.getLoginMember().getDepartment();
        List<Nurse> nurseListByDepartment = nurseRepository.findByDepartment(department);
        List<Schedule> schedules = scheduleRepository.findInNurseAndCategory(nurseListByDepartment, Category.PERSONAL);

        return schedules.stream().map(PersonalScheduleResponseDto::fromSchedule).collect(Collectors.toList());
    }

    @Transactional
    public OrsResponseDto createOrs(OrsRequestDto orsRequestDto) {
        Room room = roomRepository.findById(orsRequestDto.getRoomId()).orElseThrow(() -> new IllegalStateException("수술실이 존재하지 않습니다."));

//        if (room.getStatus().equals(Status.USING)) {
//            throw new IllegalStateException("이미 사용중인 수술실입니다.");
//        }

        boolean isOverlapping = !surgeryScheduleRepository.findOverlappingSchedules(orsRequestDto.getRoomId(), orsRequestDto.getStartTime(), orsRequestDto.getEndTime()).isEmpty();

        if (isOverlapping) {
            throw new IllegalStateException("겹치는 수술 일정이 있습니다.");
        }

        if (orsRequestDto.getNurseIds().isEmpty()) {
            throw new IllegalStateException("수술 간호사가 없습니다.");
        }

        // findById 해야하나
        boolean isValid = orsRequestDto.getNurseIds().stream().anyMatch(nurseId ->
                passwordEncoder.matches(orsRequestDto.getPassword(), nurseRepository.findById(nurseId).get().getPassword()));

        if (!isValid) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        };

        SurgerySchedule surgerySchedule = SurgerySchedule.builder()
                .room(room)
                .content(orsRequestDto.getContent())
                .startTime(orsRequestDto.getStartTime())
                .endTime(orsRequestDto.getEndTime())
                .build();

        SurgerySchedule savedSurgerySchedule = surgeryScheduleRepository.save(surgerySchedule);

        List<SurgeryNurse> surgeryNurses = orsRequestDto.getNurseIds()
                .stream()
                .map(nurseId -> {
                    Nurse findNurse = nurseRepository.findById(nurseId).orElseThrow(() -> new RuntimeException("간호사를 찾을 수 없습니다."));

                    // 이렇게 넣지 말고 수술 일정은 따로 get해서 넣어야겠다.
//                    Schedule schedule = Schedule.builder()
//                            .nurse(findNurse)
//                            .category(Category.PERSONAL)
//                            .content(savedSurgerySchedule.getContent())
//                            .startTime(savedSurgerySchedule.getStartTime())
//                            .endTime(savedSurgerySchedule.getEndTime())
//                            .build();
//
//                    scheduleRepository.save(schedule);

                    SurgeryNurse surgeryNurse = SurgeryNurse.builder()
                            .nurse(findNurse)
                            .surgerySchedule(savedSurgerySchedule)
                            .build();

                    SurgeryNurse savedSurgeryNurse = surgeryNurseRepository.save(surgeryNurse);
                    return savedSurgeryNurse;
                })
                .collect(Collectors.toList());

        OrsResponseDto orsResponseDto = OrsResponseDto.builder()
                .surgeryScheduleId(savedSurgerySchedule.getId())
                .roomId(savedSurgerySchedule.getRoom().getId())
                .content(savedSurgerySchedule.getContent())
                .nurseIds(orsRequestDto.getNurseIds())
                .startTime(savedSurgerySchedule.getStartTime())
                .endTime(savedSurgerySchedule.getEndTime())
                .build();

        return orsResponseDto; // orsResponseDto 수정해야 됌
    }

    @Transactional
    public void deleteOrs(OrsDeleteRequestDto orsDeleteRequestDto) {

        // 수술 일정 조회
        SurgerySchedule surgerySchedule = surgeryScheduleRepository.findById(orsDeleteRequestDto.getSurgeryScheduleId()).orElseThrow(() -> new RuntimeException("수술 일정을 찾을 수 없습니다."));

        // 수술 간호사 조회
        List<SurgeryNurse> surgeryNursesBySurgeryScheduleId = surgeryNurseRepository.findBySurgeryScheduleId(surgerySchedule.getId());

        // 모든 수술 간호사 비밀번호 확인
        boolean isValid = surgeryNursesBySurgeryScheduleId.stream().anyMatch(surgeryNurse ->
                passwordEncoder.matches(orsDeleteRequestDto.getPassword(), surgeryNurse.getNurse().getPassword())
        );
        if (!isValid) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        // 개인 일정 삭제, 수술 일정은 개인 일정으로 넣지 않고 수술 일정으로 해서 넣어야겠다. 억지로 수술 간호사들이 여러명 이니까 response를 거기에 맞춰서 늘려야겠다.
//        scheduleRepository.find

        // 수술 간호사 삭제
        surgeryNurseRepository.deleteAll(surgeryNursesBySurgeryScheduleId);

        // 수술 일정 삭제
        surgeryScheduleRepository.delete(surgerySchedule);
    }

//    public OrsGetResponseDto getOrs(OrsGetRequestDto orsGetRequestDto) {
//
//
//
//    }

    // join fetch 고려
    public List<OrsResponseDto> getOrs() {

        Department department = authService.getLoginMember().getDepartment();
        Long hospitalId = department.getHospital().getId();

        List<Long> roomIds = roomRepository.findByHospitalId(hospitalId).stream().map(r -> r.getId()).collect(Collectors.toList());

        List<OrsResponseDto> orsResponseDtos = surgeryScheduleRepository.findByRoomIdIn(roomIds).stream().map(s -> {
            List<Long> nurseIds = surgeryNurseRepository.findBySurgeryScheduleId(s.getId()).stream().map(sn -> sn.getNurse().getId()).collect(Collectors.toList());

//        List<OrsResponseDto> orsResponseDtos = surgeryScheduleRepository.findAll().stream().map(s -> {
//                    List<Long> nurseIds = surgeryNurseRepository.findBySurgeryScheduleId(s.getId()).stream().map(sn -> sn.getNurse().getId()).collect(Collectors.toList());

                    return OrsResponseDto.builder()
                            .surgeryScheduleId(s.getId())
                            .roomId(s.getRoom().getId())
                            .content(s.getContent())
                            .nurseIds(nurseIds)
                            .startTime(s.getStartTime())
                            .endTime(s.getEndTime())
                            .build();})
                .collect(Collectors.toList());

        return orsResponseDtos;
    }

    public List<RoomResponseDto> getRooms() {

        Department department = authService.getLoginMember().getDepartment();
        Long hospitalId = department.getHospital().getId();

        List<Room> rooms = roomRepository.findByHospitalId(hospitalId);

//        List<Room> rooms = roomRepository.findAll();

        List<RoomResponseDto> roomResponseDtos = rooms.stream().map(r ->
                RoomResponseDto.builder()
                        .roomId(r.getId())
                        .status(r.getStatus())
                        .build()
        ).collect(Collectors.toList());

        return roomResponseDtos;
    }
}
