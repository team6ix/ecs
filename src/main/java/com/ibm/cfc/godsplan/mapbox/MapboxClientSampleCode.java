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

package com.ibm.cfc.godsplan.mapbox;

import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.cfc.godsplan.http.BasicHttpClient;
import com.ibm.cfc.godsplan.http.BasicHttpClient.BasicHttpResponse;

/**
 *
 */
public class MapboxClientSampleCode
{

   protected static final Logger logger = LoggerFactory.getLogger(MapboxClientSampleCode.class);
   /***/
   public static final String MAPBOX_URL = "api.mapbox.com";
   /***/
   public static final int MAPBOX_PORT = 443;
   /***/
   public static final String MAPBOX_API_TOKEN = System.getenv("MAPBOX_API_TOKEN");
   /***/
   public static final String MAPBOX_USER = "team6ix";
   /***/
   public static final String MAPBOX_DATASET = "cjl565k8f0pc62wnxgggh6gc4";
   /***/
   public static BasicHttpClient httpClient;

   /***/
   public MapboxClientSampleCode()
   {
      try
      {
         httpClient = new BasicHttpClient("https", MAPBOX_URL, MAPBOX_PORT);
      }
      catch (HttpException e)
      {
         logger.error("Error creating connection to Mapbox.", e);
      }
   }

   /**
    * @return JSON Response from dataset command
    * @throws HttpException
    */
   public String listDatasets() throws HttpException
   {

      BasicHttpResponse httpResponse = httpClient.executeGet("/datasets/v1/" + MAPBOX_USER, getDefaultQueryParams());
      return httpResponse.getEntity();
   }

   /**
    * @param featureId
    * @return
    * @throws HttpException
    */
   public String putFeature(int featureId) throws HttpException
   {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("id", Integer.toString(featureId));
      jsonObject.addProperty("type", "Feature");
      
      JsonObject geometryJson = new JsonObject();
      geometryJson.addProperty("type", "Point");
      
      JsonObject propertiesJson = new JsonObject();
      
      JsonArray coordinates = new JsonArray();
      coordinates.add(43.6532);
      coordinates.add(79.3832);
      geometryJson.add("coordinates", coordinates);
      
      jsonObject.add("geometry", geometryJson);
      jsonObject.add("properties", propertiesJson);
      
      BasicHttpResponse response = httpClient.executePut("/datasets/v1/" + MAPBOX_USER + "/" + MAPBOX_DATASET + "/features/" + featureId, jsonObject.toString(), getDefaultQueryParams());
      return response.getEntity();
   }

   public static void main(String args[]) throws HttpException
   {
      MapboxClientSampleCode client = new MapboxClientSampleCode();
      System.out.println(client.listDatasets());
      client.putFeature(52);
   }

   private Map<String, String> getDefaultQueryParams()
   {
      return new HashMap<String, String>()
      {
         {
            put("access_token", MAPBOX_API_TOKEN);
         }
      };
   }

}
