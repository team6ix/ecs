package com.ibm.cfc.godsplan.sandbox;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import com.google.maps.errors.ApiException;
import com.ibm.cfc.godsplan.assistant.WatsonAssistantBot;
import com.ibm.cfc.godsplan.cloudant.CloudantPersistence;
import com.ibm.cfc.godsplan.cloudant.model.LocationContext;
import com.ibm.cfc.godsplan.maps.LocationMapper;
import com.ibm.cfc.godsplan.maps.model.GoogleAddressInformation;
import com.twilio.Twilio;

public class Example
{
   // Find your Account Sid and Token at twilio.com/user/account
   public static final String ACCOUNT_SID = "AC5e443ddc79e385a091623984e1903757";
   public static final String AUTH_TOKEN = "d99f21bfd7f3b13960356520153b806d";
   public static final String CLIENT_PHONE_NUMBER = "+14162093379";
   public static final String SERVER_PHONE_NUMBER = "+16476973928";

   public static void main(String[] args) throws ApiException, InterruptedException, IOException, URISyntaxException
   {
      Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

      WatsonAssistantBot watson = new WatsonAssistantBot();
      watson.sendAssistantMessage(Optional.empty(), Optional.empty());

      // Message message = Message
      // .creator(new PhoneNumber(CLIENT_PHONE_NUMBER), new
      // PhoneNumber(SERVER_PHONE_NUMBER), resp).create();
      //
      // System.out.println(message.getSid());

      String rawAddress = "8200 Warden Ave";
      LocationMapper mapper = new LocationMapper();
      List<GoogleAddressInformation> addresses = mapper.getFormattedAddress(rawAddress);
      for (GoogleAddressInformation address : addresses)
      {
         System.out.println(address);
      }

      String coords = mapper.getGeocodingCoordinates(rawAddress);
      File image = File.createTempFile("map_", ".png");
      mapper.getGoogleImage(coords, "600x800", image);
      System.out.println("Map of '" + rawAddress + "' can be found at '" + image.getAbsolutePath() + "'");

//      CloudantPersistence p = new CloudantPersistence();
//      p.removePhoneNumber(CLIENT_PHONE_NUMBER);
//      p.persistAddress(CLIENT_PHONE_NUMBER, addresses.get(0));
//      Optional<LocationContext> addressInfo = p.retrieveAddress(CLIENT_PHONE_NUMBER);
//      System.out.println(addressInfo.get().getAddress().toString());
//      
//      p.persistAddressConfirmation(CLIENT_PHONE_NUMBER, true);
//      p.shutdown();
     
      //      String imageURI = mapper.getGoogleImageURI(addresses.get(0).getFormattedAddress());
      //      String body = "Here is a map of your location";
      //      Message.creator(new PhoneNumber(CLIENT_PHONE_NUMBER), new PhoneNumber(SERVER_PHONE_NUMBER), body)
      //            .setMediaUrl(Promoter.listOfOne(URI.create(imageURI))).create();

   }
}