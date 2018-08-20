package com.ibm.cfc.godsplan.sandbox;

import java.util.Optional;
import com.ibm.cfc.godsplan.assistant.WatsonAssistantBot;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class Example
{
   // Find your Account Sid and Token at twilio.com/user/account
   public static final String ACCOUNT_SID = "AC5e443ddc79e385a091623984e1903757";
   public static final String AUTH_TOKEN = "d99f21bfd7f3b13960356520153b806d";
   public static final String CLIENT_PHONE_NUMBER = "+14162093379";
   public static final String SERVER_PHONE_NUMBER = "+16476973928";

   public static void main(String[] args)
   {
      Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

      WatsonAssistantBot watson = new WatsonAssistantBot();
      String resp = watson.sendAssistantMessage(Optional.empty(), Optional.empty());

      Message message = Message
            .creator(new PhoneNumber(CLIENT_PHONE_NUMBER), new PhoneNumber(SERVER_PHONE_NUMBER), resp).create();

      System.out.println(message.getSid());
   }
}