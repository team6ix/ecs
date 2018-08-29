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
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cloudant.client.api.Database;
import com.cloudant.client.org.lightcouch.NoDocumentException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.cfc.godsplan.cloudant.model.ChatContext;
import com.ibm.cfc.godsplan.cloudant.model.FireLocationContext;
import com.ibm.watson.developer_cloud.assistant.v1.model.Context;

/**
 * Responsible for persisting information related to chat context. Stores documents in {@link #DB}.
 * 
 * See {@link ChatContext} for the data stored in this database.
 */
public class FireLocationsPersistence
{

   /**
    * Cloudant database that contains documents defined by {@link ChatContext}
    */
   public static final String DB = "firelocations";

   protected static final Logger logger = LoggerFactory.getLogger(FireLocationsPersistence.class);

   
   Database db;
   JsonDocumentComposer compose;
   JsonParser parser;

   /**
    * 
    * @param chatDb
    */
   public FireLocationsPersistence(Database chatDb)
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
   public void persist(int id, Coordinates coordinates)
   {
      logger.info("saving fire location  for '{}'", id);
      String idString = Integer.toString(id);
      JsonObject jsonCoords = new JsonObject();
      jsonCoords.addProperty("latitude", coordinates.getLatitude());
      jsonCoords.addProperty("longitude", coordinates.getLongitude());
      try (InputStream is = db.find(idString))
      {
         JsonElement doc = compose.jsonFromStream(is);
         JsonObject json = doc.getAsJsonObject();
         json.add("coordinates", jsonCoords);
         db.update(json);
      }
      catch (NoDocumentException e)
      {
         JsonObject json = compose.blankDocument(idString);
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
   public Optional<FireLocationContext> retrieve(String id)
   {
      logger.info("retrieving fire location context for '{}'", id);
      Optional<FireLocationContext> fireLocationContext;
      try
      {
         fireLocationContext = Optional.of(db.find(FireLocationContext.class, id));
      }
      catch (NoDocumentException nde)
      {
         logger.info("No fire location context for '{}' found", id);
         fireLocationContext = Optional.empty();
      }
      return fireLocationContext;
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
