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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.errors.ApiException;
import com.ibm.cfc.godsplan.cloudant.model.SurveyContext;
import com.ibm.cfc.godsplan.disaster.DisasterInformation;
import com.ibm.cfc.godsplan.http.BasicHttpClient;
import com.ibm.cfc.godsplan.http.BasicHttpClient.BasicHttpResponse;
import com.ibm.cfc.godsplan.maps.LocationMapper;
import com.mapbox.geojson.Point;

/**
 *
 */
public class MapboxClient
{

   protected static final Logger logger = LoggerFactory.getLogger(MapboxClient.class);
   /***/
   public static final String MAPBOX_URL = "api.mapbox.com";
   /***/
   public static final int MAPBOX_PORT = 443;
   /***/
   public static final String MAPBOX_API_TOKEN = System.getenv("MAPBOX_API_TOKEN");
   /***/
   public static BasicHttpClient httpClient;
   /***/
   public final String FINAL_DESTINATION = "You have arrived at your destination";
   /***/
   public final int SECONDS_IN_A_MINUTE = 60;

   /***/
   @SuppressWarnings("javadoc")
   public enum Severity
   {
      LOWEST(1), LOW(2), MEDIUM(3), HIGH(4), HIGHEST(5);
      private final int value;

      private Severity(int value)
      {
         this.value = value;
      }

      /**
       * @return the int value
       */
      public int getValue()
      {
         return value;
      }
   }

   /***/
   public MapboxClient()
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
    * 
    * @param id
    * @param longitude
    * @param latitude
    * @throws HttpException
    */
   public void addDisaster(String id, double longitude, double latitude)
   {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("id", id);
      jsonObject.addProperty("type", "Feature");

      JsonObject geometryJson = new JsonObject();
      geometryJson.addProperty("type", "Point");

      JsonObject propertiesJson = new JsonObject();

      JsonArray coordinates = new JsonArray();
      coordinates.add(longitude);
      coordinates.add(latitude);
      geometryJson.add("coordinates", coordinates);

      jsonObject.add("geometry", geometryJson);
      jsonObject.add("properties", propertiesJson);

      addPointToDataset(id, longitude, latitude, MapboxUtils.MAPBOX_DISASTER_DATASET, jsonObject);
      MapboxUpdateQueue.getInstance().add(new MapboxUpdateData(MapboxUtils.MAPBOX_DISASTER_DATASET,
            MapboxUtils.MAPBOX_DISASTER_TILESET, MapboxUtils.MAPBOX_DISASTER_TILESET_NAME));
   }
   
   /**
    * 
    * @param id
    * @param longitude
    * @param latitude
    * @throws HttpException
    */
   public void addShelter(String id, double longitude, double latitude)
   {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("id", id);
      jsonObject.addProperty("type", "Feature");

      JsonObject geometryJson = new JsonObject();
      geometryJson.addProperty("type", "Point");

      JsonObject propertiesJson = new JsonObject();

      JsonArray coordinates = new JsonArray();
      coordinates.add(longitude);
      coordinates.add(latitude);
      geometryJson.add("coordinates", coordinates);

      jsonObject.add("geometry", geometryJson);
      jsonObject.add("properties", propertiesJson);
      
      addPointToDataset(id, longitude, latitude, MapboxUtils.MAPBOX_SHELTER_DATASET, jsonObject);
      MapboxUpdateQueue.getInstance().add(new MapboxUpdateData(MapboxUtils.MAPBOX_SHELTER_DATASET, MapboxUtils.MAPBOX_SHELTER_TILESET, MapboxUtils.MAPBOX_SHELTER_TILESET_NAME));
   }
   
   /**
    * 
    * @param id
    * @param longitude
    * @param latitude
    * @throws HttpException
    */
   public void addPerson(String id, double longitude, double latitude, int severity)
   {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("id", id);
      jsonObject.addProperty("type", "Feature");

      JsonObject geometryJson = new JsonObject();
      geometryJson.addProperty("type", "Point");

      JsonObject propertiesJson = new JsonObject();
      propertiesJson.addProperty("severity", severity);

      JsonArray coordinates = new JsonArray();
      coordinates.add(longitude);
      coordinates.add(latitude);
      geometryJson.add("coordinates", coordinates);

      jsonObject.add("geometry", geometryJson);
      jsonObject.add("properties", propertiesJson);
      
      addPointToDataset(id, longitude, latitude, MapboxUtils.MAPBOX_DATASET, jsonObject);
      MapboxUpdateQueue.getInstance().add(new MapboxUpdateData(MapboxUtils.MAPBOX_DATASET, MapboxUtils.MAPBOX_TILESET,
            MapboxUtils.MAPBOX_TILESET_NAME));
   }

   /**
    * 
    * @param id
    * @param longitude
    * @param latitude
    * @param dataset
    * @param jsonObject
    * @throws HttpException
    */
   public void addPointToDataset(String id, double longitude, double latitude, String dataset, JsonObject jsonObject)
   {
      logger.info("Adding id {} to admin map with x coordinate {} and y coordinate {} on dataset {}", id, longitude, latitude, dataset);
      
      try
      {
         BasicHttpResponse response = httpClient.executePut(
               "/datasets/v1/" + MapboxUtils.MAPBOX_USER + "/" + dataset + "/features/" + id, jsonObject.toString(),
               getDefaultQueryParams());
         if (response.getStatusCode() != 200 && response.getStatusCode() != 201)
         {
            logger.error("Received error from MapboxAPI: {}" + response.getEntity());
         }
      }
      catch (HttpException e)
      {
         logger.error("Could not save info to admin map.", e);
      }
   }

   /**
    * Update an existing person on Mapbox with a new severity
    * 
    * @param id
    * @param severity
    */
   public void updatePerson(String id, int severity)
   {
      logger.info("Updating id with severity {}", severity);
      try
      {
         BasicHttpResponse response = httpClient.executeGet(
               "/datasets/v1/" + MapboxUtils.MAPBOX_USER + "/" + MapboxUtils.MAPBOX_DATASET + "/features/" + id, getDefaultQueryParams());
         JsonObject jsonObject = (new JsonParser()).parse(response.getEntity()).getAsJsonObject();

         JsonObject propertiesJson = new JsonObject();
         propertiesJson.addProperty("severity", severity);
         jsonObject.add("properties", propertiesJson);

         response = httpClient.executePut("/datasets/v1/" + MapboxUtils.MAPBOX_USER + "/" + MapboxUtils.MAPBOX_DATASET + "/features/" + id,
               jsonObject.toString(), getDefaultQueryParams());
         if (response.getStatusCode() != 200)
         {
            logger.error("Received error from MapboxAPI: {}" + response.getEntity());
         }
      }
      catch (HttpException e)
      {
         logger.error("Could not save info to admin map.", e);
      }
      MapboxUpdateQueue.getInstance().add(new MapboxUpdateData(MapboxUtils.MAPBOX_DATASET, MapboxUtils.MAPBOX_TILESET, MapboxUtils.MAPBOX_TILESET_NAME));
   }
   
   /**
    * @param dataset
    * @param tileset 
    * @param tilesetName 
    */
   public static void updateMap(String dataset, String tileset, String tilesetName)
   {
      logger.info("Updating dataset {} with tileset {} and tilesetName {}", dataset, tileset, tilesetName);
      try
      {
         JsonObject updateJson = new JsonObject();
         updateJson.addProperty("tileset", tileset);
         updateJson.addProperty("url", "mapbox://datasets/team6ix/" + dataset);
         updateJson.addProperty("name", tilesetName);

         for(int i = 0 ; i < 5 ; i++)
         {         
            BasicHttpResponse response = httpClient.executePost("/uploads/v1/" + MapboxUtils.MAPBOX_USER,
                  updateJson.toString(), getDefaultQueryParams());
            int statusCode = response.getStatusCode();
            String entity = response.getEntity();
            if(entity.contains(MapboxUtils.CONCURRENT_TILESET_MESSAGE) || entity.contains(MapboxUtils.CONCURRENT_UPLOAD_MESSAGE))
            {
					logger.error("Received retryable error from Mapbox - waiting for 2 seconds: {}" + response.getEntity());
            
               try
               {
                  Thread.sleep(3000);
                  continue;
               }
               catch (InterruptedException e)
               {
                  // Ignore
               }
            }
            else if (statusCode != 200 && statusCode !=201)
            {
               logger.error("Received error from MapbsoxAPI updating map: {}" + response.getEntity());
               break;
            }
            else
            {
               logger.info("Updated map: {}" + tilesetName);
               break;
            }
         }

         logger.error("Could not update map in 5 attempts.");

      }
      catch (HttpException e)
      {
         logger.error("Could not save info to admin map.", e);
      }
   }

   private static Map<String, String> getDefaultQueryParams()
   {
      Map<String, String> map = new HashMap<>();
      map.put("access_token", MAPBOX_API_TOKEN);
      return map;
   }

   /**
    * Gets a google maps snapshot of the specified address and saves it to a file of the specified image size with
    * directional polyline
    *
    * @param address
    *           desired address
    * @param size
    *           image size
    * @param file
    *           the file to write
    * @throws ClientProtocolException
    * @throws IOException
    */
   public void getGoogleImage(String address, String size, String polyline, File file)
         throws ClientProtocolException, IOException
   {
      String encodedPolyline = getEncodedPolyline(polyline);
      String url = MessageFormat.format(
            "https://maps.googleapis.com/maps/api/staticmap" + "?size=400x400&center={0}&zoom=12"
                  + "&path=weight:30%7Ccolor:red%7Cenc:{1}" + "&key={2}",
            address, encodedPolyline, System.getenv("GOOGLE_API_KEY"));

      logger.info("Google URL: {}", url);

      // Make HTTP GET Request to Google maps
      try (CloseableHttpClient httpclient = HttpClients.createDefault())
      {
         HttpGet httpGet = new HttpGet(url);
         CloseableHttpResponse response1 = httpclient.execute(httpGet);

         if (!file.exists())
         {
            file.createNewFile();
         }

         writeEntityToFile(file.getAbsolutePath(), response1.getEntity());
      }
   }

   /**
    * @param address
    * @param size
    * @return
    */
   public String getEncodedPolyline(String address)
   {
      // Json return gives quoted string, strip quotes
      if (address.contains("\""))
      {
         address = address.replaceAll("\"", "");
      }

      return address.replaceAll(" ", "%20").replaceAll("@", "%40").replaceAll("]", "%5D").replaceAll("\\{", "%7B")
            .replaceAll("\\}", "%7D").replaceAll("\\[", "%5B").replaceAll("\\|", "%7C").replaceAll("`", "%60")
            .replaceAll("\\\\", "%5C").replaceAll("\\^", "%5E");
   }

   private static void writeEntityToFile(String fullPath, HttpEntity entity) throws IOException
   {
      try (FileOutputStream outStream = new FileOutputStream(fullPath))
      {
         entity.writeTo(outStream);

      }
      catch (IOException e)
      {
         logger.error("failed to write map image", e);
         throw e;
      }
   }

   /**
    * @param surveyContext
    * @return the severity based on this survey context
    */
   public static int generateSeverity(SurveyContext surveyContext)
   {
      if (surveyContext.getMustEvacuate() && surveyContext.getIsInjured())
      {
         return 5;
      }

      else if (surveyContext.getMustEvacuate() && !surveyContext.getIsInjured() && !surveyContext.getHasVehicle())
      {
         return 4;
      }
      else if (surveyContext.getMustEvacuate() && !surveyContext.getIsInjured() && surveyContext.getHasVehicle())
      {
         return 3;
      }
      else if (!surveyContext.getMustEvacuate() && !surveyContext.getHasVehicle())
      {
         return 2;
      }
      else if (!surveyContext.getMustEvacuate() && surveyContext.getHasVehicle())
      {
         return 1;
      }
      return 0;
   }
}
