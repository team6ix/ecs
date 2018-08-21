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
   private Optional<String> mediaURI;
   private final String response;

   public QueryResponse(String response)
   {
      super();
      this.response = response;
   }

   public Optional<String> getMediaURI()
   {
      return mediaURI;
   }

   public void setMediaURI(Optional<String> mediaURI)
   {
      this.mediaURI = mediaURI;
   }

   public String getResponse()
   {
      return response;
   }

}
