package com.example.plantpal.service;

import com.example.plantpal.domain.entity.Plant;
import com.example.plantpal.dto.request.PlantRequest;

import java.util.List;

public interface PlantService {

    List<Plant> findMyPlants(String email, String keyword);

    Plant findMyPlant(Long id, String email);

    Plant create(PlantRequest request, String email);

    Plant update(PlantRequest request, String email);

    void delete(Long id, String email);
}