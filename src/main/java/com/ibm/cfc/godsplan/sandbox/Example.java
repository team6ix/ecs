package com.ibm.cfc.godsplan.sandbox;

import java.io.IOException;
import java.util.Optional;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.ibm.cfc.godsplan.assistant.WatsonAssistantBot;
import com.ibm.cfc.godsplan.maps.LocationMapper;
import com.twilio.Twilio;

public class Example
{
   // Find your Account Sid and Token at twilio.com/user/account
   public static final String ACCOUNT_SID = "AC5e443ddc79e385a091623984e1903757";
   public static final String AUTH_TOKEN = "d99f21bfd7f3b13960356520153b806d";
   public static final String CLIENT_PHONE_NUMBER = "+14162093379";
   public static final String SERVER_PHONE_NUMBER = "+16476973928";

   public static void main(String[] args) throws ApiException, InterruptedException, IOException
   {
      Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

      WatsonAssistantBot watson = new WatsonAssistantBot();
      String resp = watson.sendAssistantMessage(Optional.empty(), Optional.empty());

      // Message message = Message
      // .creator(new PhoneNumber(CLIENT_PHONE_NUMBER), new
      // PhoneNumber(SERVER_PHONE_NUMBER), resp).create();
      //
      // System.out.println(message.getSid());

      String address = "8200 Warden Ave,Unionville,ON";
      LocationMapper mapper = new LocationMapper();
      GeocodingResult[] result = mapper.getGeocodingResults(address);

      System.out.println(result[0].formattedAddress);

      String coords = mapper.getGeocodingCoordinates(address);

      mapper.getGoogleImage(coords, "600x800");
   }
}