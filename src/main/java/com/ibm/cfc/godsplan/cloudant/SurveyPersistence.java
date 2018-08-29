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

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudant.client.api.Database;
import com.cloudant.client.org.lightcouch.NoDocumentException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.cfc.godsplan.cloudant.model.SurveyContext;

/**
 * 
 */
public class SurveyPersistence
{
	/**
	 * Cloudant database that contains documents defined by {@link SurveyContext}
	 */
	public static final String DB = "surveycontext";

	protected static final Logger logger = LoggerFactory.getLogger(SurveyPersistence.class);

	Database db;
	JsonDocumentComposer compose;
	JsonParser parser;

	/**
	 * 
	 * @param surveyDb
	 */
	public SurveyPersistence(Database surveyDb)
	{
		this.db = surveyDb;
		compose = new JsonDocumentComposer();
		parser = new JsonParser();
	}

	/**
	 * 
	 * @param phoneNumber
	 * @param b
	 *            true or false
	 * @param attribute
	 *            the {@link SurveyAttribute}
	 */
	public void persistMustEvacuate(String phoneNumber, boolean b)
	{
		logger.info("setting mustEvacuate for '{}'", phoneNumber);
		persistAttribute(phoneNumber, "mustEvacuate", b);
	}

	/**
	 * 
	 * @param phoneNumber
	 * @param b
	 */
	public void persistHasVehicle(String phoneNumber, boolean b)
	{
		logger.info("setting hasVehicle for '{}'", phoneNumber);
		persistAttribute(phoneNumber, "hasVehicle", b);
	}

	public void persistHasSpace(String phoneNumber, boolean b)
	{
		logger.info("setting hasSpace for '{}'", phoneNumber);
		persistAttribute(phoneNumber, "hasSpace", b);
	}

	public void persistInjuryConfirmation(String phoneNumber, boolean b)
	{
		logger.info("setting injuryConfirmed for '{}'", phoneNumber);
		persistAttribute(phoneNumber, "injuryConfirmed", b);
	}

	/**
	 * 
	 * @param phoneNumber
	 * @return the surveycontext
	 */
	public Optional<SurveyContext> retrieve(String phoneNumber)
	{
		logger.info("retrieving survey information for '{}'", phoneNumber);
		Optional<SurveyContext> surveyContext;
		try
		{
			surveyContext = Optional.of(db.find(SurveyContext.class, phoneNumber));
		}
		catch (NoDocumentException nde)
		{
			logger.info("No address information for '{}' found", phoneNumber);
			surveyContext = Optional.empty();
		}

		return surveyContext;
	}

	/**
	 * 
	 * @param phoneNumber
	 */
	public void remove(String phoneNumber)
	{
		logger.info("removing survey context for '{}'", phoneNumber);
		try (InputStream is = db.find(phoneNumber);)
		{
			JsonElement doc = compose.jsonFromStream(is);
         JsonObject json = doc.getAsJsonObject();
			db.remove(json);
		}
		catch (NoDocumentException e)
		{
			logger.info("no survey context found for '{}'", phoneNumber);
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
	}

	private void persistAttribute(String phoneNumber, String attribute, boolean b)
	{
		try (InputStream is = db.find(phoneNumber);)
		{
			JsonElement doc = compose.jsonFromStream(is);
			JsonObject json = doc.getAsJsonObject();
			json.addProperty(attribute, b);
			db.update(json);
		}
		catch (NoDocumentException e)
		{
			JsonObject json = compose.blankDocument(phoneNumber);
			json.addProperty(attribute, b);
			db.save(json);
		}
		catch (IOException e1)
		{
			e1.printStackTrace(); // log or rethrow as somethin else our app handles
		}
	}
}
