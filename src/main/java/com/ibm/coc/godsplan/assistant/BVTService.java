package com.ibm.coc.godsplan.assistant;

import com.ibm.watson.developer_cloud.assistant.v1.Assistant;
import com.ibm.watson.developer_cloud.assistant.v1.model.InputData;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.assistant.v1.model.OutputData;
import com.ibm.watson.developer_cloud.http.ServiceCallback;

import jersey.repackaged.jsr166e.CompletableFuture;

public class BVTService {

	public static void main(String[] args) throws Exception {
		Assistant service = new Assistant("2018-02-16");
		service.setUsernameAndPassword("333a833c-fe0a-4f10-af9e-739f368ff725", "5oZIyaeGU1Pc");

		InputData input = new InputData.Builder("Yes").build();
		MessageOptions options = new MessageOptions.Builder("e9fc9a95-fbfb-4210-b8e8-bd40cb3bebe2").input(input)
				.build();

		// sync
		MessageResponse response = service.message(options).execute();
		System.out.println(response);

		// async
		service.message(options).enqueue(new ServiceCallback<MessageResponse>() {
			@Override
			public void onResponse(MessageResponse response) {
				System.out.println(response);
			}

			@Override
			public void onFailure(Exception e) {
			}
		});

		// rx callback
		service.message(options).rx().thenApply(new CompletableFuture.Fun<MessageResponse, OutputData>() {
			@Override
			public OutputData apply(MessageResponse message) {
				return message.getOutput();
			}
		}).thenAccept(new CompletableFuture.Action<OutputData>() {
			@Override
			public void accept(OutputData output) {
				System.out.println(output);
			}
		});

		// rx async callback
		service.message(options).rx().thenApplyAsync(new CompletableFuture.Fun<MessageResponse, OutputData>() {
			@Override
			public OutputData apply(MessageResponse message) {
				return message.getOutput();
			}
		}).thenAccept(new CompletableFuture.Action<OutputData>() {
			@Override
			public void accept(OutputData output) {
				System.out.println(output);
			}
		});

		// rx sync
		try {
			MessageResponse rxMessageResponse = service.message(options).rx().get();
			System.out.println(rxMessageResponse);
		} catch (Exception ex) {
			// Handle exception
		}
	}
}
