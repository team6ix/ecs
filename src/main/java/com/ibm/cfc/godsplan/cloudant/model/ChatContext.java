package com.ibm.cfc.godsplan.cloudant.model;

import com.ibm.watson.developer_cloud.assistant.v1.model.Context;

/**
 * POJO for chatcontext database documents
 *
 */
public class ChatContext
{
   private final Context context;
   private final String _id;

   public ChatContext(Context watsonContext, String phoneNumber)
   {
      super();
      this.context = watsonContext;
      this._id = phoneNumber;
   }

   public Context getWatsonContext()
   {
      return context;
   }

   public String getPhoneNumber()
   {
      return _id;
   }
}
