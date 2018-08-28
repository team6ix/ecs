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

package com.ibm.cfc.godsplan.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class BasicHttpClient
{
   private static int TIMEOUT = (int) TimeUnit.SECONDS.toMillis(15);
   private CredentialsProvider credsProvider = new BasicCredentialsProvider();
   private CloseableHttpClient httpClient;
   private static final Logger logger = LoggerFactory.getLogger(BasicHttpClient.class);
   private String scheme;
   private String host;
   private int port;
   private String prefix;

   /**
    * @param scheme
    * @param host
    * @param port
    * @param prefix
    * @param trustAllCerts
    * @throws HttpException
    */
   public BasicHttpClient(String scheme, String host, int port, String prefix, boolean trustAllCerts)
         throws HttpException
   {
      this.scheme = scheme;
      this.host = host;
      this.port = port;
      this.prefix = prefix;
      try
      {
         httpClient = getClient(scheme);
      }
      catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e)
      {
         logger.error("Error creating http client.", e);
         throw new HttpException();
      }
   }

   /**
    * @param scheme
    * @param host
    * @param port
    * @param trustAllCerts
    * @throws HttpException
    */
   public BasicHttpClient(String scheme, String host, int port) throws HttpException
   {
      this.scheme = scheme;
      this.host = host;
      this.port = port;
      this.prefix = null;
      try
      {
         httpClient = getClient(scheme);
      }
      catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e)
      {
         logger.error("Error creating http client.", e);
         throw new HttpException();
      }
   }

   /**
    * @param path
    * @param params 
    * @return String body of the request
    * @throws HttpException
    */
   public BasicHttpResponse executeGet(String path, Map<String,String> params) throws HttpException
   {
      try
      {
         URI uri = buildUri(path, params);
         HttpGet httpGet = new HttpGet(uri);
         httpGet.addHeader("Connection", "close");
         return httpClient.execute(httpGet, new HttpResponseHandler());
      }
      catch (IOException e)
      {
         logger.error("Error executing http request.", e);
         throw new HttpException();
      }
   }

   /**
    * @param path
    * @return String body of the request
    * @throws HttpException
    */
   public CloseableHttpResponse executeGetZip(String path, Map<String, String> params) throws HttpException
   {
      try
      {
         URI uri = buildUri(path, params);
         HttpGet httpGet = new HttpGet(uri);
         httpGet.addHeader("Connection", "close");
         return httpClient.execute(httpGet);
      }
      catch (IOException e)
      {
         logger.error("Error executing http request.", e);
         throw new HttpException();
      }
   }

   /**
    * @param path
    * @param body
    * @return String body of the request
    * @throws HttpException
    */
   public BasicHttpResponse executePut(String path, String body, Map<String, String> params) throws HttpException
   {
      try
      {
         URI uri = buildUri(path, params);
         HttpPut httpPut = new HttpPut(uri);
         httpPut.setEntity(new StringEntity(body));
         httpPut.addHeader("Accept", "application/json");
         httpPut.addHeader("Content-type", "application/json");
         httpPut.addHeader("Connection", "close");
         return httpClient.execute(httpPut, new HttpResponseHandler());
      }
      catch (IOException e)
      {
         logger.error("Error executing http request.", e);
         throw new HttpException();
      }
   }

   /**
    * @param path
    * @param body
    * @return String body of the request
    * @throws HttpException
    */
   public BasicHttpResponse executePost(String path, String body, Map<String, String> params) throws HttpException
   {
      try
      {
         URI uri = buildUri(path, params);
         HttpPost httpPost = new HttpPost(uri);
         if (body != null)
         {
            httpPost.setEntity(new StringEntity(body));
         }
         httpPost.addHeader("Accept", "application/json");
         httpPost.addHeader("Content-type", "application/json");
         httpPost.addHeader("Connection", "close");
         return httpClient.execute(httpPost, new HttpResponseHandler());
      }
      catch (IOException e)
      {
         logger.error("Error executing http request.", e);
         throw new HttpException();
      }
   }

   /**
    * @param path
    * @return String body of the request
    * @throws HttpException
    */
   public BasicHttpResponse executePost(String path, Map<String, String> params) throws HttpException
   {
      return executePost(path, null, params);
   }

   /**
    * @param path
    * @return String body of the request
    * @throws HttpException
    */
   public BasicHttpResponse executeDelete(String path, Map<String, String> params) throws HttpException
   {
      try
      {
         URI uri = buildUri(path, params);
         HttpDelete httpDelete = new HttpDelete(uri);
         httpDelete.addHeader("Connection", "close");
         return httpClient.execute(httpDelete, new HttpResponseHandler());
      }
      catch (IOException e)
      {
         logger.error("Error executing http request.", e);
         throw new HttpException();
      }
   }

   private CloseableHttpClient getClient(String targetScheme)
         throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException
   {
      CloseableHttpClient client = null;

      RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(TIMEOUT)
            .setConnectTimeout(TIMEOUT).setSocketTimeout(TIMEOUT).build();

      if (targetScheme.equalsIgnoreCase("https"))
      {
         SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (certificate, authType) -> true)
               .build();
         client = HttpClients.custom().setSSLContext(sslContext).setDefaultRequestConfig(requestConfig)
               .setDefaultCredentialsProvider(credsProvider).build();
      }
      else
      {
         client = HttpClients.custom().setDefaultRequestConfig(requestConfig)
               .setDefaultCredentialsProvider(credsProvider).build();
      }
      return client;
   }

   private URI buildUri(String path, Map<String, String> params) throws HttpException
   {
      logger.info("Building URI for path:" + path);
      List<NameValuePair> paramPairs = new ArrayList<NameValuePair>();
      for(Entry<String, String> entry : params.entrySet())
      {
         paramPairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
      }
      URI uri = null;
      try
      {
         String pathPrefix = prefix == null ? "" : prefix;
         URIBuilder builder = new URIBuilder();
         uri = builder.setScheme(scheme).setHost(host).setPort(port).setPath(pathPrefix + path).addParameters(paramPairs).build();
      }
      catch (URISyntaxException e)
      {
         throw new HttpException();
      }
      return uri;
   }

   /**
    * Custom response handler
    */
   public class HttpResponseHandler implements ResponseHandler<BasicHttpResponse>
   {
      /**
       * {@inheritDoc}
       */
      @Override
      public BasicHttpResponse handleResponse(final HttpResponse response) throws IOException
      {
         int status = response.getStatusLine().getStatusCode();
         String reason = response.getStatusLine().getReasonPhrase();
         HttpEntity entity = response.getEntity();
         String entityStr = null;
         if (entity != null)
         {
            entityStr = EntityUtils.toString(entity);
         }
         return new BasicHttpResponse(entityStr, status, reason);
      }

   }

   /**
    *
    */
   public class BasicHttpResponse
   {
      private String entity;
      private int statusCode;
      private String statusReason;

      /**
       * @param entity
       * @param statusCode
       * @param statusReason
       */
      public BasicHttpResponse(String entity, int statusCode, String statusReason)
      {
         this.entity = entity;
         this.statusCode = statusCode;
         this.statusReason = statusReason;
      }

      /**
       * @return entity string
       */
      public String getEntity()
      {
         return entity;
      }

      /**
       * @return status code
       */
      public int getStatusCode()
      {
         return statusCode;
      }

      /**
       * @return status reason
       */
      public String getStatusReason()
      {
         return statusReason;
      }
   }
}
