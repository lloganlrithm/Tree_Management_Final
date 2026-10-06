package com.example.plantpal.service;

import com.example.plantpal.domain.entity.Species;
import com.example.plantpal.dto.request.SpeciesRequest;

import java.util.List;

public interface SpeciesService {

    List<Species> findAll();

    // id ว่าง = เพิ่มใหม่, มี id = แก้ไข
    Species save(SpeciesRequest request);

    void delete(Long id);
}