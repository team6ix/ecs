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

package com.ibm.cfc.godsplan.mapbox;

import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ibm.cfc.godsplan.http.BasicHttpClient;
import com.ibm.cfc.godsplan.http.BasicHttpClient.BasicHttpResponse;

/**
 *
 */
public class MapboxClient
{

   protected static final Logger logger = LoggerFactory.getLogger(MapboxClient.class);
   /***/
   public static final String MAPBOX_URL = "api.mapbox.com";
   /***/
   public static final int MAPBOX_PORT = 80;
   /***/
   public static final String MAPBOX_API_TOKEN = System.getenv("MAPBOX_API_TOKEN");
   /***/
   public static BasicHttpClient httpClient;

   /***/
   public MapboxClient()
   {
      try
      {
         httpClient = new BasicHttpClient("http", MAPBOX_URL, MAPBOX_PORT);
      }
      catch (HttpException e)
      {
         logger.error("Error creating connection to Mapbox.", e);
      }
   }
   
   /**
    * @return JSON Response from dataset command
    * @throws HttpException
    */
   public String listDatasets() throws HttpException
   {
      Map<String, String> hashMap = new HashMap<String, String>()
      {{
           put("access_token", MAPBOX_API_TOKEN);
      }};
      BasicHttpResponse httpResponse = httpClient.executeGet("/datasets/v1/team6ix", hashMap);
      return httpResponse.getEntity();
   }
   
   public static void main(String args[]) throws HttpException
   {
      MapboxClient client = new MapboxClient();
      System.out.println(client.listDatasets());
   }
   
}
