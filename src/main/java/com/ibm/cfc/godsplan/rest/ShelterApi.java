package com.ibm.cfc.godsplan.rest;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.maps.errors.ApiException;
import com.ibm.cfc.godsplan.cloudant.CloudantPersistence;
import com.ibm.cfc.godsplan.mapbox.MapboxClient;
import com.ibm.cfc.godsplan.maps.LocationMapper;
import com.ibm.cfc.godsplan.maps.model.GoogleAddressInformation;

@WebServlet("/shelter")
public class ShelterApi extends HttpServlet
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

		Optional<String> address = parseAddress(request);
		List<GoogleAddressInformation> addressInfo = getAddressDetail(address.get());

		GoogleAddressInformation addressInfoElement = addressInfo.get(0);

		try
		{
			addPointCloudant(addressInfoElement, metadata, id);
			logger.info("doPut ran in {} seconds", Duration.between(startTime, Instant.now()).getSeconds());
		}
		catch (Exception e)
		{
			logger.error("Uncaught Exception", e);
			throw e;
		}
      mapboxClient.addShelter(id, addressInfoElement.getLongitude(), addressInfoElement.getLatitude());
	}

	private void addPointCloudant(GoogleAddressInformation address, CloudantPersistence metadata, String id)
	{
		metadata.persistShelterLocation(id, address);

	}

	private List<GoogleAddressInformation> getAddressDetail(String smsTxtBody)
	{
		List<GoogleAddressInformation> addressInfo = new ArrayList<>();
		try
		{
			addressInfo = mapper.getFormattedAddress(smsTxtBody);
		}
		catch (ApiException | InterruptedException | IOException e)
		{
			logger.error("Formatted Address query failed", e);
		}
		return addressInfo;
	}

	private Optional<String> parseAddress(HttpServletRequest request)
	{
		Optional<String> textBody = Optional.ofNullable(request.getParameter("address"));
		logger.info("Text body: '{}'", textBody);
		return textBody;
	}

}
