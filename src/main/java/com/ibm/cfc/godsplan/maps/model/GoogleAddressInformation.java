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

package com.ibm.cfc.godsplan.maps.model;

/**
 * A class for storing relative address information
 */
public class GoogleAddressInformation
{

   private final double latitude;
   private final double longitude;
   private final String formattedAddress;

   /**
    * @param latitude
    *           the latitude coordinate for this address
    * @param longitude
    *           the longitude coordinate for this address
    * @param formattedAddress
    *           the formatted address as a string
    */
   public GoogleAddressInformation(double latitude, double longitude, String formattedAddress)
   {
      super();
      this.latitude = latitude;
      this.longitude = longitude;
      this.formattedAddress = formattedAddress;
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

   /**
    * @return the formatted address
    */
   public String getFormattedAddress()
   {
      return formattedAddress;
   }

   /**
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "GoogleAddressInformation [latitude=" + latitude + ", longitude=" + longitude + ", formattedAddress="
            + formattedAddress + "]";
   }

}
