package com.ibm.cfc.godsplan.rest;

import java.io.IOException;
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
      CloudantPersistence metadata = new CloudantPersistence();
      Optional<String> smsTxtBody = parseUserInput(request);
      Optional<String> smsPhoneNumber = parsePhoneNumber(request);
      validateInput(smsTxtBody, smsPhoneNumber);
      String watsonResponse = queryWatson(smsTxtBody.get(), smsPhoneNumber.get(), metadata);
      String twiml = generateTwiml(watsonResponse);
      sendTwimlResponse(response, twiml);
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

   private String queryWatson(String watsonQuery, String smsPhoneNumber, CloudantPersistence metadata)
   {
      WatsonAssistantBot bot = new WatsonAssistantBot();
      InputData input = new InputData.Builder(watsonQuery).build();
      Optional<ChatContext> chatContext = metadata.retrieveChatContext(smsPhoneNumber);
      Optional<Context> context;
      if (chatContext.isPresent())
      {
         context = Optional.ofNullable(chatContext.get().getWatsonContext());
      }
      else
      {
         context = Optional.empty();
      }
      return bot.sendAssistantMessage(context, Optional.of(input));
   }

   private Optional<String> parseUserInput(HttpServletRequest request)
   {
      Optional<String> textBody = Optional.ofNullable(request.getParameter("Body"));
      logger.info("Text body: '{}'", textBody);
      return textBody;
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