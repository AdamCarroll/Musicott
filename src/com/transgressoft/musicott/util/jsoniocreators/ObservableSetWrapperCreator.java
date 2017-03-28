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

package com.transgressoft.musicott.util.jsoniocreators;

import com.cedarsoftware.util.io.JsonReader.*;
import javafx.collections.*;

import java.util.*;

/**
 * Class needed by the {@code Json-io} library in order to deserialize an {@link ObservableSet}.
 *
 * @author Octavio Calleya
 * @version 0.10-b
 * @see <a href="https://github.com/jdereg/json-io">Json-io</a>
 */
public class ObservableSetWrapperCreator implements ClassFactory {

    @SuppressWarnings ("rawtypes")
    @Override
    public Object newInstance(Class c) {
        return FXCollections.observableSet(new HashSet<>());
    }
}