package com.ibm.cfc.godsplan.cloudant.model;

import java.util.Optional;

/**
 * 
 */
public class SurveyContext
{
   private String _id;
   private Optional<Boolean> mustEvacuate;
//   private Boolean physicallyAbleToEvacuate;
//   private Boolean hasVehicle;
//   private Boolean requiresEmergencyAssistance;
//   private Boolean requiresEvacuationAssistance;
//   private int availableSeatsInVehicle;
   
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
   public Optional<Boolean> getMustEvacuate()
   {
      return mustEvacuate;
   }
}
