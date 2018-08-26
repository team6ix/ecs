package com.ibm.cfc.godsplan.cloudant.model;

import java.util.Optional;

/**
 * 
 */
public class SurveyContext
{
   private String _id;
   private Boolean mustEvacuate;
   private Boolean hasVehicle;
   
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
    * @return the mustEvacuate
    */
   public Boolean getMustEvacuate()
   {
      return mustEvacuate;
   }
   
   /**
    * 
    * @param isMustEvacuate
    */
   public void setMustEvacuate(boolean isMustEvacuate)
   {
      this.mustEvacuate = isMustEvacuate;
   }
   
   /**
    * @return the hasVehicle
    */
   public Boolean getHasVehicle()
   {
      return hasVehicle;
   }
   
   /**
    * @param hasVehicle the hasVehicle to set
    */
   public void setHasVehicle(Boolean hasVehicle)
   {
      this.hasVehicle = hasVehicle;
   }

   /**
    * 
    */
   @Override
   public String toString()
   {
      return "SurveyContext [get_id()=" + get_id() + ", getMustEvacuate()=" + getMustEvacuate() + ", getHasVehicle()="
            + getHasVehicle() + "]";
   }

   
}
