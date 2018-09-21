package com.ibm.cfc.godsplan.rest;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ibm.cfc.godsplan.cloudant.CloudantPersistence;
import com.ibm.cfc.godsplan.cloudant.Coordinates;
import com.ibm.cfc.godsplan.mapbox.MapboxClient;
import com.ibm.cfc.godsplan.maps.LocationMapper;

@WebServlet("/disaster")
public class DisasterApi extends HttpServlet
{
   private static final long serialVersionUID = 1L;
   protected static final Logger logger = LoggerFactory.getLogger(DisasterApi.class);
   private static LocationMapper mapper = new LocationMapper();
   private static MapboxClient mapboxClient = new MapboxClient();

   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
   {
      logger.info("POST request: {}", request);
      Instant startTime = Instant.now();
      CloudantPersistence metadata = new CloudantPersistence();
      String id = UUID.randomUUID().toString();

      Coordinates coordinates = parseRequestCoordinates(request);

      try
      {
         addPointCloudant(coordinates, metadata, id);
         logger.info("doPut ran in {} seconds", Duration.between(startTime, Instant.now()).getSeconds());
      }
      catch (Exception e)
      {
         logger.error("Uncaught Exception", e);
         throw e;
      }
      mapboxClient.addDisaster(id, coordinates.getLongitude(), coordinates.getLatitude());
   }

   private void addPointCloudant(Coordinates coordinates, CloudantPersistence metadata, String id)
   {
      metadata.persistFireLocation(id, coordinates);
   }

   private Coordinates parseRequestCoordinates(HttpServletRequest request)
   {
      double latitude = Double.parseDouble(request.getParameter("latitude"));
      double longitude = Double.parseDouble(request.getParameter("longitude"));
      logger.info("Coordinates: '{0},{1}'", latitude, longitude);
      return new Coordinates(latitude, longitude);
   }

}
