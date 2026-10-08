package com.example.plantpal.controller.api;

import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.dto.request.PlantRequest;
import com.example.plantpal.dto.response.PlantResponse;
import com.example.plantpal.exception.InvalidRequestException;
import com.example.plantpal.exception.ResourceNotFoundException;
import com.example.plantpal.mapper.PlantMapper;
import com.example.plantpal.service.CurrentUserService;
import com.example.plantpal.service.PlantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

// REST API ต้นไม้ของฉัน (CRUD) ส่งออกเป็น PlantResponse เสมอ ไม่ส่ง Entity
// ทุก endpoint ใช้เฉพาะต้นไม้ของคนที่ login อยู่ (ต้นของคนอื่น = 404 เหมือนไม่มีอยู่)
// error (404 / 400) ให้ GlobalExceptionHandler แปลงเป็น error response มาตรฐาน
@RestController
@RequestMapping("/api/v1/plants")
@RequiredArgsConstructor
public class PlantApiController {

    private final PlantService plantService;
    private final CurrentUserService currentUserService;
    private final PlantMapper mapper;

    // GET /api/v1/plants?page=0&size=10&sort=createdAt,desc -> 200 + รายการแบบแบ่งหน้า
    @GetMapping
    public PagedModel<PlantResponse> list(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Plant> page = plantService.findMyPlantsPage(currentEmail(), pageable);
        return new PagedModel<>(page.map(mapper::toResponse));
    }

    // GET /api/v1/plants/{id} -> 200 หรือ 404
    @GetMapping("/{id}")
    public PlantResponse get(@PathVariable Long id) {
        return mapper.toResponse(findOwned(id));
    }

    // POST /api/v1/plants (JSON) -> 201 Created + Location ของต้นใหม่
    // สร้างแล้วระบบส่ง PlantCreatedEvent เหมือนหน้าเว็บ จึงได้ตารางดูแลอัตโนมัติด้วย
    @PostMapping
    public ResponseEntity<PlantResponse> create(@Valid @RequestBody PlantRequest request) {
        request.setId(null);   // สร้างใหม่เสมอ ไม่สนใจ id ที่ส่งมาใน JSON
        Plant plant;
        try {
            plant = plantService.create(request, currentEmail());
        } catch (IllegalArgumentException e) {   // เช่น speciesId ไม่มีอยู่จริง
            throw new InvalidRequestException(e.getMessage());
        }
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(plant.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(plant));
    }

    // PUT /api/v1/plants/{id} (JSON) -> 200, ไม่พบ -> 404, ข้อมูลผิด -> 400
    // ค่าเดิมถูกเก็บไว้ให้ย้อนได้ (Memento) เหมือนแก้จากหน้าเว็บ
    @PutMapping("/{id}")
    public PlantResponse update(@PathVariable Long id, @Valid @RequestBody PlantRequest request) {
        findOwned(id);         // ต้นของคนอื่น/ไม่มีอยู่ -> 404 ก่อน
        request.setId(id);     // ใช้ id จาก URL เท่านั้น
        try {
            return mapper.toResponse(plantService.update(request, currentEmail()));
        } catch (IllegalArgumentException e) {   // เช่น speciesId ไม่มีอยู่จริง
            throw new InvalidRequestException(e.getMessage());
        }
    }

    // DELETE /api/v1/plants/{id} -> 204 No Content, ไม่พบ -> 404
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        findOwned(id);
        plantService.delete(id, currentEmail());
        return ResponseEntity.noContent().build();
    }

    // หาต้นไม้ของคนที่ login อยู่ ไม่เจอ (หรือเป็นของคนอื่น) -> 404
    private Plant findOwned(Long id) {
        try {
            return plantService.findMyPlant(id, currentEmail());
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
    }

    private String currentEmail() {
        return currentUserService.getCurrentUser().getEmail();
    }
}
