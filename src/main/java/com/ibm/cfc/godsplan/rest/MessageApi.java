package com.ibm.cfc.godsplan.rest;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ibm.cfc.godsplan.assistant.WatsonAssistantBot;
import com.ibm.cfc.godsplan.cloudant.CloudantPersistence;
import com.ibm.cfc.godsplan.cloudant.model.ChatContext;
import com.ibm.watson.developer_cloud.assistant.v1.model.Context;
import com.ibm.watson.developer_cloud.assistant.v1.model.InputData;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;

/**
 * Servlet implementation class
 */
@WebServlet("/message")
public class MessageApi extends HttpServlet
{
   private static final long serialVersionUID = 1L;
   protected static final Logger logger = LoggerFactory.getLogger(MessageApi.class);

   /**
    * @throws IOException
    * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
    */
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
   {
      logger.trace("POST request: {}", request);
      try
      {
         Instant startTime = Instant.now();
         CloudantPersistence metadata = new CloudantPersistence();
         WatsonAssistantBot bot = new WatsonAssistantBot();

         Optional<String> smsTxtBody = parseUserInput(request);
         Optional<String> smsPhoneNumber = parsePhoneNumber(request);
         validateInput(smsTxtBody, smsPhoneNumber);

         String responseMsg = processQuery(metadata, bot, smsTxtBody.get(), smsPhoneNumber.get());
         String twiml = generateTwiml(responseMsg);
         sendTwimlResponse(response, twiml);

         logger.info("doPost ran in {} seconds", Duration.between(startTime, Instant.now()).getSeconds());
      }
      catch (Exception e)
      {
         logger.error("Uncaught Exception", e);
         throw e;
      }
   }

   private String processQuery(CloudantPersistence metadata, WatsonAssistantBot bot, String smsTxtBody,
         String smsPhoneNumber)
   {
      String responseMsg;
      if (isClearMetadata(smsTxtBody))
      {
         clearMetadata(metadata, smsPhoneNumber);
         responseMsg = "Cleared persisted context";
      }
      else
      {
         responseMsg = queryWatson(bot, smsTxtBody, smsPhoneNumber, metadata);
      }
      return responseMsg;
   }

   private void clearMetadata(CloudantPersistence metadata, String phoneNumber)
   {
      logger.info("Clearing context for phone number : '{}'", phoneNumber);
      metadata.removeChatContext(phoneNumber);
   }

   private void validateInput(Optional<String> smsTxtBody, Optional<String> smsPhoneNumber) throws IOException
   {
      if (!smsTxtBody.isPresent() || !smsPhoneNumber.isPresent())
      {
         if (!smsTxtBody.isPresent())
         {
            logger.error("smsTxtBody not present");
         }
         if (!smsPhoneNumber.isPresent())
         {
            logger.error("smsPhoneNumber not present");
         }
         throw new IOException("failed to parse sms body or sms phone number.");
      }
   }

   private Optional<String> parsePhoneNumber(HttpServletRequest request)
   {
      Optional<String> textPhoneNumber = Optional.ofNullable(request.getParameter("From"));
      logger.info("Text number: '{}'", textPhoneNumber);
      return textPhoneNumber;
   }

   private void sendTwimlResponse(HttpServletResponse response, String twiml) throws IOException
   {
      try
      {
         response.setContentType("application/xml");
         response.getWriter().write(twiml);
      }
      catch (IOException ioe)
      {
         logger.error("Failed to send Twiml response", ioe);
         throw ioe;
      }
   }

   private String generateTwiml(String input)
   {
      Body body = new Body.Builder(input).build();
      Message msg = new Message.Builder().body(body).build();
      MessagingResponse twiml = new MessagingResponse.Builder().message(msg).build();
      return twiml.toXml();
   }

   private String queryWatson(WatsonAssistantBot bot, String userInputBody, String userPhoneNumber,
         CloudantPersistence metadata)
   {
      Optional<InputData> input = Optional.of(new InputData.Builder(userInputBody).build());
      Optional<Context> context = getPersistedContext(userPhoneNumber, metadata);

      String watsonResponse = bot.sendAssistantMessage(context, input);
      persistContext(userPhoneNumber, bot.getLastContext(), metadata);
      return watsonResponse;
   }

   private void persistContext(String userPhoneNumber, Optional<Context> responseContext, CloudantPersistence metadata)
   {
      if (responseContext.isPresent())
      {
         metadata.persistChatContext(userPhoneNumber, responseContext.get());
      }
   }

   private Optional<Context> getPersistedContext(String phoneNumber, CloudantPersistence metadata)
   {
      Optional<ChatContext> chatContext = metadata.retrieveChatContext(phoneNumber);
      Optional<Context> context;
      if (chatContext.isPresent())
      {
         context = Optional.ofNullable(chatContext.get().getWatsonContext());
      }
      else
      {
         context = Optional.empty();
      }
      return context;
   }

   private Optional<String> parseUserInput(HttpServletRequest request)
   {
      Optional<String> textBody = Optional.ofNullable(request.getParameter("Body"));
      logger.info("Text body: '{}'", textBody);
      return textBody;
   }

   private boolean isClearMetadata(String smsText)
   {
      return smsText.trim().equalsIgnoreCase("Clear");
   }

   /**
    * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      response.setContentType("text/html");
      response.getWriter().print("Saving the world.");
   }

}