package com.example.demo.response;

import java.util.List;

public class FavoriteRes extends BasicRes{

	List <Integer> favoriteStores;

	
	public void setFavoriteStores(List<Integer> favoriteStores) {
		this.favoriteStores = favoriteStores;
	}


	public List<Integer> getFavoriteStores() {
		return favoriteStores;
	}


	public FavoriteRes(int code, String message, List<Integer> favoriteStores) {
		super(code, message);
		this.favoriteStores = favoriteStores;
	}
	
	
}
