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
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mapbox.geojson.Point;

/**
 * Information related to current disaster
 */
public class DisasterInformation
{
   private static Logger logger = LoggerFactory.getLogger(DisasterInformation.class);
   private static DisasterInformation disaster = null;
   private List<Point> disasterCoordinates;
   private DISASTER_TYPE disasterType;

   private DisasterInformation(DISASTER_TYPE type, Point... affectedCoordinates)
   {
      this.disasterType = type;
      this.disasterCoordinates = affectedCoordinates.length > 0 ? Arrays.asList(affectedCoordinates)
            : new ArrayList<>();
   }

   /**
    * 
    * @return information on current disaster
    */
   public static DisasterInformation getInstance()
   {
      if (disaster == null)
      {
         disaster = new DisasterInformation(DISASTER_TYPE.UNKNOWN);
      }

      return disaster;
   }

   /**
    * 
    * @return list of disaster points
    */
   public List<Point> getDisasterPoints()
   {
      return this.disasterCoordinates;
   }

   /**
    * Add new disaster coordinate(s)
    * 
    * @param points
    *           disaster coordinate(s)
    */
   public void addDisasterPoint(Point... points)
   {
      for (Point p : points)
      {
         logger.info("Adding disaster coordinate(s), {}", p.toString());
         disasterCoordinates.add(p);
      }

   }

   /**
    * Remove existing disaster coordinates
    * 
    * @param point
    *           disaster coordinate
    */
   public void removeDisasterPoint(Point point)
   {
      logger.info("Removing disaster coordinate, {}", point.toString());
      disasterCoordinates.remove(point);
   }

   /**
    * 
    * @return current type of disaster
    */
   public DISASTER_TYPE getDisasterType()
   {
      return this.disasterType;
   }

   /**
    * Clears any current disaster information
    */
   public void clearDisasterInformation()
   {
      this.disasterCoordinates.clear();
      this.disasterType = DISASTER_TYPE.UNKNOWN;
   }

   /**
    * Type of disaster
    */
   public enum DISASTER_TYPE
   {
      /***/
      WILD_FIRE("wildfire"),
      /***/
      TSUNAMI("tsunami"),
      /***/
      EARTHQUAKE("earthquake"),
      /***/
      UNKNOWN("unknown");

      private String disasterName;

      private DISASTER_TYPE(String name)
      {
         this.disasterName = name;
      }

      /**
       * 
       * @return string name of disaster type
       */
      public String getValue()
      {
         return this.disasterName;
      }
   }
}
