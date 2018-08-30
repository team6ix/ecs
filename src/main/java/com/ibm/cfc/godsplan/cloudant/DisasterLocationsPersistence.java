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
import com.ibm.cfc.godsplan.cloudant.model.ChatContext;
import com.ibm.cfc.godsplan.cloudant.model.DisasterLocationContext;

/**
 * Responsible for persisting information related to chat context. Stores documents in {@link #DB}.
 * 
 * See {@link ChatContext} for the data stored in this database.
 */
public class DisasterLocationsPersistence
{

   /**
    * Cloudant database that contains documents defined by {@link ChatContext}
    */
   public static final String DB = "firelocations";

   protected static final Logger logger = LoggerFactory.getLogger(DisasterLocationsPersistence.class);

   
   Database db;
   JsonDocumentComposer compose;
   JsonParser parser;

   /**
    * 
    * @param chatDb
    */
   public DisasterLocationsPersistence(Database chatDb)
   {
      this.db = chatDb;
      compose = new JsonDocumentComposer();
      parser = new JsonParser();
   }

   /**
    * 
    * @param id
    * @param coordinates
    */
   public void persist(String id, Coordinates coordinates)
   {
      logger.info("saving fire location  for '{}'", id);
      JsonObject jsonCoords = new JsonObject();
      jsonCoords.addProperty("latitude", coordinates.getLatitude());
      jsonCoords.addProperty("longitude", coordinates.getLongitude());
      try (InputStream is = db.find(id))
      {
         JsonElement doc = compose.jsonFromStream(is);
         JsonObject json = doc.getAsJsonObject();
         json.add("coordinates", jsonCoords);
         db.update(json);
      }
      catch (NoDocumentException e)
      {
         JsonObject json = compose.blankDocument(id);
         json.add("coordinates", jsonCoords);
         db.save(json);
      }
      catch (IOException e1)
      {
         e1.printStackTrace(); // log or rethrow as something else handled by app
      }
   }

   /**
    * 
    * @param id
    * @return Optional<FireLocationContext>
    */
   public Optional<DisasterLocationContext> retrieve(String id)
   {
      logger.info("retrieving fire location context for '{}'", id);
      Optional<DisasterLocationContext> disasterLocationContext;
      try
      {
         disasterLocationContext = Optional.of(db.find(DisasterLocationContext.class, id));
      }
      catch (NoDocumentException nde)
      {
         logger.info("No fire location context for '{}' found", id);
         disasterLocationContext = Optional.empty();
      }
      return disasterLocationContext;
   }

   /**
    * 
    * @return a list of all fire locations
    */
   public List<DisasterLocationContext> retrieveAll()
   {
      logger.info("retrieving all fire locations");
      
      AllDocsRequestBuilder builder = db.getAllDocsRequestBuilder();
      List<DisasterLocationContext> disasterLocations = new ArrayList<>();

      try
      {
         disasterLocations = builder.includeDocs(true).build().getResponse().getDocsAs(DisasterLocationContext.class);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

      return disasterLocations;
   }
   /**
    * 
    * @param id
    */
   public void remove(String id)
   {
      logger.info("removing fire location context for '{}'", id);
      try (InputStream is = db.find(id);)
      {
         JsonElement doc = compose.jsonFromStream(is);
         JsonObject json = doc.getAsJsonObject();
         db.remove(json);
      }
      catch (NoDocumentException e)
      {
         logger.info("no fire location context found for '{}'", id);
      }
      catch (IOException e1)
      {
         e1.printStackTrace();
      }
   }
}
