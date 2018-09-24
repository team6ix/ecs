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

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 0 argument constructor
 */
public class MapboxUpdateThread implements Callable<Void>
{

   private static final int TASK_QUEUE_POLL_WAIT_TIME = 10;
   private static final Logger logger = LoggerFactory.getLogger(MapboxUpdateThread.class);

   /**
    *
    */
   public MapboxUpdateThread()
   {
      super();
   }

   /**
    * (non-Javadoc)
    *
    * @see java.util.concurrent.Callable#call()
    */
   @Override
   public Void call()
   {
      ArrayBlockingQueue<MapboxUpdateData> queue = MapboxUpdateQueue.getInstance();
      boolean isRunning = true;

      while (isRunning)
      {
         try
         {
            MapboxUpdateData data = queue.poll(TASK_QUEUE_POLL_WAIT_TIME, TimeUnit.SECONDS);
            if(data!=null)
            {
               MapboxClient.updateMap(data.getDataset(), data.getTileset(), data.getTilesetName());
            }
         }
         catch (InterruptedException ie)
         {
            logger.warn("Task Thread shutdown, Thread interrupted exception caught.", ie);
            isRunning = false;
            Thread.currentThread().interrupt();
         }
      }
      return null;
   }
}
