package com.ibm.cfc.godsplan.mapbox;

import com.ibm.cfc.godsplan.cloudant.model.SurveyContext;

public class MapboxSeverity
{

	public static int generateSeverity(SurveyContext surveyContext)
	{
		if (surveyContext.getMustEvacuate() && surveyContext.getIsInjured())
		{
			return 5;
		}

		else if (surveyContext.getMustEvacuate() && !surveyContext.getIsInjured() && !surveyContext.getHasVehicle())
		{
			return 4;
		}
		else if (surveyContext.getMustEvacuate() && !surveyContext.getIsInjured() && surveyContext.getHasVehicle())
		{
			return 3;
		}
		else if (!surveyContext.getMustEvacuate() && !surveyContext.getHasVehicle())
		{
			return 2;
		}
		else if (!surveyContext.getMustEvacuate() && surveyContext.getHasVehicle())
		{
			return 1;
		}
		return 0;
	}

}
