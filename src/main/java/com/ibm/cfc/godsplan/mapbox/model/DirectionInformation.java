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

package com.ibm.cfc.godsplan.mapbox.model;

import java.util.List;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Point;

/**
 * Holds direction information between two points
 */
public class DirectionInformation
{
   private Point origin;
   private Point destination;
   private String distance;
   private List<String> directions;
   private String travelProfile;
   private JsonObject route;
   private String polyLine;
   private boolean isValid;

   /**
    * Constructs a DirectionInformation object
    * 
    * @param org
    *           starting point
    * @param dest
    *           end point
    * @param dist
    *           distance in meters between points
    * @param profile
    *           travel profile
    * @param directs
    *           step by step directions to get from start to end
    * @param route
    *           legs from directions api
    * @param isValid
    *           true if direction information is valid
    */
   public DirectionInformation(Point org, Point dest, String dist, String profile, List<String> directs,
         JsonObject route, boolean isValid)
   {
      this.origin = org;
      this.destination = dest;
      this.distance = dist;
      this.directions = directs;
      this.travelProfile = profile;
      this.route = route != null ? route : null;
      this.polyLine = route != null ? route.get("overview_polyline").getAsJsonObject().get("points").toString() : null;
      this.isValid = isValid;
   }

   /**
    * 
    * @return starting point
    */
   public Point getOrigin()
   {
      return origin;
   }

   /**
    * 
    * @return end point
    */
   public Point getDestination()
   {
      return destination;
   }

   /**
    * 
    * @return distance in meters
    */
   public String getDistance()
   {
      return distance;
   }

   /**
    * 
    * @return step by step directions
    */
   public List<String> getDirections()
   {
      return directions;
   }

   /**
    * 
    * @return mode of travel
    */
   public String getTravelProfile()
   {
      return travelProfile;
   }

   /**
    * 
    * @return full route details
    */
   public JsonObject getRouteDetails()
   {
      return route;
   }

   /**
    * @return polyline encoding of directions
    * 
    */
   public String getPolyline()
   {
      return polyLine;
   }

   /**
    * 
    * @return true if directions is valid
    */
   public boolean isValid()
   {
      return isValid;
   }
}
