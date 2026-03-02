package com.example.demo.dao;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Menu;
import com.example.demo.entity.Stores;
import com.example.demo.projection.StoreDistanceProjection;

@Repository
public interface StoresSearchDao extends JpaRepository<Stores, Integer> {

//	拿取全店家
	@Query(value = "SELECT * FROM stores ", nativeQuery = true)
	public List<Stores> getAllStores();

//	名字模糊搜尋
	@Query(value = "SELECT * FROM stores " + "WHERE name LIKE CONCAT('%', ?1, '%') " + "AND is_deleted = false "
			+ "ORDER BY id DESC", nativeQuery = true)
	public List<Stores> findStoresByNameLike(String name);

	@Query(value = "SELECT * FROM stores WHERE id = ?1 AND is_deleted = false", nativeQuery = true)
	public Stores getStoreById(int id);

	// 取得營業時間
	@Query(value = "SELECT id, stores_id as storesId, week, open_time as openTime, close_time as closeTime FROM store_operating_hours WHERE stores_id = ?1", nativeQuery = true)
	public List<Map<String, Object>> getOperatingHoursByStoreId(int storeId);

	// 取得菜單品項
	@Query(value = "SELECT stores_id as storesId, id, category_id as categoryId, name, description, base_price as basePrice, image, is_available as available, unusual FROM menu WHERE stores_id = ?1", nativeQuery = true)
	public List<Map<String, Object>> getMenuByStoreId(int storeId);

	// 取得品項類別 (包含價格級距 JSON)
	@Query(value = "SELECT id, stores_id as storesId, name, price_level as priceLevel FROM menu_categories WHERE stores_id = ?1", nativeQuery = true)
	public List<Map<String, Object>> getCategoriesByStoreId(int storeId);

	// 取得選項群組
	@Query(value = "SELECT stores_id as storesId, id, name, is_required as required, max_selection as maxSelection FROM product_option_groups WHERE stores_id = ?1", nativeQuery = true)
	public List<Map<String, Object>> getOptionGroupsByStoreId(int storeId);

	// 取得選項群組內的細項 (根據群組 ID)
	@Query(value = "SELECT id, group_id as groupId, name, extra_price as extraPrice FROM product_option_items WHERE group_id = ?1", nativeQuery = true)
	public List<Map<String, Object>> getOptionItemsByGroupId(int groupId);

	// 單個菜單(查價)
	@Query(value = "SELECT * FROM menu WHERE id IN (?1)", nativeQuery = true)
	public Menu getMenuByMenuId(int menuId);

	// (給EVENT) 依值搜尋(多個)品項
	@Query(value = "SELECT * FROM menu WHERE id IN (?1)", nativeQuery = true)
	public List<Menu> getMenuByMenuId(List<Integer> menuId);

	// 用 storeId 取得地址
	@Query(value = "select address from stores where id = ?1", nativeQuery = true)
	public String findAddressByStoreId(int id);

	// 找飯店(? (附近)

	@Query(value = "SELECT * FROM (" + "  SELECT id, name, address, phone, image, category, "
			+ "  ROUND((6371 * acos(cos(radians(:lat)) * cos(radians(lat)) * cos(radians(lng) - radians(:lng)) "
			+ "  + sin(radians(:lat)) * sin(radians(lat)))), 3) AS distance " //
			+ "  FROM stores  WHERE is_deleted = false AND is_public = true " + "  AND lat BETWEEN :minLat AND :maxLat "
			+ "  AND lng BETWEEN :minLng AND :maxLng " + ") AS temp_table " + "WHERE distance <= :radius "
			+ "ORDER BY distance ASC", nativeQuery = true)
	public List<StoreDistanceProjection> findNearbyWithDistance(@Param("lat") double lat, @Param("lng") double lng,
			@Param("radius") double radius, @Param("minLat") double minLat, @Param("maxLat") double maxLat,
			@Param("minLng") double minLng, @Param("maxLng") double maxLng);

	//	根據範圍(距離、種類)找營業中店家
	@Query(value = "SELECT s.id, s.name, s.category, s.image, h.open_time, h.close_time " +
            "FROM stores s " +
            "JOIN store_operating_hours h ON s.id = h.stores_id " +
            "WHERE s.id IN :storeIds " +
            "AND s.is_deleted = 0 " +
            "AND h.week = :dayOfWeek " +
            "AND h.is_closed = 0 " + 
            "AND (" +
            "  CASE " +
            "    WHEN h.open_time < h.close_time THEN :now BETWEEN h.open_time AND h.close_time " +
            "    ELSE :now >= h.open_time OR :now <= h.close_time " +
            "  END" +
            ")", nativeQuery = true)
List<Map<String, Object>> findOperatingStoresByIds(
 @Param("storeIds") List<Integer> storeIds, 
 @Param("now") LocalTime now,
 @Param("dayOfWeek") int dayOfWeek
);
	
	//	查詢多個店家是否存在
	@Query(value="select id from stores where stores.id in :storesId", nativeQuery = true)
	List<Integer> exsitStores(@Param("storesId") List<Integer>storesId) ;
	
	//	查價格級距
	@Query(value="SELECT mc.price_level "
			+ "FROM menu m "
			+ "JOIN menu_categories mc ON m.category_id = mc.id "
			+ "WHERE m.id = ?", nativeQuery = true)
	String getPriceLevelByMenuId(@Param("menuId") int menuId) ;
}
