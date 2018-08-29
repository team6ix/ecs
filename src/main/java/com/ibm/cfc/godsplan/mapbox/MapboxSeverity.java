package com.ibm.cfc.godsplan.mapbox;

import com.ibm.cfc.godsplan.cloudant.model.SurveyContext;

public class MapboxSeverity
{
	private int severity;
	SurveyContext surveyContext;

	public int getSeverity()
	{
		return this.severity;
	}

	public void generateSeverity()
	{
		if (surveyContext.getMustEvacuate() && surveyContext.getIsInjured())
			this.severity = 5;
		else if (surveyContext.getMustEvacuate() && !surveyContext.getIsInjured() && !surveyContext.getHasVehicle())
			this.severity = 4;
		else if (surveyContext.getMustEvacuate() && !surveyContext.getIsInjured() && surveyContext.getHasVehicle())
			this.severity = 3;
		else if (!surveyContext.getMustEvacuate() && !surveyContext.getHasVehicle())
			this.severity = 2;
		else if (!surveyContext.getMustEvacuate() && surveyContext.getHasVehicle())
			this.severity = 1;
	}

}
