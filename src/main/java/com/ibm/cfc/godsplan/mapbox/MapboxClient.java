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
   public static final String MAPBOX_USER = "team6ix";
   /***/
   public static final String MAPBOX_DATASET = "cjl565k8f0pc62wnxgggh6gc4";
   /***/
   public static final String MAPBOX_TILESET = "team6ix.cjl565k8f0pc62wnxgggh6gc4-6yi10";
   /***/
   public static final String MAPBOX_TILESET_NAME = "TorontoDisaster";
   /***/
   public static BasicHttpClient httpClient;
   /***/
   public final String FINAL_DESTINATION = "You have arrived at your destination";
   /***/
   public final int SECONDS_IN_A_MINUTE = 60;

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
    * @param xCoordinate
    * @param yCoordinate
    * @param featureId
    * @throws HttpException
    */
   public void addPerson(String id, double xCoordinate, double yCoordinate)
   {
      logger.info("Adding id {} to admin map with x coordinate {} and y coordinate {}", id, xCoordinate, yCoordinate);

      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("id", id);
      jsonObject.addProperty("type", "Feature");

      JsonObject geometryJson = new JsonObject();
      geometryJson.addProperty("type", "Point");

      JsonObject propertiesJson = new JsonObject();
      propertiesJson.addProperty("severity", 5);

      JsonArray coordinates = new JsonArray();
      coordinates.add(xCoordinate);
      coordinates.add(yCoordinate);
      geometryJson.add("coordinates", coordinates);

      jsonObject.add("geometry", geometryJson);
      jsonObject.add("properties", propertiesJson);

      try
      {
         BasicHttpResponse response = httpClient.executePut(
               "/datasets/v1/" + MAPBOX_USER + "/" + MAPBOX_DATASET + "/features/" + id, jsonObject.toString(),
               getDefaultQueryParams());
         if (response.getStatusCode() != 200)
         {
            logger.error("Received error from MapboxAPI: {}" + response.getEntity());
         }
      }
      catch (HttpException e)
      {
         logger.error("Could not save info to admin map.", e);
      }
      updateMap();
   }
   
   /**
    * Update an existing person on Mapbox with a new severity
    * @param id
    * @param severity
    */
   public void updatePerson(String id, int severity)
   {
      logger.info("Updating id with severity {}", severity);
      try
      {
         BasicHttpResponse response = httpClient.executeGet("/datasets/v1/" + MAPBOX_USER + "/" + MAPBOX_DATASET + "/features/" + id, getDefaultQueryParams());
         JsonObject jsonObject = (new JsonParser()).parse(response.getEntity()).getAsJsonObject();
         
         JsonObject propertiesJson = new JsonObject();
         propertiesJson.addProperty("severity", severity);
         jsonObject.add("properties", propertiesJson);
         
         response = httpClient.executePut("/datasets/v1/" + MAPBOX_USER + "/" + MAPBOX_DATASET + "/features/" + id, jsonObject.toString(), getDefaultQueryParams());
         if(response.getStatusCode() != 200)
         {
            logger.error("Received error from MapboxAPI: {}" + response.getEntity());
         }
      }
      catch (HttpException e)
      {
         logger.error("Could not save info to admin map.", e);
      }
      updateMap();
   }
   
   /**
    *  Sends request to update mapbox tileset
    */
   public void updateMap()
   {
      try
      {
         JsonObject updateJson = new JsonObject();
         updateJson.addProperty("tileset", MAPBOX_TILESET);
         updateJson.addProperty("url", "mapbox://datasets/team6ix/" + MAPBOX_DATASET);
         updateJson.addProperty("name", MAPBOX_TILESET_NAME);

         BasicHttpResponse response = httpClient.executePost("/uploads/v1/" + MAPBOX_USER, updateJson.toString(), getDefaultQueryParams());
         if(response.getStatusCode() != 200)
         {
            System.out.println(response.getStatusCode());
            logger.error("Received error from MapboxAPI updating map: {}" + response.getEntity());
         }
      }
      catch (HttpException e)
      {
         logger.error("Could not save info to admin map.", e);
      }
   }

   private Map<String, String> getDefaultQueryParams()
   {
      Map<String, String> map = new HashMap<>();
      map.put("access_token", MAPBOX_API_TOKEN);
      return map;
   }

   public static void main(String args[]) throws HttpException, IOException, ApiException, InterruptedException
   {
      MapboxClient client = new MapboxClient();
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      DisasterInformation info = DisasterInformation.getInstance();
      info.addDisasterPoint(Point.fromLngLat(-79.338368, 43.848631));

      LocationMapper mapper = new LocationMapper();
      Point o = mapper.getGeocodingCoordinates("8200 Warden Ave");
      Point d = mapper.getGeocodingCoordinates("First Markham Place");
      File file = new File("./src/main/resources/directions.jpg");

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

}
