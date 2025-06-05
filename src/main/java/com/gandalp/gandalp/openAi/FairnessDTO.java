package com.gandalp.gandalp.openAi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class FairnessDTO {

    private Long nurseId;

    private String nurseName;

    private int totalWorked;      // 총 근무 일수 (계산용)

    private int dayWorked;        // 데이 근무 횟수

    private int eveningWorked;    // 이브닝 근무 횟수

    private int nightWorked;      // 나이트 근무 횟수

    private int tempTotalWorked;

    private int tempDayWorked;

    private int tempEveningWorked;

    private int tempNightWorked;

}
