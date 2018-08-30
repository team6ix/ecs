package com.ibm.cfc.godsplan.maps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.ibm.cfc.godsplan.mapbox.model.DirectionInformation;
import com.ibm.cfc.godsplan.maps.model.GoogleAddressInformation;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.geojson.Point;

/**
 * Class for handling maps and directions
 */
public class LocationMapper
{
   private static final String IMAGESIZE_DEFAULT = "800x600";
   private final String key = System.getenv("GOOGLE_API_KEY");
   private final GeoApiContext context;
   // address, size, apikey | request centers a map and places a red pin at
   // specified locations, and creates a snapshot of specified size
   private static final String URL_GMAP_API = "https://maps.googleapis.com/maps/api/staticmap?center={0}&zoom=13&size={1}&"
         + "markers=color:red%7Clabel:S%7C{0}&key={2}";
   private static final String URL_GMAP_DIRECTIONS_API = "https://maps.googleapis.com/maps/api/staticmap"
         + "?size={0}&path=weight:3%7Ccolor:red%7Cenc:{1}&key={2}";
   protected static final Logger logger = LoggerFactory.getLogger(LocationMapper.class);

   /**
    * Constructor
    */
   public LocationMapper()
   {
      context = new GeoApiContext.Builder().apiKey(key).build();
   }

   /**
    * Gets an array of results returning geocode data for address specified
    *
    * @param address
    *           desired address
    * @return geocode data
    * @throws ApiException
    * @throws InterruptedException
    * @throws IOException
    */
   public GeocodingResult[] getGeocodingResults(String address) throws ApiException, InterruptedException, IOException
   {
      return GeocodingApi.geocode(context, address).await();
   }

   /**
    * @param rawAddress
    *           unstructed address to look up
    * @return a List of potential formatted addresses.
    * @throws ApiException
    * @throws InterruptedException
    * @throws IOException
    */
   public List<GoogleAddressInformation> getFormattedAddress(String rawAddress)
         throws ApiException, InterruptedException, IOException
   {
      List<GoogleAddressInformation> addressesFound = new ArrayList<>();
      GeocodingResult[] results = getGeocodingResults(rawAddress);
      for (GeocodingResult result : results)
      {
         GoogleAddressInformation address = new GoogleAddressInformation(result.geometry.location.lat,
               result.geometry.location.lng, result.formattedAddress);
         addressesFound.add(address);
      }
      return addressesFound;
   }

   /**
    * Gets the longitude and latitude of specified address
    *
    * @param address
    *           desired address
    * @return latitude and longitude of address
    * @throws ApiException
    * @throws InterruptedException
    * @throws IOException
    */
   public Point getGeocodingCoordinates(String address) throws ApiException, InterruptedException, IOException
   {
      GeocodingResult[] results = getGeocodingResults(address);
      LatLng coords = results[0].geometry.location;
      return Point.fromLngLat(coords.lng, coords.lat);

   }

   /**
    * List route steps between two locations
    * 
    * @param origin
    *           point coordinates of start location
    * @param dest
    *           point coordinates of end location
    * @param wayPts
    *           list of way points
    * @param profile
    *           route transportation profile, default is walking
    * @return information regarding directions between points
    */
   public DirectionInformation getDirectionInformation(Point origin, Point dest, Optional<List<Point>> wayPts,
         Optional<String> profile)
   {
      boolean isValid = true;
      List<String> stepsList;
      String distance = "";
      String tProfile = profile.isPresent() ? profile.get() : DirectionsCriteria.PROFILE_WALKING;
      tProfile = "mode=" + tProfile + "&";
      String start = origin.latitude() + "," + origin.longitude();
      String end = dest.latitude() + "," + dest.longitude();

      // Directions url call
      String url = MessageFormat.format(
            "https://maps.googleapis.com/maps/api/directions/json?origin={0}&destination={1}&{2}key={3}", start, end,
            tProfile, key);

      String entity;
      // Make HTTP GET Request to Google maps
      try (CloseableHttpClient httpclient = HttpClients.createDefault())
      {
         HttpGet httpGet = new HttpGet(url);
         CloseableHttpResponse response1 = httpclient.execute(httpGet);
         entity = EntityUtils.toString(response1.getEntity());
      }
      catch (ParseException | IOException e)
      {
         logger.error("What the guatemala unable to retrieve response content");
         e.printStackTrace();
         entity = "";
      }

      JsonObject response = new Gson().fromJson(entity, JsonObject.class);
      JsonObject route = response.getAsJsonArray("routes").get(0).getAsJsonObject();
      JsonObject legs = route.get("legs").getAsJsonArray().get(0).getAsJsonObject();
      distance = legs.get("distance").getAsJsonObject().get("text").getAsString();
      JsonArray steps = legs.get("steps").getAsJsonArray();

      stepsList = getTextDirections(steps);

      return new DirectionInformation(origin, dest, distance, tProfile, stepsList, route, isValid);
   }

   private List<String> getTextDirections(JsonArray steps)
   {
      List<String> stepList = new ArrayList<>();

      for (int i = 0; i < steps.size(); i++)
      {
         JsonObject step = steps.get(i).getAsJsonObject();
         String distance = step.get("distance").getAsJsonObject().get("text").getAsString();

         String direction = "{0}, continue for {1}.";
         String htmlInstruction = step.get("html_instructions").getAsString();
         String instruction = StringEscapeUtils.unescapeHtml4(htmlInstruction);
         instruction = instruction.replaceAll("\\<.\\>", "").replaceAll("\\<\\/.\\>", "")
               .replaceAll("\\<div.*\\\\\"\\>", "").replaceAll("\\<\\/div\\>", "");

         stepList.add(MessageFormat.format(direction, instruction, distance));
      }

      return stepList;
   }

   /**
    * Gets a google maps snapshot of the specified address and saves it to a file of the specified image size
    *
    * @param address
    *           desired address
    * @param size
    *           image size
    * @param file
    *           the file to write
    * @param directions
    *           directions information
    * @throws ClientProtocolException
    * @throws IOException
    */
   public void getGoogleImage(String address, String size, File file, Optional<DirectionInformation> directions)
         throws ClientProtocolException, IOException
   {
      String url = getGoogleImageURI(address, directions);

      logger.info("Making http call with URL: {}", url);
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
    * @param directions
    * @return
    */
   public String getGoogleImageURI(String address, Optional<DirectionInformation> directions)
   {
      return getGoogleImageURI(address, IMAGESIZE_DEFAULT, directions);
   }

   /**
    * @param address
    * @param size
    * @param directions
    * @return
    */
   public String getGoogleImageURI(String address, String size, Optional<DirectionInformation> directions)
   {
      // Json return gives quoted string, strip quotes
      if (address.contains("\""))
      {
         address = address.replaceAll("\"", "");
      }

      if (directions.isPresent())
      {
         try
         {
            return MessageFormat.format(URL_GMAP_DIRECTIONS_API, size,
                  URLEncoder.encode(directions.get().getPolyline().replaceAll("\"", ""), "UTF-8"), key);
         }
         catch (UnsupportedEncodingException e)
         {
            logger.info("Ermagawddd could not encode that polyline {}", directions.get().getPolyline());
            e.printStackTrace();
         }
      }

      return MessageFormat.format(URL_GMAP_API, address, size, key).replaceAll(" ", "%20");
   }

   private void writeEntityToFile(String fullPath, HttpEntity entity) throws IOException
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
