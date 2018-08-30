package com.ibm.cfc.godsplan.cloudant.model;

/**
 * 
 */
public class SurveyContext
{
	private String _id;
	private Boolean mustEvacuate;
	private Boolean canEvacuate;
	private Boolean evacuateConfirmation;
	private Boolean injuryConfirmed;

	public Boolean getIsInjured()
	{
		return injuryConfirmed;
	}

	public void setIsInjured(Boolean isInjured)
	{
		this.injuryConfirmed = isInjured;
	}

	/**
	 * @return the _id
	 */
	public String get_id()
	{
		return _id;
	}

	/**
	 * @param _id
	 *            the _id to set
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
		return canEvacuate;
	}

	/**
	 * @param hasVehicle
	 *            the hasVehicle to set
	 */
	public void setHasVehicle(Boolean hasVehicle)
	{
		this.canEvacuate = hasVehicle;
	}

	/**
	 * 
	 * @return
	 */
	public Boolean getHasSpace()
	{
		return this.evacuateConfirmation;
	}

	/**
	 * 
	 * @param hasSpace
	 */
	public void setHasSpace(Boolean hasSpace)
	{
		this.evacuateConfirmation = hasSpace;
	}

	/**
	* 
	*/
	@Override
	public String toString()
	{
		return "SurveyContext [get_id()=" + get_id() + ", getMustEvacuate()=" + getMustEvacuate()
				+ ", getCanEvacuate()=" + getHasVehicle() + ", getEvacuateConfirmation()=" + getHasSpace()
				+ ", getIsInjured()=" + getIsInjured() + "]";
	}

}
