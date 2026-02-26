package com.example.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.example.demo.constants.SalesStatsType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
	@Table(name = "sales_stats", indexes = {
	    @Index(name = "idx_stats_lookup", columnList = "stats_type, store_id, sales_volume")
	})
	public class SalesStats {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(name = "store_id", nullable = false)
	    private Integer storeId;

	    @Column(name = "menu_id", nullable = false)
	    private Integer menuId;

	    @Column(name = "sales_volume")
	    private Integer salesVolume = 0;

	    @Enumerated(EnumType.STRING)
	    @Column(name = "stats_type", nullable = false)
	    private SalesStatsType statsType;

	    @Column(name = "stats_date")
	    private LocalDate statsDate;

	    @UpdateTimestamp
	    @Column(name = "updated_at")
	    private LocalDateTime updatedAt;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Integer getStoreId() {
			return storeId;
		}

		public void setStoreId(Integer storeId) {
			this.storeId = storeId;
		}

		public Integer getMenuId() {
			return menuId;
		}

		public void setMenuId(Integer menuId) {
			this.menuId = menuId;
		}

		public Integer getSalesVolume() {
			return salesVolume;
		}

		public void setSalesVolume(Integer salesVolume) {
			this.salesVolume = salesVolume;
		}

		public SalesStatsType getStatsType() {
			return statsType;
		}

		public void setStatsType(SalesStatsType statsType) {
			this.statsType = statsType;
		}

		public LocalDate getStatsDate() {
			return statsDate;
		}

		public void setStatsDate(LocalDate statsDate) {
			this.statsDate = statsDate;
		}

		public LocalDateTime getUpdatedAt() {
			return updatedAt;
		}

		public void setUpdatedAt(LocalDateTime updatedAt) {
			this.updatedAt = updatedAt;
		}

		public SalesStats() {
			super();
		}

		public SalesStats(Long id, Integer storeId, Integer menuId, Integer salesVolume, SalesStatsType statsType,
				LocalDate statsDate, LocalDateTime updatedAt) {
			super();
			this.id = id;
			this.storeId = storeId;
			this.menuId = menuId;
			this.salesVolume = salesVolume;
			this.statsType = statsType;
			this.statsDate = statsDate;
			this.updatedAt = updatedAt;
		}

	   
	}
	
