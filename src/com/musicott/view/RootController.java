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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.controlsfx.control.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.musicott.SceneManager;
import com.musicott.model.MusicLibrary;
import com.musicott.model.Track;
import com.musicott.player.FlacPlayer;
import com.musicott.player.NativePlayer;
import com.musicott.player.PlayerFacade;
import com.musicott.player.TrackPlayer;
import com.musicott.task.TaskPoolManager;
import com.musicott.view.custom.WaveformPanel;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
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
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;
import javafx.util.Duration;

/**
 * @author Octavio Calleya
 *
 */
public class RootController {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass().getName());
	private static final double AMOUNT_VOLUME = 0.05;

	@FXML
	private BorderPane rootBorderPane;
	@FXML
	private Menu menuFile;
	@FXML
	private MenuItem menuItemItunesImport;
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
	private MenuItem menuItemPrev;
	@FXML
	private MenuItem menuItemNext;
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
	private SwingNode waveformSwingNode;
	@FXML
	private StackPane playerStackPane;
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
	private TextField searchTextField;
	@FXML
	private ProgressBar volumeProgressBar;
	@FXML
	private TableView<Map.Entry<Integer, Track>> trackTable;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, String> nameCol;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, String> artistCol;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, String> albumCol;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, String> genreCol;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, String> commentsCol;
	@FXML 
	private TableColumn<Map.Entry<Integer, Track>, String> albumArtistCol;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, String> labelCol;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, LocalDateTime> dateModifiedCol;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, LocalDateTime> dateAddedCol;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, Number> sizeCol;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, Duration> totalTimeCol;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, Number> trackNumberCol;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, Number> yearCol;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, Number> bitRateCol;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, Number> playCountCol;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, Number> discNumberCol;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, Number> bpmCol;
	@FXML
	private TableColumn<Map.Entry<Integer, Track>, Boolean> coverCol;
	private AnchorPane playQueuePane;
	private StatusBar statusBar;

	private ObservableMap<Integer, Track> map;
	private ObservableList<Map.Entry<Integer, Track>> tracks;
	private List<Map.Entry<Integer, Track>> selection;
	private Stage rootStage;
	private SceneManager sc;
	private MusicLibrary ml;
	private PlayerFacade player;
	private WaveformPanel mainWaveformPane;
	private HostServices hostServices;
	
	public RootController() {
	}
	
	@FXML
	public void initialize() {
		sc = SceneManager.getInstance();
		ml = MusicLibrary.getInstance();
		map = ml.getTracks();
		tracks = FXCollections.observableArrayList(map.entrySet());
		map.addListener((MapChangeListener.Change<? extends Integer, ? extends Track> c) -> {
			if(c.wasAdded()) {
				Track added = c.getValueAdded();
	            tracks.add(new AbstractMap.SimpleEntry<Integer, Track>(added.getTrackID(), added));
				if(playButton.isDisable())
					playButton.setDisable(false);
	        }
			else if (c.wasRemoved()) {
	          	Track removed = c.getValueRemoved();
	            tracks.remove(new AbstractMap.SimpleEntry<Integer, Track>(removed.getTrackID(), removed));
				LOG.info("Deleted track: {}", removed);
			}
			if(tracks.isEmpty())
				playButton.setDisable(true);
		});
		
		if(map.isEmpty())
			playButton.setDisable(true);
		prevButton.setDisable(true);
		menuItemPrev.disableProperty().bind(prevButton.disableProperty());
		nextButton.setDisable(true);
		menuItemNext.disableProperty().bind(nextButton.disableProperty());
		trackSlider.setDisable(true);
		trackSlider.setValue(0.0);
		volumeSlider.setMin(0.0);
		volumeSlider.setMax(1.0);
		volumeSlider.setValue(1.0);
		volumeProgressBar.setProgress(1.0);
		volumeSlider.valueChangingProperty().addListener((observable, wasChanging, isChanging) -> {if(!isChanging) volumeProgressBar.setProgress(volumeSlider.getValue());});
		volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> volumeProgressBar.setProgress(newValue.doubleValue()));
		initColumns();
		
		statusBar = new StatusBar();
		statusBar.setMaxHeight(3.0);
		statusBar.setText("");
		rootBorderPane.setBottom(statusBar);
		SwingUtilities.invokeLater(() -> {
			mainWaveformPane = new WaveformPanel(520, 50);
            waveformSwingNode.setContent(mainWaveformPane);
		});
		playerStackPane.getChildren().add(0, waveformSwingNode);
	
		player = PlayerFacade.getInstance();
		selection = trackTable.getSelectionModel().getSelectedItems();
		trackTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		trackTable.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> selection = trackTable.getSelectionModel().getSelectedItems()));
		trackTable.getSortOrder().add(dateAddedCol);
		
		FilteredList<Map.Entry<Integer, Track>> filteredTracks = new FilteredList<>(tracks, predicate -> true);
		searchTextField.textProperty().addListener((observable, oldText, newText) -> {
			filteredTracks.setPredicate(trackEntry -> {
				boolean result = true;
				Track track = trackEntry.getValue();
				if(newText != null && !newText.isEmpty()) {
					if(track.getName().toLowerCase().contains(newText.toLowerCase()) ||
					   track.getArtist().toLowerCase().contains(newText.toLowerCase()) ||
					   track.getLabel().toLowerCase().contains(newText.toLowerCase()) ||
					   track.getGenre().toLowerCase().contains(newText.toLowerCase()) ||
					   track.getAlbum().toLowerCase().contains(newText.toLowerCase()))
						result = true;
					else	
						result = false;
				}
				return result;
			});
		});
		SortedList<Map.Entry<Integer, Track>> sortedTracks = new SortedList<>(filteredTracks);
		sortedTracks.comparatorProperty().bind(trackTable.comparatorProperty());
		trackTable.setItems(sortedTracks);
		
		// Set up the ContextMenu
		MenuItem cmPlay = new MenuItem("Play");
		cmPlay.setOnAction(event -> {if(!selection.isEmpty()) player.addTracks(selection.stream().map(Map.Entry::getKey).collect(Collectors.toList()), true);});
		MenuItem cmEdit = new MenuItem("Edit");
		cmEdit.setOnAction(event -> doEdit());
		MenuItem cmDelete = new MenuItem("Delete");
		cmDelete.setOnAction(event -> doDelete());
		MenuItem cmAddToQueue = new MenuItem("Add to Play Queue");
		cmAddToQueue.setOnAction(event -> {
			if(!selection.isEmpty())
				player.addTracks(selection.stream().map(Map.Entry::getKey).collect(Collectors.toList()), false);
		});
		ContextMenu cm = new ContextMenu();
		cm.getItems().add(cmPlay);
		cm.getItems().add(cmAddToQueue);
		cm.getItems().add(cmEdit);
		cm.getItems().add(cmDelete);
		
		// Double click on row = play that track
		trackTable.setRowFactory(tv -> {
			TableRow<Map.Entry<Integer, Track>> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if(event.getClickCount() == 2 && !row.isEmpty()) {
					List<Integer> singleTrackIDList = new ArrayList<>();
					singleTrackIDList.add(row.getItem().getKey());
					player.addTracks(singleTrackIDList, true);
				}
			});
			return row;
		});
		// Right click on row = show ContextMenu
		trackTable.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			if(event.getButton() == MouseButton.SECONDARY) {
				cm.show(trackTable,event.getScreenX(),event.getScreenY());
				LOG.debug("Showing context menu");
			}
			else if(event.getButton() == MouseButton.PRIMARY && cm.isShowing())
				cm.hide();
		});
		// Enter key pressed = play; Space key = pause/resume
		trackTable.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
			if(event.getCode() == KeyCode.ENTER) {
				player.addTracks(selection.stream().map(Map.Entry::getKey).collect(Collectors.toList()), true);
			}
			else if(event.getCode() == KeyCode.SPACE) {
				String playerStatus = player.getTrackPlayer().getStatus();
				if(playerStatus.equals("PLAYING"))
					player.pause();
				else if(playerStatus.equals("PAUSED"))
					player.resume();
				else if(playerStatus.equals("STOPPED"))
					player.play(true);
			}
		});
	}
	
	private void initColumns() {
		Callback<TableColumn<Map.Entry<Integer, Track>,Number>, TableCell<Map.Entry<Integer, Track>,Number>> numericCallback = columns -> new TableCell<Map.Entry<Integer, Track>, Number>() {
			@Override
			protected void updateItem(Number item, boolean empty) {
				super.updateItem(item, empty);
				if(empty || item == null)
					setText("");
				else if(((int) item) < 1)
						setText("");
					else
						setText(""+item);
			}
		};
		Callback<TableColumn<Map.Entry<Integer, Track>, LocalDateTime>, TableCell<Map.Entry<Integer, Track>, LocalDateTime>> dateCallback = column -> new TableCell<Map.Entry<Integer, Track>, LocalDateTime>() {
			@Override
			protected void updateItem(LocalDateTime item, boolean empty) {
				super.updateItem(item, empty);
				DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
				if(item == null)
					setText("");
				else
					setText(item.format(dateFormatter));
			}
		};
		nameCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getNameProperty());
		artistCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getArtistProperty());
		albumCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getAlbumProperty());
		genreCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getGenreProperty());
		commentsCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getCommentsProperty());
		albumArtistCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getAlbumArtistProperty());
		labelCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getLabelProperty());
		dateModifiedCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getDateModifiedProperty());
		dateModifiedCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		dateModifiedCol.setCellFactory(dateCallback);
		dateAddedCol.setCellValueFactory(cellData -> new SimpleObjectProperty<LocalDateTime>(cellData.getValue().getValue().getDateAdded()));
		dateAddedCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		dateAddedCol.setSortType(TableColumn.SortType.DESCENDING); 	// Default sort of the table
		dateAddedCol.setCellFactory(dateCallback);
		sizeCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getValue().getSize()));
		sizeCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		sizeCol.setCellFactory(column -> {return new TableCell<Map.Entry<Integer, Track>,Number>() {
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
			};});
		totalTimeCol.setCellValueFactory(cellData -> new SimpleObjectProperty<Duration>(cellData.getValue().getValue().getTotalTime()));
		totalTimeCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		totalTimeCol.setCellFactory(column -> {return new TableCell<Map.Entry<Integer, Track>, Duration>() {
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
			};});
		yearCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getYearProperty());
		yearCol.setCellFactory(numericCallback);
		yearCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		playCountCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getPlayCountProperty());
		playCountCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		discNumberCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getDiscNumberProperty());
		discNumberCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		discNumberCol.setCellFactory(numericCallback);
		trackNumberCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getTrackNumberProperty());
		trackNumberCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		trackNumberCol.setCellFactory(numericCallback);
		bitRateCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getValue().getBitRate()));
		bitRateCol.setCellFactory(columns -> new TableCell<Map.Entry<Integer, Track>, Number>() {
			@Override
			protected void updateItem(Number item, boolean empty) {
				super.updateItem(item, empty);
				if(empty || item == null)
					setText("");
				else if(((int) item) == 0)
						setText("");
					else
						setText(""+item);
			}
		});
		bitRateCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		coverCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getHasCoverProperty());
		coverCol.setCellFactory(CheckBoxTableCell.forTableColumn(coverCol));
		bpmCol.setCellValueFactory(cellData -> cellData.getValue().getValue().getBpmProperty());
		bpmCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		bpmCol.setCellFactory(numericCallback);
	}
	
	public void setStage(Stage stage) {
		rootStage = stage;
		rootStage.setOnCloseRequest(event -> handleExit());
	}
	
	public void setApplicationHostServices(HostServices applicationHostServices) {
		this.hostServices = applicationHostServices;
	}
	
	public void setStatusMessage(String message) {
		statusBar.setText(message);
	}
	
	public void setStatusProgress(double progress) {
		statusBar.setProgress(progress);
	}
	
	public void preparePlayerInfo(TrackPlayer currentPlayer, Track currentTrack) {
		// Set up the player and the view related to it
		LOG.debug("Setting up player and view for track {}", currentTrack);
		if(ml.containsWaveform(currentTrack.getTrackID()))
			setWaveform(currentTrack);
		else if (currentTrack.getFileFormat().equals("wav") || currentTrack.getFileFormat().equals("mp3") || currentTrack.getFileFormat().equals("m4a")) 
			TaskPoolManager.getInstance().addTrackToProcess(currentTrack);
		if(currentPlayer instanceof NativePlayer)
			setUpPlayer(((NativePlayer) currentPlayer).getMediaPlayer());
		else if(currentPlayer instanceof FlacPlayer)
			setUpPlayer((FlacPlayer) currentPlayer);
		
		SwingUtilities.invokeLater(() -> mainWaveformPane.setTrack(currentTrack));
		titleLabel.textProperty().bind(currentTrack.getNameProperty());;
		artistAlbumLabel.textProperty().bind(Bindings.createStringBinding(
				() -> currentTrack.getArtistProperty().get()+" - "+currentTrack.getAlbumProperty().get(), currentTrack.getArtistProperty(), currentTrack.getAlbumProperty())
		);
		if(currentTrack.hasCover())
			currentCover.setImage(new Image(new ByteArrayInputStream(currentTrack.getCoverBytes())));
		else
			currentCover.setImage(new Image(getClass().getResourceAsStream("/images/default-cover-image.png")));
	//	currentTrack.getHasCoverProperty().addListener(observable -> currentCover.setImage(new Image(new ByteArrayInputStream(currentTrack.getCoverBytes()))));
	}
	
	public void setWaveform(Track track) {
		mainWaveformPane.setTrack(track);
	}
	
	private void setUpPlayer(MediaPlayer mediaPlayer) {
		trackSlider.valueProperty().addListener((observable) -> {
			double endTime = mediaPlayer.getStopTime().toMillis();
			if(trackSlider.isValueChanging() && (!(endTime == Double.POSITIVE_INFINITY) || !(endTime == Double.NaN))) {
				trackProgressBar.setProgress(trackSlider.getValue() / endTime);
				mediaPlayer.seek(Duration.millis(trackSlider.getValue()));
			}
		});
		trackSlider.addEventHandler(MouseEvent.MOUSE_CLICKED, (event) -> {
			double endTime = mediaPlayer.getStopTime().toMillis();
			if(!(endTime == Double.POSITIVE_INFINITY) || !(endTime == Double.NaN)) {
			trackProgressBar.setProgress(trackSlider.getValue() / endTime);
			mediaPlayer.seek(Duration.millis(trackSlider.getValue()));
			}
		});
		mediaPlayer.totalDurationProperty().addListener((observable, oldDuration, newDuration) -> trackSlider.setMax(newDuration.toMillis()));
		mediaPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) -> {if (!trackSlider.isValueChanging()) trackSlider.setValue(newTime.toMillis());});
		mediaPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) -> trackProgressBar.setProgress(newTime.toMillis() / mediaPlayer.getStopTime().toMillis()));
		mediaPlayer.volumeProperty().bindBidirectional(volumeSlider.valueProperty());
		mediaPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) -> formatTime(newTime,mediaPlayer.getMedia().getDuration()));	
		mediaPlayer.statusProperty().addListener((observable, oldStatus, newStatus) -> {
			if(newStatus == Status.PLAYING) {
				setPlaying();
			}
			else if (newStatus == Status.PAUSED) {
				playButton.setSelected(false);
			}
			else if (newStatus == Status.STOPPED) {
				setStopped();
			}
		});
		mediaPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) -> {
			if(newTime.greaterThanOrEqualTo(mediaPlayer.getStopTime().divide(2.0)))
				player.incrementCurentTrackPlayCount();
		});
	}
	
	private void setUpPlayer(FlacPlayer flacPlayer) {
		//TODO
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
	
	private void setPlaying() {
		playButton.setSelected(true);
		trackSlider.setDisable(false);
		nextButton.setDisable(false);
		prevButton.setDisable(false);
		currentCover.setVisible(true);
	}
	
	public void setStopped() {
		playButton.setSelected(false);
		trackSlider.setDisable(true);
		nextButton.setDisable(true);
		prevButton.setDisable(true);
		titleLabel.textProperty().unbind();
		titleLabel.setText("");
		artistAlbumLabel.textProperty().unbind();
		artistAlbumLabel.setText("");
		currentCover.setVisible(false);
		currentTimeLabel.setText("");
		remainingTimeLabel.setText("");
		setStatusMessage("");
		SwingUtilities.invokeLater(() -> mainWaveformPane.clear());
		if(player.getTrackPlayer() instanceof NativePlayer)
			volumeSlider.valueProperty().unbindBidirectional(((NativePlayer)player.getTrackPlayer()).getMediaPlayer().volumeProperty());
		else if (player.getTrackPlayer() instanceof FlacPlayer) {
			//TODO
		}
	}
	
	@FXML
	private void doShowHidePlayQueue() {
		if(playQueuePane == null)
			playQueuePane = (AnchorPane) rootStage.getScene().lookup("#playQueuePane");
		if(playQueuePane.isVisible())
			playQueuePane.setVisible(false);
		else
			playQueuePane.setVisible(true);
		LOG.trace("Play queue show/hide");
	}
	
	@FXML
	private void doPlayPause() {
		LOG.trace("Play/pause button clicked");
		if(playButton.isSelected()) {	// play
			if(player.getCurrentTrack() != null)
				player.resume();
			else
				player.play(true);
		}
		else							// pause
			player.pause();
	}
	
	@FXML
	private void doNext() {
		player.next();
	}
	
	@FXML
	private void doPrevious() {	
		player.previous();
	}
	
	@FXML
	private void doIncreaseVolume() {
		if(player != null)
			player.increaseVolume(AMOUNT_VOLUME);
		volumeSlider.setValue(volumeSlider.getValue()+AMOUNT_VOLUME);
		LOG.trace("Volume increased "+volumeSlider.getValue());
	}
	
	@FXML
	private void doDecreaseVolume() {
		if(player != null)
			player.decreaseVolume(AMOUNT_VOLUME);
		volumeSlider.setValue(volumeSlider.getValue()-AMOUNT_VOLUME);
		LOG.trace("Volume decreased "+volumeSlider.getValue());
	}
	
	@FXML
	private void doSelectCurrentTrack() {
		Track currentTrack = player.getCurrentTrack();
		trackTable.getSelectionModel().clearSelection();
		if(currentTrack != null) {
			Map.Entry<Integer, Track> currentEntry = new AbstractMap.SimpleEntry<Integer, Track>(currentTrack.getTrackID(), currentTrack);
			trackTable.getSelectionModel().select(currentEntry);
			trackTable.scrollTo(currentEntry);
		}
		LOG.debug("Current track in the player selected in the table");
	}
	
	@FXML
	private void doDelete() {
		if(selection != null && !selection.isEmpty()) {
			int numDeletedTracks = selection.size();
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/dialog.css").toExternalForm());
			alert.setTitle("");
			alert.setHeaderText("");
			alert.setContentText("Delete "+numDeletedTracks+" files from Musicott?");
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK) {
				new Thread(() -> {
					ml.removeTracks(selection.stream().map(Map.Entry::getKey).collect(Collectors.toList()));
					Platform.runLater(() -> sc.closeIndeterminatedProgressScene());
				}).start();
				sc.openIndeterminatedProgressScene();
			}
			else
				alert.close();
		}
	}
	
	@FXML
	private void doEdit() {
		if(selection != null & !selection.isEmpty()) {
			if(selection.size() > 1) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/dialog.css").toExternalForm());
				alert.setTitle("");
				alert.setHeaderText("");
				alert.setContentText("Are you sure you want to edit multiple files?");
				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK) {
					sc.openEditScene(selection.stream().map(Map.Entry::getValue).collect(Collectors.toList()));
					LOG.debug("Opened edit stage for various tracks");
				}
				else
					alert.close();
			}
			else {
				sc.openEditScene(selection.stream().map(Map.Entry::getValue).collect(Collectors.toList()));
				LOG.debug("Opened edit stage for a single track");
			}
		}
	}
	
	@FXML
	private void doOpen() {
		LOG.debug("Selecting files to open");
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Open file(s)...");
		chooser.getExtensionFilters().addAll(
				new ExtensionFilter("All Supported (*.mp3, *.flac, *.wav, *.m4a)","*.mp3", "*.flac", "*.wav", "*.m4a"),
				new ExtensionFilter("mp3 files (*.mp3)", "*.mp3"),
				new ExtensionFilter("flac files (*.flac)","*.flac"),
				new ExtensionFilter("wav files (*.wav)", "*.wav"),
				new ExtensionFilter("m4a files (*.wav)", "*.m4a"));
		List<File> files = chooser.showOpenMultipleDialog(rootStage);
		if(files != null) {
			TaskPoolManager.getInstance().parseFiles(files, true);
			setStatusMessage("Opening files");
		}
	}
	
	@FXML
	private void doImportFolder() {
		sc.openImportScene();
	}
	
	@FXML
	private void doItunesImport() {
		sc.openItunesImportScene();
	}
	
	@FXML
	private void doAbout() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/dialog.css").toExternalForm());
		alert.setTitle("About Musicott");
		alert.setHeaderText("Musicott");
		Label text = new Label(" Version 0.7.0\n\n Copyright © 2015 Octavio Calleya.");
		Label text2 = new Label(" Licensed under GNU GPLv3. This product includes\n software developed by other open source projects.");
		Hyperlink githubLink = new Hyperlink("https://github.com/octaviospain/Musicott/");
		githubLink.setOnAction(event -> hostServices.showDocument(githubLink.getText()));
		FlowPane fp = new FlowPane();
		fp.getChildren().addAll(text, githubLink, text2);
		alert.getDialogPane().contentProperty().set(fp);
		ImageView iv = new ImageView();
		iv.setImage(new Image("file:resources/images/musicotticon.png"));
		alert.setGraphic(iv);
		alert.showAndWait();
		LOG.debug("Showing about window");
	}
	
	@FXML
	private void handleExit() {
		LOG.info("Exiting Musicott");
		System.exit(0);
	}
}