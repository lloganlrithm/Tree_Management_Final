package com.example.plantpal.iterator;

import com.example.plantpal.domain.entity.CareSchedule;
import com.example.plantpal.domain.enums.ActionType;
import com.example.plantpal.dto.response.CareCalendarDay;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// ทดสอบ Iterator ของปฏิทิน: เดินทีละวัน งานลงถูกวัน และหยุดเมื่อครบจำนวนวัน
class CareCalendarIteratorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 8);

    private CareSchedule schedule(ActionType type, LocalDate due) {
        return CareSchedule.builder().actionType(type).nextDueDate(due).build();
    }

    private List<CareCalendarDay> walk(CareCalendarIterator iterator) {
        List<CareCalendarDay> days = new ArrayList<>();
        while (iterator.hasNext()) {
            days.add(iterator.next());
        }
        return days;
    }

    @Test
    void walksExactlyTheRequestedNumberOfDays() {
        List<CareCalendarDay> days = walk(new CareCalendarIterator(TODAY, 30, List.of()));

        assertThat(days).hasSize(30);
        assertThat(days.get(0).getDate()).isEqualTo(TODAY);
        assertThat(days.get(29).getDate()).isEqualTo(TODAY.plusDays(29));
    }

    @Test
    void onlyFirstDayIsMarkedToday() {
        List<CareCalendarDay> days = walk(new CareCalendarIterator(TODAY, 3, List.of()));

        assertThat(days.get(0).isToday()).isTrue();
        assertThat(days.get(1).isToday()).isFalse();
        assertThat(days.get(2).isToday()).isFalse();
    }

    @Test
    void putsTasksOnTheirDueDate() {
        CareSchedule water = schedule(ActionType.WATER, TODAY.plusDays(2));
        CareSchedule fertilize = schedule(ActionType.FERTILIZE, TODAY.plusDays(2));
        CareSchedule repot = schedule(ActionType.REPOT, TODAY.plusDays(5));

        List<CareCalendarDay> days = walk(new CareCalendarIterator(TODAY, 7, List.of(water, fertilize, repot)));

        assertThat(days.get(0).getTasks()).isEmpty();
        assertThat(days.get(2).getTasks()).containsExactlyInAnyOrder(water, fertilize);
        assertThat(days.get(5).getTasks()).containsExactly(repot);
    }

    @Test
    void nextAfterLastDayThrows() {
        CareCalendarIterator iterator = new CareCalendarIterator(TODAY, 1, List.of());
        iterator.next();

        assertThat(iterator.hasNext()).isFalse();
        assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void rejectsZeroDays() {
        assertThatThrownBy(() -> new CareCalendarIterator(TODAY, 0, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}