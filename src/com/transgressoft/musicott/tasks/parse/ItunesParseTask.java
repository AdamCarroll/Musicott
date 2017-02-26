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

package com.transgressoft.musicott.tasks.parse;

import com.google.common.collect.*;
import com.transgressoft.musicott.*;
import com.transgressoft.musicott.model.*;
import com.transgressoft.musicott.util.*;
import com.worldsworstsoftware.itunes.*;
import com.worldsworstsoftware.itunes.parser.*;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.stage.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import java.util.stream.*;

import static com.transgressoft.musicott.view.MusicottController.*;

/**
 * Extends from {@link BaseParseTask} to perform the operation of import a
 * <tt>iTunes</tt> library to the {@link MusicLibrary} of the application.
 *
 * @author Octavio Calleya
 * @version 0.9.1-b
 */
public class ItunesParseTask extends BaseParseTask {

    public static final int METADATA_POLICY = 0;
    public static final int ITUNES_DATA_POLICY = 1;
    private final Logger LOG = LoggerFactory.getLogger(getClass().getName());
    private final String itunesLibraryXmlPath;
    private final int metadataPolicy;
    private final boolean importPlaylists;
    private final boolean holdPlayCount;

    private Semaphore waitConfirmationSemaphore;

    private List<ItunesTrack> itunesTracks;
    private List<ItunesPlaylist> itunesPlaylists;
    private List<Playlist> playlists;
    private List<String> notFoundFiles;

    private int currentItunesItemsParsed;
    private int totalItunesItemsToParse;

    public ItunesParseTask(String path) {
        super();
        itunesLibraryXmlPath = path;
        MainPreferences mainPreferences = MainPreferences.getInstance();
        metadataPolicy = mainPreferences.getItunesImportMetadataPolicy();
        importPlaylists = mainPreferences.getItunesImportPlaylists();
        holdPlayCount = mainPreferences.getItunesImportHoldPlaycount();
        waitConfirmationSemaphore = new Semaphore(0);
        currentItunesItemsParsed = 0;
        playlists = new ArrayList<>();
    }

    @Override
    protected int getNumberOfParsedItemsAndIncrement() {
        return ++ currentItunesItemsParsed;
    }

    @Override
    protected int getTotalItemsToParse() {
        return totalItunesItemsToParse;
    }

    @Override
    protected Void call() throws Exception {
        if (isValidItunesXML())
            parseItunesFile();
        else {
            errorDemon.showErrorDialog("The selected xml file is not valid");
            cancel();
        }
        waitConfirmationSemaphore.acquire();

        startMillis = System.currentTimeMillis();
        ForkJoinPool forkJoinPool = new ForkJoinPool(6);
        ItunesTracksParseAction itunesTracksParseAction = new ItunesTracksParseAction(itunesTracks, metadataPolicy,
                                                                                      holdPlayCount, this);
        ItunesParseResult itunesParseResult = forkJoinPool.invoke(itunesTracksParseAction);

        parsedTracks = itunesParseResult.getParsedItems();
        parseErrors = itunesParseResult.getParseErrors();
        notFoundFiles = itunesParseResult.getNotFoundFiles();

        if (importPlaylists) {
            currentItunesItemsParsed = 0;
            totalItunesItemsToParse = itunesPlaylists.size();
            Map<Integer, Integer> itunesIdToMusicottIdMap = itunesParseResult.getItunesIdToMusicottIdMap();
            ItunesPlaylistsParseAction itunesPlaylistsParseAction = new ItunesPlaylistsParseAction(itunesPlaylists,
                                                                                                   itunesIdToMusicottIdMap,
                                                                                                   this);
            playlists = forkJoinPool.invoke(itunesPlaylistsParseAction).getParsedItems();
        }
        return null;
    }

    private boolean isValidItunesXML() {
        boolean valid = false;
        String itunesXmlSampleLine = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        String itunesPlistSampleLine = "<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" " + "\"http"
                + "://www.apple.com/DTDs/PropertyList-1.0.dtd\">";
        try (Scanner scanner = new Scanner(new File(itunesLibraryXmlPath))) {
            scanner.useDelimiter(Pattern.compile(">"));
            if (scanner.hasNextLine() && scanner.nextLine().contains(itunesXmlSampleLine))
                valid = true;
            if (scanner.hasNextLine() && scanner.nextLine().contains(itunesPlistSampleLine))
                valid = true;
        }
        catch (FileNotFoundException exception) {
            LOG.info("Error accessing to the itunes xml: ", exception);
            errorDemon.showErrorDialog("Error opening the iTunes Library file", "", exception);
            valid = false;
        }
        return valid;
    }

    /**
     * Parses the iTunes Library file and asks the user for a confirmation to continue
     * showing the number of parsedTracks and playlists to import.
     */
    @SuppressWarnings ("unchecked")
    private void parseItunesFile() {
        Platform.runLater(() -> {
            navigationController.setStatusMessage("Scanning itunes library...");
            navigationController.setStatusProgress(- 1);
        });
        ItunesParserLogger itunesLogger = new ItunesParserLogger();
        ItunesLibrary itunesLibrary = ItunesLibraryParser.parseLibrary(itunesLibraryXmlPath, itunesLogger);
        Map<Integer, ItunesTrack> itunesTrackMap = ImmutableMap.copyOf(itunesLibrary.getTracks());
        itunesTracks = ImmutableList.copyOf(itunesTrackMap.values());
        totalItunesItemsToParse = itunesTracks.size();

        String playlistsAlertText[] = new String[]{""};
        if (importPlaylists) {
            itunesPlaylists = (List<ItunesPlaylist>) itunesLibrary.getPlaylists();
            itunesPlaylists = itunesPlaylists.stream().filter(this::isValidItunesPlaylist).collect(Collectors.toList());

            int totalPlaylists = itunesPlaylists.size();
            playlistsAlertText[0] += "and " + Integer.toString(totalPlaylists) + " playlists ";
        }
        Platform.runLater(() -> showConfirmationAlert(playlistsAlertText[0]));
    }

    private void showConfirmationAlert(String playlistsAlertText) {
        String itunesTracksString = Integer.toString(totalItunesItemsToParse);
        String headerText = "Import " + itunesTracksString + " parsedTracks " + playlistsAlertText + "from itunes?";

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().add(getClass().getResource(DIALOG_STYLE).toExternalForm());
        alert.setTitle("Import");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setHeaderText(headerText);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
            waitConfirmationSemaphore.release();
            navigationController.setStatusMessage("Importing files");
        }
        else
            cancel();
    }

    private boolean isValidItunesPlaylist(ItunesPlaylist itunesPlaylist) {
        return ! "####!####".equals(itunesPlaylist.getName()) && itunesPlaylist.isAllItems() && ! itunesPlaylist
                .getPlaylistItems().isEmpty();
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        updateMessage("Itunes import succeeded");
        LOG.info("Itunes import task completed");

        stageDemon.showIndeterminateProgress();
        Thread addTracksAndPlaylistToMusicLibrary = new Thread(this::addResultsToMusicLibrary);
        addTracksAndPlaylistToMusicLibrary.start();

        if (! notFoundFiles.isEmpty())
            errorDemon.showExpandableErrorsDialog("Some files were not found", "", notFoundFiles);
        if (! parseErrors.isEmpty())
            errorDemon.showExpandableErrorsDialog("Errors importing files", "", parseErrors);
    }

    @Override
    protected void addResultsToMusicLibrary() {
        Platform.runLater(() -> updateTaskProgressOnView(- 1, ""));
        playlists.forEach(playlist -> Platform.runLater(() -> navigationController.addNewPlaylist(playlist)));
        musicLibrary.addTracks(parsedTracks);
        Platform.runLater(stageDemon::closeIndeterminateProgress);
        computeAndShowElapsedTime();
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        updateMessage("Itunes import cancelled");
        LOG.info("Itunes import task cancelled");
    }
}
