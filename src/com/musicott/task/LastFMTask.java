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

package com.musicott.task;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.musicott.SceneManager;
import com.musicott.error.ErrorHandler;
import com.musicott.model.Track;
import com.musicott.services.LastFMService;
import com.musicott.services.ServiceManager;
import com.musicott.services.lastfm.LastFMError;
import com.musicott.services.lastfm.LastFMResponse;

import javafx.application.Platform;

/**
 * @author Octavio Calleya
 *
 */
public class LastFMTask extends Thread {

	private final Logger LOG = LoggerFactory.getLogger(getClass().getName());
	private ErrorHandler eh;
	private ServiceManager sm;
	private LastFMService lastfm;
	private Semaphore semaphore;
	private Track actualTrack;
	private List<Map<Track, Integer>> tracksToScrobbleLater;
	private boolean end;
	
	public LastFMTask(String id, ServiceManager services) {
		super(id);
		eh = ErrorHandler.getInstance();
		sm = services;
		end = false;
		lastfm = new LastFMService();
		semaphore = new Semaphore(0);
	}
	
	@Override
	public void run() {
		LastFMResponse lfm;
		while(true) {
			try {
				semaphore.acquire();
				if(!lastfm.isValidAPIConfig()) {
					eh.showLastFMErrorDialog("LastFM error", "LastFM API Key or Secret are invalid");
					sm.endLastfmUsage();
					break;
				}	
				lfm = lastfm.getSession();
				if(lfm.getStatus().equals("failed")) {
					handleLastFMError(lfm.getError());					
					if(end)
						break;
				}
				lfm = lastfm.updateNowPlaying(actualTrack);
				if(lfm.getStatus().equals("failed")) {
					handleLastFMError(lfm.getError());
					if(end)
						break;
				}
				lfm = lastfm.scrobble(actualTrack);
				if(lfm.getStatus().equals("failed")) {
					handleLastFMError(lfm.getError());
					addTrackToScrobbleLater(actualTrack);
					if(end)
						break;
				}
				Platform.runLater(() -> {
					SceneManager.getInstance().getRootController().setStatusMessage("Track updated and scrobbled on LastFM");
				});
			} catch (InterruptedException e) {
				LOG.warn("Lastfm thread error: {}", e);
				break;
			}
		}
	}
	
	public void updateAndScrobble(Track track) {
		actualTrack = track;
		semaphore.release();
	}
	
	private void addTrackToScrobbleLater(Track track) {
		ListIterator<Map<Track, Integer>> scrobblesIt = tracksToScrobbleLater.listIterator();
		boolean done = false;
		while(scrobblesIt.hasNext() && !done) {
			Map<Track, Integer> mapBatch = scrobblesIt.next();
			if(mapBatch.size() < 50) {
				mapBatch.put(track, (int)System.currentTimeMillis()/1000);
				done = true;
			}
			else if (scrobblesIt.nextIndex() == mapBatch.size()) {
				Map<Track, Integer> newMapBatch = new HashMap<>();
				newMapBatch.put(track, (int)System.currentTimeMillis()/1000);
				scrobblesIt.add(newMapBatch);
				done = true;
			}
		}	
	}		
	
	private void handleLastFMError(LastFMError error) {
		//TODO Error handling for Last FM error statuses
		LOG.info("LastFM error: {}", error.getMessage());
		eh.showLastFMErrorDialog("LastFM error "+error.getCode(), error.getMessage());
		Platform.runLater(() -> SceneManager.getInstance().getRootController().setStatusMessage("Error updating LastFM"));
		sm.endLastfmUsage();
		end = true;
	}
}