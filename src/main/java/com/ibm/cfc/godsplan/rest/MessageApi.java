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
      Optional<String> userInput = parseUserInput(request);
      String watsonResponse = queryWatson(userInput);
      String twiml = generateTwiml(watsonResponse);
      sendTwimlResponse(response, twiml);
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

   private String generateTwiml(String watsonResponse)
   {
      Body body = new Body.Builder(watsonResponse).build();
      Message sms = new Message.Builder().body(body).build();
      MessagingResponse twiml = new MessagingResponse.Builder().message(sms).build();
      return twiml.toXml();
   }

   private String queryWatson(Optional<String> userInput)
   {
      WatsonAssistantBot watsonAssistant = new WatsonAssistantBot();
      InputData input = new InputData.Builder(userInput.get()).build();
      String watsonResponse = watsonAssistant.sendAssistantMessage(Optional.empty(), Optional.of(input));
      return watsonResponse;
   }

   private Optional<String> parseUserInput(HttpServletRequest request)
   {
      Optional<String> textBody = Optional.ofNullable(request.getParameter("Body"));
      Optional<String> textPhoneNumber = Optional.ofNullable(request.getParameter("From"));
      logger.info("Text body: " + textBody);
      logger.info("Text number: " + textPhoneNumber);
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