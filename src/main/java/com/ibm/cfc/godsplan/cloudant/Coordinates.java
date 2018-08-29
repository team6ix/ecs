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

/**
 * 
 */
public class Coordinates
{
   private double latitude;
   private double longitude;
   
   
   /**
    * 
    * @param latitude
    * @param longitude
    */
   public Coordinates(double latitude, double longitude)
   {
      super();
      this.latitude = latitude;
      this.longitude = longitude;
   }
   
   /**
    * @return the latitude
    */
   public double getLatitude()
   {
      return latitude;
   }
   /**
    * @return the longitude
    */
   public double getLongitude()
   {
      return longitude;
   }
   
   
}
