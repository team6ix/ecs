package com.ibm.cfc.godsplan.cloudant;

import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.ibm.cfc.godsplan.cloudant.model.ChatContext;
import com.ibm.cfc.godsplan.cloudant.model.LocationContext;
import com.ibm.cfc.godsplan.cloudant.model.SurveyContext;
import com.ibm.cfc.godsplan.maps.model.GoogleAddressInformation;
import com.ibm.watson.developer_cloud.assistant.v1.model.Context;

/**
 * 
 */
public class CloudantPersistence
{
   private ChatPersistence chatDb;
   private LocationPersistence locationDb;
   private SurveyPersistence surveyDb;

   private CloudantClient client;

   protected static final Logger logger = LoggerFactory.getLogger(CloudantPersistence.class);

   /**
    * Responsible for persisting all user information to Cloudant.
    */
   public CloudantPersistence()
   {
      client = ClientBuilder.account("c008a85f-96b2-4d29-98c7-eedff0e86b1f-bluemix")
            .username("c008a85f-96b2-4d29-98c7-eedff0e86b1f-bluemix")
            .password("39965f7b72264bcd70f7bc27de159a629da46f2ac7a4f63108fa8d9b150d8c22").build();
      
      chatDb = new ChatPersistence(client.database(ChatPersistence.CHAT_CONTEXT_DB, false));
      locationDb = new LocationPersistence(client.database(LocationPersistence.LOCATION_CONTEXT_DB, false));
      surveyDb = new SurveyPersistence(client.database(SurveyPersistence.SURVEY_CONTEXT_DB, false));
      
   }

   /**
    *
    * @param phoneNumber
    * @param context
    * @throws IOException
    */
   public void persistChatContext(String phoneNumber, Context context)
   {
      chatDb.persist(phoneNumber, context);
   }

   /**
    * 
    * @param phoneNumber
    * @return Optional<ChatContext>
    */
   public Optional<ChatContext> retrieveChatContext(String phoneNumber)
   {
      return chatDb.retrieve(phoneNumber);
   }

   /**
    * 
    * @param phoneNumber
    * @param address
    */
   public void persistAddress(String phoneNumber, GoogleAddressInformation address)
   {
      locationDb.persist(phoneNumber, address);
   }

   /**
    * 
    * @param phoneNumber
    * @param confirm
    *           true if user confirmed address is correct, false is address is incorrect or unconfirmed
    */
   public void persistAddressConfirmation(String phoneNumber, boolean confirm)
   {
      locationDb.persistAddressConfirmation(phoneNumber, confirm);
   }

   /**
    * 
    * @param phoneNumber
    * @return Optional<LocationContext>
    */
   public Optional<LocationContext> retrieveAddress(String phoneNumber)
   {
      return locationDb.retrieve(phoneNumber);
   }

   /**
    * 
    * @param clientPhoneNumber
    * @param b
    */
   public void persistMustEvacuate(String clientPhoneNumber, boolean b)
   {
      surveyDb.persistMustEvacuate(clientPhoneNumber, b);
   }
   
   /**
    * 
    * @param phoneNumber
    * @param b
    */
   public void persistHasVehicle(String phoneNumber, boolean b)
   {
      surveyDb.persistHasVehicle(phoneNumber, b);
   }
   
   /**
    * 
    * @param clientPhoneNumber
    * @return  the survey context
    */
   public Optional<SurveyContext> retrieveSurveyContext(String clientPhoneNumber)
   {
      return surveyDb.retrieve(clientPhoneNumber);
   }
   
   /**
    * 
    * @param phoneNumber
    */
   public void removeChatContext(String phoneNumber)
   {
      chatDb.remove(phoneNumber);
   }

   /**
    * 
    * @param phoneNumber
    */
   public void removeLocationContext(String phoneNumber)
   {
      locationDb.remove(phoneNumber);
   }

   /**
    * 
    * @param phoneNumber
    */
   public void removeSurveyContext(String phoneNumber)
   {
      surveyDb.remove(phoneNumber);
   }

   /**
    * 
    * @param phoneNumber
    */
   public void removePhoneNumber(String phoneNumber)
   {
      chatDb.remove(phoneNumber);
      locationDb.remove(phoneNumber);
      surveyDb.remove(phoneNumber);
   }

   /**
    * Shuts down the connection manager for this instance.
    */
   public void shutdown()
   {
      client.shutdown();
   }
}