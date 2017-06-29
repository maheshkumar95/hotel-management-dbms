package com.dbms.HRS;

public class FoodOrderDetails {
	
	private int id;
	private String description;
	private int foodOrder;
	private int quantity;
	
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getFoodOrder() {
		return foodOrder;
	}
	public void setFoodOrder(int foodOrder) {
		this.foodOrder = foodOrder;
	}
	
	

}
