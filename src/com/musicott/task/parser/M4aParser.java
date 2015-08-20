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
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.util.Duration;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;

import com.musicott.error.ParseException;
import com.musicott.error.WriteMetadataException;
import com.musicott.model.Track;

/**
 * @author Octavio Calleya
 *
 */
public class M4aParser {

	private static Track track;
	
	public static Track parseM4aFile(final File fileToParse) throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException, ParseException, WriteMetadataException {
		track = new Track();
		AudioFile audioFile = AudioFileIO.read(fileToParse);
		String encoding = audioFile.getAudioHeader().getEncodingType();
		if(!encoding.equals("AAC"))
			throw new ParseException(encoding+" encoding not supported", fileToParse);
		Mp4Tag tag = (Mp4Tag) audioFile.getTag();
		track.setFileFolder(new File(fileToParse.getParent()).getAbsolutePath());
		track.setFileName(fileToParse.getName());
		track.setInDisk(true);
		track.setSize((int) (fileToParse.length()));
		track.setBitRate(Integer.parseInt(audioFile.getAudioHeader().getBitRate()));
		track.setTotalTime(Duration.seconds(audioFile.getAudioHeader().getTrackLength()));
		checkCover(tag);
		for(Mp4FieldKey fk: Mp4FieldKey.values()) {
			switch(fk) {
				case TITLE:
					track.setName(tag.getFirst(fk));
					break;
				case ARTIST:
					track.setArtist(tag.getFirst(fk));
					break;
				case ALBUM:
					track.setAlbum(tag.getFirst(fk));
					break;
				case ALBUM_ARTIST:
					track.setAlbumArtist(tag.getFirst(fk));
					break;
				case BPM:
					try {
						track.setBpm(Integer.parseInt(tag.getFirst(fk)));
					}
					catch (NumberFormatException e) {}
					break;
				case COMMENT:
					track.setComments(tag.getFirst(fk));
					break;
				case GENRE:
					if(track.getGenre().equals(""))
						track.setGenre(tag.getFirst(fk));
					break;
				case GENRE_CUSTOM:
					if(track.getGenre().equals(""))
						track.setGenre(tag.getFirst(fk));
					break;
				case GROUPING:
					track.setLabel(tag.getFirst(fk));
					break;
				case COMPILATION:
					try {
						track.setCompilation(Integer.parseInt(tag.getFirst(fk)) == 1 ? true : false);
					}
					catch (NumberFormatException e) {}
					break;
				case TRACK:
					try {
						track.setTrackNumber(Integer.parseInt(tag.getFirst(fk)));
					}
					catch (NumberFormatException e) {}
					break;
				case DISCNUMBER:
					try {
						track.setDiscNumber(Integer.parseInt(tag.getFirst(fk)));
					}
					catch (NumberFormatException e) {}
					break;
				case MM_ORIGINAL_YEAR:
					try {
						track.setYear(Integer.parseInt(tag.getFirst(fk)));
					}
					catch (NumberFormatException e) {}
					break;
				default:
			}
		}
		return track;
	}
	
	public static void checkCover(Mp4Tag tag) throws WriteMetadataException {
		if(!tag.getArtworkList().isEmpty())
			track.setHasCover(true);
		else {
			File f = new File(track.getFileFolder()+"/cover.jpg");
			if(f.exists()) {
				try {
					track.setCoverFile(Files.readAllBytes(Paths.get(f.getPath())),"jpg");
					track.setHasCover(true);
				} catch (IOException e) {}
			}
			else {
				f = new File(track.getFileFolder()+"/cover.jpeg");
				if(f.exists()) {
					try {
						track.setCoverFile(Files.readAllBytes(Paths.get(f.getPath())),"jpeg");
						track.setHasCover(true);
					} catch (IOException e) {}
				}				
				else {
					f = new File(track.getFileFolder()+"/cover.png");
					if(f.exists()) {
						try {
							track.setCoverFile(Files.readAllBytes(Paths.get(f.getPath())),"png");
							track.setHasCover(true);
						} catch (IOException e) {}
					}
					else
						track.setHasCover(false);
				}
			}
		}
	}
}