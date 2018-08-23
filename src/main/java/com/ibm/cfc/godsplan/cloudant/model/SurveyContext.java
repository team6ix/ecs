package com.ibm.cfc.godsplan.cloudant.model;

/**
 * 
 */
public class SurveyContext
{
   private String _id;
   private boolean mustEvacuate;
   private boolean isPhysicallyAbleToEvacuate;
   private boolean hasVehicle;
   private boolean requiresEmergencyAssistance;
   private boolean requiresEvacuationAssistance;
   private int availableSeatsInVehicle;
   
   
   /**
    * @return _id phone number
    */
   public String get_id()
   {
      return _id;
   }
   
   /**
    * 
    * @param _id
    */
   public void set_id(String _id)
   {
      this._id = _id;
   }
   public boolean isMustEvacuate()
   {
      return mustEvacuate;
   }
   public void setMustEvacuate(boolean mustEvacuate)
   {
      this.mustEvacuate = mustEvacuate;
   }
   public boolean isPhysicallyAbleToEvacuate()
   {
      return isPhysicallyAbleToEvacuate;
   }
   public void setPhysicallyAbleToEvacuate(boolean isPhysicallyAbleToEvacuate)
   {
      this.isPhysicallyAbleToEvacuate = isPhysicallyAbleToEvacuate;
   }
   public boolean isHasVehicle()
   {
      return hasVehicle;
   }
   public void setHasVehicle(boolean hasVehicle)
   {
      this.hasVehicle = hasVehicle;
   }
   public boolean isRequiresEmergencyAssistance()
   {
      return requiresEmergencyAssistance;
   }
   public void setRequiresEmergencyAssistance(boolean requiresEmergencyAssistance)
   {
      this.requiresEmergencyAssistance = requiresEmergencyAssistance;
   }
   public boolean isRequiresEvacuationAssistance()
   {
      return requiresEvacuationAssistance;
   }
   public void setRequiresEvacuationAssistance(boolean requiresEvacuationAssistance)
   {
      this.requiresEvacuationAssistance = requiresEvacuationAssistance;
   }
   public int getAvailableSeatsInVehicle()
   {
      return availableSeatsInVehicle;
   }
   public void setAvailableSeatsInVehicle(int availableSeatsInVehicle)
   {
      this.availableSeatsInVehicle = availableSeatsInVehicle;
   }  
}
