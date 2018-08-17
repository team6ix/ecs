package com.ibm.coc.godsplan.assistant;

import java.util.Optional;

import com.ibm.watson.developer_cloud.assistant.v1.Assistant;
import com.ibm.watson.developer_cloud.assistant.v1.model.Context;
import com.ibm.watson.developer_cloud.assistant.v1.model.InputData;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageResponse;

public class WatsonAssistantBot {

	private Assistant servissimo;
	private MessageResponse lastResponse;

	public WatsonAssistantBot() {
		this.servissimo = new Assistant("2018-02-16");
		this.servissimo.setUsernameAndPassword("333a833c-fe0a-4f10-af9e-739f368ff725", "5oZIyaeGU1Pc");
	}

	public MessageResponse sendAssistantMessage(Optional<Context> context, Optional<InputData> input) {

		MessageOptions options;

		if (context.isPresent() && input.isPresent()) {
			options = new MessageOptions.Builder("e9fc9a95-fbfb-4210-b8e8-bd40cb3bebe2").input(input.get())
					.context(context.get()).build();
		} else {
			options = new MessageOptions.Builder("e9fc9a95-fbfb-4210-b8e8-bd40cb3bebe2").build();
		}

		this.lastResponse = this.servissimo.message(options).execute();
		return this.lastResponse;
	}

	public MessageResponse getLastResponse() {
		return this.lastResponse;
	}
}
