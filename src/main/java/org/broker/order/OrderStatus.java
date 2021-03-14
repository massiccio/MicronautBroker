package org.broker.order;


public enum OrderStatus {
	PENDING("Pending"), FILLED("Filled"), CANCELLED("Cancelled");

	private final String status;
	
	private OrderStatus(String status) {
		this.status = status;
	}
	
	public String getStatus() {
		return status;
	}

	@Override
	public String toString() {
		return getStatus();
	}
}