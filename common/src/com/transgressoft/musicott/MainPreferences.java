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

package com.transgressoft.musicott;

import com.google.common.io.*;
import com.google.inject.*;
import com.transgressoft.musicott.model.*;
import javafx.beans.value.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.prefs.*;

/**
 * Singleton class that isolates some user preferences, such as the application folder
 * or the the iTunes import options, using the java predefined class {@link Preferences}.
 *
 * @author Octavio Calleya
 * @version 0.10.1-b
 */
@Singleton
public class MainPreferences {

    public static final String DEFAULT_MUSICOTT_LOCATION = File.separator + "Music" + File.separator  + "Musicott";
    public static final int METADATA_POLICY = 0;
    public static final int ITUNES_DATA_POLICY = 1;

    /**
     * The path where the application files will be stored
     */
    private static final String MUSICOTT_FOLDER = "musicott_folder";

    /**
     * The sequence number of the keys of the {@link com.transgressoft.musicott.model.Track} map
     */
    private static final String TRACK_SEQUENCE = "track_sequence";

    private static final String IMPORT_MP3 = "import_mp3_flag";
    private static final String IMPORT_M4A = "import_m4a_flag";
    private static final String IMPORT_WAV = "import_wav_flag";
    private static final String IMPORT_FLAC = "import_flac_flag";

    /**
     * Flag that indicates if the play count must be kept when importing
     * from iTunes, instead of reset them to 0 plays.
     */
    private static final String ITUNES_IMPORT_HOLD_PLAYCOUNT = "itunes_import_hold_playcount";

    /**
     * The flag to choose between parse the metadata of the imported files,
     * or the iTunes data saved in the library, when importing from a iTunes library.
     */
    private static final String ITUNES_IMPORT_METADATA_POLICY = "itunes_import_policy";

    private final TracksLibrary tracksLibrary;
    private Preferences preferences;
    private AtomicInteger sequence;
    private Set<String> importExtensions;
    private ChangeListener<String> userFolderListener;

    /**
     * Private constructor of the class.
     * By default, if the application is used in the first time, the only valid
     * extension when importing files is {@code *.mp3}.
     */
    @Inject
    public MainPreferences(TracksLibrary tracksLibrary, ChangeListener<String> userFolderListener) {
        this.tracksLibrary = tracksLibrary;
        this.userFolderListener = userFolderListener;
        preferences = Preferences.userNodeForPackage(getClass());
        sequence = new AtomicInteger(preferences.getInt(TRACK_SEQUENCE, 0));
        importExtensions = new HashSet<>();
        if (preferences.getBoolean(IMPORT_MP3, true))
            importExtensions.add("mp3");
        if (preferences.getBoolean(IMPORT_M4A, false))
            importExtensions.add("m4a");
        if (preferences.getBoolean(IMPORT_WAV, false))
            importExtensions.add("wav");
        if (preferences.getBoolean(IMPORT_FLAC, false))
            importExtensions.add("flac");
    }

    /**
     * Returns 0 if the application is used in the first time, that is,
     * if there is no record for the track sequence in the class {@link Preferences};
     * or the next integer to use for the tracks map
     *
     * @return The next integer to use for the track map
     */
    public synchronized int getTrackSequence() {
        while (tracksLibrary.getTrack(sequence.getAndIncrement()).isPresent()) ;
        preferences.putInt(TRACK_SEQUENCE, sequence.get());
        return sequence.get();
    }

    public synchronized void resetTrackSequence() {
        sequence.set(0);
        preferences.putInt(TRACK_SEQUENCE, 0);
    }

    /**
     * Sets the application folder path, and saves the application files in the new path
     *
     * @param path The path to the application folder
     */
    public synchronized void setMusicottUserFolder(String path) throws IOException {
        Files.createParentDirs(new File(path, "test"));
        preferences.put(MUSICOTT_FOLDER, path);
        userFolderListener.changed(null, null, null);
    }

    public synchronized String getMusicottUserFolder() {
        return preferences.get(MUSICOTT_FOLDER, null);
    }

    public int getItunesImportMetadataPolicy() {
        return preferences.getInt(ITUNES_IMPORT_METADATA_POLICY, ITUNES_DATA_POLICY);
    }

    public void setItunesImportMetadataPolicy(int policy) {
        preferences.putInt(ITUNES_IMPORT_METADATA_POLICY, policy);
    }

    public boolean getItunesImportHoldPlaycount() {
        return preferences.getBoolean(ITUNES_IMPORT_HOLD_PLAYCOUNT, true);
    }

    public void setItunesImportHoldPlaycount(boolean holdPlayCount) {
        preferences.putBoolean(ITUNES_IMPORT_HOLD_PLAYCOUNT, holdPlayCount);
    }

    public Set<String> getImportFilterExtensions() {
        return importExtensions;
    }

    public void setImportFilterExtensions(String... newImportFilterExtensions) {
        importExtensions.clear();
        importExtensions.addAll(Arrays.asList(newImportFilterExtensions));
        preferences.putBoolean(IMPORT_MP3, importExtensions.contains("mp3"));
        preferences.putBoolean(IMPORT_M4A, importExtensions.contains("m4a"));
        preferences.putBoolean(IMPORT_WAV, importExtensions.contains("wav"));
        preferences.putBoolean(IMPORT_FLAC, importExtensions.contains("flac"));
    }
}
