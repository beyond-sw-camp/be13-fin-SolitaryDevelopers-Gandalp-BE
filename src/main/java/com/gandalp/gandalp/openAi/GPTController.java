package com.gandalp.gandalp.openAi;

import com.gandalp.gandalp.schedule.domain.dto.OffScheduleTempResponseDto;
import com.gandalp.gandalp.schedule.domain.dto.WorkTempResponseDto;
import com.gandalp.gandalp.schedule.domain.entity.ScheduleTemp;
import com.gandalp.gandalp.schedule.domain.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/gpt")
@RequiredArgsConstructor
public class GPTController {

    private final OpenAIService openAIService;
    private final ScheduleService scheduleService;


    @GetMapping("/test")
    public String testGpt(@RequestParam(required = true) String question) {
        return openAIService.askQuestion(question);
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateSchedule(@RequestBody OpenAIRequestDTO request) {
        try{
            ScheduleResult scheduleJson = openAIService.requestScheduleFromGPT(request);
            return ResponseEntity.ok().body(scheduleJson);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PostMapping("/createWorkTemp")
    public ResponseEntity<?> createWorkTemp(@RequestBody ScheduleResult scheduleResult){
        try {
            List<WorkTempResponseDto> scheduleTempResponseDtos = scheduleService.createWorkTemp(scheduleResult);
            return ResponseEntity.ok().body(scheduleTempResponseDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/fairness")
    public ResponseEntity<?> showFairness(){

        try {
            List<FairnessDTO> dto = openAIService.buildFairnessFromDatabase();
            return ResponseEntity.ok().body(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PostMapping("/generate-and-create")
    public ResponseEntity<?> generateAndCreateSchedule() {
        try {
            // 1. 요청 DTO 구성 (DB에서)
            OpenAIRequestDTO dto = openAIService.buildRequestDTOFromDatabase();

            // 2. GPT 호출
            ScheduleResult scheduleResult = openAIService.requestScheduleFromGPT(dto);

            // 3. DB 저장
            List<WorkTempResponseDto> resultDtos = scheduleService.createWorkTemp(scheduleResult);

            return ResponseEntity.ok(resultDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
