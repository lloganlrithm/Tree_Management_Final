package com.example.plantpal.iterator;

import com.example.plantpal.domain.entity.CareSchedule;
import com.example.plantpal.dto.response.CareCalendarDay;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

// Iterator: เดินปฏิทินทีละวัน ตั้งแต่วันเริ่มต้นไปจนครบจำนวนวันที่กำหนด
// แต่ละครั้งที่เรียก next() จะได้ "วัน" 1 วัน พร้อมงานดูแลที่ครบกำหนดวันนั้น
// หน้า HTML จึงไม่ต้องคำนวณวันที่เอง แค่วนแสดงผลตามลำดับที่ได้
public class CareCalendarIterator implements Iterator<CareCalendarDay> {

    private final LocalDate today;
    private final LocalDate lastDay; // วันสุดท้าย (รวมวันนี้ด้วย)
    private final Map<LocalDate, List<CareSchedule>> tasksByDate; // จัดกลุ่มงานตามวันกำหนด
    private LocalDate current; // วันที่ next() จะคืนครั้งถัดไป

    public CareCalendarIterator(LocalDate start, int days, List<CareSchedule> schedules) {
        if (days < 1) {
            throw new IllegalArgumentException("จำนวนวันต้องมากกว่า 0");
        }
        this.today = start;
        this.current = start;
        this.lastDay = start.plusDays(days - 1L);
        this.tasksByDate = schedules.stream()
                .collect(Collectors.groupingBy(CareSchedule::getNextDueDate));
    }

    @Override
    public boolean hasNext() {
        return !current.isAfter(lastDay);
    }

    @Override
    public CareCalendarDay next() {
        if (!hasNext()) {
            throw new NoSuchElementException("เดินครบทุกวันแล้ว");
        }
        CareCalendarDay day = new CareCalendarDay(
                current,
                tasksByDate.getOrDefault(current, List.of()),
                current.equals(today));
        current = current.plusDays(1);
        return day;
    }
}