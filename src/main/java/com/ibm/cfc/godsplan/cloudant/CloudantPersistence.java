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
   
   /**
    * Cloudant database that contains documents defined by {@link SurveyContext}
    */
   private static final String SURVEY_CONTEXT_DB = "surveycontext";

   /**
    * Cloudant database that contains documents defined by {@link LocationContext}
    */
   private static final String LOCATION_CONTEXT_DB = "locationcontext";

   /**
    * Cloudant database that contains documents defined by {@link ChatContext}
    */
   private static final String CHAT_CONTEXT_DB = "chatcontext";

   /**
    * Key for all documents. Represents a phone number.
    */
   public static final String _ID = "_id";
   
   /**
    * Cloudant document reivison. Needed to update all documents.
    */
   public static final String _REV = "_rev";
   
   /**
    * Field in {@link LocationContext} that represents  {@link GoogleAddressInformation#getLongitude()}
    */
   public static final String LONGITUDE = "longitude";
   
   /**
    * Field in {@link LocationContext} that represents {@link GoogleAddressInformation#getLatitude()}
    */
   public static final String LATITUDE = "latitude";
   
   /**
    * Field in {@link LocationContext} that represents {@link GoogleAddressInformation#getFormattedAddress()}
    */
   public static final String FORMATTED_ADDRESS = "formattedAddress";
   
   /**
    * Field in {@link LocationContext} that represents {@link LocationContext#getAddressConfirmed()}
    */
   public static final String CONFIRMED_ADDRESS = "confirmedAddress";
   
   /**
    * Field in {@link LocationContext} that holds data in {@link GoogleAddressInformation}
    */
   public static final String LOCATION = "location";
   
   private Database chatContextDb;
   private Database locationContextDb;
   private Database surveyContextDb;
   
   private CloudantClient client;
   private JsonParser parser;
   
   protected static final Logger logger = LoggerFactory.getLogger(CloudantPersistence.class);

   /**
    * 
    */
   public CloudantPersistence()
   {
      client = ClientBuilder.account("c008a85f-96b2-4d29-98c7-eedff0e86b1f-bluemix")
            .username("c008a85f-96b2-4d29-98c7-eedff0e86b1f-bluemix")
            .password("39965f7b72264bcd70f7bc27de159a629da46f2ac7a4f63108fa8d9b150d8c22").build();

      chatContextDb = client.database(CHAT_CONTEXT_DB, false);
      locationContextDb = client.database(LOCATION_CONTEXT_DB, false);
      surveyContextDb = client.database(SURVEY_CONTEXT_DB, false);

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
         JsonElement doc = parseJsonFromStream(is);
         JsonObject json = composeExistingDocument(doc);
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
         JsonElement doc = parseJsonFromStream(is);
         JsonObject json = composeExistingDocument(doc);
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
    * @param confirm true if user confirmed address is correct, false if user says address is incorrect
    */
   public void persistAddressConfirmation(String phoneNumber, boolean confirm)
   {
     logger.info("saving address confirmation for '{}'", phoneNumber);
     try (InputStream is = locationContextDb.find(phoneNumber);)
     {
        JsonElement doc = parseJsonFromStream(is);
        JsonObject json = composeExistingLocationDocument(doc);
        json.addProperty(CONFIRMED_ADDRESS, confirm);
        locationContextDb.update(json);
     }
     catch (NoDocumentException e)
     {
        logger.info("no address found for '{}' to confirm, phoneNumber");
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
   public void removePhoneNumber(String phoneNumber)
   {
      removeChatContext(phoneNumber);
      removeLocationContext(phoneNumber);
      removeSurveyContext(phoneNumber);
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
         JsonElement doc = parseJsonFromStream(is);
         JsonObject json = composeExistingDocument(doc);
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
   public void removeLocationContext(String phoneNumber)
   {
	   logger.info("removing location context for '{}'", phoneNumber);
      try (InputStream is = locationContextDb.find(phoneNumber);)
      {
         JsonElement doc = parseJsonFromStream(is);
         JsonObject json = composeExistingDocument(doc);
         locationContextDb.remove(json);
      }
      catch (NoDocumentException e)
      {
         logger.info("no location context found for '{}'", phoneNumber);
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
   public void removeSurveyContext(String phoneNumber)
   {
      logger.info("removing survey context for '{}'", phoneNumber);
      try (InputStream is = surveyContextDb.find(phoneNumber);)
      {
         JsonElement doc = parseJsonFromStream(is);
         JsonObject json = composeExistingDocument(doc);
         surveyContextDb.remove(json);
      }
      catch (NoDocumentException e)
      {
         logger.info("no survey context found for '{}'", phoneNumber);
      }
      catch (IOException e1)
      {
         e1.printStackTrace();
      }
   }

   /**
    * Shuts down the connection manager for this instance
    */
   public void shutdown()
   {
      client.shutdown();
   }
   private JsonObject composeNewDocument(String phoneNumber)
   {
      JsonObject json = new JsonObject();
      json.addProperty(_ID, phoneNumber);
      return json;
   }

   private JsonObject composeExistingLocationDocument(JsonElement doc)
   {
      JsonObject json = composeExistingDocument(doc);
      json.add(LOCATION, doc.getAsJsonObject().get(LOCATION));
      return json;
   }
   
   private JsonObject composeExistingDocument(JsonElement doc)
   {
      JsonElement id = doc.getAsJsonObject().get(_ID);
      JsonElement rev = doc.getAsJsonObject().get(_REV);
      JsonObject json = new JsonObject();
      json.add(_ID, id);
      json.add(_REV, rev);
      return json;
   }
   
   private void composeLocationJson(GoogleAddressInformation address, JsonObject json)
   {
     JsonObject location = new JsonObject();
	  location.addProperty(FORMATTED_ADDRESS, address.getFormattedAddress());
	  location.addProperty(LATITUDE, address.getLatitude());
	  location.addProperty(LONGITUDE, address.getLongitude());
	  json.add(LOCATION, location);
	  json.addProperty(CONFIRMED_ADDRESS, false);
   }
   
   private JsonElement parseJsonFromStream(InputStream is)
   {
      return parser.parse(new InputStreamReader(is));
   }
}
