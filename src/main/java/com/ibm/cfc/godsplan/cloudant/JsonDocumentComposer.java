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
   public static final String CONFIRMED_ADDRESS = "confirmedAddress";

   /**
    * Field in {@link LocationContext} that holds data in {@link GoogleAddressInformation}
    */
   public static final String LOCATION = "location";
   
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
    * @param doc
    * @return 
    */
   public JsonObject existingLocationDocument(JsonElement doc)
   {
      JsonObject json = existingDocument(doc);
      json.add(LOCATION, doc.getAsJsonObject().get(LOCATION));
      return json;
   }

   /**
    * 
    * @param doc
    * @return
    */
   public JsonObject existingDocument(JsonElement doc)
   {
      JsonElement id = doc.getAsJsonObject().get(_ID);
      JsonElement rev = doc.getAsJsonObject().get(_REV);
      JsonObject json = new JsonObject();
      json.add(_ID, id);
      json.add(_REV, rev);
      return json;
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
      json.addProperty(CONFIRMED_ADDRESS, false);
   }
   
   /**
    * 
    * @param address
    * @param json
    */
   public void locationDocument(GoogleAddressInformation address, JsonObject json)
   {
      JsonObject location = new JsonObject();
      location.addProperty(FORMATTED_ADDRESS, address.getFormattedAddress());
      location.addProperty(LATITUDE, address.getLatitude());
      location.addProperty(LONGITUDE, address.getLongitude());
      json.add(LOCATION, location);
      json.addProperty(CONFIRMED_ADDRESS, false);
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
    * @return
    */
   public JsonElement jsonFromString(String s)
   {
      return jsonParser.parse(s.toString());

   }
}
