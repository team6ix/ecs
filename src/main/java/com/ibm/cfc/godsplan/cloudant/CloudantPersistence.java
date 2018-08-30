package com.ibm.cfc.godsplan.cloudant;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.ibm.cfc.godsplan.cloudant.model.ChatContext;
import com.ibm.cfc.godsplan.cloudant.model.DisasterLocationContext;
import com.ibm.cfc.godsplan.cloudant.model.LocationContext;
import com.ibm.cfc.godsplan.cloudant.model.ShelterLocationContext;
import com.ibm.cfc.godsplan.cloudant.model.SurveyContext;
import com.ibm.cfc.godsplan.maps.model.GoogleAddressInformation;
import com.ibm.watson.developer_cloud.assistant.v1.model.Context;

/**
 * 
 */
public class CloudantPersistence
{
	private ChatPersistence chatDb;
	private LocationPersistence locationDb;
	private SurveyPersistence surveyDb;
	private ShelterLocationsPersistence shelterLocationsDb;
	private DisasterLocationsPersistence disasterLocationsDb;

	private CloudantClient client;

	protected static final Logger logger = LoggerFactory.getLogger(CloudantPersistence.class);

	/**
	 * Responsible for persisting all user information to Cloudant.
	 */
	public CloudantPersistence()
	{
		client = ClientBuilder.account("c008a85f-96b2-4d29-98c7-eedff0e86b1f-bluemix")
				.username("c008a85f-96b2-4d29-98c7-eedff0e86b1f-bluemix")
				.password("39965f7b72264bcd70f7bc27de159a629da46f2ac7a4f63108fa8d9b150d8c22").build();

		chatDb = new ChatPersistence(client.database(ChatPersistence.DB, false));
		locationDb = new LocationPersistence(client.database(LocationPersistence.DB, false));
		surveyDb = new SurveyPersistence(client.database(SurveyPersistence.DB, false));
		shelterLocationsDb = new ShelterLocationsPersistence(client.database(ShelterLocationsPersistence.DB, false));
		disasterLocationsDb = new DisasterLocationsPersistence(client.database(DisasterLocationsPersistence.DB, false));

	}

	/**
	 *
	 * @param phoneNumber
	 * @param context
	 * @throws IOException
	 */
	public void persistChatContext(String phoneNumber, Context context)
	{
		chatDb.persist(phoneNumber, context);
	}

	/**
	 * 
	 * @param phoneNumber
	 * @return Optional<ChatContext>
	 */
	public Optional<ChatContext> retrieveChatContext(String phoneNumber)
	{
		return chatDb.retrieve(phoneNumber);
	}

	/**
	 * 
	 * @param phoneNumber
	 * @param address
	 */
	public void persistAddress(String phoneNumber, GoogleAddressInformation address)
	{
		locationDb.persist(phoneNumber, address);
	}

	/**
	 * 
	 * @param phoneNumber
	 * @param confirm
	 *            true if user confirmed address is correct, false is address is
	 *            incorrect or unconfirmed
	 */
	public void persistAddressConfirmation(String phoneNumber, boolean confirm)
	{
		locationDb.persistAddressConfirmation(phoneNumber, confirm);
	}

	/**
	 * 
	 * @param phoneNumber
	 * @return Optional<LocationContext>
	 */
	public Optional<LocationContext> retrieveAddress(String phoneNumber)
	{
		return locationDb.retrieve(phoneNumber);
	}

	/**
	 * 
	 * @param clientPhoneNumber
	 * @param b
	 */
	public void persistMustEvacuate(String clientPhoneNumber, boolean b)
	{
		surveyDb.persistMustEvacuate(clientPhoneNumber, b);
	}

	/**
	 * 
	 * @param phoneNumber
	 * @param b
	 */
	public void persistInjuryConfirmation(String phoneNumber, boolean b)
	{
		surveyDb.persistInjuryConfirmation(phoneNumber, b);
	}

	/**
	 * 
	 * @param phoneNumber
	 * @param b
	 */
	public void persistHasVehicle(String phoneNumber, boolean b)
	{
		surveyDb.persistHasVehicle(phoneNumber, b);
	}

	/**
	 * 
	 * @param phoneNumber
	 * @param b
	 */
	public void persistHasSpace(String phoneNumber, boolean b)
	{
		surveyDb.persistHasSpace(phoneNumber, b);
	}

	/**
	 * 
	 * @param clientPhoneNumber
	 * @return the survey context
	 */
	public Optional<SurveyContext> retrieveSurveyContext(String clientPhoneNumber)
	{
		return surveyDb.retrieve(clientPhoneNumber);
	}

	/**
	 * 
	 * @param shelterId
	 * @param address
	 */
	public void persistShelterLocation(String shelterId, GoogleAddressInformation address)
	{
	   shelterLocationsDb.persist(shelterId, address);
	}
	
	/**
	 * 
	 * @param shelterId
	 * @param b
	 */
	public void persistShelterCanAcceptMore(String shelterId, boolean b)
	{
	   shelterLocationsDb.persistCanAcceptMore(shelterId, b);
	}
	
	/**
	 * 
	 * @param id
	 * @param coordinates
	 */
	public void persistFireLocation(String id, Coordinates coordinates)
	{
	   disasterLocationsDb.persist(id, coordinates);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public Optional<DisasterLocationContext> retrieve(String id)
	{
	   return disasterLocationsDb.retrieve(id);
	}
	
	/**
	 * 
	 * @return list of all disaster locations
	 */
	public List<DisasterLocationContext> retrieveDisasterLocations()
	{
	   return disasterLocationsDb.retrieveAll();
	}
	
	/**
	 * 
	 * @return list of all shelter locations
	 */
	public List<ShelterLocationContext> retrieveShelterLocations()
	{
	   return shelterLocationsDb.retrieveAll();
	}
	
	/**
	 * 
	 * @param phoneNumber
	 */
	public void removeChatContext(String phoneNumber)
	{
		chatDb.remove(phoneNumber);
	}

	/**
	 * 
	 * @param phoneNumber
	 */
	public void removeLocationContext(String phoneNumber)
	{
		locationDb.remove(phoneNumber);
	}

	/**
	 * 
	 * @param phoneNumber
	 */
	public void removeSurveyContext(String phoneNumber)
	{
		surveyDb.remove(phoneNumber);
	}

	/**
	 * 
	 * @param phoneNumber
	 */
	public void removePhoneNumber(String phoneNumber)
	{
		chatDb.remove(phoneNumber);
		locationDb.remove(phoneNumber);
		surveyDb.remove(phoneNumber);
	}
	
	/**
	 * 
	 * @param shelterId
	 */
	public void removeShelterLocation(String shelterId)
	{
	   shelterLocationsDb.remove(shelterId);
	}

	/**
	 * Shuts down the connection manager for this instance.
	 */
	public void shutdown()
	{
		client.shutdown();
	}
}