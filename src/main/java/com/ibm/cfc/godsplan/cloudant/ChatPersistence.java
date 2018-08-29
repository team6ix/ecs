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
import com.ibm.watson.developer_cloud.assistant.v1.model.Context;

/**
 * Responsible for persisting information related to chat context. Stores documents in {@link #DB}.
 * 
 * See {@link ChatContext} for the data stored in this database.
 */
public class ChatPersistence
{

   /**
    * Cloudant database that contains documents defined by {@link ChatContext}
    */
   public static final String DB = "chatcontext";

   protected static final Logger logger = LoggerFactory.getLogger(ChatPersistence.class);

   
   Database db;
   JsonDocumentComposer compose;
   JsonParser parser;

   /**
    * 
    * @param chatDb
    */
   public ChatPersistence(Database chatDb)
   {
      this.db = chatDb;
      compose = new JsonDocumentComposer();
      parser = new JsonParser();
   }
   
   /**
   *
   * @param phoneNumber
   * @param context
   * @throws IOException
   */
  public void persist(String phoneNumber, Context context)
  {
     logger.info("saving chat context for '{}'", phoneNumber);
     JsonElement contextJson = compose.jsonFromString(context.toString());

     try (InputStream is = db.find(phoneNumber))
     {
        JsonElement doc = compose.jsonFromStream(is);
        JsonObject json = doc.getAsJsonObject();
        json.add("context", contextJson);
        db.update(json);
     }
     catch (NoDocumentException e)
     {
        JsonObject json = compose.blankDocument(phoneNumber);
        json.add("context", contextJson);
        db.save(json);
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
  public Optional<ChatContext> retrieve(String phoneNumber)
  {
     logger.info("retrieving chat context for '{}'", phoneNumber);
     Optional<ChatContext> context;
     try
     {
        context = Optional.of(db.find(ChatContext.class, phoneNumber));
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
   */
  public void remove(String phoneNumber)
  {
     logger.info("removing chat context for '{}'", phoneNumber);
     try (InputStream is = db.find(phoneNumber);)
     {
        JsonElement doc = compose.jsonFromStream(is);
        JsonObject json = doc.getAsJsonObject();
        db.remove(json);
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
  
}
