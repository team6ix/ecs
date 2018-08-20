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
import com.ibm.watson.developer_cloud.assistant.v1.model.Context;

public class CloudantPersistence
{

   Database chatContextDb;
   Database userContextDb;
   JsonParser parser;
   protected static final Logger logger = LoggerFactory.getLogger(CloudantPersistence.class);

   public CloudantPersistence()
   {
      CloudantClient client = ClientBuilder.account("c008a85f-96b2-4d29-98c7-eedff0e86b1f-bluemix")
            .username("c008a85f-96b2-4d29-98c7-eedff0e86b1f-bluemix")
            .password("39965f7b72264bcd70f7bc27de159a629da46f2ac7a4f63108fa8d9b150d8c22").build();

      chatContextDb = client.database("chatcontext", false);
      userContextDb = client.database("usercontext", false);

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
      JsonElement contextJson = parser.parse(context.toString());

      try (InputStream is = chatContextDb.find(phoneNumber);)
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

   public void persistAddress(String phoneNumber, String address)
   {
      try (InputStream is = userContextDb.find(phoneNumber);)
      {
         JsonObject json = composeExistingDocument(is);
         json.addProperty("address", address);
         userContextDb.update(json);

      }
      catch (NoDocumentException e)
      {
         JsonObject json = composeNewDocument(phoneNumber);
         json.addProperty("address", address);
         userContextDb.save(json);
      }
      catch (IOException e1)
      {
         e1.printStackTrace(); // log or rethrow as somethin else our app handles
      }
   }

   public void removeChatContext(String phoneNumber)
   {
      try (InputStream is = chatContextDb.find(phoneNumber);)
      {
         JsonObject json = composeExistingDocument(is);
         chatContextDb.remove(json);
      }
      catch (NoDocumentException e)
      {
         // log no record found for this phone number
      }
      catch (IOException e1)
      {
         e1.printStackTrace();
      }
   }

   public void removeUserContext(String phoneNumber)
   {
      try (InputStream is = userContextDb.find(phoneNumber);)
      {
         JsonObject json = composeExistingDocument(is);
         userContextDb.remove(json);
      }
      catch (NoDocumentException e)
      {
         // log no record found for this phone number
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
}
