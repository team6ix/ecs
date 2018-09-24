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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import com.ibm.cfc.godsplan.mapbox.MapboxUpdateThread;

/**
 *
 */
@WebListener
public class ServerStartupListener implements ServletContextListener
{
   private static final ExecutorService executor = Executors.newFixedThreadPool(1);
   private static final List<Future<Void>> futures = new ArrayList<>();


   /**
    * (non-Javadoc)
    *
    * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
    */
   @Override
   public void contextInitialized(ServletContextEvent context)
   {
      futures.add(executor.submit(new MapboxUpdateThread()));
   }


   @Override
   public void contextDestroyed(ServletContextEvent sce)
   {
   }

}
