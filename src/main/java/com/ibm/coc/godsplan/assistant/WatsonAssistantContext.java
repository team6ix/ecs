package com.ibm.coc.godsplan.assistant;

import com.ibm.watson.developer_cloud.assistant.v1.model.Context;

public class WatsonAssistantContext {

	private Context watsonContext;
	private String phoneNumber;
	
	public WatsonAssistantContext(Context watsonContext, String phoneNumber) {
		super();
		this.watsonContext = watsonContext;
		this.phoneNumber = phoneNumber;
	}

	public Context getWatsonContext() {
		return watsonContext;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	
	
}
