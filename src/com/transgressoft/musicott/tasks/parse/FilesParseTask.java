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

import com.transgressoft.musicott.model.*;
import com.transgressoft.musicott.player.*;
import javafx.application.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Extends from {@link BaseParseTask} to perform the operation of import several
 * audio files and add them to the {@link MusicLibrary}.
 *
 * @author Octavio Calleya
 * @version 0.9.1-b
 */
public class FilesParseTask extends BaseParseTask {

    private final Logger LOG = LoggerFactory.getLogger(getClass().getName());

    private List<File> filesToParse;
    private int currentParsedFiles;
    private int totalFilesToParse;
    private boolean playAtTheEnd;

    private PlayerFacade player = PlayerFacade.getInstance();

    public FilesParseTask(List<File> files, boolean playAtTheEnd) {
        filesToParse = files;
        this.playAtTheEnd = playAtTheEnd;
        currentParsedFiles = 0;
        totalFilesToParse = filesToParse.size();
    }

    @Override
    protected int getNumberOfParsedItemsAndIncrement() {
        return ++ currentParsedFiles;
    }

    @Override
    protected int getTotalItemsToParse() {
        return totalFilesToParse;
    }

    @Override
    protected Void call() {
        startMillis = System.currentTimeMillis();
        LOG.debug("Starting file importing");
        ForkJoinPool forkJoinPool = new ForkJoinPool(4);
        ParseResult<Map<Integer, Track>> result = forkJoinPool.invoke(new FilesParseAction(filesToParse, this));
        parsedTracks = result.getParsedItems();
        parseErrors = result.getParseErrors();
        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        updateMessage("Parse succeeded");
        LOG.info("Parse task completed");

        Thread addTracksToMusicLibraryThread = new Thread(this::addResultsToMusicLibrary);
        addTracksToMusicLibraryThread.start();
        stageDemon.showIndeterminateProgress();

        if (! parseErrors.isEmpty())
            errorDemon.showExpandableErrorsDialog("Errors importing files", "", parseErrors);
        if (playAtTheEnd)
            player.addTracksToPlayQueue(parsedTracks.keySet(), true);
    }

    @Override
    protected void addResultsToMusicLibrary() {
        Platform.runLater(() -> updateTaskProgressOnView(- 1, ""));
        musicLibrary.addTracks(parsedTracks);
        Platform.runLater(stageDemon::closeIndeterminateProgress);
        computeAndShowElapsedTime();
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        updateMessage("Parse cancelled");
        LOG.info("Parse task cancelled");
    }
}
