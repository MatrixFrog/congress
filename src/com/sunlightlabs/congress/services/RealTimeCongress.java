package com.sunlightlabs.congress.services;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.util.EntityUtils;

import android.text.TextUtils;

import com.sunlightlabs.congress.models.CongressException;


public class RealTimeCongress {
	public static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	public static final String dateOnlyFormat = "yyyy-MM-dd";
	
	public static final String baseUrl = "http://api.realtimecongress.org/api/v1/";
	public static final String format = "json";
	
	// filled in by the client
	public static String userAgent = "com.sunlightlabs.congress.services.RealTimeCongress";
	public static String appVersion = null;
	public static String osVersion = null;
	public static String apiKey = "";
	
	public static final int MAX_PER_PAGE = 500;
	
	public static String url(String method, String[] sections, Map<String,String> params) {
		return url(method, sections, params, -1, -1);
	}
	
	public static String url(String method, String[] sections, Map<String,String> params, int page, int per_page) {
		params.put("apikey", apiKey);
		
		if (sections != null && sections.length > 0)
			params.put("sections", TextUtils.join(",", sections));
		
		if (page > 0 && per_page > 0) {
			params.put("page", String.valueOf(page));
			params.put("per_page", String.valueOf(per_page));
		}
		
		StringBuilder query = new StringBuilder("");
		Iterator<String> iterator = params.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			query.append(URLEncoder.encode(key));
			query.append("=");
			query.append(URLEncoder.encode(params.get(key)));
			if (iterator.hasNext())
				query.append("&");
		}
		
		return baseUrl + method + "." + format + "?" + query.toString();
	}
	
	public static Date parseDate(String date) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat(dateFormat);
		format.setTimeZone(DateUtils.GMT);
		return format.parse(date);
	}
	
	public static Date parseDateOnly(String date) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat(dateOnlyFormat);
		format.setTimeZone(DateUtils.GMT);
		return format.parse(date);
	}
	
	public static String formatDate(Date date) {
		return DateUtils.formatDate(date, dateFormat);
	}
	
	public static String fetchJSON(String url) throws CongressException {
		HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", userAgent);
        
        if (osVersion != null)
        	request.addHeader("x-os-version", osVersion);
        
        if (appVersion != null)
        	request.addHeader("x-app-version", appVersion);
		
        DefaultHttpClient client = new DefaultHttpClient();
        
        try {
	        HttpResponse response = client.execute(request);
	        int statusCode = response.getStatusLine().getStatusCode();
	        
	        if (statusCode == HttpStatus.SC_OK) {
	        	String body = EntityUtils.toString(response.getEntity());
	        	return body;
	        } else if (statusCode == HttpStatus.SC_NOT_FOUND)
	        	throw new CongressException.NotFound("404 Not Found from " + url);
	        else
	        	throw new CongressException("Bad status code " + statusCode + " on fetching JSON from " + url);
        } catch (ClientProtocolException e) {
	    	throw new CongressException(e, "Problem fetching JSON from " + url);
	    } catch (IOException e) {
	    	throw new CongressException(e, "Problem fetching JSON from " + url);
	    }
	}
}