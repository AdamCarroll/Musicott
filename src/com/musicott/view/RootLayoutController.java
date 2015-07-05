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
import com.musicott.model.ObservableTrack;
import com.musicott.task.OpenTask;
import com.musicott.task.SaveLibraryTask;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * @author Octavio Calleya
 *
 */
public class RootLayoutController {
	
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
	private TableView<ObservableTrack> trackTable;
	@FXML
	private TableColumn<ObservableTrack,String> nameCol;
	@FXML
	private TableColumn<ObservableTrack,String> artistCol;
	@FXML
	private TableColumn<ObservableTrack,String> albumCol;
	@FXML
	private TableColumn<ObservableTrack,String> genreCol;
	@FXML
	private TableColumn<ObservableTrack,String> commentsCol;
	@FXML
	private TableColumn<ObservableTrack,String> albumArtistCol;
	@FXML
	private TableColumn<ObservableTrack,String> labelCol;
	@FXML
	private TableColumn<ObservableTrack,LocalDate> dateModifiedCol;
	@FXML
	private TableColumn<ObservableTrack,LocalDate> dateAddedCol;
	@FXML
	private TableColumn<ObservableTrack,Number> sizeCol;
	@FXML
	private TableColumn<ObservableTrack,Number> totalTimeCol;
	@FXML
	private TableColumn<ObservableTrack,Number> trackNumberCol;
	@FXML
	private TableColumn<ObservableTrack,Number> yearCol;
	@FXML
	private TableColumn<ObservableTrack,Number> bitRateCol;
	@FXML
	private TableColumn<ObservableTrack,Number> playCountCol;
	@FXML
	private TableColumn<ObservableTrack,Number> discNumberCol;
	@FXML
	private TableColumn<ObservableTrack,Number> bpmCol;
	@FXML
	private TableColumn<ObservableTrack,Boolean> coverCol;
	@FXML
	private TableColumn<ObservableTrack,Boolean> inDiskCol;

	private ObservableList<ObservableTrack> tracks;
	private ObservableList<ObservableTrack> selection;
	
	private Stage rootStage;
	
	private SceneManager sc;
	
 	public RootLayoutController() {
	}
	
	@FXML
	public void initialize() {
		nameCol.setCellValueFactory(cellData -> cellData.getValue().getName());
		artistCol.setCellValueFactory(cellData -> cellData.getValue().getArtist());
		albumCol.setCellValueFactory(cellData -> cellData.getValue().getAlbum());
		genreCol.setCellValueFactory(cellData -> cellData.getValue().getGenre());
		commentsCol.setCellValueFactory(cellData -> cellData.getValue().getComments());
		albumArtistCol.setCellValueFactory(cellData -> cellData.getValue().getAlbumArtist());
		labelCol.setCellValueFactory(cellData -> cellData.getValue().getLabel());
		dateModifiedCol.setCellValueFactory(cellData -> cellData.getValue().getDateModified());
		dateAddedCol.setCellValueFactory(cellData -> cellData.getValue().getDateAdded());
		sizeCol.setCellValueFactory(cellData -> cellData.getValue().getSize());
		totalTimeCol.setCellValueFactory(cellData -> cellData.getValue().getTotalTime());
		yearCol.setCellValueFactory(cellData -> cellData.getValue().getYear());
		bitRateCol.setCellValueFactory(cellData -> cellData.getValue().getBitRate());
		playCountCol.setCellValueFactory(cellData -> cellData.getValue().getPlayCount());
		discNumberCol.setCellValueFactory(cellData -> cellData.getValue().getDiscNumber());
		trackNumberCol.setCellValueFactory(cellData -> cellData.getValue().getTrackNumber());
		bpmCol.setCellValueFactory(cellData -> cellData.getValue().getBPM());
		coverCol.setCellValueFactory(cellData -> cellData.getValue().getHasCover());
		coverCol.setCellFactory(CheckBoxTableCell.forTableColumn(coverCol));
		inDiskCol.setCellValueFactory(cellData -> cellData.getValue().getIsInDisk());
		inDiskCol.setCellFactory(CheckBoxTableCell.forTableColumn(inDiskCol));
		bpmCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		bpmCol.setCellFactory(columns -> {
			return new TableCell<ObservableTrack, Number>() {
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
			return new TableCell<ObservableTrack, Number>() {
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
			return new TableCell<ObservableTrack, Number>() {
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
			return new TableCell<ObservableTrack, Number>() {
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
			return new TableCell<ObservableTrack,LocalDate>() {
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
			return new TableCell<ObservableTrack,LocalDate>() {
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
			return new TableCell<ObservableTrack,Number>() {
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
			return new TableCell<ObservableTrack, Number>() {
				@Override
				protected void updateItem(Number item, boolean empty) {
					super.updateItem(item, empty);
					if(item == null)
						setText("");
					else {
						int seconds = (int) item;
						if(seconds<1)
							setText("-");
						else
							if(seconds<60) {
								setText("0:"+(seconds<10 ? "0"+seconds : seconds));
							}
							else
								setText(seconds/60+":"+(seconds%60<10 ? "0"+seconds%60 : seconds%60));
					}
				}
			};
		});
		playCountCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		bitRateCol.setStyle("-fx-alignment: CENTER-RIGHT;");
		tracks = trackTable.getItems();
		selection = trackTable.getSelectionModel().getSelectedItems();
		trackTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		trackTable.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> selection = trackTable.getSelectionModel().getSelectedItems()));
		sc = SceneManager.getInstance();
	}	
	
	public void addTracks(List<ObservableTrack> listTracks) {
		if(listTracks != null && listTracks.size()!=0) {
			trackTable.getItems().addAll(listTracks);
			MusicLibrary.getInstance().setTracks(tracks);
		}
	}
	
	public void setStage(Stage stage) {
		rootStage = stage;
	}
	
	@FXML
	private void doDelete() {
		if(selection != null && selection.size() !=0) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("");
			alert.setHeaderText("");
			alert.setContentText("Delete this files from Musicott?");
			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK)
				tracks.removeAll(selection);
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
			Thread t = new Thread(task);
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
		alert.setContentText("Version 0.2.0\n\nCopyright © 2015 Octavio Calleya https://github.com/octaviospain/Musicott/ \n\nLicensed under GNU GPLv3. This product includes software developed by other open source projects.");
		ImageView iv = new ImageView();
		iv.setImage(new Image("file:resources/images/musicotticon.png"));
		alert.setGraphic(iv);
		alert.showAndWait();
	}
	
	@FXML
	private void handleExit() {
		SaveLibraryTask task = new SaveLibraryTask();
		sc.showImportProgressScene(task,true);
		System.exit(0);
	}
}