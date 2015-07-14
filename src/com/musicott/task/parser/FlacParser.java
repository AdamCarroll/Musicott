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

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.flac.FlacTag;

import com.musicott.model.Track;

/**
 * @author Octavio Calleya
 *
 */
public class FlacParser {

	private static Track track;
	
	public static Track parseFlacFile(final File fileToParse) throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
		track = new Track();
		AudioFile audioFile = AudioFileIO.read(fileToParse);
		FlacTag tag = (FlacTag) audioFile.getTag();
		track.setFileFolder(new File(fileToParse.getParent()).getAbsolutePath());
		track.setFileName(fileToParse.getName());
		track.setInDisk(true);
		track.setSize((int) (fileToParse.length()));
		track.setBitRate(Integer.parseInt(audioFile.getAudioHeader().getBitRate()));
		track.setTotalTime(audioFile.getAudioHeader().getTrackLength());
		//TODO check Image Cover
		for(FieldKey t: FieldKey.values()) {
			switch (t){
			case TITLE:
				track.getNameProperty().set(tag.getFirst(t));
				break;
			case ARTIST:
				track.getArtistProperty().set(tag.getFirst(t));
				break;
			case ALBUM:
				track.getAlbumProperty().set(tag.getFirst(t));
				break;
			case ALBUM_ARTIST:
				track.getAlbumArtistProperty().set(tag.getFirst(t));
				break;
			case BPM:
				try {
					track.getBpmProperty().set(Integer.parseInt(tag.getFirst(t)));
				} catch (NumberFormatException e) {}
				break;
			case COMMENT:
				track.getCommentsProperty().set(tag.getFirst(t));
				break;
			case GENRE:
				track.getGenreProperty().set(tag.getFirst(t));
				break;
			case GROUPING:
				track.getLabelProperty().set(tag.getFirst(t));
				break;
			case IS_COMPILATION:
				track.setCompilation(tag.getFirst(t).equals("true") ? Boolean.valueOf(tag.getFirst(t)) : false);
				break;
			case TRACK:
				try {
					track.getTrackNumberProperty().set(Integer.parseInt(tag.getFirst(t)));
				} catch (NumberFormatException e) {}
				break;
			case DISC_NO:
				try {
					track.getDiscNumberProperty().set(Integer.parseInt(tag.getFirst(t)));
				} catch (NumberFormatException e) {}
				break;
			case YEAR:
				try {
					track.getYearProperty().set(Integer.parseInt(tag.getFirst(t)));
				} catch (NumberFormatException e) {}
				break;
			}
		}
		return track;
	}
}