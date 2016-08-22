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

package com.transgressoft.musicott.tests;

import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import org.testfx.framework.junit.*;

/**
 * Base test class to initialize the FX Toolkit
 *
 * @author Octavio Calleya
 */
public class BaseJavaFxTest extends ApplicationTest {

	public Stage testStage;

	@Override
	public void start(Stage stage) throws Exception {
		testStage = stage;
		testStage.setScene(new Scene(new AnchorPane()));
		testStage.setTitle("Test");
	}
}
