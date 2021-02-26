package net.javaguides.usermanagement.web;

public class CancellationPolicy {

	public static String id = "";
	public static String effectiveDate = "";
	public static String expiredDate = "";
	
	public CancellationPolicy(String effectiveDate, String expiredDate, String id) {
		// TODO Auto-generated constructor stub
		CancellationPolicy.id = id;
		CancellationPolicy.expiredDate = expiredDate;
		CancellationPolicy.effectiveDate = effectiveDate;
		
	}

	public String getId() {
		return id;
	}

	public static void setId(String id) {
		CancellationPolicy.id = id;
	}

	public String getEffectiveDate() {
		return effectiveDate;
	}

	public static void setEffectiveDate(String effectiveDate) {
		CancellationPolicy.effectiveDate = effectiveDate;
	}

	public String getExpiredDate() {
		return expiredDate;
	}

	public static void setExpiredDate(String expiredDate) {
		CancellationPolicy.expiredDate = expiredDate;
	}
	

	
	
}
