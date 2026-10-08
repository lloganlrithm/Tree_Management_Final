package com.example.plantpal.job;

import com.example.plantpal.domain.entity.CareSchedule;
import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.enums.ActionType;
import com.example.plantpal.event.CareDueEvent;
import com.example.plantpal.service.CareService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// ทดสอบ Template Method: เรียก run() ตรงๆ ไม่ต้องรอ 08:00
// คลาสแม่ AbstractDailyJob ต้องเรียก findTargets -> process ครบทุกรายการ ในลำดับเดียวกันทั้งสอง job
@ExtendWith(MockitoExtension.class)
class CareReminderJobsTest {

    @Mock private PlatformTransactionManager transactionManager;   // ตัวปลอม ไม่มี DB จริง
    @Mock private CareService careService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private final LocalDate today = LocalDate.now(AbstractDailyJob.ZONE);

    private CareSchedule schedule(String nickname, ActionType action, LocalDate due) {
        Species species = new Species();
        species.setName("ฟิโลเดนดรอน");
        Plant plant = new Plant();
        plant.setId(10L);
        plant.setNickname(nickname);
        plant.setSpecies(species);
        plant.setUser(User.builder().id(7L).build());
        CareSchedule schedule = new CareSchedule();
        schedule.setPlant(plant);
        schedule.setActionType(action);
        schedule.setNextDueDate(due);
        return schedule;
    }

    @Test
    void dueReminderPublishesOneEventPerScheduleDueTomorrow() {
        LocalDate tomorrow = today.plusDays(1);
        when(careService.findDueBetween(tomorrow, tomorrow)).thenReturn(List.of(
                schedule("ฟิโลหัวใจ", ActionType.WATER, tomorrow),
                schedule("ยางดำ", ActionType.FERTILIZE, tomorrow)));
        CareDueReminderJob job = new CareDueReminderJob(transactionManager, careService, eventPublisher);

        int done = job.run();

        assertThat(done).isEqualTo(2);
        ArgumentCaptor<CareDueEvent> captor = ArgumentCaptor.forClass(CareDueEvent.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());
        CareDueEvent first = captor.getAllValues().get(0);
        assertThat(first.isOverdue()).isFalse();
        assertThat(first.getOwnerId()).isEqualTo(7L);
        assertThat(first.getPlantName()).isEqualTo("ฟิโลหัวใจ");
        assertThat(first.getActionType()).isEqualTo(ActionType.WATER);
    }

    @Test
    void overdueReminderMarksEventsOverdue() {
        when(careService.findOverdue(today)).thenReturn(List.of(
                schedule("ยางดำ", ActionType.REPOT, today.minusDays(3))));
        OverdueCareReminderJob job = new OverdueCareReminderJob(transactionManager, careService, eventPublisher);

        int done = job.run();

        assertThat(done).isEqualTo(1);
        ArgumentCaptor<CareDueEvent> captor = ArgumentCaptor.forClass(CareDueEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().isOverdue()).isTrue();
        assertThat(captor.getValue().getDueDate()).isEqualTo(today.minusDays(3));
    }

    @Test
    void plantWithoutNicknameUsesSpeciesName() {
        when(careService.findOverdue(today)).thenReturn(List.of(schedule(null, ActionType.WATER, today.minusDays(1))));
        OverdueCareReminderJob job = new OverdueCareReminderJob(transactionManager, careService, eventPublisher);

        job.run();

        ArgumentCaptor<CareDueEvent> captor = ArgumentCaptor.forClass(CareDueEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getPlantName()).isEqualTo("ฟิโลเดนดรอน");
    }

    @Test
    void noTargetsPublishesNothing() {
        LocalDate tomorrow = today.plusDays(1);
        when(careService.findDueBetween(tomorrow, tomorrow)).thenReturn(List.of());
        CareDueReminderJob job = new CareDueReminderJob(transactionManager, careService, eventPublisher);

        assertThat(job.run()).isZero();
        verify(eventPublisher, never()).publishEvent(any());
    }
}
