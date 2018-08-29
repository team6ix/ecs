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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.maps.errors.ApiException;
import com.ibm.cfc.godsplan.disaster.DisasterInformation;
import com.ibm.cfc.godsplan.disaster.DisasterProximityCalculator;
import com.ibm.cfc.godsplan.http.BasicHttpClient;
import com.ibm.cfc.godsplan.http.BasicHttpClient.BasicHttpResponse;
import com.ibm.cfc.godsplan.mapbox.model.DirectionInformation;
import com.ibm.cfc.godsplan.maps.LocationMapper;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.MapboxDirections.Builder;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.api.directions.v5.models.StepManeuver;
import com.mapbox.geojson.Point;
import retrofit2.Response;

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
   }

   private Map<String, String> getDefaultQueryParams()
   {
      Map<String, String> map = new HashMap<>();
      map.put("access_token", MAPBOX_API_TOKEN);
      return map;
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
      MapboxDirections request;
      Builder builder;
      boolean isValid = true;
      List<String> stepsList = new ArrayList<>();
      Double distance = (double) -1;
      String tProfile = profile.isPresent() ? profile.get() : DirectionsCriteria.PROFILE_WALKING;

      // 1. Pass in all the required information to get a route.
      logger.info("Finding route, setting movement profile to {}", tProfile);
      builder = MapboxDirections.builder().accessToken(MAPBOX_API_TOKEN).origin(origin).destination(dest)
            .profile(tProfile).steps(true);

      // Add way points to path if present
      if (wayPts.isPresent())
      {
         for (Point p : wayPts.get())
         {
            builder.addWaypoint(p).build();
         }
      }

      request = builder.build();
      Response<DirectionsResponse> response;

      try
      {
         response = request.executeCall();
      }
      catch (IOException e)
      {
         logger.error("Unable to make call to mapbox for directions between, {} and {}", origin, dest, e);
         stepsList.add("ERROR: Could not find directions");
         return new DirectionInformation(origin, dest, distance, tProfile, stepsList, null, false);
      }

      // 3. Log information from the response
      if (response.isSuccessful())
      {
         int count = 0;
         logger.info("MapBox directions call successful");
         List<LegStep> steps = response.body().routes().get(0).legs().get(0).steps();
         double routeDurationMins = response.body().routes().get(0).duration() / SECONDS_IN_A_MINUTE;
         logger.info("Route found with {} steps, estimated duration {}", steps.size(), routeDurationMins);
         distance = response.body().routes().get(0).distance();

         for (LegStep stepLeg : steps)
         {

            String stepName = stepLeg.name();
            StepManeuver maneuver = stepLeg.maneuver();
            Point location = maneuver.location();
            DisasterProximityCalculator calc = new DisasterProximityCalculator(location);
            logger.info("Coordinate step {} of {}", ++count, steps.size());

            if (calc.getIsWithinDisasterZone() && !wayPts.isPresent())
            {
               logger.info("Direction point {}: ({} {}) is within danger zone, redirecting...", stepName,
                     location.longitude(), location.latitude());
               isValid = false;
            }

            if (!stepName.isEmpty() && !maneuver.instruction().contains(FINAL_DESTINATION))
            {
               stepsList.add(MessageFormat.format("{0}, for {1} meters.", stepLeg.maneuver().instruction(),
                     stepLeg.distance()));
            }
            else if (maneuver.instruction().contains(FINAL_DESTINATION))
            {
               stepsList.add(maneuver.instruction());
            }
         }
      }

      return new DirectionInformation(origin, dest, distance, tProfile, stepsList, response, isValid);
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

      System.out.println("Origin: " + o);
      System.out.println("Destination: " + d);
      DirectionInformation infomein = client.getDirectionInformation(o, d, Optional.empty(), Optional.empty());
      System.out.println("Destination: " + infomein.getDestination());
      System.out.println("Origin: " + infomein.getOrigin());
      System.out.println("Distance: " + infomein.getDistance());

      for (String stp : infomein.getDirections())
      {
         System.out.println(stp);
      }

      System.out.println(gson.toJson(infomein.getRouteDetails()));
   }
}
