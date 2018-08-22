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

package com.ibm.cfc.godsplan.rest;

import java.util.Optional;

public class QueryResponse
{
   private final Optional<String> mediaURI;
   private final String response;

   public QueryResponse(String response)
   {
      this(response, Optional.empty());
   }

   public QueryResponse(String response, Optional<String> mediaURI)
   {
      this.response = response;
      this.mediaURI = mediaURI;
   }

   public Optional<String> getMediaURI()
   {
      return mediaURI;
   }

   public String getResponse()
   {
      return response;
   }

}
