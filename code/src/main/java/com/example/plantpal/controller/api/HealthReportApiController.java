package com.example.plantpal.controller.api;

import com.example.plantpal.domain.entity.HealthReport;
import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.enums.ReportStatus;
import com.example.plantpal.dto.request.HealthReportRequest;
import com.example.plantpal.dto.request.ReportReplyRequest;
import com.example.plantpal.dto.response.HealthReportResponse;
import com.example.plantpal.exception.ForbiddenException;
import com.example.plantpal.mapper.HealthReportMapper;
import com.example.plantpal.service.CurrentUserService;
import com.example.plantpal.service.HealthReportService;
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

// REST API รายงานสุขภาพ (CRUD) ส่งออกเป็น HealthReportResponse เสมอ ไม่ส่ง Entity
// error (404 / 403 / 400) ให้ GlobalExceptionHandler ของพรีมแปลงเป็น error response มาตรฐาน
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class HealthReportApiController {

    private final HealthReportService healthReportService;
    private final CurrentUserService currentUserService;
    private final HealthReportMapper mapper;

    // GET /api/v1/reports?status=PENDING&page=0&size=10&sort=createdAt,desc
    // USER เห็นเฉพาะของตัวเอง, ADMIN เห็นทุกรายงาน
    @GetMapping
    public PagedModel<HealthReportResponse> list(
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        User me = currentUserService.getCurrentUser();
        Page<HealthReport> page = currentUserService.isAdmin()
                ? healthReportService.findAll(status, null, pageable)
                : healthReportService.findMyReports(me.getEmail(), status, pageable);
        return new PagedModel<>(page.map(mapper::toResponse));
    }

    // GET /api/v1/reports/{id} -> 200 หรือ 404
    @GetMapping("/{id}")
    public HealthReportResponse get(@PathVariable Long id) {
        HealthReport report = currentUserService.isAdmin()
                ? healthReportService.findById(id)
                : healthReportService.findMyReport(id, currentUserService.getCurrentUser().getEmail());
        return mapper.toResponse(report);
    }

    // POST /api/v1/reports (JSON) -> 201 Created + Location ของรายงานใหม่
    // แนบรูปผ่าน API ไม่ได้ (รูปส่งผ่านฟอร์มหน้าเว็บ) field image ใน JSON จะถูกข้าม
    @PostMapping
    public ResponseEntity<HealthReportResponse> create(@Valid @RequestBody HealthReportRequest request) {
        HealthReport report = healthReportService.create(request, currentUserService.getCurrentUser().getEmail());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(report.getId()).toUri();
        return ResponseEntity.created(location).body(mapper.toResponse(report));
    }

    // PATCH /api/v1/reports/{id}/reply (เฉพาะ ADMIN) -> 200, ไม่ใช่ admin -> 403
    @PatchMapping("/{id}/reply")
    public HealthReportResponse reply(@PathVariable Long id, @Valid @RequestBody ReportReplyRequest request) {
        if (!currentUserService.isAdmin()) {
            throw new ForbiddenException("เฉพาะผู้ดูแลระบบเท่านั้นที่ตอบรายงานได้");
        }
        return mapper.toResponse(healthReportService.reply(id, request.getStatus(), request.getAdminReply(), request.getPlantHealthStatus()));
    }

    // DELETE /api/v1/reports/{id} (เจ้าของเท่านั้น) -> 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        healthReportService.deleteMyReport(id, currentUserService.getCurrentUser().getEmail());
        return ResponseEntity.noContent().build();
    }
}
