package com.ibm.cfc.godsplan.cloudant;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.org.lightcouch.NoDocumentException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.cfc.godsplan.cloudant.model.ChatContext;
import com.ibm.cfc.godsplan.cloudant.model.LocationContext;
import com.ibm.cfc.godsplan.maps.model.GoogleAddressInformation;
import com.ibm.watson.developer_cloud.assistant.v1.model.Context;

/**
 * 
 */
public class CloudantPersistence
{

   Database chatContextDb;
   Database locationContextDb;
   JsonParser parser;
   protected static final Logger logger = LoggerFactory.getLogger(CloudantPersistence.class);

   /**
    * 
    */
   public CloudantPersistence()
   {
      CloudantClient client = ClientBuilder.account("c008a85f-96b2-4d29-98c7-eedff0e86b1f-bluemix")
            .username("c008a85f-96b2-4d29-98c7-eedff0e86b1f-bluemix")
            .password("39965f7b72264bcd70f7bc27de159a629da46f2ac7a4f63108fa8d9b150d8c22").build();

      chatContextDb = client.database("chatcontext", false);
      locationContextDb = client.database("locationcontext", false);

      parser = new JsonParser();
   }

   /**
    *
    * @param phoneNumber
    * @param context
    * @throws IOException
    */
   public void persistChatContext(String phoneNumber, Context context)
   {
	  logger.info("saving chat context for '{}'", phoneNumber); 
      JsonElement contextJson = parser.parse(context.toString());

      try (InputStream is = chatContextDb.find(phoneNumber))
      {
         JsonObject json = composeExistingDocument(is);
         json.add("context", contextJson);
         chatContextDb.update(json);
      }
      catch (NoDocumentException e)
      {
         JsonObject json = composeNewDocument(phoneNumber);
         json.add("context", contextJson);
         chatContextDb.save(json);
      }
      catch (IOException e1)
      {

         e1.printStackTrace(); // log or rethrow as something else handled by app
      }
   }

   /**
    * 
    * @param phoneNumber
    * @return Optional<ChatContext> 
    */
   public Optional<ChatContext> retrieveChatContext(String phoneNumber)
   {
      logger.info("retrieving chat context for '{}'", phoneNumber);
      Optional<ChatContext> context;
      try
      {
         context = Optional.of(chatContextDb.find(ChatContext.class, phoneNumber));
      }
      catch (NoDocumentException nde)
      {
         logger.info("No chat context for '{}' found", phoneNumber);
         context = Optional.empty();
      }
      return context;
   }

   /**
    * 
    * @param phoneNumber
    * @param address
    */
   public void persistAddress(String phoneNumber, GoogleAddressInformation address)
   {
	  logger.info("saving address information for '{}'", phoneNumber); 
      try (InputStream is = locationContextDb.find(phoneNumber);)
      {
         JsonObject json = composeExistingDocument(is);
         composeLocationJson(address, json);
         locationContextDb.update(json);

      }
      catch (NoDocumentException e)
      {
         JsonObject json = composeNewDocument(phoneNumber);
         composeLocationJson(address, json);
         locationContextDb.save(json);
      }
      catch (IOException e1)
      {
         e1.printStackTrace(); // log or rethrow as somethin else our app handles
      }
   }
   
   /**
    * 
    * @param phoneNumber
    * @return Optional<LocationContext>
    */
   public Optional<LocationContext> retrieveAddress(String phoneNumber)
   {
     logger.info("retrieving address information for '{}'", phoneNumber);  
	 Optional<LocationContext> locationContext;
	 try
	 {
	   locationContext = Optional.of(locationContextDb.find(LocationContext.class, phoneNumber));
	 }
	 catch (NoDocumentException nde)
	 {
	   logger.info("No address information for '{}' found", phoneNumber);
	   locationContext = Optional.empty();
	 }
	 
	 return locationContext;
   }

   /**
    * 
    * @param phoneNumber
    */
   public void removeChatContext(String phoneNumber)
   {
	  logger.info("removing chat context for '{}'", phoneNumber);
      try (InputStream is = chatContextDb.find(phoneNumber);)
      {
         JsonObject json = composeExistingDocument(is);
         chatContextDb.remove(json);
      }
      catch (NoDocumentException e)
      {
    	  logger.info("no chat context found for '{}'", phoneNumber);
      }
      catch (IOException e1)
      {
         e1.printStackTrace();
      }
   }

   /**
    * 
    * @param phoneNumber
    */
   public void removeUserContext(String phoneNumber)
   {
	  logger.info("removing user context for '{}'", phoneNumber);
      try (InputStream is = locationContextDb.find(phoneNumber);)
      {
         JsonObject json = composeExistingDocument(is);
         locationContextDb.remove(json);
      }
      catch (NoDocumentException e)
      {
         logger.info("no user context found for '{}'", phoneNumber);
      }
      catch (IOException e1)
      {
         e1.printStackTrace();
      }
   }

   private JsonObject composeNewDocument(String phoneNumber)
   {
      JsonObject json = new JsonObject();
      json.addProperty("_id", phoneNumber);
      return json;
   }

   private JsonObject composeExistingDocument(InputStream is)
   {
      JsonElement doc = parser.parse(new InputStreamReader(is));
      JsonElement id = doc.getAsJsonObject().get("_id");
      JsonElement rev = doc.getAsJsonObject().get("_rev");
      JsonObject json = new JsonObject();
      json.add("_id", id);
      json.add("_rev", rev);
      return json;
   }
   
   private void composeLocationJson(GoogleAddressInformation address, JsonObject json) 
   {
 	  JsonObject location = new JsonObject();
	  location.addProperty("formattedAddress", address.getFormattedAddress());
	  location.addProperty("latitude", address.getLatitude());
	  location.addProperty("longitude", address.getLongitude());
	  json.add("location", location);
   }
}
