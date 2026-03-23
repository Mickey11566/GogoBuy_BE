package com.example.demo.dao;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.MenuCategories;

@Repository
public interface MenuCategoryDao extends JpaRepository<MenuCategories, Integer>{
	// 繼承 JpaRepository 之後，save() 方法就會自動出現且支援自動獲取 ID
}
