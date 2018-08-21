package com.ibm.cfc.godsplan.maps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.ibm.cfc.godsplan.maps.model.GoogleAddressInformation;

public class LocationMapper
{
   private static final String IMAGESIZE_DEFAULT = "800x600";
   private final String key = System.getProperty("googlekey");
   private final GeoApiContext context;
   // address, size, apikey | request centers a map and places a red pin at
   // specified locations, and creates a snapshot of specified size
   private static final String URL_GMAP_API = "https://maps.googleapis.com/maps/api/staticmap?center={0}&zoom=13&size={1}&"
         + "markers=color:red%7Clabel:S%7C{0}&key={2}";
   protected static final Logger logger = LoggerFactory.getLogger(LocationMapper.class);

   public LocationMapper()
   {
      context = new GeoApiContext.Builder().apiKey(key).build();
   }

   /**
    * Gets an array of results returning geocode data for address specified
    *
    * @param address
    *           desired address
    * @return geocode data
    * @throws ApiException
    * @throws InterruptedException
    * @throws IOException
    */
   public GeocodingResult[] getGeocodingResults(String address) throws ApiException, InterruptedException, IOException
   {
      return GeocodingApi.geocode(context, address).await();
   }

   /**
    * @param rawAddress
    *           unstructed address to look up
    * @return a List of potential formatted addresses.
    * @throws ApiException
    * @throws InterruptedException
    * @throws IOException
    */
   public List<GoogleAddressInformation> getFormattedAddress(String rawAddress)
         throws ApiException, InterruptedException, IOException
   {
      List<GoogleAddressInformation> addressesFound = new ArrayList<>();
      GeocodingResult[] results = getGeocodingResults(rawAddress);
      for (GeocodingResult result : results)
      {
         GoogleAddressInformation address = new GoogleAddressInformation(result.geometry.location.lat,
               result.geometry.location.lng, result.formattedAddress);
         addressesFound.add(address);
      }
      return addressesFound;
   }

   /**
    * Gets the longitude and latitude of specified address
    *
    * @param address
    *           desired address
    * @return latitude and longitude of address
    * @throws ApiException
    * @throws InterruptedException
    * @throws IOException
    */
   public String getGeocodingCoordinates(String address) throws ApiException, InterruptedException, IOException
   {
      GeocodingResult[] results = getGeocodingResults(address);
      LatLng coords = results[0].geometry.location;
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      return gson.toJson(coords.toUrlValue());

   }

   /**
    * Gets a google maps snapshot of the specified address and saves it to a file of the specified image size
    *
    * @param address
    *           desired address
    * @param size
    *           image size
    * @param file
    *           the file to write
    * @throws ClientProtocolException
    * @throws IOException
    */
   public void getGoogleImage(String address, String size, File file) throws ClientProtocolException, IOException
   {
      String url = getGoogleImageURI(address);

      // Make HTTP GET Request to Google maps
      try (CloseableHttpClient httpclient = HttpClients.createDefault())
      {
         HttpGet httpGet = new HttpGet(url);
         CloseableHttpResponse response1 = httpclient.execute(httpGet);

         if (!file.exists())
         {
            file.createNewFile();
         }

         writeEntityToFile(file.getAbsolutePath(), response1.getEntity());
      }
   }

   /**
    * @param address
    * @return
    */
   public String getGoogleImageURI(String address)
   {
      return getGoogleImageURI(address, IMAGESIZE_DEFAULT);
   }

   /**
    * @param address
    * @param size
    * @return
    */
   public String getGoogleImageURI(String address, String size)
   {
      // Json return gives quoted string, strip quotes
      if (address.contains("\""))
      {
         address = address.replaceAll("\"", "");
      }

      return MessageFormat.format(URL_GMAP_API, address, size, key);
   }

   private void writeEntityToFile(String fullPath, HttpEntity entity) throws IOException
   {
      try (FileOutputStream outStream = new FileOutputStream(fullPath))
      {
         entity.writeTo(outStream);

      }
      catch (IOException e)
      {
         logger.error("failed to write map image", e);
         throw e;
      }
   }
}
