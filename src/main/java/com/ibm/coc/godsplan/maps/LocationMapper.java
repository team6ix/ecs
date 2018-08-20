package com.ibm.coc.godsplan.maps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;

public class LocationMapper {
	private String key = System.getenv("googlekey");
	private GeoApiContext context;
	// address, size, apikey | request centers a map and places a red pin at
	// specified locations, and creates a snapshot of specified size
	private String requestUrlNoMarkers = "https://maps.googleapis.com/maps/api/staticmap?center={0}&zoom=13&size={1}&"
			+ "markers=color:red%7Clabel:S%7C{0}&key={2}";

	public LocationMapper() {
		context = new GeoApiContext.Builder().apiKey(key).build();
	}

	/**
	 * Gets an array of results returning geocode data for address specified
	 * 
	 * @param address
	 *            desired address
	 * @return geocode data
	 * @throws ApiException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public GeocodingResult[] getGeocodingResults(String address)
			throws ApiException, InterruptedException, IOException {
		return GeocodingApi.geocode(context, address).await();
	}

	/**
	 * Gets the longitude and latitude of specified address
	 * 
	 * @param address
	 *            desired address
	 * @return latitude and longitude of address
	 * @throws ApiException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public String getGeocodingCoordinates(String address) throws ApiException, InterruptedException, IOException {
		GeocodingResult[] results = getGeocodingResults(address);
		LatLng coords = results[0].geometry.location;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(coords.toUrlValue());

	}

	/**
	 * Gets a google maps snapshot of the specified address and saves it to a file
	 * of the specified image size
	 * 
	 * @param address
	 *            desired address
	 * @param size
	 *            image size
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public void getGoogleImage(String address, String size) throws ClientProtocolException, IOException {

		// Json return gives quoted string, strip quotes
		if (address.contains("\"")) {
			address = address.replaceAll("\"", "");
		}

		// Make HTTP GET Request to Google maps
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			String url = MessageFormat.format(requestUrlNoMarkers, address, size, key);

			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response1 = httpclient.execute(httpGet);

			File file = new File("./src/main/resources/googleimage");
			file.createNewFile();

			writeEntityToFile(file.getAbsolutePath(), response1.getEntity());
		}
	}

	private void writeEntityToFile(String fullPath, HttpEntity entity) {
		try (FileOutputStream outStream = new FileOutputStream(fullPath)) {
			entity.writeTo(outStream);

		} catch (IOException e) {
			//
		}
	}
}
