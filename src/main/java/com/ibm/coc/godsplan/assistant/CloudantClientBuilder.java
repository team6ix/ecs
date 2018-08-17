package com.ibm.coc.godsplan.assistant;

import java.util.Set;
import java.util.Map.Entry;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.ibm.watson.developer_cloud.assistant.v1.model.Context;

public class CloudantClientBuilder {

	public CloudantClient build()
	{
		CloudantClient client = ClientBuilder.account("c008a85f-96b2-4d29-98c7-eedff0e86b1f-bluemix")
				.password("39965f7b72264bcd70f7bc27de159a629da46f2ac7a4f63108fa8d9b150d8c22").build();
		
		return client;
//		
//		Set<Entry<String, Object>> contextSet = context.entrySet();
//		Context newContext = new Context();
//
//		for (Entry<String, Object> entry : contextSet) {
//			System.out.println("Key:" + entry.getKey() + ", " + entry.getValue());
//			newContext.put(entry.getKey(), entry.getValue());
		}

		

}
