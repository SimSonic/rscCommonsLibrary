package ru.simsonic.rscCommonsLibrary;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

public class GenericWebConnection
{
	public static <T> T webExecuteObj(String url, Object payload, Class<T> responseClass, Map<String, String> headers) throws IOException
	{
		final Gson gson = new Gson();
		final String jsonPayload = gson.toJson(payload);
		return webExecute(url, jsonPayload, responseClass, headers);
	}
	public static <T> T webExecuteObj(String url, Object payload, Class<T> responseClass) throws IOException
	{
		return webExecuteObj(url, payload, responseClass, null);
	}
	public static <T> T webExecute(String url, String payload, Class<T> responseClass, Map<String, String> headers) throws IOException
	{
		final Gson gson = new Gson();
		final String jsonResponse = webExecute(url, payload, headers);
		try
		{
			return gson.fromJson(jsonResponse, responseClass);
		} catch(JsonParseException ex) {
			throw new IOException(jsonResponse);
		}
	}
	public static <T> T webExecute(String url, String payload, Class<T> responseClass) throws IOException
	{
		return webExecute(url, payload, responseClass, null);
	}
	public static String webExecute(String url, String payload) throws IOException
	{
		return webExecute(url, payload, Collections.EMPTY_MAP);
	}
	public static String webExecute(String url, String payload, Map<String, String> headers) throws IOException
	{
		try
		{
			final HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			connection.setConnectTimeout(5000);
			if(headers != null)
				for(Map.Entry<String, String> header : headers.entrySet())
					connection.setRequestProperty(header.getKey(), header.getValue());
			if(headers != null && headers.containsKey("Content-Type") == false)
				connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			connection.setReadTimeout(5000);
			connection.setUseCaches(false);
			try(final DataOutputStream dos = new DataOutputStream(connection.getOutputStream()))
			{
				if(payload != null)
					dos.write(payload.getBytes("UTF-8"));
				dos.flush();
			}
			final int responseCode = connection.getResponseCode();
			if(responseCode == HttpURLConnection.HTTP_OK)
				return readStream(connection.getInputStream());
			throw new IOException(new StringBuilder()
				.append(Integer.toString(responseCode))
				.append("Erroneous result of executing web-method: ")
				.append(connection.getResponseMessage())
				.append("\r\n")
				.append(readStream(connection.getErrorStream()))
				.toString());
		} catch(JsonParseException | MalformedURLException ex) {
			throw new IOException(ex);
		} catch(IOException ex) {
			throw ex;
		}
	}
	private static String readStream(InputStream is) throws IOException
	{
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream())
		{
			final byte[] buffer = new byte[1024];
			for(int length = 0; length != -1; length = is.read(buffer))
				baos.write(buffer, 0, length);
			return new String(baos.toByteArray(), "UTF-8");
		}
	}
}
