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

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.musicott.SceneManager;
import com.musicott.model.MusicLibrary;
import com.musicott.model.Track;
import com.musicott.view.RootController;

import be.tarsos.transcoder.DefaultAttributes;
import be.tarsos.transcoder.Transcoder;
import be.tarsos.transcoder.ffmpeg.EncoderException;
import javafx.concurrent.Task;

/**
 * @author Octavio Calleya
 *
 */
public class WaveformTask extends Task<float[]> {
	
	private final Logger LOG = LoggerFactory.getLogger(WaveformTask.class.getName());
	
	private Map<Integer,float[]> waveforms = MusicLibrary.getInstance().getWaveforms();
	private final double HEIGHT_COEFICIENT = 4.2;
	private Track track;
	private float[] waveform;
	
	private SceneManager sc = SceneManager.getInstance();
	private RootController rootController;
	
	public WaveformTask(Track track) {
		this.track = track;
		this.rootController = sc.getRootController();
	}

	@Override
	protected float[] call() {
		LOG.info("Processing waveform of track "+track);
		if(track.getFileFormat().equals("wav"))
			waveform = processWav();
		else
			if(track.getFileFormat().equals("mp3")) {
				waveform = processMp3();
			}
		return waveform;
	}

	@Override
	protected void succeeded() {
		super.succeeded();
		if(waveform != null) {
			waveforms.put(track.getTrackID(), waveform);
			Track currentTrack = sc.getPlayQueueController().getCurrentTrack();
			if(currentTrack != null && currentTrack.equals(track))
				SwingUtilities.invokeLater(() -> rootController.setWaveform(track));
			LOG.info("Waveform of track {} completed", track);
			sc.saveLibrary(false, true);
		}
	}

	private float[] processMp3() {
		float[] waveData;
		String trackName = track.getName();
		Path temporalDecodedPath = FileSystems.getDefault().getPath("temp", new String("decoded_"+trackName+".wav").replaceAll(" ","_"));
		Path trackPath = FileSystems.getDefault().getPath(track.getFileFolder(), track.getFileName());
		Path temporalCoppiedPath = FileSystems.getDefault().getPath("temp", new String("original_"+trackName+".mp3").replaceAll(" ","_"));
		File temporalDecodedFile = temporalDecodedPath.toFile();
		File temporalCoppiedFile = temporalCoppiedPath.toFile();
		try {
			temporalDecodedFile.createNewFile();
			CopyOption[] options = new CopyOption[]{COPY_ATTRIBUTES, REPLACE_EXISTING}; 
			Files.copy(trackPath, temporalCoppiedPath, options);
			Transcoder.transcode(temporalCoppiedPath.toString(), temporalDecodedPath.toString(), DefaultAttributes.WAV_PCM_S16LE_STEREO_44KHZ.getAttributes());
			waveData = processAmplitudes(getWavAmplitudes(temporalDecodedFile));
		} catch (EncoderException | UnsupportedAudioFileException | IOException e) {
			LOG.warn("Error processing audio waveform of {}: "+e.getMessage(), track);
			waveData = null;
		} finally {
			if(temporalDecodedFile.exists())
				temporalDecodedFile.delete();
			if(temporalCoppiedFile.exists())
				temporalCoppiedFile.delete();
		}
		return waveData;
	}
	
	
	private float[] processWav() {
		float[] waveData;
		String trackPath = track.getFileFolder()+"/"+track.getFileName();
		File trackFile = new File(trackPath);
		try {
			waveData = processAmplitudes(getWavAmplitudes(trackFile));
		} catch (UnsupportedAudioFileException | IOException e) {
			LOG.warn("Error processing audio waveform of {}: "+e.getMessage(), track);
			waveData = null;
		}
		return waveData;
	}
	
	
	private int[] getWavAmplitudes(File file) throws UnsupportedAudioFileException, IOException {
		int[] amp = null;
		AudioInputStream input = AudioSystem.getAudioInputStream(file);
		AudioFormat baseFormat = input.getFormat();
		AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 
		                                            baseFormat.getSampleRate(),
		                                            16,
		                                            baseFormat.getChannels(),
		                                            baseFormat.getChannels() * 2,
		                                            baseFormat.getSampleRate(),
		                                            false);
		AudioInputStream pcmDecodedInput = AudioSystem.getAudioInputStream(decodedFormat, input);			
		int available = input.available();
		amp = new int[available];
		byte[] buffer = new byte[available];
		pcmDecodedInput.read(buffer, 0, available);
		for(int i=0; i<available-1 ; i+=2) {
			amp[i] = ((buffer[i+1] << 8) | buffer[i]) << 16;
			amp[i] /= 32767;
			amp[i] *= HEIGHT_COEFICIENT;
		}
		input.close();
		pcmDecodedInput.close();
		return amp;
	}
	
	
	private float[] processAmplitudes(int[] sourcePCMData) {
		int width = 515;	// the width of th waveform panel
		float[] waveData = new float[width];
		int	nSamplesPerPixel = sourcePCMData.length / width;
		for (int i = 0; i<width; i++) {
			float nValue = 0.0f;
			for (int j = 0; j<nSamplesPerPixel; j++) {
				nValue += (float) (Math.abs(sourcePCMData[i * nSamplesPerPixel + j]) / 65536.0f);
			}
			nValue /= nSamplesPerPixel;
			waveData[i] = nValue;
		}
		return waveData;
	}
}