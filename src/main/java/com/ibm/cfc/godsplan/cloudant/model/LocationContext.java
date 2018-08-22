package com.ibm.cfc.godsplan.cloudant.model;

import com.ibm.cfc.godsplan.maps.model.GoogleAddressInformation;

/**
 * POJO for usercontext database documents
 *
 */
public class LocationContext {
	
	private GoogleAddressInformation location;
	private String _id;
	
	public LocationContext(String phoneNumber, GoogleAddressInformation location) {
		this.location = location;
		this._id = phoneNumber;
	}

	public GoogleAddressInformation getAddress() {
		return location;
	}

	public String getPhoneNumber() {
		return _id;
	}	
}
