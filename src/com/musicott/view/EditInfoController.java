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

package com.musicott.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.musicott.error.ErrorHandler;
import com.musicott.error.ErrorType;
import com.musicott.model.Track;
import com.musicott.model.TrackField;
import com.musicott.task.UpdateMetadataTask;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.CacheHint;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * @author Octavio Calleya
 *
 */
public class EditInfoController {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass().getName());

	@FXML
	private TextField name;
	@FXML
	private TextField artist;
	@FXML
	private TextField album;
	@FXML
	private TextField albumArtist;
	@FXML
	private TextField genre;
	@FXML
	private TextField label;
	@FXML
	private TextField year;
	@FXML
	private TextField bpm;
	@FXML
	private TextArea comments;
	@FXML
	private TextField trackNum;
	@FXML
	private TextField discNum;
	@FXML
	private Label titleName;
	@FXML
	private Label titleArtist;
	@FXML
	private Label titleAlbum;
	@FXML
	private ImageView coverImage;
	@FXML
	private CheckBox isCompilationCheckBox;
	@FXML
	private Button cancelEditButton;
	@FXML
	private Button okEditButton;
	
	private Map<TrackField,TextInputControl> editFieldsMap;
	private File newCoverImage;
	private Image defaultImage;
	private Stage editStage;
	private List<Track> trackSelection;
	
	public EditInfoController() {
	}
	
	@FXML
	private void initialize() {
		defaultImage = new Image(getClass().getResourceAsStream("/images/default-cover-image.png"));
		editFieldsMap = new HashMap<>();
		editFieldsMap.put(TrackField.NAME, name);
		editFieldsMap.put(TrackField.ARTIST, artist);
		editFieldsMap.put(TrackField.ALBUM, album);
		editFieldsMap.put(TrackField.GENRE, genre);
		editFieldsMap.put(TrackField.COMMENTS, comments);
		editFieldsMap.put(TrackField.ALBUM_ARTIST, albumArtist);
		editFieldsMap.put(TrackField.LABEL, label);
		editFieldsMap.put(TrackField.TRACK_NUMBER, trackNum);
		editFieldsMap.put(TrackField.DISC_NUMBER, discNum);
		editFieldsMap.put(TrackField.YEAR, year);
		editFieldsMap.put(TrackField.BPM, bpm);
		
		titleName.textProperty().bind(name.textProperty());
		titleArtist.textProperty().bind(artist.textProperty());
		titleAlbum.textProperty().bind(album.textProperty());
		
		// Validation of the numeric fields
		EventHandler<? super KeyEvent> nonNumericFilter = event -> {if(!event.getCharacter().matches("[0-9]")) event.consume();};
		trackNum.addEventFilter(KeyEvent.KEY_TYPED, nonNumericFilter); 
		trackNum.addEventFilter(KeyEvent.KEY_TYPED, event -> {if(trackNum.getText().length() == 3) event.consume();});
		discNum.addEventFilter(KeyEvent.KEY_TYPED, nonNumericFilter);
		discNum.addEventFilter(KeyEvent.KEY_TYPED, event -> {if(discNum.getText().length() == 3) event.consume();});
		bpm.addEventFilter(KeyEvent.KEY_TYPED, nonNumericFilter);
		bpm.addEventFilter(KeyEvent.KEY_TYPED, event -> {if(bpm.getText().length() == 3) event.consume();});
		year.addEventFilter(KeyEvent.KEY_TYPED, nonNumericFilter);
		year.addEventFilter(KeyEvent.KEY_TYPED, event -> {if(year.getText().length() == 4) event.consume();});
		
		coverImage.setImage(defaultImage);
		coverImage.setCacheHint(CacheHint.QUALITY);
		coverImage.setOnMouseClicked(event -> {
			if(event.getClickCount() <= 2) {
				LOG.debug("Choosing cover image");
				FileChooser chooser = new FileChooser();
				chooser.setTitle("Open file(s)...");
				chooser.getExtensionFilters().addAll(new ExtensionFilter("Image files (*.png, *.jpg, *.jpeg)","*.png", "*.jpg", "*.jpeg"));
				newCoverImage = chooser.showOpenDialog(editStage);
				byte[] newCoverBytes;
				if(newCoverImage != null) {
					try {
						newCoverBytes = Files.readAllBytes(Paths.get(newCoverImage.getPath()));
						coverImage.setImage(new Image(new ByteArrayInputStream(newCoverBytes)));
					} catch (IOException e) {
						ErrorHandler.getInstance().addError(e, ErrorType.COMMON);
						ErrorHandler.getInstance().showErrorDialog(editStage.getScene(), ErrorType.COMMON);
						LOG.error("Error setting image: "+e.getMessage());
					}
				}
			}
		});
	}
	
	public void setStage(Stage stage) {
		editStage = stage;
	}
	
	public void setSelection(List<Track> selection) {
		trackSelection = selection;
		setFields();
	}

	@FXML
	private void doOK() {
		boolean changed;
		String newValue;
		for(int i=0; i<trackSelection.size() ;i++) {
			Track track = trackSelection.get(i);
			Map<TrackField, Property<?>> trackPropertiesMap = track.getPropertiesMap();
			changed = false;
			
			for(TrackField field: editFieldsMap.keySet()) {
				newValue = editFieldsMap.get(field).textProperty().getValue();
				if(field == TrackField.TRACK_NUMBER || field == TrackField.DISC_NUMBER || field == TrackField.YEAR || field == TrackField.BPM) {
					try {
						IntegerProperty ip = (IntegerProperty) trackPropertiesMap.get(field);
						if(!newValue.equals("-") || (!newValue.equals("") && ip.get() != Integer.parseInt(newValue))) {
							ip.setValue(Integer.parseInt(newValue));
							changed = true;
						}
					} catch(NumberFormatException e) {}
				}
				else {
					StringProperty sp = (StringProperty) trackPropertiesMap.get(field);
					if(!newValue.equals("-") && !sp.get().equals(newValue)) {
						sp.setValue(newValue);
						changed = true;
					}
				}
			}
			if(!isCompilationCheckBox.isIndeterminate()) {
				track.setCompilation(isCompilationCheckBox.isSelected());
				changed = true;
			}
			if(changed) {
				track.setDateModified(LocalDateTime.now());				
				LOG.info("Track {} edited to {}", track.getTrackID(), track);
			}
		}
		UpdateMetadataTask updateTask = new UpdateMetadataTask(trackSelection, newCoverImage);
		updateTask.setDaemon(true);
		updateTask.start();
		newCoverImage = null;
		editStage.close();
	}
		
	@FXML
	private void doCancel() {
		LOG.info("Edit stage cancelled");
		newCoverImage = null;
		editStage.close();
	}
	
	private void setFields() {
		List<String> valuesList = new ArrayList<String>();
		for(TrackField field: editFieldsMap.keySet()) {
			for(Track t: trackSelection) {
				Map<TrackField, Property<?>> propertyMap = t.getPropertiesMap();
				Property<?> trackProperty = propertyMap.get(field);
				if(field == TrackField.TRACK_NUMBER || field == TrackField.DISC_NUMBER || field == TrackField.YEAR || field == TrackField.BPM) {
					IntegerProperty ip = (IntegerProperty) trackProperty;
					if(ip.get() == 0 || ip.get() == -1)
						valuesList.add("");
					else
						valuesList.add("" + ip.get());
				}
				else
					valuesList.add("" + trackProperty.getValue());
			}
			editFieldsMap.get(field).textProperty().setValue(matchCommonString(valuesList));
			valuesList.clear();
		}
		// Check for the same cover image and compilation value
		
		byte[] commonCover = matchCommonCover();
		if(commonCover != null)
			coverImage.setImage(new Image(new ByteArrayInputStream(commonCover)));
		else
			coverImage.setImage(defaultImage);

		if(!matchCommonCompilation())
			isCompilationCheckBox.setIndeterminate(true);
		else
			isCompilationCheckBox.setSelected(trackSelection.get(0).getIsCompilation());
	}
	
	private byte[] matchCommonCover() {
		byte[] coverBytes = null;
		String sameAlbum = trackSelection.get(0).getAlbum();
		for(Track t: trackSelection)
			if(!t.getAlbum().equalsIgnoreCase(sameAlbum)) {
				sameAlbum = null;
				break;
			}
		if(sameAlbum != null)
			for(Track t: trackSelection)
				if(t.hasCover())
					coverBytes = t.getCoverBytes();
		return coverBytes;
	}
	
	private boolean matchCommonCompilation() {
		Boolean isCommon = trackSelection.get(0).getIsCompilation();
		if(trackSelection.stream().allMatch(t -> isCommon.equals(t.getIsCompilation())))
			return true;
		else
			return false;
	}

	private String matchCommonString(List<String> list) {
		String commonString;
		if(list.stream().allMatch(st -> st.equalsIgnoreCase(list.get(0))))
			commonString = list.get(0);
		else
			commonString = "-";
		return commonString;
	}
}