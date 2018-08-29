import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import com.ibm.cfc.godsplan.cloudant.CloudantPersistence;
import com.ibm.cfc.godsplan.cloudant.Coordinates;

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

/**
 * Reads a list of fire coordinates from a file and persists them to cloudant. 
 * 
 * The file has a list of line separated coordinates (latitude,longitude), e.g.:
 * 
 * 43.774642,-79.364945
 * 43.771667,-79.345041
 */
public class FireCoordinates
{
   /**
    * Main method
    * 
    * @param args
    * @throws IOException
    */
   public static void main(String[] args) throws IOException
   {
      if (args.length < 1)
      {
         System.out.println("Missing required argument <fire coordinates filename>.");
         return;
      }
   
      FireCoordinates coords = new FireCoordinates();
      coords.addFiresToCloudant(args[0]);
   }
   
   
   CloudantPersistence cloudant;
   
   /**
    * Constructor. Initializes the Cloudant client.
    */
   public FireCoordinates()
   {
      cloudant = new CloudantPersistence();
   }
   
   /**
    * persists coordinates to cloudant
    * @param filename
    * @throws IOException
    */
   public void addFiresToCloudant(String filename) throws IOException
   {
      URL url = createURL(this.getClass(), filename);
      File file = createFile(url);
      List<String> fireCoords = FileUtils.readLines(file);
      
      int id = 0;
      for (String coords : fireCoords)
      {
         String[] temp = coords.split(",");
         String latitude = temp[0].trim();
         String longitude = temp[1].trim();
         Coordinates fireCoordinates = new Coordinates(Double.parseDouble(latitude),Double.parseDouble(longitude));
         cloudant.persistFireLocation(id, fireCoordinates);
         id++;
      }
   }
   
   private <T> URL createURL(Class<T> clazz, String filename)
   {
      URL url = clazz.getResource(filename);
      if (url == null)
      {
         Assert.fail("Could not find '" + filename + "'");
      }
      return url;
   }

   private File createFile(URL url)
   {
      File logFile = new File(url.getFile());
      if (!logFile.exists())
      {
         Assert.fail("The file '" + url.getFile() + "' does not exist.");
      }
      return logFile;
}
}
