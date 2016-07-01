package com.musicott.view;

import com.musicott.*;
import com.musicott.model.*;
import com.musicott.player.*;
import com.musicott.services.*;

/**
 * Interface that represent a controller of the Musicott application.
 * Stores constants of layout files, logos, and css stylesheets.
 *
 * @author Octavio Calleya
 * @version 0.9
 * @since 0.9
 */
public interface MusicottController {

	String LAYOUTS_PATH = "/view/";

	String ROOT_LAYOUT = LAYOUTS_PATH + "RootLayout.fxml";
	String NAVIGATION_LAYOUT = LAYOUTS_PATH +  "NavigationLayout.fxml";
	String PRELOADER_LAYOUT = LAYOUTS_PATH + "PreloaderPromptLayout.fxml";
	String EDIT_LAYOUT = LAYOUTS_PATH + "EditLayout.fxml";
	String PLAYQUEUE_LAYOUT = LAYOUTS_PATH + "PlayQueueLayout.fxml";
	String PROGRESS_LAYOUT = LAYOUTS_PATH + "ProgressLayout.fxml";
	String PREFERENCES_LAYOUT = LAYOUTS_PATH + "PreferencesLayout.fxml";
	String PLAYER_LAYOUT = LAYOUTS_PATH + "PlayerLayout.fxml";

	String DEFAULT_COVER_IMAGE = "/images/default-cover-image.png";
	String LASTFM_LOGO = "/images/lastfm-logo.png";
	String MUSICOTT_ICON = "/images/musicotticon.png";

	String DIALOG_STYLE = "/css/dialog.css";

	MusicLibrary musicLibrary = MusicLibrary.getInstance();
	MainPreferences preferences = MainPreferences.getInstance();
	PlayerFacade player = PlayerFacade.getInstance();
	Services services = Services.getInstance();
	StageDemon stageDemon = StageDemon.getInstance();
	ErrorDemon errorDemon = ErrorDemon.getInstance();
}