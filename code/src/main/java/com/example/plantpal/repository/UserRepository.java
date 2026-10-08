package com.example.plantpal.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.plantpal.domain.entity.User;
import com.example.plantpal.domain.enums.Role;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // admin ค้นหาผู้ใช้จากอีเมล ชื่อ หรือนามสกุล (q = "" คือแสดงทั้งหมด) + แบ่งหน้า
    @Query(value = """
            select u from User u left join fetch u.profile p
            where lower(u.email) like lower(concat('%', :q, '%'))
               or lower(coalesce(p.firstName, '')) like lower(concat('%', :q, '%'))
               or lower(coalesce(p.lastName, '')) like lower(concat('%', :q, '%'))
            """,
           countQuery = """
            select count(u) from User u left join u.profile p
            where lower(u.email) like lower(concat('%', :q, '%'))
               or lower(coalesce(p.firstName, '')) like lower(concat('%', :q, '%'))
               or lower(coalesce(p.lastName, '')) like lower(concat('%', :q, '%'))
            """)
    Page<User> search(@Param("q") String q, Pageable pageable);

    // จำนวนต้นไม้ของผู้ใช้หลายคนในครั้งเดียว: แต่ละแถวคือ [userId, count]
    @Query("select p.user.id, count(p) from Plant p where p.user.id in :ids group by p.user.id")
    List<Object[]> countPlantsByUserIds(@Param("ids") Collection<Long> ids);

    @Query("select count(p) from Plant p where p.user.id = :userId")
    long countPlantsByUserId(@Param("userId") Long userId);

    @Query("select count(c) from CareLog c where c.plant.user.id = :userId")
    long countCareLogsByUserId(@Param("userId") Long userId);

    // id ของผู้ใช้ตาม role ที่ยังเปิดใช้งานอยู่ (ใช้ส่งแจ้งเตือนถึง admin ทุกคน)
    @Query("select u.id from User u where u.role = :role and u.isActive = true")
    List<Long> findActiveIdsByRole(@Param("role") Role role);
}
