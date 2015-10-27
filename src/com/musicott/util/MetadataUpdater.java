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

package com.musicott.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.musicott.error.ErrorHandler;
import com.musicott.error.ErrorType;
import com.musicott.error.WriteMetadataException;
import com.musicott.model.Track;

/**
 * @author Octavio Calleya
 *
 */
public class MetadataUpdater {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass().getName());
	protected boolean succeeded;
	protected Track track;
	
	public MetadataUpdater(Track track) {
		this.track = track;
	}
	
	public boolean updateMetadata() {
		succeeded = false;
		Path trackPath = Paths.get(track.getFileFolder(), track.getFileName());
		AudioFile audio;
		try {
			audio = AudioFileIO.read(trackPath.toFile());
			baseUpdater(audio.getTag());
			audio.commit();
			succeeded = true;
		} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
				| InvalidAudioFrameException | CannotWriteException e) {
			treatException("Error writing metadata "+e.getMessage(), e, track);
		}
		return succeeded;
	}
	
	public void baseUpdater(Tag tag) throws KeyNotFoundException, FieldDataInvalidException {
	//	tag.setEncoding(Charset.forName("UTF-8"));	//TODO when jaudiotagger supports it
		tag.setField(FieldKey.TITLE, track.getName());
		tag.setField(FieldKey.ALBUM, track.getAlbum());
		tag.setField(FieldKey.ALBUM_ARTIST, track.getAlbumArtist());
		tag.setField(FieldKey.ARTIST, track.getArtist());
		tag.setField(FieldKey.GENRE, track.getGenre());
		tag.setField(FieldKey.COMMENT, track.getComments());
		tag.setField(FieldKey.GROUPING, track.getLabel());
		tag.setField(FieldKey.TRACK, ""+track.getTrackNumber());
		tag.setField(FieldKey.DISC_NO, ""+track.getDiscNumber());
		tag.setField(FieldKey.YEAR, ""+track.getYear());
		tag.setField(FieldKey.BPM, ""+track.getBpm());
		if(track.getFileFormat().equals("m4a"))
			((Mp4Tag)tag).setField(Mp4FieldKey.COMPILATION, track.getIsCompilation() ? "1" : "0");
		tag.setField(FieldKey.IS_COMPILATION, ""+track.getIsCompilation());
	}
	
	public boolean updateCover(File coverFile) {
		succeeded = false;
		if(!track.getFileFormat().equals("wav")) {
			Path trackPath = Paths.get(track.getFileFolder(), track.getFileName());
			try {
				AudioFile audioFile = AudioFileIO.read(trackPath.toFile());
				Tag tag = audioFile.getTag();
				Artwork cover = ArtworkFactory.createArtworkFromFile(coverFile);
				tag.deleteArtworkField();
				tag.addField(cover);
				audioFile.commit();
				succeeded = true;
			} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
					| InvalidAudioFrameException | CannotWriteException e) {
				treatException("Error saving cover: "+e.getMessage(), e, track);
			}
		}
		if(succeeded)
			track.setHasCover(true);
		return succeeded;
	}
	
	public boolean searchCoverInFolder() {
		boolean finded = false;
		String[] mimeTypes = {"jpg","jpeg","png"};
		String trackFolder = track.getFileFolder();
		File coverFile = null;
		for(String m: mimeTypes) {
			File aux = new File(trackFolder+"/cover."+m);
			if(aux.exists()) {
				coverFile = aux;
				break;
			}
		}
		if(coverFile != null) {
			try {
				Artwork cover = ArtworkFactory.createArtworkFromFile(coverFile);
				AudioFile audioFile = AudioFileIO.read(new File(trackFolder+"/"+track.getFileName()));
				Tag tag = audioFile.getTag();
				tag.addField(cover);
				audioFile.commit();
				track.setHasCover(true);
				finded = true;
			} catch (IOException | TagException | CannotWriteException | CannotReadException
					| ReadOnlyFileException | InvalidAudioFrameException e) {}
		}
		return finded;
	}
	
	private void treatException(String msg, Exception ex, Track track) {
		succeeded = false;
		WriteMetadataException wme = new WriteMetadataException(msg, ex, track);
		ErrorHandler.getInstance().addError(wme, ErrorType.METADATA);  // Comment for testing
		LOG.error(wme.getMessage());
	}
}