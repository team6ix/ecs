package com.ibm.cfc.godsplan.rest;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.maps.errors.ApiException;
import com.ibm.cfc.godsplan.assistant.WatsonAssistantBot;
import com.ibm.cfc.godsplan.cloudant.CloudantPersistence;
import com.ibm.cfc.godsplan.cloudant.model.ChatContext;
import com.ibm.cfc.godsplan.cloudant.model.DisasterLocationContext;
import com.ibm.cfc.godsplan.cloudant.model.LocationContext;
import com.ibm.cfc.godsplan.cloudant.model.ShelterLocationContext;
import com.ibm.cfc.godsplan.disaster.DisasterProximityCalculator;
import com.ibm.cfc.godsplan.mapbox.MapboxClient;
import com.ibm.cfc.godsplan.mapbox.model.DirectionInformation;
import com.ibm.cfc.godsplan.maps.LocationMapper;
import com.ibm.cfc.godsplan.maps.model.GoogleAddressInformation;
import com.ibm.watson.developer_cloud.assistant.v1.model.Context;
import com.ibm.watson.developer_cloud.assistant.v1.model.InputData;
import com.mapbox.geojson.Point;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Media;
import com.twilio.twiml.messaging.Message;
import com.twilio.twiml.messaging.Message.Builder;

/**
 * Servlet implementation class
 */
@WebServlet("/message")
public class MessageApi extends HttpServlet
{
   private static final long serialVersionUID = 1L;
   protected static final Logger logger = LoggerFactory.getLogger(MessageApi.class);
   private static LocationMapper mapper = new LocationMapper();
   private static MapboxClient mapboxClient = new MapboxClient();

   /**
    * @throws IOException
    * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
    */
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
   {
      logger.trace("POST request: {}", request);
      Instant startTime = Instant.now();
      CloudantPersistence metadata = new CloudantPersistence();
      WatsonAssistantBot bot = new WatsonAssistantBot();

      Optional<String> smsTxtBody = parseUserInput(request);
      Optional<String> smsPhoneNumber = parsePhoneNumber(request);
      validateInput(smsTxtBody, smsPhoneNumber);
      try
      {
         QueryResponse queryResponse = processQuery(metadata, bot, smsTxtBody.get(), smsPhoneNumber.get());
         String twiml = generateTwiml(queryResponse.getResponse(), queryResponse.getMediaURI());
         sendTwimlResponse(response, twiml);

         logger.info("doPost ran in {} seconds", Duration.between(startTime, Instant.now()).getSeconds());
      }
      catch (Exception e)
      {
         logger.error("Uncaught Exception, clearing user metadata", e);
         if (smsTxtBody.isPresent())
         {
            clearMetadata(metadata, smsTxtBody.get());
         }
         throw e;
      }
   }

   private QueryResponse processQuery(CloudantPersistence metadata, WatsonAssistantBot bot, String smsTxtBody,
         String smsPhoneNumber)
   {
      QueryResponse response;
      if (isClearMetadata(smsTxtBody))
      {
         clearMetadata(metadata, smsPhoneNumber);
         response = new QueryResponse("Cleared persisted context", Optional.empty());
      }
      else
      {
         Optional<Context> persistedContext = getPersistedContext(smsPhoneNumber, metadata);
         response = queryWatson(bot, smsTxtBody, smsPhoneNumber, metadata, persistedContext);

      }
      logger.info("Returning the response to user: {}", response.getResponse());
      return response;
   }

   private void clearMetadata(CloudantPersistence metadata, String phoneNumber)
   {
      logger.info("Clearing context for phone number : '{}'", phoneNumber);
      metadata.removePhoneNumber(phoneNumber);
   }

   private void validateInput(Optional<String> smsTxtBody, Optional<String> smsPhoneNumber) throws IOException
   {
      if (!smsTxtBody.isPresent() || !smsPhoneNumber.isPresent())
      {
         if (!smsTxtBody.isPresent())
         {
            logger.error("smsTxtBody not present");
         }
         if (!smsPhoneNumber.isPresent())
         {
            logger.error("smsPhoneNumber not present");
         }
         throw new IOException("failed to parse sms body or sms phone number.");
      }
   }

   private Optional<String> parsePhoneNumber(HttpServletRequest request)
   {
      Optional<String> textPhoneNumber = Optional.ofNullable(request.getParameter("From"));
      logger.info("Text number: '{}'", textPhoneNumber);
      return textPhoneNumber;
   }

   private void sendTwimlResponse(HttpServletResponse response, String twiml) throws IOException
   {
      try
      {
         response.setContentType("application/xml");
         response.getWriter().write(twiml);
      }
      catch (IOException ioe)
      {
         logger.error("Failed to send Twiml response", ioe);
         throw ioe;
      }
   }

   private String generateTwiml(String input, Optional<String> mediaURI)
   {
      Body body = new Body.Builder(input).build();
      Builder builder = new Message.Builder().body(body);
      if (mediaURI.isPresent())
      {
         Media media = new Media.Builder(mediaURI.get()).build();
         builder.media(media);
      }
      Message msg = builder.build();
      MessagingResponse twiml = new MessagingResponse.Builder().message(msg).build();
      return twiml.toXml();
   }

   private QueryResponse queryWatson(WatsonAssistantBot bot, String smsTxtBody, String userPhoneNumber,
         CloudantPersistence metadata, Optional<Context> persistedContext)
   {
      Optional<InputData> input = Optional.of(new InputData.Builder(smsTxtBody).build());
      Optional<String> mediaURI = Optional.empty();
      ResponsePosition position = getPosition(persistedContext);
      logger.info("Conversation position '{}' retrieved for number '{}'", position.toString(), userPhoneNumber);
      String watsonResponse = bot.sendAssistantMessage(persistedContext, input);
      persistContext(userPhoneNumber, bot.getLastContext(), metadata);

      return getQueryResponse(smsTxtBody, userPhoneNumber, metadata, mediaURI, position, watsonResponse);
   }

   private QueryResponse getQueryResponse(String smsTxtBody, String userPhoneNumber, CloudantPersistence metadata,
         Optional<String> mediaURI, ResponsePosition position, String watsonResponse)
   {
      QueryResponse response;
      if (position.equals(ResponsePosition.ADDRESS_INPUT))
      {
         response = getAddressResponse(smsTxtBody, userPhoneNumber, metadata, mediaURI, watsonResponse);
      }
      else if (position.equals(ResponsePosition.ADDRESS_CONFIRMATION))
      {
         response = getAddressConfirmationResponse(isPositiveConfirmation(smsTxtBody), userPhoneNumber, metadata,
               watsonResponse);
      }
      else if (position.equals(ResponsePosition.INJURY_CONFIRMATION))
      {
         response = confirmResponse(isPositiveConfirmation(smsTxtBody), userPhoneNumber, metadata, watsonResponse,
               ResponsePosition.INJURY_CONFIRMATION);
      }
      else if (position.equals(ResponsePosition.ABLE_TO_EVACUATE))
      {
         logger.info("Able to evacuate endpoint reached sending directions to {}", userPhoneNumber);
         response = getDirectionsResponse(userPhoneNumber, metadata, mediaURI, watsonResponse);
         //         response = confirmResponse(isPositiveConfirmation(smsTxtBody), userPhoneNumber, metadata, watsonResponse,
         //               ResponsePosition.ABLE_TO_EVACUATE);
      }
      else if (position.equals(ResponsePosition.ABLE_TO_EVACUATE_CONFIRMATION))
      {
         logger.info("Able to evacuate endpoint reached sending directions to {}", userPhoneNumber);
         response = getDirectionsResponse(userPhoneNumber, metadata, mediaURI, watsonResponse);
      }
      else if (position.equals(ResponsePosition.NO_VEHICLE_ENDPOINT))
      {
         logger.info("No vehicle endpoint reached sending directions to {}", userPhoneNumber);
         response = getDirectionsResponse(userPhoneNumber, metadata, mediaURI, watsonResponse);
      }
      else if (position.equals(ResponsePosition.VEHICLE_WITH_SPACE_ENDPOINT))
      {
         logger.info("Vehicle with space endpoint received sending directions to {}", userPhoneNumber);
         response = getDirectionsResponse(userPhoneNumber, metadata, mediaURI, watsonResponse);
      }
      else
      {
         response = new QueryResponse(watsonResponse, mediaURI);
      }
      logger.info("Survey Context: " + metadata.retrieveSurveyContext(userPhoneNumber).toString());
      return response;
   }

   private QueryResponse getAddressConfirmationResponse(boolean isPositiveConfirmation, String userPhoneNumber,
         CloudantPersistence metadata, String watsonResponse)
   {
      QueryResponse response;
      response = confirmResponse(isPositiveConfirmation, userPhoneNumber, metadata, watsonResponse,
            ResponsePosition.ADDRESS_CONFIRMATION);
      Optional<LocationContext> userAddress = metadata.retrieveAddress(userPhoneNumber);
      if (isPositiveConfirmation)
      {
         if (userAddress.isPresent())
         {
            if (isUserOutsideDisasterZone(isPositiveConfirmation, metadata, userAddress.get()))
            {
               clearMetadata(metadata, userPhoneNumber);
               mapboxClient.updatePerson(userPhoneNumber, MapboxClient.Severity.LOWEST.getValue());
               response = new QueryResponse(
                     "You are not in immediate danger. Please keep safe and stay in your location. Respond back if your location changes.");
            }
            else
            {
               metadata.persistMustEvacuate(userPhoneNumber, true);
            }
         }
         else
         {
            logger.error("User sent confirmation but address was not found in metadata, clearing user metadata");
            clearMetadata(metadata, userPhoneNumber);
            response = new QueryResponse("Something went wrong, please reply to start over again");
         }
      }
      return response;
   }

   private boolean isUserOutsideDisasterZone(boolean isPositiveConfirmation, CloudantPersistence metadata,
         LocationContext userAddress)
   {
      return !(isPositiveConfirmation && isUserInDisasterZone(metadata, userAddress));
   }

   private boolean isUserInDisasterZone(CloudantPersistence metadata, LocationContext userLocationContext)
   {
      List<Point> disasterPoints = getDisasterPoints(metadata);
      Point userPoint = getUserLocationPoint(userLocationContext);
      DisasterProximityCalculator calc = new DisasterProximityCalculator(userPoint);
      boolean isInDisasterZone = false;
      for (Point disasterPoint : disasterPoints)
      {
         isInDisasterZone = isInDisasterZone || calc.isPointWithinDisasterZone(userPoint, disasterPoint);
      }
      return isInDisasterZone;
   }

   private Point getUserLocationPoint(LocationContext userAddress)
   {
      return Point.fromLngLat(userAddress.getAddress().getLongitude(), userAddress.getAddress().getLatitude());
   }

   private List<Point> getDisasterPoints(CloudantPersistence metadata)
   {
      List<Point> disasterPoints = new ArrayList<>();
      List<DisasterLocationContext> disasterLocations = metadata.retrieveDisasterLocations();
      for (DisasterLocationContext location : disasterLocations)
      {
         disasterPoints.add(
               Point.fromLngLat(location.getCoordinates().getLongitude(), location.getCoordinates().getLatitude()));
      }
      return disasterPoints;
   }

   private ShelterLocationContext getNearestShelterLocation(CloudantPersistence metadata,
         LocationContext userLocationContext)
   {
      List<ShelterLocationContext> shelterLocations = metadata.retrieveShelterLocations();
      Point userPoint = getUserLocationPoint(userLocationContext);
      DisasterProximityCalculator calc = new DisasterProximityCalculator(userPoint);

      double distance = Double.MAX_VALUE;
      ShelterLocationContext closestShelter = null;
      for (ShelterLocationContext shelter : shelterLocations)
      {
         Point shelterPoint = Point.fromLngLat(shelter.getLocation().getLongitude(),
               shelter.getLocation().getLatitude());
         double newDistance = calc.distance(userPoint, shelterPoint);
         if (newDistance < distance)
         {
            distance = newDistance;
            closestShelter = shelter;
         }
      }
      return closestShelter;
   }

   private QueryResponse confirmResponse(boolean confirmed, String userPhoneNumber, CloudantPersistence metadata,
         String watsonResponse, ResponsePosition position)
   {
      String response = watsonResponse;
      persistResponse(confirmed, position, metadata, userPhoneNumber);

      return new QueryResponse(response);

   }

   private boolean isPositiveConfirmation(String smsTxtBody)
   {
      boolean confirmed;
      if (smsTxtBody.trim().equalsIgnoreCase("yes"))
         confirmed = true;
      else if (smsTxtBody.trim().equalsIgnoreCase("no"))
         confirmed = false;
      else
      {
         logger.error("Unrecognized response '{}'", smsTxtBody);
         confirmed = false;
      }
      return confirmed;
   }

   private void persistResponse(boolean confirmation, ResponsePosition position, CloudantPersistence metadata,
         String userPhoneNumber)
   {
      if (position.equals(ResponsePosition.ADDRESS_CONFIRMATION))
      {
         metadata.persistAddressConfirmation(userPhoneNumber, confirmation);
      }
      else if (position.equals(ResponsePosition.INJURY_CONFIRMATION))
      {
         metadata.persistInjuryConfirmation(userPhoneNumber, confirmation);
      }
      else if (position.equals(ResponsePosition.ABLE_TO_EVACUATE))
      {
         metadata.persistHasVehicle(userPhoneNumber, confirmation);
      }
      else if (position.equals(ResponsePosition.ABLE_TO_EVACUATE_CONFIRMATION))
      {
         metadata.persistHasSpace(userPhoneNumber, confirmation);
      }
      else if (position.equals(ResponsePosition.NO_VEHICLE_ENDPOINT)
            || position.equals(ResponsePosition.VEHICLE_WITH_SPACE_ENDPOINT))
      {
         int severity = MapboxClient.generateSeverity(metadata.retrieveSurveyContext(userPhoneNumber).get());
         mapboxClient.updatePerson(userPhoneNumber, severity);
      }
   }

   private QueryResponse getAddressResponse(String smsTxtBody, String userPhoneNumber, CloudantPersistence metadata,
         Optional<String> mediaURI, String watsonResponse)
   {
      String response = watsonResponse;
      List<GoogleAddressInformation> addressInfo = getAddressDetail(smsTxtBody);
      if (addressInfo.size() == 1)
      {
         GoogleAddressInformation addressInfoElement = addressInfo.get(0);
         metadata.persistAddress(userPhoneNumber, addressInfoElement);
         mapboxClient.addPerson(userPhoneNumber, addressInfoElement.getLongitude(), addressInfoElement.getLatitude());
         String formattedAddress = addressInfoElement.getFormattedAddress().trim();
         mediaURI = Optional.of(mapper.getGoogleImageURI(formattedAddress, Optional.empty()));
         response += " [" + formattedAddress + "]";
      }
      else
      {
         // TODO if 0 or more than 1 returned we need to ask for more information.
         logger.error(
               "multiple addresses returned for user input, need to query for more precise location. Address info '{}'",
               addressInfo);
      }
      return new QueryResponse(response, mediaURI);
   }

   private QueryResponse getDirectionsResponse(String userPhoneNumber, CloudantPersistence metadata,
         Optional<String> mediaURI, String watsonResponse)
   {
      String response = watsonResponse;
      Optional<LocationContext> location = metadata.retrieveAddress(userPhoneNumber);
      String formattedLocation = location.get().getAddress().getFormattedAddress();
      ShelterLocationContext shelter = getNearestShelterLocation(metadata, location.get());
      Point origin = getUserLocationPoint(location.get());
      Point destination = Point.fromLngLat(shelter.getLocation().getLongitude(), shelter.getLocation().getLatitude());

      logger.info("Plotting directions from {} to {}", formattedLocation, shelter.getLocation().getFormattedAddress());
      DirectionInformation info = mapper.getDirectionInformation(origin, destination, Optional.empty(),
            Optional.empty());

      response += "[Directions to Shelter: ";
      for (String direction : info.getDirections())
      {
         response += direction;
      }
      response += "]";
      mediaURI = Optional.of(mapper.getGoogleImageURI(formattedLocation, Optional.of(info)));

      return new QueryResponse(response, mediaURI);
   }

   private ResponsePosition getPosition(Optional<Context> persistedContext)
   {
      ResponsePosition pos = ResponsePosition.OTHER;
      if (persistedContext.isPresent())
      {
         Context context = persistedContext.get();
         Object dialogStack = context.getSystem().get("dialog_stack");
         if (dialogStack != null)
         {
            String nodeID = dialogStack.toString();
            pos = ResponsePosition.getPosition(nodeID);
         }
      }
      return pos;
   }

   private List<GoogleAddressInformation> getAddressDetail(String smsTxtBody)
   {
      List<GoogleAddressInformation> addressInfo = new ArrayList<>();
      try
      {
         addressInfo = mapper.getFormattedAddress(smsTxtBody);
      }
      catch (ApiException | InterruptedException | IOException e)
      {
         logger.error("Formatted Address query failed", e);
      }
      return addressInfo;
   }

   private void persistContext(String userPhoneNumber, Optional<Context> responseContext, CloudantPersistence metadata)
   {
      if (responseContext.isPresent())
      {
         metadata.persistChatContext(userPhoneNumber, responseContext.get());
      }
   }

   private Optional<Context> getPersistedContext(String phoneNumber, CloudantPersistence metadata)
   {
      Optional<ChatContext> chatContext = metadata.retrieveChatContext(phoneNumber);
      Optional<Context> context;
      if (chatContext.isPresent())
      {
         context = Optional.ofNullable(chatContext.get().getWatsonContext());
      }
      else
      {
         context = Optional.empty();
      }
      return context;
   }

   private Optional<String> parseUserInput(HttpServletRequest request)
   {
      Optional<String> textBody = Optional.ofNullable(request.getParameter("Body"));
      logger.info("Text body: '{}'", textBody);
      return textBody;
   }

   private boolean isClearMetadata(String smsText)
   {
      return smsText.trim().equalsIgnoreCase("Clear");
   }

   /**
    * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      response.setContentType("text/html");
      response.getWriter().print("Saving the world.");
   }

}