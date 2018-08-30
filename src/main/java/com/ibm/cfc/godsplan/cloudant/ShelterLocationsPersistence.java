/* _______________________________________________________ {COPYRIGHT-TOP} _____
 * IBM Confidential
 * IBM Lift CLI Source Materials
 *
 * (C) Copyright IBM Corp. 2018  All Rights Reserved.
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 * _______________________________________________________ {COPYRIGHT-END} _____*/

package com.ibm.cfc.godsplan.cloudant;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.views.AllDocsRequestBuilder;
import com.cloudant.client.org.lightcouch.NoDocumentException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.cfc.godsplan.cloudant.model.DisasterLocationContext;
import com.ibm.cfc.godsplan.cloudant.model.LocationContext;
import com.ibm.cfc.godsplan.cloudant.model.ShelterLocationContext;
import com.ibm.cfc.godsplan.maps.model.GoogleAddressInformation;

/**
 * Responsible for persisting information related to chat context. Stores documents in {@link #LOCATION_CONTEXT_DB}.
 * 
 * See {@link LocationContext} for the data stored in this database.
 */
public class ShelterLocationsPersistence
{
   /**
    * Cloudant database that contains documents defined by {@link LocationContext}
    */
   public static final String DB = "shelterlocations";
   
   /**
    * Field in {@link LocationContext} that represents {@link GoogleAddressInformation#getLongitude()}
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
   public static final String CAN_ACCEPT_MORE = "canAcceptMore";

   /**
    * Field in {@link LocationContext} that holds data in {@link GoogleAddressInformation}
    */
   public static final String LOCATION = "location";

   protected static final Logger logger = LoggerFactory.getLogger(ShelterLocationsPersistence.class);

   
   Database db;
   JsonDocumentComposer compose;
   JsonParser parser;

   /**
    * 
    * @param locationDb
    */
   public ShelterLocationsPersistence(Database locationDb)
   {
      this.db = locationDb;
      compose = new JsonDocumentComposer();
      parser = new JsonParser();
   }

   /**
    * 
    * @param shelterId
    * @param address
    */
   public void persist(String shelterId, GoogleAddressInformation address)
   {
      logger.info("saving address information for '{}'", shelterId);
      try (InputStream is = db.find(shelterId);)
      {
         JsonElement doc = compose.jsonFromStream(is);
         JsonObject json = doc.getAsJsonObject();
         addLocation(address,json);
         db.update(json);
      }
      catch (NoDocumentException e)
      {
         JsonObject json = compose.blankDocument(shelterId);
         addLocation(address, json);
         db.save(json);
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
   public Optional<ShelterLocationContext> retrieve(String phoneNumber)
   {
      logger.info("retrieving address information for '{}'", phoneNumber);
      Optional<ShelterLocationContext> locationContext;
      try
      {
         locationContext = Optional.of(db.find(ShelterLocationContext.class, phoneNumber));
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
    * @return a list of all shelters
    */
   public List<ShelterLocationContext> retrieveAll()
   {
      logger.info("retrieving all shelter locations");

      AllDocsRequestBuilder builder = db.getAllDocsRequestBuilder();
      List<ShelterLocationContext> shelterLocations = new ArrayList<>();

      try
      {
         shelterLocations = builder.includeDocs(true).build().getResponse().getDocsAs(ShelterLocationContext.class);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

      return shelterLocations;
   }
   
   /**
    * 
    * @param shelterId
    * @param canAcceptMore
    *           true if user confirmed address is correct, false if user says address is incorrect
    */
   public void persistCanAcceptMore(String shelterId, boolean canAcceptMore)
   {
      logger.info("saving address confirmation for '{}'", shelterId);
      try (InputStream is = db.find(shelterId);)
      {
         JsonElement doc = compose.jsonFromStream(is);
         JsonObject json = doc.getAsJsonObject();
         json.addProperty("canAcceptMore", canAcceptMore);
         db.update(json);
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
    * @param shelterId
    */
   public void remove(String shelterId)
   {
      logger.info("removing location context for '{}'", shelterId);
      try (InputStream is = db.find(shelterId);)
      {
         JsonElement doc = compose.jsonFromStream(is);
         JsonObject json = doc.getAsJsonObject();
         db.remove(json);
      }
      catch (NoDocumentException e)
      {
         logger.info("no location context found for '{}'", shelterId);
      }
      catch (IOException e1)
      {
         e1.printStackTrace();
      }
   }
   
   /**
    * 
    * @param address
    * @param json
    */
   public void addLocation(GoogleAddressInformation address, JsonObject json)
   {
      JsonObject location = new JsonObject();
      location.addProperty(FORMATTED_ADDRESS, address.getFormattedAddress());
      location.addProperty(LATITUDE, address.getLatitude());
      location.addProperty(LONGITUDE, address.getLongitude());
      json.add(LOCATION, location);
      json.addProperty(CAN_ACCEPT_MORE, true);
   }
}
