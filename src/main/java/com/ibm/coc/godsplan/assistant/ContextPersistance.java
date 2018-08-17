package com.ibm.coc.godsplan.assistant;

import java.util.Optional;

import com.cloudant.client.api.CloudantClient;

public class ContextPersistance {

	CloudantClient client;
	
	public ContextPersistance(CloudantClient client)
	{
		this.client = client;
	}

	public void persist(WatsonAssistantContext context) {

	}
	
	public Optional<WatsonAssistantContext> retrieve(String phoneNumber) {
		return null;
	}
}
