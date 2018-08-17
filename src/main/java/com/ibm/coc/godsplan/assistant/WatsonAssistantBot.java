package com.ibm.coc.godsplan.assistant;

import java.util.Optional;
import com.ibm.watson.developer_cloud.assistant.v1.Assistant;
import com.ibm.watson.developer_cloud.assistant.v1.model.Context;
import com.ibm.watson.developer_cloud.assistant.v1.model.InputData;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageResponse;

/**
 * A class to interact with the GodsPlan Watson Assistant
 */
public class WatsonAssistantBot
{

   private static final String WORKSPACE_ID = "e9fc9a95-fbfb-4210-b8e8-bd40cb3bebe2";
   private static final String ENC_PASSWD = "5oZIyaeGU1Pc";
   private static final String USERNAME = "333a833c-fe0a-4f10-af9e-739f368ff725";
   private static final String REST_API_VERSION = "2018-02-16";
   private final Assistant servissimo;
   private Optional<MessageResponse> lastResponse;

   /**
    * zero argument constructor
    */
   public WatsonAssistantBot()
   {
      this.servissimo = new Assistant(REST_API_VERSION);
      this.servissimo.setUsernameAndPassword(USERNAME, ENC_PASSWD);
      this.lastResponse = Optional.empty();
   }

   /**
    * @param context
    *           context to provide with the input
    * @param input
    *           the message to send to Watson assistant
    * @return the return text from Watson assistant
    */
   public String sendAssistantMessage(Optional<Context> context, Optional<InputData> input)
   {

      MessageOptions options;
      if (context.isPresent() && input.isPresent())
      {
         options = new MessageOptions.Builder(WORKSPACE_ID).input(input.get())
               .context(context.get()).build();
      }
      else
      {
         options = new MessageOptions.Builder(WORKSPACE_ID).build();
      }

      MessageResponse resp = this.servissimo.message(options).execute();
      lastResponse = Optional.ofNullable(resp);
      return resp.getOutput().getText().toString();
   }

   /**
    * @return the most recent context
    */
   public Optional<Context> getLastContext()
   {
      Context context = null;
      if (lastResponse.isPresent())
      {
         context = lastResponse.get().getContext();
      }
      return Optional.ofNullable(context);
   }
}
