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

import java.io.InputStream;
import java.io.InputStreamReader;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.cfc.godsplan.cloudant.model.LocationContext;
import com.ibm.cfc.godsplan.maps.model.GoogleAddressInformation;

/**
 * 
 */
public class JsonDocumentComposer
{

   /**
    * Key for all documents. Represents a phone number.
    */
   public static final String _ID = "_id";
   
   /**
    * Cloudant document reivison. Needed to update all documents.
    */
   public static final String _REV = "_rev";
   
   private JsonParser jsonParser;
   
   /**
    * 
    */
   public JsonDocumentComposer()
   {
      jsonParser = new JsonParser();
   }
   
   /**
    * 
    * @param phoneNumber
    * @return
    */
   public JsonObject blankDocument(String phoneNumber)
   {
      JsonObject json = new JsonObject();
      json.addProperty(_ID, phoneNumber);
      return json;
   }

   /**
    * 
    * @param is
    * @return
    */
   public JsonElement jsonFromStream(InputStream is)
   {
      return jsonParser.parse(new InputStreamReader(is));
   }
   
   /**
    * 
    * @param s 
    * @return
    */
   public JsonElement jsonFromString(String s)
   {
      return jsonParser.parse(s.toString());

   }
}
