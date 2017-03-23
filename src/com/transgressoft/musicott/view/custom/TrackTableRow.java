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

package com.transgressoft.musicott.view.custom;

import com.transgressoft.musicott.*;
import com.transgressoft.musicott.model.*;
import com.transgressoft.musicott.player.*;
import javafx.collections.*;
import javafx.scene.control.*;
import javafx.scene.input.*;

import java.util.*;
import java.util.Map.*;
import java.util.stream.*;

/**
 * Custom {@link TableRow} that represents an {@link Entry} with
 * a track id as a key and the {@link Track} as value.
 *
 * @author Octavio Calleya
 * @version 0.9.2-b
 * @since 0.9.1-b
 */
public class TrackTableRow extends TableRow<Entry<Integer, Track>> {

    public static final DataFormat TRACK_ID_MIME_TYPE = new DataFormat("application/x-java-tracks-id");

    private PlayerFacade player = PlayerFacade.getInstance();
    private StageDemon stageDemon = StageDemon.getInstance();

    public TrackTableRow() {
        super();
        setOnMouseClicked(this::playTrackOnMouseClickedHandler);
        setOnDragDetected(this::onDragDetectedMovingTracks);
        hoverProperty().addListener((observable, oldHovered, newHovered) -> {
            if (newHovered) {
                Optional<byte[]> cover;
                if (getItem() != null)
                    cover = getItem().getValue().getCoverImage();
                else
                    cover = Optional.empty();
                stageDemon.getRootController().updateTrackHoveredCover(cover);
            }
        });
    }

    /**
     * Fires the play of a {@link Track}
     * when the user double-clicks a row.
     */
    private void playTrackOnMouseClickedHandler(MouseEvent event) {
        if (event.getClickCount() == 2 && ! isEmpty()) {
            List<Integer> singleTrackIdList = Collections.singletonList(getItem().getKey());
            player.addTracksToPlayQueue(singleTrackIdList, true);
            stageDemon.getNavigationController().updateCurrentPlayingPlaylist();
        }
    }

    private void onDragDetectedMovingTracks(MouseEvent event) {
        if (! isEmpty()) {
            Dragboard dragboard = startDragAndDrop(TransferMode.ANY);
            dragboard.setDragView(snapshot(null, null));

            ObservableList<Entry<Integer, Track>> selection = stageDemon.getRootController().getSelectedTracks();
            List<Integer> selectionIds = selection.stream().map(Entry::getKey).collect(Collectors.toList());
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.put(TRACK_ID_MIME_TYPE, selectionIds);

            dragboard.setContent(clipboardContent);
            event.consume();
        }
    }
}
