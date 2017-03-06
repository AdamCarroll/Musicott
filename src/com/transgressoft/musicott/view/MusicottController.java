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

/**
 * Interface that represent a controller of the Musicott application.
 * Stores constants of layout files, logos, css stylesheets and
 * references to the singleton classes.
 *
 * @author Octavio Calleya
 * @version 0.9.2-b
 * @since 0.9
 */
public interface MusicottController {

    String TRACKS_PERSISTENCE_FILE = "Musicott-tracks.json";
    String WAVEFORMS_PERSISTENCE_FILE = "Musicott-waveforms.json";
    String PLAYLISTS_PERSISTENCE_FILE = "Musicott-playlists.json";

    String LAYOUTS_PATH = "/view/";
    String IMAGES_PATH = "/images/";
    String STYLES_PATH = "/css/";

    String ROOT_LAYOUT = LAYOUTS_PATH + "RootLayout.fxml";
    String NAVIGATION_LAYOUT = LAYOUTS_PATH + "NavigationLayout.fxml";
    String PRELOADER_INIT_LAYOUT = LAYOUTS_PATH + "PreloaderLayout.fxml";
    String PRELOADER_FIRST_USE_PROMPT = LAYOUTS_PATH + "PreloaderPromptLayout.fxml";
    String EDIT_LAYOUT = LAYOUTS_PATH + "EditLayout.fxml";
    String PLAYQUEUE_LAYOUT = LAYOUTS_PATH + "PlayQueueLayout.fxml";
    String PROGRESS_LAYOUT = LAYOUTS_PATH + "ProgressLayout.fxml";
    String PREFERENCES_LAYOUT = LAYOUTS_PATH + "PreferencesLayout.fxml";
    String PLAYER_LAYOUT = LAYOUTS_PATH + "PlayerLayout.fxml";
    String ERROR_ALERT_LAYOUT = LAYOUTS_PATH + "ErrorDialogLayout.fxml";

    String DEFAULT_COVER_IMAGE = IMAGES_PATH + "default-cover-image.png";
    String COMMON_ERROR_IMAGE = IMAGES_PATH + "common-error.png";
    String LASTFM_LOGO = IMAGES_PATH + "lastfm-logo.png";
    String MUSICOTT_APP_ICON = IMAGES_PATH + "musicott-app-icon.png";
    String MUSICOTT_ABOUT_LOGO = IMAGES_PATH + "musicott-about-logo.png";

    String DIALOG_STYLE = STYLES_PATH + "dialog.css";
    String TRACK_TABLE_STYLE = STYLES_PATH + "tracktable.css";
    String BASE_STYLE = STYLES_PATH + "base.css";
    String TRACKAREASET_TRACK_TABLE_STYLE = STYLES_PATH + "trackareaset-tracktable.css";
}
