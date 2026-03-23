package com.example.demo.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.entity.StoreOperatingHours;
import com.example.demo.entity.StoreOperatingHoursId;

@Repository
public interface StoreOperatingHoursDao extends JpaRepository<StoreOperatingHours, StoreOperatingHoursId> {
    List<StoreOperatingHours> findByStoresId(int storesId);
}
