package com.ibm.coc.godsplan.assistant;

import java.util.Optional;

import com.cloudant.client.api.CloudantClient;
import com.ibm.watson.developer_cloud.assistant.v1.model.Context;

public class ContextPersistence {

	CloudantClient client;
	
	public ContextPersistence(CloudantClient client)
	{
		this.client = client;
	}

	public void persist(String phoneNumber, Context context) {

	}
	
	public Optional<Context> retrieve(String phoneNumber) {
		return null;
	}
}
