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
 * Copyright (C) 2005, 2006 Octavio Calleya
 */

package com.musicott.view.custom;

import com.musicott.*;
import com.musicott.model.*;
import com.musicott.player.*;
import com.musicott.tasks.*;
import com.musicott.util.*;
import de.codecentric.centerdevice.*;
import javafx.application.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.input.KeyCombination.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.stage.FileChooser.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;

import static javafx.scene.input.KeyCodeCombination.ALT_DOWN;

/**
 * Creates a MenuBar or a native OS X menu bar for Musicott
 * 
 * @author Octavio Calleya
 *
 */
public class MusicottMenuBar extends MenuBar {
	
	private final Logger LOG = LoggerFactory.getLogger(getClass().getName());
	
	private StageDemon stageDemon = StageDemon.getInstance();
	
	private Menu fileMN, editMN, controlsMN, viewMN, aboutMN;
	private MenuItem openFileMI, importFolderMI, importItunesMI, preferencesMI, editMI, deleteMI, prevMI, nextMI, volIncrMI, volDecrMI, selCurrMI, aboutMI;
	private MenuItem newPlaylistMI, showHideNavigationPaneMI, showHideTableInfoPaneMI;
	private VBox headerVBox;
	
	public MusicottMenuBar(VBox headerMenuBarVBox) {
		super();
		headerVBox = headerMenuBarVBox;
		String os = System.getProperty ("os.name");
		// Key acceleratos. Command down for os x and control down for windows and linux
		Modifier keyModifierOS = os != null && os.startsWith ("Mac") ? KeyCodeCombination.META_DOWN : KeyCodeCombination.CONTROL_DOWN;
		
		createMenus();		
		setAccelerators(keyModifierOS);
		
		// actions and bindings for the menus
		setFileMenuActions();
		setEditMenuActions();
		setControlsMenuActions();
		setViewMenuActions();
		
		aboutMI.setOnAction(e -> {
			Alert alert = stageDemon.createAlert("About Musicott", "Musicott", "", AlertType.INFORMATION);
			Label text = new Label(" Version 0.8.0\n\n Copyright © 2015 Octavio Calleya.");
			Label text2 = new Label(" Licensed under GNU GPLv3. This product includes\n software developed by other open source projects.");
			Hyperlink githubLink = new Hyperlink("https://github.com/octaviospain/Musicott/");
			githubLink.setOnAction(event -> stageDemon.getApplicationHostServices().showDocument(githubLink.getText()));
			FlowPane fp = new FlowPane();
			fp.getChildren().addAll(text, githubLink, text2);
			alert.getDialogPane().contentProperty().set(fp);
			ImageView iv = new ImageView();
			iv.setImage(new Image("file:resources/images/musicotticon.png"));
			alert.setGraphic(iv);
			alert.showAndWait();
			LOG.debug("Showing about window");
		});
		
		if (os != null && os.startsWith ("Mac")) {
			MenuToolkit tk = MenuToolkit.toolkit();
			
			Menu appMenu = new Menu("Musicott");
			appMenu.getItems().addAll(preferencesMI, new SeparatorMenuItem(), tk.createQuitMenuItem("Musicott"));				
			Menu windowMenu = new Menu("Window");
			windowMenu.getItems().addAll(tk.createMinimizeMenuItem(), tk.createZoomMenuItem(),
			new SeparatorMenuItem(), tk.createBringAllToFrontItem());
			
			tk.setApplicationMenu(appMenu);
			getMenus().addAll(appMenu, fileMN, editMN, controlsMN, viewMN, windowMenu, aboutMN);
			tk.autoAddWindowMenuItems(windowMenu);
			tk.setGlobalMenuBar(this);
			LOG.debug("OS X native menubar created");
		}
		else {	// Default MenuBar
			MenuItem closeMI = new MenuItem("Close");
			closeMI.setAccelerator(new KeyCodeCombination (KeyCode.F4, ALT_DOWN));
			closeMI.setOnAction(event -> {LOG.info("Exiting Musicott"); System.exit(0);});
			fileMN.getItems().addAll(new SeparatorMenuItem(), preferencesMI, new SeparatorMenuItem(), closeMI);
			
			getMenus().addAll(fileMN, editMN, controlsMN, viewMN, aboutMN);
	
			headerVBox.getChildren().add(0, this);
			LOG.debug("Default menubar created");
		}
	}
	
	private void createMenus() {
		fileMN = new Menu("File");
		editMN = new Menu("Menu");
		controlsMN = new Menu("Controls");
		viewMN = new Menu("View");
		aboutMN = new Menu("About");
		openFileMI = new MenuItem("Open File(s)...");
		importFolderMI = new MenuItem("Import Folder...");
		importItunesMI = new MenuItem("Import from iTunes Library...");
		preferencesMI = new MenuItem("Preferences");
		editMI = new MenuItem("Edit");
		deleteMI = new MenuItem("Delete");
		prevMI = new MenuItem("Previous");
		nextMI = new MenuItem("Next");
		volIncrMI = new MenuItem("Increase volume");
		volDecrMI = new MenuItem("Decrease volume");
		selCurrMI = new MenuItem("Select Current Track");
		aboutMI = new MenuItem("About");
		newPlaylistMI = new MenuItem("Add new playlist");
		showHideNavigationPaneMI = new MenuItem("Hide navigation pane");
		showHideTableInfoPaneMI = new MenuItem("Hide table info pane");

		fileMN.getItems().addAll(openFileMI, importFolderMI, importItunesMI);
		editMN.getItems().addAll(editMI, deleteMI, new SeparatorMenuItem(), newPlaylistMI);
		controlsMN.getItems().addAll(prevMI, nextMI, new SeparatorMenuItem(), volIncrMI, volDecrMI, new SeparatorMenuItem(), selCurrMI);
		viewMN.getItems().addAll(showHideNavigationPaneMI, showHideTableInfoPaneMI);
		aboutMN.getItems().add(aboutMI);		
	}
	
	private void setAccelerators(Modifier keyModifierOS) {
		openFileMI.setAccelerator(new KeyCodeCombination(KeyCode.O, keyModifierOS));
		importFolderMI.setAccelerator(new KeyCodeCombination(KeyCode.O, keyModifierOS, KeyCodeCombination.SHIFT_DOWN));
		importItunesMI.setAccelerator(new KeyCodeCombination(KeyCode.I, keyModifierOS, KeyCodeCombination.SHIFT_DOWN));
		preferencesMI.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, keyModifierOS));
		editMI.setAccelerator(new KeyCodeCombination(KeyCode.I, keyModifierOS));
		deleteMI.setAccelerator(new KeyCodeCombination(KeyCode.BACK_SPACE, keyModifierOS));
		prevMI.setAccelerator(new KeyCodeCombination(KeyCode.RIGHT, keyModifierOS));
		nextMI.setAccelerator(new KeyCodeCombination(KeyCode.LEFT, keyModifierOS));
		volIncrMI.setAccelerator(new KeyCodeCombination(KeyCode.UP, keyModifierOS));
		volDecrMI.setAccelerator(new KeyCodeCombination(KeyCode.DOWN, keyModifierOS));
		selCurrMI.setAccelerator(new KeyCodeCombination(KeyCode.L, keyModifierOS));
		newPlaylistMI.setAccelerator(new KeyCodeCombination(KeyCode.N, keyModifierOS));
		showHideNavigationPaneMI.setAccelerator(new KeyCodeCombination(KeyCode.R, keyModifierOS, KeyCodeCombination.SHIFT_DOWN));
		showHideTableInfoPaneMI.setAccelerator(new KeyCodeCombination(KeyCode.U, keyModifierOS, KeyCodeCombination.SHIFT_DOWN));
	}
	
	private void setFileMenuActions() {
		openFileMI.setOnAction(e -> {
			LOG.debug("Selecting files to open");
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Open file(s)...");
			chooser.getExtensionFilters().addAll(
					new ExtensionFilter ("All Supported (*.mp3, *.flac, *.wav, *.m4a)","*.mp3", "*.flac", "*.wav", "*.m4a"),
					new ExtensionFilter("mp3 files (*.mp3)", "*.mp3"),
					new ExtensionFilter("flac files (*.flac)","*.flac"),
					new ExtensionFilter("wav files (*.wav)", "*.wav"),
					new ExtensionFilter("m4a files (*.wav)", "*.m4a"));
			List<File> files = chooser.showOpenMultipleDialog(stageDemon.getMainStage());
			if(files != null) {
				TaskDemon.getInstance().parseFiles(files, true);
				stageDemon.getNavigationController().setStatusMessage("Opening files");
			}
		});
		importFolderMI.setOnAction(e -> {
			LOG.debug("Choosing folder to being imported");
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Choose folder");
			File folder = chooser.showDialog(stageDemon.getMainStage());
			if(folder != null) {
				Platform.runLater(() -> {stageDemon.getNavigationController().setStatusMessage("Scanning folders..."); stageDemon.getNavigationController().setStatusProgress(-1);});
				Thread countFilesThread = new Thread(() -> {
					List<File> files = Utils.getAllFilesInFolder(folder, MainPreferences.getInstance().getExtensionsFileFilter(), 0);
					Platform.runLater(() -> {
						stageDemon.getNavigationController().setStatusMessage("");
						stageDemon.getNavigationController().setStatusProgress(0);
						if(files.isEmpty()) {
							Alert alert = stageDemon.createAlert("Import", "No files", "There are no valid files to import on the selected folder."
									+ "Change the folder or the import options in preferences", AlertType.WARNING);
							alert.showAndWait();
						}
						else {
							Alert alert = stageDemon.createAlert("Import", "Import " + files.size() + " files?", "", AlertType.CONFIRMATION);
							Optional<ButtonType> result = alert.showAndWait();
							if(result.isPresent() && result.get().equals(ButtonType.OK)) {
								TaskDemon.getInstance().parseFiles(files, false);
								stageDemon.getNavigationController().setStatusMessage("Importing files");
							}
						}
					});
				});
				countFilesThread.start();
			}
		});
		importItunesMI.setOnAction(e -> {
			LOG.debug("Choosing Itunes xml file");
			FileChooser chooser = new FileChooser();
			chooser.setTitle("Select 'iTunes Music Library.xml' file");
			chooser.getExtensionFilters().add(new ExtensionFilter("xml files (*.xml)", "*.xml"));
			File xmlFile = chooser.showOpenDialog(stageDemon.getMainStage());
			if(xmlFile != null) 
				TaskDemon.getInstance().parseItunesLibrary(xmlFile.getAbsolutePath());
		});
		preferencesMI.setOnAction(e -> stageDemon.openPreferencesScene());
	}
	
	private void setEditMenuActions() {
		editMI.setOnAction(e -> stageDemon.editTracks());
		deleteMI.setOnAction(e -> stageDemon.deleteTracks());
		newPlaylistMI.setOnAction(e -> stageDemon.getRootController().setNewPlaylistMode(false));
	}
	
	private void setControlsMenuActions() {
		prevMI.disableProperty().bind(stageDemon.getPlayerController().previousButtonDisableProperty());
		prevMI.setOnAction(e -> PlayerFacade.getInstance().previous());
		
		nextMI.disableProperty().bind(stageDemon.getPlayerController().nextButtonDisableProperty());
		nextMI.setOnAction(e -> PlayerFacade.getInstance().next());
		
		volIncrMI.setOnAction(e -> stageDemon.getPlayerController().doIncreaseVolume());
		volDecrMI.setOnAction(e -> stageDemon.getPlayerController().doDecreaseVolume());
		
		selCurrMI.setOnAction(e -> {
			Track currentTrack = PlayerFacade.getInstance().getCurrentTrack();
			TrackTableView trackTable = (TrackTableView) stageDemon.getMainStage().getScene().lookup("#trackTable");
			trackTable.getSelectionModel().clearSelection();
			if(currentTrack != null) {
				Map.Entry<Integer, Track> currentEntry = new AbstractMap.SimpleEntry<Integer, Track>(currentTrack.getTrackID(), currentTrack);
				trackTable.getSelectionModel().select(currentEntry);
				trackTable.scrollTo(currentEntry);
			}
			LOG.debug("Current track in the player selected in the table");
		});
	}
	
	private void setViewMenuActions() {
//		showHideNavigationPaneMI.setOnAction(e -> {
//			if(showHideNavigationPaneMI.getText().startsWith("Show")) {
//				sc.getRootController().showNavigationPane(true);
//				showHideNavigationPaneMI.setText(showHideNavigationPaneMI.getText().replaceFirst("Show", "Hide"));
//			} else if(showHideNavigationPaneMI.getText().startsWith("Hide")) {
//				sc.getRootController().showNavigationPane(false);
//				showHideNavigationPaneMI.setText(showHideNavigationPaneMI.getText().replaceFirst("Hide", "Show"));				
//			}
//		});
		showHideTableInfoPaneMI.setOnAction(e -> {
			if(showHideTableInfoPaneMI.getText().startsWith("Show")) {
				stageDemon.getRootController().showTableInfoPane();
				showHideTableInfoPaneMI.setText(showHideTableInfoPaneMI.getText().replaceFirst("Show", "Hide"));
			} else if(showHideTableInfoPaneMI.getText().startsWith("Hide")) {
				stageDemon.getRootController().hideTableInfoPane();
				showHideTableInfoPaneMI.setText(showHideTableInfoPaneMI.getText().replaceFirst("Hide", "Show"));	
			}
		});

		ReadOnlyObjectProperty<NavigationMode> selectedMenu = stageDemon.getNavigationController().selectedMenuProperty();
		showHideTableInfoPaneMI.disableProperty().bind(Bindings.createBooleanBinding(() -> selectedMenu.getValue() != null, selectedMenu));
	}
}