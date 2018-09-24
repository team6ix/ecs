/* _______________________________________________________ {COPYRIGHT-TOP} _____
 * IBM Confidential
 * IBM Bluemix Lift CLI Source Materials
 *
 * (C) Copyright IBM Corp. 2018  All Rights Reserved.
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 * _______________________________________________________ {COPYRIGHT-END} _____*/

package com.ibm.cfc.godsplan.mapbox;

import java.util.concurrent.ArrayBlockingQueue;

/**
 *
 */
public class MapboxUpdateQueue extends ArrayBlockingQueue<MapboxUpdateData>
{
   /**
    * generated Serial Version UID
    */
   private static final long serialVersionUID = -8399734377704451956L;

   private static class MapboxQueueHolder
   {
      private static final MapboxUpdateQueue INSTANCE = new MapboxUpdateQueue();

      private MapboxQueueHolder()
      {
      }
   }

   private MapboxUpdateQueue()
   {
      super(5);
   }


   /**
    * @return get an instance of this queue
    */
   public static MapboxUpdateQueue getInstance()
   {
      return MapboxQueueHolder.INSTANCE;
   }
}
