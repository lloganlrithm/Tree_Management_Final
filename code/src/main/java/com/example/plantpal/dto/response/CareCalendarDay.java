package com.example.plantpal.dto.response;

import com.example.plantpal.domain.entity.CareSchedule;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

// ปฏิทินการดูแล 1 วัน (CareCalendarIterator สร้างให้ทีละวัน)
@Getter
@AllArgsConstructor
public class CareCalendarDay {
    private LocalDate date;
    private List<CareSchedule> tasks; // งานดูแลที่ครบกำหนดวันนี้ (ว่างได้)
    private boolean today; // เป็นวันนี้หรือไม่ (ไว้ไฮไลต์ในปฏิทิน)
}