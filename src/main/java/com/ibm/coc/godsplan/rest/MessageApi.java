package com.ibm.coc.godsplan.rest;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.twilio.twiml.MessagingResponse;
import com.twilio.twiml.messaging.Body;
import com.twilio.twiml.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class
 */
@WebServlet("/message")
public class MessageApi extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected static final Logger logger = LoggerFactory.getLogger(MessageApi.class);

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		StringBuilder jb = new StringBuilder();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
			logger.info("Received request with body " + jb.toString());
		} catch (IOException e) {
			logger.error("Could not retrieve body from request.");
		}

		response.setContentType("application/xml");
		Body body = new Body.Builder(jb.toString()).build();
		Message sms = new Message.Builder().body(body).build();
		MessagingResponse twiml = new MessagingResponse.Builder().message(sms).build();

		response.getWriter().write(twiml.toXml());
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html");
		response.getWriter().print("Saving the world.");
	}

}