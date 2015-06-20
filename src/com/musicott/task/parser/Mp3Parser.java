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
 * along with Musicott library.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.musicott.task.parser;

import java.io.File;
import java.io.IOException;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import com.musicott.model.Track;

/**
 * @author Octavio Calleya
 *
 */
public class Mp3Parser {
	
	private static File file;

	public static Track parseMp3File(final File fileToParse) throws UnsupportedTagException, InvalidDataException, IOException {
		file = fileToParse;
		Track mp3Track = new Track();
		Mp3File mp3File = new Mp3File(file);
		mp3Track.setFileFolder(new File(file.getParent()).getAbsolutePath());
		mp3Track.setFileName(file.getName());
		checkCover(mp3Track);
		mp3Track.getIsInDisk().set(true);
		mp3Track.getSize().set((int) (file.length()));
		mp3Track.getBitRate().set(mp3File.getBitrate());
		mp3Track.getTotalTime().set((int)mp3File.getLengthInSeconds());
		if(mp3File.hasId3v2Tag())
			readId3v2Tag(mp3Track, mp3File);
		else
			if(mp3File.hasId3v1Tag())
				readId3v1Tag(mp3Track, mp3File);
			else {
				mp3Track.getName().set(file.getName());
			}
		return mp3Track;
	}
	
	private static void readId3v2Tag(Track track, Mp3File file) {
		ID3v2 tag = file.getId3v2Tag();
		if(tag.getTitle() != null)
			track.getName().set((tag.getTitle()));
		if(tag.getArtist() != null)
			track.getArtist().set(tag.getArtist());
		if(tag.getAlbum() != null)
			track.getAlbum().set(tag.getAlbum());
		if(tag.getAlbumArtist() != null)
			track.getAlbumArtist().set(tag.getAlbumArtist());
		track.getBPM().set(tag.getBPM());
		if(tag.getComment() != null)
			track.getComments().set(tag.getComment());
		if(tag.getGenreDescription() != null)
		track.getGenre().set(tag.getGenreDescription());
		if(tag.getGrouping() != null)
			track.getLabel().set(tag.getGrouping());
		track.getIsCompilation().set(tag.isCompilation());
		try {
			track.getTrackNumber().set(Integer.valueOf(tag.getTrack()));
			track.getYear().set(Integer.valueOf(tag.getYear()));
			track.getDiscNumber().set(Integer.valueOf(tag.getPartOfSet()));
		} catch (NumberFormatException e) {
			
		}
		//TODO Think what to do with trackId here
	}
	
	private static void readId3v1Tag(Track track, Mp3File file) {
		ID3v1 tag = file.getId3v1Tag();
		if(tag.getTitle() != null)
			track.getName().set(tag.getTitle());
		if(tag.getArtist() != null)
			track.getArtist().set(tag.getArtist());
		if(tag.getAlbum() != null)
			track.getAlbum().set(tag.getAlbum());
		if(tag.getComment() != null)
			track.getComments().set(tag.getComment());
		if(tag.getGenreDescription() != null)
			track.getGenre().set(tag.getGenreDescription());
		try {
			track.getTrackNumber().set(Integer.valueOf(tag.getTrack()));
			track.getYear().set(Integer.valueOf(tag.getYear()));
		} catch (NumberFormatException e) {
			
		}
	}
	
	private static void checkCover(Track track) {
		if(new File(file.getParentFile().getAbsolutePath()+"/cover.jpg").exists())
			track.getHasCover().set(true);
		else
			if(new File(file.getParentFile().getAbsolutePath()+"/cover.jpeg").exists())
				track.getHasCover().set(true);
			else
				if(new File(file.getParentFile().getAbsolutePath()+"/cover.png").exists())
					track.getHasCover().set(true);
				else
					track.getHasCover().set(false);
		
		//TODO Check for the cover image in the ID3Layer
	}
}