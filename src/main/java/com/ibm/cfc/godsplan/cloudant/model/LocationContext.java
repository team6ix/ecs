package com.ibm.cfc.godsplan.cloudant.model;

import com.ibm.cfc.godsplan.maps.model.GoogleAddressInformation;

/**
 * POJO for locationcontext database documents
 *
 */
public class LocationContext
{
   private String _id;
   private GoogleAddressInformation location;
   private boolean addressConfirmed;

   /**
    * 
    * @param phoneNumber
    * @param location
    * @param addressConfirmed
    */
   public LocationContext(String phoneNumber, GoogleAddressInformation location, boolean addressConfirmed)
   {
      this._id = phoneNumber;
      this.location = location;
      this.addressConfirmed = addressConfirmed;
   }

   /**
    * 
    * @return true if user has confirmed address is correct
    */
   public boolean getAddressConfirmed()
   {
      return addressConfirmed;
   }
   
   /**
    * 
    * @return {@link GoogleAddressInformation}
    */
   public GoogleAddressInformation getAddress()
   {
      return location;
   }

   /**
    * 
    * @return the user phone number
    */
   public String getPhoneNumber()
   {
      return _id;
   }
}
