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
import com.transgressoft.musicott.tests.*;
import com.transgressoft.musicott.util.guice.annotations.*;
import com.transgressoft.musicott.util.guice.factories.*;
import com.transgressoft.musicott.util.guice.modules.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.*;

import java.util.Map.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Octavio Calleya
 */
public class ArtistsViewControllerTest extends JavaFxTestBase<ArtistsViewController> {

    static BooleanProperty falseProperty = new SimpleBooleanProperty(false);

    @Override
    @Start
    public void start(Stage stage) throws Exception {
        injector = injectorWithSimpleMocks(new TestModule(), StageDemon.class, TaskDemon.class,
                                           PlayerFacade.class, PlayerController.class, TrackSetAreaRowFactory.class,
                                           RootController.class, NavigationController.class);

        loadControllerModule(Layout.ARTISTS);
        stage.setScene(new Scene(module.providesController().getRoot()));

        injector = injector.createChildInjector(module);

        stage.show();
    }

    @Test
    @DisplayName("Singleton controller")
    void singletonController() {
        ArtistsViewController anotherController = injector.getInstance(ArtistsViewController.class);

        assertSame(controller, anotherController);
    }

    private class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            install(new ParseModule());
            install(new TrackFactoryModule());
        }

        @Provides
        @RootPlaylist
        Playlist providesRootPlaylist(PlaylistFactory factory) {
            return factory.create("ROOT", true);
        }

        @Provides
        @EmptyLibraryProperty
        ReadOnlyBooleanProperty providesEmptyLibraryProperty() {
            return falseProperty;
        }

        @Provides
        @SearchingTextProperty
        StringProperty providesSearchingTextProperty() {
            return new SimpleStringProperty("test");
        }

        @Provides
        @ShowingTracksProperty
        ListProperty<Entry<Integer, Track>> providesShowingTracksProperty() {
            return new SimpleListProperty<>();
        }
    }
}