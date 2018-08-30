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

package com.ibm.cfc.godsplan.disaster;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mapbox.geojson.Point;

/**
 * Class to determine disaster zone calculations
 */
public class DisasterProximityCalculator
{
   private static Logger logger = LoggerFactory.getLogger(DisasterProximityCalculator.class);
   private final List<Point> disasterPoints = DisasterInformation.getInstance().getDisasterPoints();
   private static List<Point> requiredWayPoints = new ArrayList<>();
   private Integer dangerPointsEast = 0;
   private Integer dangerPointsWest = 0;
   private Integer dangerPointsNorth = 0;
   private Integer dangerPointsSouth = 0;
   private boolean isCloseToDanger = false;
   private static int counter = 0;
   private final double DISASTER_RADIUS = 50;
   private final double SAFE_DISTANCE = 0.00055; //0.00055 change in longitude/latitude approximates to ~100 meters

   /**
    * Calculates a disaster proximity data to provided point
    *
    * @param currentPoint
    *           point of interest
    */
   public DisasterProximityCalculator(Point currentPoint)
   {
      logger.info("Calculating disaster proximity for {}", currentPoint.toJson());
      countRelativePositions(currentPoint);
      this.isCloseToDanger = determineProximityAlongRoute(currentPoint);
   }

   /**
    *
    * @param p
    *           point of interest
    * @return true if point of interest is within a danger zone
    */
   public boolean determineProximityAlongRoute(Point p)
   {
      for (Point disasterPoint : disasterPoints)
      {
         if (isPointWithinDisasterZone(p, disasterPoint))
         {
            addNewRequiredWaypoint(p);
            return true;
         }
      }

      return false;
   }

   private void countRelativePositions(Point p)
   {
      double lat = p.latitude();
      double lng = p.longitude();

      for (Point disasterPoint : disasterPoints)
      {
         double dLat = disasterPoint.latitude();
         double dLng = disasterPoint.longitude();

         if (lat < dLat)
         {
            dangerPointsWest++;
         }
         else if (lat > dLat)
         {
            dangerPointsEast++;
         }

         if (lng < dLng)
         {
            dangerPointsNorth++;
         }
         else if (lng > dLng)
         {
            dangerPointsSouth++;
         }
      }

      logger.debug("Number of danger zones to the north {}, south {}, east {}, and west {}", dangerPointsNorth,
            dangerPointsSouth, dangerPointsEast, dangerPointsWest);
   }

   /**
    * @param p
    *           the point to query if it is within DISASTER_RADIUS of dp
    * @param dp
    *           the center point of the disaster.
    * @return true if p is within DISASTER_RADIUS of dp
    */
   public boolean isPointWithinDisasterZone(Point p, Point dp)
   {
      double distance = distance(p, dp);
      logger.info("Coordinate distance from danger zone, {}", distance);

      return distance <= DISASTER_RADIUS;
   }

   /**
    * 
    * 
    * @param p
    * @param dp
    * @return the distance in meters of two coordinates on earth
    */
   public double distance(Point p, Point dp)
   {
      final int R = 6371; // Radius of the earth

      double latDistance = Math.toRadians(dp.latitude() - p.latitude());
      double lonDistance = Math.toRadians(dp.longitude() - p.longitude());
      double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(p.latitude()))
            * Math.cos(Math.toRadians(dp.latitude())) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      double distance = R * c * 1000; // convert to meters
      distance = Math.sqrt(distance);
      
      return distance;
   }
   
   private void addNewRequiredWaypoint(Point p)
   {
      double lat = p.latitude();
      double lng = p.longitude();
      double changeSafeDistance = SAFE_DISTANCE * counter++;

      double newLat = dangerPointsWest < dangerPointsEast ? lat + changeSafeDistance : lat - changeSafeDistance;
      double newLng = dangerPointsNorth < dangerPointsSouth ? lng + changeSafeDistance : lng - changeSafeDistance;

      Point newPoint = Point.fromLngLat(newLng, newLat);
      logger.info("Route coordinates within disaster zone, creating new mandatory waypoint, {}", newPoint.toJson());
      requiredWayPoints.add(newPoint);
   }

   /**
    *
    * @return true if current point is within a listed disaster zone
    */
   public boolean getIsWithinDisasterZone()
   {
      return this.isCloseToDanger;
   }

   /**
    *
    * @return list of required waypoints
    */
   public List<Point> getWaypoints()
   {
      return this.requiredWayPoints;
   }

}
