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

package com.ibm.cfc.godsplan.cloudant.model;

import com.ibm.cfc.godsplan.maps.model.GoogleAddressInformation;

/**
 * 
 */
public class ShelterLocationContext
{
   private String _id;
   private GoogleAddressInformation location;
   private boolean canAcceptMore;
   
   /**
    * @return the _id
    */
   public String get_id()
   {
      return _id;
   }
   /**
    * @param _id the _id to set
    */
   public void set_id(String _id)
   {
      this._id = _id;
   }
   /**
    * @return the location
    */
   public GoogleAddressInformation getLocation()
   {
      return location;
   }
   /**
    * @param location the location to set
    */
   public void setLocation(GoogleAddressInformation location)
   {
      this.location = location;
   }
   /**
    * @return the canAcceptMore
    */
   public boolean isCanAcceptMore()
   {
      return canAcceptMore;
   }
   /**
    * @param canAcceptMore the canAcceptMore to set
    */
   public void setCanAcceptMore(boolean canAcceptMore)
   {
      this.canAcceptMore = canAcceptMore;
   }

   
   
   
}
