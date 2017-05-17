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
 * Copyright (C) 2015 - 2017 Octavio Calleya
 */

package com.transgressoft.musicott.view;

import com.google.inject.*;
import com.transgressoft.musicott.*;
import com.transgressoft.musicott.model.*;
import com.transgressoft.musicott.player.*;
import com.transgressoft.musicott.tasks.*;
import com.transgressoft.musicott.util.*;
import com.transgressoft.musicott.util.guice.annotations.*;
import com.transgressoft.musicott.view.custom.*;
import de.codecentric.centerdevice.*;
import javafx.application.*;
import javafx.beans.property.*;
import javafx.fxml.*;
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
import java.util.AbstractMap.*;
import java.util.*;
import java.util.Map.*;
import java.util.stream.*;

import static com.transgressoft.musicott.model.NavigationMode.*;
import static com.transgressoft.musicott.util.Utils.*;
import static com.transgressoft.musicott.view.MusicottLayout.*;
import static javafx.scene.input.KeyCombination.*;
import static org.fxmisc.easybind.EasyBind.*;

/**
 * Controller of the MenuBar of the application. If the Operative System
 * is Max OS X, creates native OS X menu bar with the same behaviour.
 *
 * @author Octavio Calleya
 * @version 0.10-b
 */
@Singleton
public class RootMenuBarController extends InjectableController<MenuBar> {

	private final Logger LOG = LoggerFactory.getLogger(getClass().getName());

	private static final String MUSICOTT_GITHUB_LINK = "https://github.com/octaviospain/Musicott/";
	private static final String ABOUT_MUSICOTT_FIRST_LINE = " Version 0.10-b\n\n Copyright © 2015-2017 Octavio Calleya.";
	private static final String ABOUT_MUSICOTT_SECOND_LINE = " Licensed under GNU GPLv3. This product includes\n" + " " +
																"software developed by other open source projects.";

    private final Image musicottLogo = new Image(getClass().getResourceAsStream(MUSICOTT_ABOUT_LOGO));
    private final ImageView musicottLogoImageView = new ImageView(musicottLogo);

    private EditController editController;
    private RootController rootController;
    private NavigationController navigationController;
    private PlayerController playerController;
    private PreferencesController preferencesController;

    private HostServices hostServices;
    private MusicLibrary musicLibrary;
    private TaskDemon taskDemon;
    private PlayerFacade playerFacade;
    private MainPreferences mainPreferences;
    private BooleanProperty playPauseProperty;
    private ReadOnlyBooleanProperty emptyLibraryProperty;
    private ReadOnlyBooleanProperty searchingProperty;
    private ReadOnlyBooleanProperty previousButtonDisabledProperty;
    private ReadOnlyBooleanProperty nextButtonDisabledProperty;

    @FXML
    private MenuBar rootMenuBar;
	@FXML
    private Menu fileMenu;
    @FXML
    private MenuItem openFileMenuItem;
    @FXML
    private MenuItem importFolderMenuItem;
    @FXML
    private MenuItem importItunesMenuItem;
    @FXML
    private MenuItem newPlaylistMenuItem;
    @FXML
    private MenuItem newPlaylistFolderMenuItem;
    @FXML
    private MenuItem preferencesMenuItem;
    @FXML
    private MenuItem closeMenuItem;
    @FXML
    private Menu editMenu;
    @FXML
    private MenuItem editMenuItem;
    @FXML
    private MenuItem deleteMenuItem;
    @FXML
    private MenuItem selectAllMenuItem;
    @FXML
    private MenuItem dontSelectAllMenuItem;
    @FXML
    private MenuItem findMenuItem;
    @FXML
    private Menu controlsMenu;
    @FXML
    private MenuItem playPauseMenuItem;
    @FXML
    private MenuItem previousMenuItem;
    @FXML
    private MenuItem nextMenuItem;
    @FXML
    private MenuItem increaseVolumeMenuItem;
    @FXML
    private MenuItem decreaseVolumeMenuItem;
    @FXML
    private MenuItem selectCurrentTrackMenuItem;
    @FXML
    private Menu viewMenu;
    @FXML
    private MenuItem showHideNavigationPaneMenuItem;
    @FXML
    private MenuItem showHideTableInfoPaneMenuItem;
    @FXML
    private Menu aboutMenu;
    @FXML
    private MenuItem aboutMenuItem;
    @Inject
    private Injector injector;
    private ReadOnlyObjectProperty<NavigationMode> selectedMenuProperty;
    private ReadOnlyBooleanProperty editingProperty;
    private ReadOnlyBooleanProperty showingNavigationPanePropery;
    private ReadOnlyBooleanProperty showingTableInfoPaneProperty;

    @Inject
    public RootMenuBarController(MainPreferences mainPreferences, MusicLibrary musicLibrary,
            HostServices hostServices, TaskDemon taskDemon, PlayerFacade playerFacade) {
        this.mainPreferences = mainPreferences;
        this.musicLibrary = musicLibrary;
        this.hostServices = hostServices;
        this.taskDemon = taskDemon;
        this.playerFacade = playerFacade;
    }

    @FXML
    public void initialize() {
        setAboutMenuActions();
        setFileMenuActions();
    }

    @Override
    public void configure() {
        setEditMenuActions();
        setEditMenuActions();
        setControlsMenuActions();
        setViewMenuActions();
        bindShowHideTableInfo();
        bindEditingProperty();
        bindShowingNavigationPane();
            bindShowingTableInfoPane();
    }

    private void bindShowHideTableInfo() {
        if (selectedMenuProperty != null && showHideTableInfoPaneMenuItem != null)
            showHideTableInfoPaneMenuItem.disableProperty().bind(
                    map(selectedMenuProperty, menu -> ! menu.equals(PLAYLIST)));
    }

    private void bindEditingProperty() {
        if (editingProperty != null && searchingProperty != null && selectAllMenuItem != null)
            selectAllMenuItem.disableProperty().bind(
                    combine(editingProperty, searchingProperty, (e, s) -> e || s));
        if (editingProperty != null && dontSelectAllMenuItem != null)
            dontSelectAllMenuItem.disableProperty().bind(editingProperty);
    }

    private void bindShowingNavigationPane() {
        if (showingNavigationPanePropery != null && showHideNavigationPaneMenuItem != null)
            showHideNavigationPaneMenuItem.textProperty().bind(
                    map(showingNavigationPanePropery,
                        showing -> showing ? "Hide navigation pane" : "Show navigation pane"));
    }

    private void bindShowingTableInfoPane() {
        if (showingTableInfoPaneProperty != null && showHideTableInfoPaneMenuItem != null)
            showHideTableInfoPaneMenuItem.textProperty().bind(
                    map(showingTableInfoPaneProperty,
                        showing -> showing ? "Hide table information pane" : "Show table information pane"));
    }

    /**
     * Configures the {@link MenuBar} as a native Mac OS X one
     *
     * @see <a href="https://github.com/codecentric/NSMenuFX">NSMenuFX</a>
     */
    void macMenuBar() {
        MenuToolkit menuToolkit = MenuToolkit.toolkit();
        Menu appMenu = new Menu("Musicott");
        appMenu.getItems().addAll(preferencesMenuItem, new SeparatorMenuItem());
        appMenu.getItems().add(menuToolkit.createQuitMenuItem("Musicott"));
        Menu windowMenu = new Menu("Window");
        windowMenu.getItems().addAll(menuToolkit.createMinimizeMenuItem(), menuToolkit.createCloseWindowMenuItem());
        windowMenu.getItems().addAll(menuToolkit.createZoomMenuItem(), new SeparatorMenuItem());
        windowMenu.getItems().addAll(menuToolkit.createHideOthersMenuItem(), menuToolkit.createUnhideAllMenuItem());
        windowMenu.getItems().addAll(menuToolkit.createBringAllToFrontItem());

        fileMenu.getItems().remove(5, 8);
        menuToolkit.setApplicationMenu(appMenu);
        rootMenuBar.getMenus().add(0, appMenu);
        rootMenuBar.getMenus().add(5, windowMenu);
        menuToolkit.autoAddWindowMenuItems(windowMenu);
        menuToolkit.setGlobalMenuBar(rootMenuBar);

        setAccelerators(KeyCodeCombination.META_DOWN);
        LOG.debug("OS X native menubar created");
    }

    /**
     * Configures the {@link MenuBar} bar with default accelerators and menus.
     */
    void defaultMenuBar() {
        closeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F4, ALT_DOWN));
        closeMenuItem.setOnAction(event -> {
            LOG.info("Exiting Musicott");
            taskDemon.shutDownTasks();
            System.exit(0);
        });
        setAccelerators(KeyCodeCombination.CONTROL_DOWN);
        LOG.debug("Default menu bar configured");
    }

    private void setFileMenuActions() {
        openFileMenuItem.setOnAction(e -> {
            LOG.debug("Selecting files to open");
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Open file(s)...");
            chooser.getExtensionFilters()
                   .addAll(new ExtensionFilter("All Supported (*.mp3, *.flac, *.wav, *.m4a)", "*.mp3", "*.flac",
                                               "*.wav", "*.m4a"), new ExtensionFilter("mp3 files (*.mp3)", "*.mp3"),
                           new ExtensionFilter("flac files (*.flac)", "*.flac"),
                           new ExtensionFilter("wav files (*.wav)", "*.wav"),
                           new ExtensionFilter("m4a files (*.wav)", "*.m4a"));
            List<File> filesToImport = chooser.showOpenMultipleDialog(stage);
            if (filesToImport != null) {
                taskDemon.importFiles(filesToImport, true);
                navigationController.setStatusMessage("Opening files");
            }
        });
        importFolderMenuItem.setOnAction(e -> {
            LOG.debug("Choosing folder to being imported");
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose folder");
            File folder = chooser.showDialog(stage);
            if (folder != null)
                countFilesToImport(folder);
        });
        importItunesMenuItem.setOnAction(e -> {
            LOG.debug("Choosing Itunes xml file");
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select 'iTunes Music Library.xml' file");
            chooser.getExtensionFilters().add(new ExtensionFilter("xml files (*.xml)", "*.xml"));
            File xmlFile = chooser.showOpenDialog(stage);
            if (xmlFile != null)
                taskDemon.importFromItunesLibrary(xmlFile.getAbsolutePath());
        });
        preferencesMenuItem.setOnAction(e -> preferencesController.getStage().show());
        newPlaylistMenuItem.setOnAction(e -> rootController.enterNewPlaylistName(false));
        newPlaylistFolderMenuItem.setOnAction(e -> rootController.enterNewPlaylistName(true));
    }

    private void setEditMenuActions() {
        editMenuItem.setOnAction(e -> editController.editTracks(trackSelectionList()));
        deleteMenuItem.setOnAction(e -> musicLibrary.deleteTracks(trackSelectionList()));
        selectAllMenuItem.setOnAction(e -> rootController.selectAllTracks());
        dontSelectAllMenuItem.setOnAction(e -> rootController.deselectAllTracks());
        findMenuItem.setOnAction(e -> playerController.focusSearchField());
    }

    private void setControlsMenuActions() {
        playPauseMenuItem.textProperty().bind(map(playPauseProperty, play -> play ? "Pause" : "Play"));
        playPauseMenuItem.disableProperty().bind(emptyLibraryProperty);
        playPauseMenuItem.setOnAction(e -> TrackTableView.spacePressedOnTableAction(playerFacade.getPlayerStatus()));
        previousMenuItem.disableProperty().bind(previousButtonDisabledProperty);
        previousMenuItem.setOnAction(e -> playerFacade.previous());

        nextMenuItem.disableProperty().bind(nextButtonDisabledProperty);
        nextMenuItem.setOnAction(e -> playerFacade.next());

        increaseVolumeMenuItem.setOnAction(e -> playerController.increaseVolume());
        decreaseVolumeMenuItem.setOnAction(e -> playerController.decreaseVolume());

        selectCurrentTrackMenuItem.setOnAction(e -> {
            Optional<Track> currentTrack = playerFacade.getCurrentTrack();
            currentTrack.ifPresent(track -> {
                int currentTrackId = track.getTrackId();
                Entry<Integer, Track> currentEntry = new SimpleEntry<>(currentTrackId, track);
                rootController.selectTrack(currentEntry);
            });
            LOG.debug("Current track in the player selected in the table");
        });
    }

    private void setViewMenuActions() {
        showHideNavigationPaneMenuItem.setOnAction(e -> {
            if (showHideNavigationPaneMenuItem.getText().startsWith("Show"))
                rootController.showNavigationPane();
            else if (showHideNavigationPaneMenuItem.getText().startsWith("Hide"))
                rootController.hideNavigationPane();
        });
        showHideTableInfoPaneMenuItem.setOnAction(e -> {
            if (showHideTableInfoPaneMenuItem.getText().startsWith("Show"))
                rootController.showTableInfoPane();
            else if (showHideTableInfoPaneMenuItem.getText().startsWith("Hide"))
                rootController.hideTableInfoPane();
        });
    }

    private void setAboutMenuActions() {
        aboutMenuItem.setOnAction(e -> {
            Alert alert = createAlert("About Musicott", " ", "", AlertType.INFORMATION, stage);
            alert.getDialogPane().getStylesheets().add(getClass().getResource(DIALOG_STYLE).toExternalForm());
            Label aboutLabel1 = new Label(ABOUT_MUSICOTT_FIRST_LINE);
            Label aboutLabel2 = new Label(ABOUT_MUSICOTT_SECOND_LINE);
            Hyperlink githubLink = new Hyperlink(MUSICOTT_GITHUB_LINK);
            githubLink.setOnAction(event -> hostServices.showDocument(githubLink.getText()));
            FlowPane flowPane = new FlowPane();
            flowPane.getChildren().addAll(aboutLabel1, githubLink, aboutLabel2);
            alert.getDialogPane().contentProperty().set(flowPane);
            alert.setGraphic(musicottLogoImageView);
            alert.showAndWait();
            LOG.debug("Showing about window");
        });
    }

    private List<Track> trackSelectionList() {
        List<Entry<Integer, Track>> trackSelection = rootController.getSelectedTracks();
        return trackSelection.stream().map(Entry::getValue).collect(Collectors.toList());
    }

    private void countFilesToImport(File folder) {
        LOG.debug("Starting scanning of {}", folder);
        Platform.runLater(() -> {
            navigationController.setStatusMessage("Scanning folders...");
            navigationController.setStatusProgress(- 1);
        });

        Thread countFilesThread = new Thread(() -> countFilesToImportTask(folder));
        countFilesThread.start();
    }

    private void countFilesToImportTask(File folder) {
        Set<String> extensions = mainPreferences.getImportFilterExtensions();
        ExtensionFileFilter filter = injector.getInstance(ExtensionFileFilter.class);
        extensions.forEach(filter::addExtension);
        Platform.runLater(() -> {
            navigationController.setStatusMessage("");
            navigationController.setStatusProgress(0);
        });
        LOG.debug("Counting files to import {}", extensions);
        List<File> files = Utils.getAllFilesInFolder(folder, filter, 0);
        if (files.isEmpty())
            showNoFilesToImportAlert();
        else
            showImportConfirmationAlert(files);
    }

    private void showNoFilesToImportAlert() {
        String alertContentText = "There are no valid files to import in the selected folder. " + "Change the folder " +
				"" + "or the import options in preferences";
        Platform.runLater(() -> {
            Alert alert = createAlert("Import", "No files", alertContentText, AlertType.WARNING, stage);
            alert.showAndWait();
        });
    }

    private void showImportConfirmationAlert(List<File> filesToImport) {
        String alertContentText = "Import " + filesToImport.size() + " files?";
        Platform.runLater(() -> {
            Alert alert = createAlert("Import", alertContentText, "", AlertType.CONFIRMATION, stage);
            alert.getDialogPane().getStylesheets().add(getClass().getResource(DIALOG_STYLE).toExternalForm());
            LOG.debug("Showing confirmation alert to import {} files", filesToImport.size());
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                taskDemon.importFiles(filesToImport, false);
                navigationController.setStatusMessage("Importing files");
            }
        });
    }

    private void setAccelerators(Modifier operativeSystemModifier) {
        Modifier shiftDown = KeyCodeCombination.SHIFT_DOWN;
        openFileMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, operativeSystemModifier));
        importFolderMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, operativeSystemModifier, shiftDown));
        importItunesMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.I, operativeSystemModifier, shiftDown));
        preferencesMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, operativeSystemModifier));
        editMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.I, operativeSystemModifier));
        deleteMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.BACK_SPACE, operativeSystemModifier));
        playPauseMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.SPACE));
        previousMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.LEFT, operativeSystemModifier));
        nextMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.RIGHT, operativeSystemModifier));
        increaseVolumeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.UP, operativeSystemModifier));
        decreaseVolumeMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.DOWN, operativeSystemModifier));
        selectCurrentTrackMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.L, operativeSystemModifier));
        newPlaylistMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, operativeSystemModifier));
        newPlaylistFolderMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.N, operativeSystemModifier, shiftDown));
        showHideNavigationPaneMenuItem
                .setAccelerator(new KeyCodeCombination(KeyCode.R, operativeSystemModifier, shiftDown));
        showHideTableInfoPaneMenuItem
                .setAccelerator(new KeyCodeCombination(KeyCode.U, operativeSystemModifier, shiftDown));
        selectAllMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.A, operativeSystemModifier));
        dontSelectAllMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.A, operativeSystemModifier, shiftDown));
        findMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.F, operativeSystemModifier));
    }

    @Inject (optional = true)
    public void setRootController(@RootCtrl RootController c) {
        rootController = c;
    }

    @Inject (optional = true)
    public void setEditController(@EditCtrl EditController c) {
        editController = c;
    }

    @Inject
    public void setNavigationController(@NavigationCtrl NavigationController c) {
        navigationController = c;
    }

    @Inject
    public void setPlayerController(@PlayerCtrl PlayerController c) {
        playerController = c;
    }

    @Inject
    public void setPreferencesController(@PrefCtrl PreferencesController c) {
        preferencesController = c;
    }

    @Inject
    public void setEmptyLibraryProperty(@EmptyLibraryProperty ReadOnlyBooleanProperty p) {
        this.emptyLibraryProperty = p;
    }

    @Inject
    public void setSearchFieldFocusedProperty(@SearchingProperty ReadOnlyBooleanProperty p) {
        searchingProperty = p;
    }

    @Inject
    public void setPlayPauseProperty(@PlayPauseProperty BooleanProperty p) {
        playPauseProperty = p;
    }

    @Inject
    public void setPrevButtonDisabledProperty(@PreviousButtonDisabledProperty ReadOnlyBooleanProperty p) {
        previousButtonDisabledProperty = p;
    }

    @Inject
    public void setNextButtonDisabledProperty(@NextButtonDisabledProperty ReadOnlyBooleanProperty p) {
        nextButtonDisabledProperty = p;
    }

    @Inject (optional = true)
    public void setSelectedMenuProperty(@SelectedMenuProperty ReadOnlyObjectProperty<NavigationMode> p) {
        selectedMenuProperty = p;
        bindShowHideTableInfo();
    }

    @Inject (optional = true)
    public void setShowingNavigationPaneProperty(@ShowingNavigationPaneProperty ReadOnlyBooleanProperty property) {
        showingNavigationPanePropery = property;
        bindShowingNavigationPane();
    }

    @Inject (optional = true)
    public void setShowingTableInfoPaneProperty(@ShowingTableInfoPaneProperty ReadOnlyBooleanProperty property) {
        showingTableInfoPaneProperty = property;
        bindShowingTableInfoPane();
    }

    @Inject (optional = true)
    public void setShowingEditingProperty(@ShowingEditing ReadOnlyBooleanProperty property) {
        editingProperty = property;
        bindEditingProperty();
    }
}
