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

package com.musicott.view;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.musicott.SceneManager;
import com.musicott.model.MusicLibrary;
import com.musicott.model.Track;
import com.musicott.player.Mp3Player;
import com.musicott.task.OpenTask;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * @author Octavio Calleya
 *
 */
public class RootLayoutController {
	
	private static final double AMOUNT_VOLUME = 0.05;
	
	@FXML
	private Menu menuFile;
	@FXML
	private MenuItem menuItemImport;
	@FXML
	private MenuItem  menuItemOpen;
	@FXML
	private Menu menuEdit;
	@FXML
	private MenuItem menuItemDelete;
	@FXML
	private MenuItem menuItemEdit; 
	@FXML
	private Menu menuAbout;
	@FXML
	private MenuItem menuItemAbout;
	@FXML
	private ToggleButton playButton;
	@FXML
	private Button prevButton;
	@FXML
	private Button nextButton;
	@FXML
	private ToggleButton playQueueButton;
	@FXML
	private ImageView currentCover;
	@FXML
	private Label titleLabel;
	@FXML
	private Label artistAlbumLabel;
	@FXML
	private Label currentTimeLabel;
	@FXML
	private Label remainingTimeLabel;
	@FXML
	private Slider trackSlider;
	@FXML
	private ProgressBar trackProgressBar;
	@FXML
	private Slider volumeSlider;
	@FXML
	private ProgressBar volumeProgressBar;	
	@FXML
	private TableView<Track> trackTable;
	@FXML
	private TableColumn<Track,String> nameCol;
	@FXML
	private TableColumn<Track,String> artistCol;
	@FXML
	private TableColumn<Track,String> albumCol;
	@FXML
	private TableColumn<Track,String> genreCol;
	@FXML
	private TableColumn<Track,String> commentsCol;
	@FXML
	private TableColumn<Track,String> albumArtistCol;
	@FXML
	private TableColumn<Track,String> labelCol;
	@FXML
	private TableColumn<Track,LocalDate> dateModifiedCol;
	@FXML
	private TableColumn<Track,LocalDate> dateAddedCol;
	@FXML
	private TableColumn<Track,Number> sizeCol;
	@FXML
	private TableColumn<Track,Duration> totalTimeCol;
	@FXML
	private TableColumn<Track,Number> trackNumberCol;
	@FXML
	private TableColumn<Track,Number> yearCol;
	@FXML
	private TableColumn<Track,Number> bitRateCol;
	@FXML
	private TableColumn<Track,Number> playCountCol;
	@FXML
	private TableColumn<Track,Number> discNumberCol;
	@FXML
	private TableColumn<Track,Number> bpmCol;
	@FXML
	private TableColumn<Track,Boolean> coverCol;
	@FXML
	private TableColumn<Track,Boolean> inDiskCol;

	private ObservableList<Track> tracks;
	private ObservableList<Track> selection;
	
	private Stage rootStage;
	private SceneManager sc;
	
	private MusicLibrary ml;
	private Mp3Player player;
	
	private boolean emptyTable = true;
	
 	public RootLayoutController() {
	}
	
	@FXML
	public void initialize() {
		nameCol.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
		artistCol.setCellValueFactory(cellData -> cellData.getValue().getArtistProperty());
		albumCol.setCellValueFactory(cellData -> cellData.getValue().getAlbumProperty());
		genreCol.setCellValueFactory(cellData -> cellData.getValue().getGenreProperty());
		commentsCol.setCellValueFactory(cellData -> cellData.getValue().getCommentsProperty());
		albumArtistCol.setCellValueFactory(cellData -> cellData.getValue().getAlbumArtistProperty());
		labelCol.setCellValueFactory(cellData -> cellData.getValue().getLabelProperty());
		dateModifiedCol.setCellValueFactory(cellData -> new SimpleObjectProperty<LocalDate>(cellData.getValue().getDateModified()));
		dateAddedCol.setCellValueFactory(cellData -> new SimpleObjectProperty<LocalDate>(cellData.getValue().getDateAdded()));
		sizeCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getSize()));
		totalTimeCol.setCellValueFactory(cellData -> new SimpleObjectProperty<Duration>(cellData.getValue().getTotalTime()));
		yearCol.setCellValueFactory(cellData -> cellData.getValue().getYearProperty());
		bitRateCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getBitRate()));
		playCountCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getPlayCount()));
		discNumberCol.setCellValueFactory(cellData -> cellData.getValue().getDiscNumberProperty());
		trackNumberCol.setCellValueFactory(cellData -> cellData.getValue().getTrackNumberProperty());
		bpmCol.setCellValueFactory(cellData -> cellData.getValue().getBpmProperty());
		coverCol.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().getHasCover()));
		coverCol.setCellFactory(CheckBoxTableCell.forTableColumn(coverCol));
		inDiskCol.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().getIsInDisk()));
		inDiskCol.setCellFactory(CheckBoxTableCell.forTableColumn(inDiskCol));
		bpmCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		bpmCol.setCellFactory(columns -> {
			return new TableCell<Track, Number>() {
				@Override
				protected void updateItem(Number item, boolean empty) {
					super.updateItem(item, empty);
					if(empty || item == null)
						setText("");
					else
						if(((int) item) == 0 || ((int) item) == -1)
							setText("");
						else
							setText(""+item);
				}
			};
		});
		discNumberCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		discNumberCol.setCellFactory(columns -> {
			return new TableCell<Track, Number>() {
				@Override
				protected void updateItem(Number item, boolean empty) {
					super.updateItem(item, empty);
					if(empty || item == null)
						setText("");
					else
						if(((int) item) == 0)
							setText("");
						else
							setText(""+item);
				}
			};
		});
		trackNumberCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		trackNumberCol.setCellFactory(columns -> {
			return new TableCell<Track, Number>() {
				@Override
				protected void updateItem(Number item, boolean empty) {
					super.updateItem(item, empty);
					if(empty || item == null)
						setText("");
					else
						if(((int) item) == 0)
							setText("");
						else
							setText(""+item);
				}
			};
		});
		yearCol.setCellFactory(columns -> {
			return new TableCell<Track, Number>() {
				@Override
				protected void updateItem(Number item, boolean empty) {
					super.updateItem(item, empty);
					if(empty || item == null)
						setText("");
					else
						if(((int) item) == 0)
							setText("");
						else
							setText(""+item);
				}
			};
		});
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");
		dateModifiedCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		dateModifiedCol.setCellFactory(column -> {
			return new TableCell<Track,LocalDate>() {
				@Override
				protected void updateItem(LocalDate item, boolean empty) {
					super.updateItem(item, empty);
					if(item == null)
						setText("");
					else
						setText(dateFormatter.format(item));
				}
			}; 
		});
		dateAddedCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		dateAddedCol.setCellFactory(column -> {
			return new TableCell<Track,LocalDate>() {
				@Override
				protected void updateItem(LocalDate item, boolean empty) {
					super.updateItem(item, empty);
					if(item == null)
						setText("");
					else
						setText(dateFormatter.format(item));
				}
			}; 
		});
		sizeCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		sizeCol.setCellFactory(column -> {
			return new TableCell<Track,Number>() {
				@Override
				protected void updateItem(Number item, boolean empty) {
					super.updateItem(item, empty);
					if(item == null)
						setText("");	
					else {
						int kiloBytes = ((int) item)/1024;
						if(kiloBytes < 1024)
							setText(kiloBytes+" KB");
						else {
							int megaBytes = kiloBytes/1024;
							String strKiloBytes = ""+kiloBytes%1024;
							setText(megaBytes+","+(strKiloBytes.length()>1 ? strKiloBytes.substring(0, 1) : strKiloBytes)+" MB");
						}
					}
				}
			};
		});
		totalTimeCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		totalTimeCol.setCellFactory(column -> {
			return new TableCell<Track, Duration>() {
				@Override
				protected void updateItem(Duration item, boolean empty) {
					super.updateItem(item, empty);
					if(item == null)
						setText("");
					else {
						int hours = (int)item.toHours();
						int mins = (int)item.subtract(Duration.hours(hours)).toMinutes();
						int secs = (int)item.subtract(Duration.minutes(mins)).subtract(Duration.hours(hours)).toSeconds();
						setText((hours>0 ? hours+":" : "")+(mins<10 ? "0"+mins : mins)+":"+(secs<10 ? "0"+secs : secs));
					}
				}
			};
		});
		playCountCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		bitRateCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		selection = trackTable.getSelectionModel().getSelectedItems();
		trackTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		trackTable.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> selection = trackTable.getSelectionModel().getSelectedItems()));
		sc = SceneManager.getInstance();
		ml = MusicLibrary.getInstance();
		prevButton.setDisable(true);
		nextButton.setDisable(true);
		playButton.setDisable(true);
		trackSlider.setDisable(true);
		trackSlider.setValue(0.0);
		volumeSlider.setMin(0.0);
		volumeSlider.setMax(1.0);
		volumeSlider.setValue(1.0);
		volumeProgressBar.setProgress(1.0);
		volumeSlider.valueChangingProperty().addListener((observable, wasChanging, isChanging) -> {if(!isChanging) volumeProgressBar.setProgress(volumeSlider.getValue());});
		volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> volumeProgressBar.setProgress(newValue.doubleValue()));
		
		tracks = trackTable.getItems();
		ml.setTracks(tracks);
		if(tracks.size() == 0) {
			playButton.setDisable(true);
			trackSlider.setDisable(true);
			emptyTable = true;
		}

		prevButton.setDisable(true);
		nextButton.setDisable(true);
		
		player = new Mp3Player(tracks);	
		tracks.addListener(((ListChangeListener.Change<? extends Track> c) -> {
			while(c.next()) {
				if(c.wasRemoved()) {
					player.removeTracks(c.getRemoved());
					sc.saveLibrary();
				}
				else
					sc.saveLibrary();
			}
		}));
		
		// Set up the ContextMenu
		MenuItem cmPlay = new MenuItem("Play");
		cmPlay.setOnAction(event -> {if(!selection.isEmpty()) {player.play(selection); setPlaying();}});
		MenuItem cmEdit = new MenuItem("Edit");
		cmEdit.setOnAction(event -> doEdit());
		MenuItem cmDelete = new MenuItem("Delete");
		cmDelete.setOnAction(event -> doDelete());
		MenuItem cmAddToQueue = new MenuItem("Add to Play Queue");
		cmAddToQueue.setOnAction(event -> {if(!selection.isEmpty())	player.addTracks(selection);});
		ContextMenu cm = new ContextMenu();
		cm.getItems().add(cmPlay);
		cm.getItems().add(cmAddToQueue);
		cm.getItems().add(cmEdit);
		cm.getItems().add(cmDelete);
		// Double click on row = play that track
		trackTable.setRowFactory(tv -> {
			TableRow<Track> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if(event.getClickCount() == 2 && !row.isEmpty()) {
					player.play(row.getItem());
					setPlaying();
				}
			});
			return row;
		});
		trackTable.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {if(event.getButton() == MouseButton.SECONDARY) cm.show(trackTable,event.getScreenX(),event.getScreenY());});
		trackTable.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if(event.getCode() == KeyCode.ENTER) {
				player.play(selection);
				setPlaying();
		}});
		trackTable.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if(event.getCode() == KeyCode.SPACE)
				if(player.getMediaPlayer().getStatus() == Status.PLAYING) {
					playButton.setSelected(false);
					player.pause();
				}
				else if(player.getMediaPlayer().getStatus() == Status.PAUSED) {
					playButton.setSelected(true);
					player.play();
				}
		});
	}	
	
	public void addTracks(List<Track> listTracks) {
		if(listTracks != null && !listTracks.isEmpty()) {
			trackTable.getItems().addAll(listTracks);	
			sc.getPlayQueueController().setPlayer(player);
			if(emptyTable) {
				playButton.setDisable(false);
				emptyTable = false;
			}
		}
	}
	
	public void setStage(Stage stage) {
		rootStage = stage;
	}
	
	public void setStopped() {
		playButton.setSelected(false);
		trackSlider.setDisable(true);
		nextButton.setDisable(true);
		prevButton.setDisable(true);
		titleLabel.setText("");
		artistAlbumLabel.setVisible(false);
		currentTimeLabel.setVisible(false);
		remainingTimeLabel.setVisible(false);
		currentCover.setImage(null);
		volumeSlider.valueProperty().unbindBidirectional(player.getMediaPlayer().volumeProperty());
	}
	
	public void setPlaying() {
		playButton.setSelected(true);
		trackSlider.setDisable(false);
		nextButton.setDisable(false);
		prevButton.setDisable(false);
		artistAlbumLabel.setVisible(true);
		currentTimeLabel.setVisible(true);
		remainingTimeLabel.setVisible(true);
	}
	
	public void preparePlayerInfo(MediaPlayer currentPlayer, Track currentTrack) {
		// Set up the player and the view related to it
		currentCover.setImage(new Image("file:"+currentTrack.getCoverFileName()));
		trackSlider.valueProperty().addListener((observable) -> {
			if(trackSlider.isValueChanging()) {
				trackProgressBar.setProgress(trackSlider.getValue() / currentPlayer.getStopTime().toSeconds());
				currentPlayer.seek(Duration.seconds(trackSlider.getValue()));
			}
		});
		trackSlider.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
			trackProgressBar.setProgress(trackSlider.getValue() / currentPlayer.getStopTime().toSeconds());
			currentPlayer.seek(Duration.seconds(trackSlider.getValue()));
		});
		currentPlayer.totalDurationProperty().addListener((observable, oldDuration, newDuration) -> trackSlider.setMax(newDuration.toSeconds()));
		currentPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) -> {if (!trackSlider.isValueChanging()) trackSlider.setValue(newTime.toSeconds());});
		currentPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) -> trackProgressBar.setProgress(newTime.toSeconds() / currentPlayer.getStopTime().toSeconds()));
		currentPlayer.volumeProperty().bindBidirectional(volumeSlider.valueProperty());
		
		titleLabel.setText(currentTrack.getName());
		artistAlbumLabel.setText(currentTrack.getArtist()+" - "+currentTrack.getAlbum());
		currentPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) -> formatTime(newTime,currentPlayer.getMedia().getDuration()));
	}
	
	private void formatTime(Duration elapsed, Duration total) {
		int currentHours = (int)elapsed.toHours();
		int currentMins = (int)elapsed.subtract(Duration.hours(currentHours)).toMinutes();
		int currentSecs = (int)elapsed.subtract(Duration.minutes(currentMins)).subtract(Duration.hours(currentHours)).toSeconds();
		currentTimeLabel.setText(((int)total.toHours()>0 ? currentHours+":" : "")+(currentMins<10 ? "0"+currentMins : currentMins)+":"+(currentSecs<10 ? "0"+currentSecs : currentSecs));
		
		Duration remaining = total.subtract(elapsed);
		int remainingHours = (int)remaining.toHours();
		int remainingMins = (int)remaining.subtract(Duration.hours(remainingHours)).toMinutes();
		int remainingSecs = (int)remaining.subtract(Duration.minutes(remainingMins)).subtract(Duration.hours(remainingHours)).toSeconds();
		remainingTimeLabel.setText("-"+((int)total.toHours()>0 ? remainingHours+":" : "")+(remainingMins<10 ? "0"+remainingMins : remainingMins)+":"+(remainingSecs<10 ? "0"+remainingSecs : remainingSecs));
	}
	
	@FXML
	private void doShowHidePlayQueue() {
		sc.showHidePlayQueue();
	}
	
	@FXML
	private void doPlayPause() {
		if(playButton.isSelected()) {
			player.play();
			if(trackSlider.isDisabled())
				trackSlider.setDisable(false);
			if(prevButton.isDisabled())
				prevButton.setDisable(false);
			if(nextButton.isDisabled())
				nextButton.setDisable(false);
		}
		else
			player.pause();
	}
	
	@FXML
	private void doNext() {
		if(!nextButton.isDisabled())
			player.next();
	}
	
	@FXML
	private void doPrevious() {	
		if(!prevButton.isDisabled())
			player.previous();
	}
	
	@FXML
	private void doIncreaseVolume() {
		if(player != null)
			player.increaseVolume(AMOUNT_VOLUME);
		volumeSlider.setValue(volumeSlider.getValue()+AMOUNT_VOLUME);
	}
	
	@FXML
	private void doDecreaseVolume() {
		if(player != null)
			player.decreaseVolume(AMOUNT_VOLUME);
		volumeSlider.setValue(volumeSlider.getValue()-AMOUNT_VOLUME);
	}
	
	@FXML
	private void doSelectCurrentTrack() {
		trackTable.getSelectionModel().clearSelection();
		trackTable.getSelectionModel().select(player.getCurrentTrack());
	}
	
	@FXML
	private void doDelete() {
		if(selection != null && selection.size() !=0) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("");
			alert.setHeaderText("");
			alert.setContentText("Delete this files from Musicott?");
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK) {
				tracks.removeAll(selection);
				if(tracks.size() == 0) {
					prevButton.setDisable(true);
					nextButton.setDisable(true);
					playButton.setDisable(true);
					trackSlider.setDisable(true);
					emptyTable = true;
				}
			}
			else
				alert.close();
		}
	}
	
	@FXML
	private void doEdit() {
		if(selection != null & selection.size() !=0) {
			if(selection.size() > 1) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("");
				alert.setHeaderText("");
				alert.setContentText("Are you sure you want to edit multiple files?");
				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK)
					sc.openEditScene(selection);
				else
					alert.close();
			}
			else
				sc.openEditScene(selection);
		}
	}
	
	@FXML
	private void doOpen() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Open file(s)...");
		chooser.getExtensionFilters().addAll(
				new ExtensionFilter("All Supported (*.mp3, *.flac)","*.mp3", "*.flac"), //TODO m4a & wav when implemented
				new ExtensionFilter("Mp3 Files", "*.mp3"),
				new ExtensionFilter("Flac Files","*.flac"));
		List<File> files = chooser.showOpenMultipleDialog(rootStage);
		if(files != null) {
			OpenTask task = new OpenTask(files);
			sc.showImportProgressScene(task,false);
			Thread t = new Thread(task, "OpenThread");
			t.setDaemon(true);
			t.start();
		}
	}
	
	@FXML
	private void doImportCollection() {
		sc.openImportScene();
	}
	
	@FXML
	private void doAbout() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("About Musicott");
		alert.setHeaderText("Musicott");
		alert.setContentText("Version 0.3.1\n\nCopyright © 2015 Octavio Calleya https://github.com/octaviospain/Musicott/ \n\nLicensed under GNU GPLv3. This product includes software developed by other open source projects.");
		ImageView iv = new ImageView();
		iv.setImage(new Image("file:resources/images/musicotticon.png"));
		alert.setGraphic(iv);
		alert.showAndWait();
	}
	
	@FXML
	private void handleExit() {
		System.exit(0);
	}
}