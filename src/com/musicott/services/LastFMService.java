/*
 * This file is part of Musicott software.
 *
 * Musicott software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Musicott library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Musicott. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.musicott.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.musicott.MainPreferences;
import com.musicott.model.Track;
import com.musicott.services.lastfm.LastFMError;
import com.musicott.services.lastfm.LastFMResponse;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * @author Octavio Calleya
 *
 */
public class LastFMService {

	private final Logger LOG = LoggerFactory.getLogger(getClass().getName());
	/**
	 * The API Key for the application submitted in LastFM.
	 * Retrieved from config file for security reasons
	 */
	private String API_KEY;
	/**
	 * The API Secret for the application submitted in LastFM.
	 * Retrieved from config file for security reasons
	 */
	private String API_SECRET;
	private final String CONFIG_FILE = "resources/config/config.properties";
	private final String API_ROOT_URL = "https://ws.audioscrobbler.com/2.0/";
	private final String USERNAME;
	private final String PASSWORD;
	private String sessionKey;
	private Client client;
	private WebResource resource;
	
	public LastFMService() {
		client = Client.create();
		resource = client.resource(API_ROOT_URL);
		USERNAME = MainPreferences.getInstance().getLastFMUsername();
		PASSWORD = MainPreferences.getInstance().getLastFMPassword();
		sessionKey = MainPreferences.getInstance().getLastFMSessionKey();
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(CONFIG_FILE));
			API_KEY = prop.getProperty("lastfm_api_key");
			API_SECRET = prop.getProperty("lastfm_api_secret");
		} catch (IOException e) {}
	}
	
	public LastFMResponse updateNowPlaying(Track track) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("artist", track.getArtist());
        queryParams.add("track", track.getName());
        queryParams.add("sk", sessionKey);
        queryParams.add("method", "track.updateNowPlaying");
        queryParams.add("api_key", API_KEY);
        queryParams.add("sk", sessionKey);
        queryParams.add("password", PASSWORD);
        queryParams.add("username", USERNAME);
        queryParams.add("api_sig", buildSignature(queryParams));
        return makeRequest(queryParams, HttpMethod.POST);
	}
	
	public LastFMResponse scrobble(Track track) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.add("artist", track.getArtist());
		queryParams.add("track", track.getName());
		queryParams.add("timestamp", ""+System.currentTimeMillis()/1000);
        queryParams.add("method", "track.scrobble");
        queryParams.add("api_key", API_KEY);
        queryParams.add("sk", sessionKey);
        queryParams.add("password", PASSWORD);
        queryParams.add("username", USERNAME);
        queryParams.add("api_sig", buildSignature(queryParams));
    	return makeRequest(queryParams, HttpMethod.POST);
	}
	
	public LastFMResponse scrobbleBatch(Map<Track, Integer> trackBatch) {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		int i = 0;
		for(Track t: trackBatch.keySet()) {
			queryParams.add("artist["+i+"]", t.getArtist());
			queryParams.add("track"+i+"]", t.getName());
			queryParams.add("timestamp"+i+"]", ""+trackBatch.get(t));
			i++;
		}
        queryParams.add("method", "track.scrobble");
        queryParams.add("api_key", API_KEY);
        queryParams.add("sk", sessionKey);
        queryParams.add("password", PASSWORD);
        queryParams.add("username", USERNAME);
        queryParams.add("api_sig", buildSignature(queryParams));
    	return makeRequest(queryParams, HttpMethod.POST);
	}
	
	public LastFMResponse getSession() {
		LastFMResponse lfm;
		if(sessionKey == null) {
			MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
	        queryParams.add("method", "auth.getMobileSession");
	        queryParams.add("api_key", API_KEY);
	        queryParams.add("password", PASSWORD);
	        queryParams.add("username", USERNAME);
	        queryParams.add("api_sig", buildSignature(queryParams));
	        lfm = makeRequest(queryParams, HttpMethod.POST);
	        if(lfm.getStatus().equals("ok")) {
	        	sessionKey = lfm.getSession().getSessionKey();
	        	MainPreferences.getInstance().setLastFMSessionkey(sessionKey);
	        }
		}
		else {
			lfm = new LastFMResponse();
			lfm.setStatus("ok");
		}
		return lfm;
	}
	
	public boolean isValidAPIConfig() {
		return API_KEY != null && API_SECRET != null;
	}
	
	private LastFMResponse makeRequest(MultivaluedMap<String, String> params, String method) {
		LastFMResponse lfmResponse = null;
    	ClientResponse response = null;
        try {
        	if(method.equals(HttpMethod.GET))
	        		response = resource.queryParams(params).get(ClientResponse.class);
	        else if(method.equals(HttpMethod.POST))
	        		response = resource.queryParams(params).post(ClientResponse.class);			
	     	LOG.debug("LastFM API GET petition status: {}", response.getStatus());
	     	lfmResponse = response.getEntity(LastFMResponse.class);
        } catch (RuntimeException e) {
    		lfmResponse = new LastFMResponse();
    		lfmResponse.setStatus("failed");
    		LastFMError lfmError = new LastFMError();
    		lfmError.setCode("U1");
    		lfmError.setMessage(e.getMessage());
    		lfmResponse.setError(lfmError);
        } finally {
        	if(response != null)
        		response.close();
        }
        if(lfmResponse == null) {
        	LastFMError lastFMError = new LastFMError();
        	lfmResponse = new LastFMResponse();
        	lastFMError.setCode(""+response.getStatus());
        	lastFMError.setMessage("LastFM "+method+" petition error "+response.getStatus());
        	lfmResponse.setStatus("failed");
        	lfmResponse.setError(lastFMError);
        }
		return lfmResponse;
	}
	
	private String buildSignature(MultivaluedMap<String, String> params) {
		String sig = "";
		Set<String> sortedParams = new TreeSet<String>(params.keySet());
		for(String key: sortedParams)
			sig += key+params.getFirst(key);
		sig += API_SECRET;
		return MD5(sig);
	}
	
	private String MD5(String message) {
		String md5 = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(message.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i=0; i<array.length; i++)
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			md5 = sb.toString();
	    } catch (NoSuchAlgorithmException e) {}
	    return md5;
	}
}