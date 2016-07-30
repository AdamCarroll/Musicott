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
 * Copyright (C) 2015, 2016 Octavio Calleya
 */

package com.transgressoft.musicott.view.custom;

import com.transgressoft.musicott.*;
import com.transgressoft.musicott.model.*;
import javafx.scene.control.*;

/**
 * ListView for the application navigation showing modes.
 *
 * @author Octavio Calleya
 * @vesion 0.9
 */
public class NavigationMenuListView extends ListView<NavigationMode> {

	private StageDemon stageDemon = StageDemon.getInstance();

	public NavigationMenuListView() {
		super();
		setId("showMenuListView");
		setPrefHeight(USE_COMPUTED_SIZE);
		setPrefWidth(USE_COMPUTED_SIZE);
		getSelectionModel().selectedItemProperty().addListener((obs, oldMenu, newMode) -> {
			if (newMode != null)
				stageDemon.getNavigationController().setNavigationMode(newMode);
		});
	}
}
