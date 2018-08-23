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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cloudant.client.api.Database;
import com.cloudant.client.org.lightcouch.NoDocumentException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.cfc.godsplan.cloudant.model.SurveyContext;

/**
 * 
 */
public class SurveyPersistence
{
   /**
    * Cloudant database that contains documents defined by {@link SurveyContext}
    */
   public static final String SURVEY_CONTEXT_DB = "surveycontext";

   protected static final Logger logger = LoggerFactory.getLogger(SurveyPersistence.class);

   
   Database db;
   JsonDocumentComposer compose;
   JsonParser parser;

   /**
    * 
    * @param surveyDb
    */
   public SurveyPersistence(Database surveyDb)
   {
      this.db = surveyDb;
      compose = new JsonDocumentComposer();
      parser = new JsonParser();
   }

   /**
    * 
    * @param phoneNumber
    */
   public void remove(String phoneNumber)
   {
      logger.info("removing survey context for '{}'", phoneNumber);
      try (InputStream is = db.find(phoneNumber);)
      {
         JsonElement doc = compose.jsonFromStream(is);
         JsonObject json = compose.existingDocument(doc);
         db.remove(json);
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
}
