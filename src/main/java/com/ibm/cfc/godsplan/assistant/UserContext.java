package com.ibm.cfc.godsplan.assistant;

/**
 * POJO for usercontext database documents
 *
 */
public class UserContext {
	private String address ;
	private String _id;
	
	public UserContext(String phoneNumber, String address) {
		this.address = address;
		this._id = phoneNumber;
	}

	public String getAddress() {
		return address;
	}

	public String getPhoneNumber() {
		return _id;
	}	
}
