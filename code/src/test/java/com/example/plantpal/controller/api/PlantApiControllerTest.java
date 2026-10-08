package com.example.plantpal.controller.api;

import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.dto.request.PlantRequest;
import com.example.plantpal.dto.response.PlantResponse;
import com.example.plantpal.exception.InvalidRequestException;
import com.example.plantpal.exception.ResourceNotFoundException;
import com.example.plantpal.mapper.PlantMapper;
import com.example.plantpal.service.CurrentUserService;
import com.example.plantpal.service.PlantService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Unit test ของ REST API /api/v1/plants: status code, การแปลงเป็น DTO, และการกันต้นไม้ของคนอื่น
@ExtendWith(MockitoExtension.class)
class PlantApiControllerTest {

    private static final String OWNER = "user@plantpal.com";

    @Mock
    private PlantService plantService;

    @Mock
    private CurrentUserService currentUserService;

    @Spy
    private PlantMapper mapper = new PlantMapper();   // ใช้ mapper ตัวจริง

    @InjectMocks
    private PlantApiController controller;

    private Plant plant;

    @BeforeEach
    void setUp() {
        User me = new User();
        me.setEmail(OWNER);
        lenient().when(currentUserService.getCurrentUser()).thenReturn(me);

        Species species = new Species();
        species.setId(1L);
        species.setName("มอนสเตอร่า");

        plant = new Plant();
        plant.setId(5L);
        plant.setSpecies(species);
        plant.setNickname("ปา");

        // จำลอง request ปัจจุบัน ให้ create() สร้าง Location ได้
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/plants");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    private PlantRequest request(Long speciesId, String nickname) {
        PlantRequest r = new PlantRequest();
        r.setSpeciesId(speciesId);
        r.setNickname(nickname);
        return r;
    }

    @Test
    void listReturnsMyPlantsAsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(plantService.findMyPlantsPage(OWNER, pageable)).thenReturn(new PageImpl<>(List.of(plant), pageable, 1));

        PagedModel<PlantResponse> result = controller.list(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nickname()).isEqualTo("ปา");
        assertThat(result.getMetadata().totalElements()).isEqualTo(1);
    }

    @Test
    void getOtherUsersPlantReturns404() {
        when(plantService.findMyPlant(99L, OWNER)).thenThrow(new IllegalArgumentException("ไม่พบต้นไม้นี้"));

        assertThatThrownBy(() -> controller.get(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("ไม่พบต้นไม้นี้");
    }

    @Test
    void createReturns201WithLocation() {
        when(plantService.create(any(PlantRequest.class), eq(OWNER))).thenReturn(plant);

        ResponseEntity<PlantResponse> response = controller.create(request(1L, "ปา"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("http://localhost/api/v1/plants/5");
        assertThat(response.getBody().id()).isEqualTo(5L);
    }

    @Test
    void createWithUnknownSpeciesReturns400() {
        when(plantService.create(any(PlantRequest.class), eq(OWNER)))
                .thenThrow(new IllegalArgumentException("ไม่พบพันธุ์ไม้ที่เลือก"));

        assertThatThrownBy(() -> controller.create(request(404L, "ปา")))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void updateUsesIdFromUrl() {
        when(plantService.findMyPlant(5L, OWNER)).thenReturn(plant);
        when(plantService.update(any(PlantRequest.class), eq(OWNER))).thenReturn(plant);
        PlantRequest body = request(1L, "ปาใหม่");
        body.setId(123L);   // id ใน JSON ต้องถูกแทนด้วย id จาก URL

        controller.update(5L, body);

        ArgumentCaptor<PlantRequest> captor = ArgumentCaptor.forClass(PlantRequest.class);
        verify(plantService).update(captor.capture(), eq(OWNER));
        assertThat(captor.getValue().getId()).isEqualTo(5L);
    }

    @Test
    void deleteReturns204() {
        when(plantService.findMyPlant(5L, OWNER)).thenReturn(plant);

        ResponseEntity<Void> response = controller.delete(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(plantService).delete(5L, OWNER);
    }

    @Test
    void deleteOtherUsersPlantReturns404AndDeletesNothing() {
        when(plantService.findMyPlant(99L, OWNER)).thenThrow(new IllegalArgumentException("ไม่พบต้นไม้นี้"));

        assertThatThrownBy(() -> controller.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(plantService, never()).delete(anyLong(), any());
    }
}
