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
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import retrofit2.Response;

/**
 * Holds direction information between two points
 */
public class DirectionInformation
{
   private Point origin;
   private Point destination;
   private Double distance;
   private List<String> directions;
   private String travelProfile;
   private DirectionsRoute route;
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
    * @param response
    *           response from directions api
    * @param isValid
    *           true if direction information is valid
    */
   public DirectionInformation(Point org, Point dest, double dist, String profile, List<String> directs,
         Response<DirectionsResponse> response, boolean isValid)
   {
      this.origin = org;
      this.destination = dest;
      this.distance = dist;
      this.directions = directs;
      this.travelProfile = profile;
      this.route = response != null ? response.body().routes().get(0) : null;
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
   public double getDistance()
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
   public DirectionsRoute getRouteDetails()
   {
      return route;
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
