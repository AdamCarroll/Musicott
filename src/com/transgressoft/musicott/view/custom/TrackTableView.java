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

import com.transgressoft.musicott.model.*;
import com.transgressoft.musicott.player.*;
import com.transgressoft.musicott.util.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.util.*;
import javafx.util.Duration;

import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.Map.*;
import java.util.stream.*;

/**
 * Class that extends from {@link TableView} and models a table that represents {@link Track}
 * instances in their rows, showing almost all of the properties of a <tt>Track</tt> in the columns.
 *
 * @author Octavio Calleya
 * @version 0.9-b
 */
public class TrackTableView extends TableView<Entry<Integer, Track>> {

	private static final String CENTER_RIGHT_STYLE = "-fx-alignment: CENTER-RIGHT";

	private TableColumn<Entry<Integer, Track>, String> nameCol;
	private TableColumn<Entry<Integer, Track>, String> artistCol;
	private TableColumn<Entry<Integer, Track>, String> albumCol;
	private TableColumn<Entry<Integer, Track>, String> genreCol;
	private TableColumn<Entry<Integer, Track>, String> commentsCol;
	private TableColumn<Entry<Integer, Track>, String> albumArtistCol;
	private TableColumn<Entry<Integer, Track>, String> labelCol;
	private TableColumn<Entry<Integer, Track>, Number> sizeCol;
	private TableColumn<Entry<Integer, Track>, Number> yearCol;
	private TableColumn<Entry<Integer, Track>, Number> bitRateCol;
	private TableColumn<Entry<Integer, Track>, Number> playCountCol;
	private TableColumn<Entry<Integer, Track>, Number> discNumberCol;
	private TableColumn<Entry<Integer, Track>, Number> bpmCol;
	private TableColumn<Entry<Integer, Track>, Number> trackNumberCol;
	private TableColumn<Entry<Integer, Track>, LocalDateTime> dateModifiedCol;
	private TableColumn<Entry<Integer, Track>, LocalDateTime> dateAddedCol;
	private TableColumn<Entry<Integer, Track>, Duration> totalTimeCol;
	private TableColumn<Entry<Integer, Track>, Boolean> coverCol;

	private TrackTableViewContextMenu trackTableContextMenu;
	private List<Entry<Integer, Track>> selection;

	private PlayerFacade player;

	@SuppressWarnings ("unchecked")
	public TrackTableView() {
		super();
		player = PlayerFacade.getInstance();
		selection = getSelectionModel().getSelectedItems();
		setId("trackTable");
		initColumns();
		getColumns().addAll(artistCol, nameCol, albumCol, genreCol, labelCol, bpmCol, totalTimeCol);
		getColumns().addAll(yearCol, sizeCol, trackNumberCol, discNumberCol, albumArtistCol, commentsCol);
		getColumns().addAll(bitRateCol, playCountCol, dateModifiedCol, dateAddedCol, coverCol);
		GridPane.setHgrow(this, Priority.ALWAYS);
		GridPane.setVgrow(this, Priority.ALWAYS);
		setPrefWidth(USE_COMPUTED_SIZE);
		setPrefHeight(USE_COMPUTED_SIZE);
		setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);
		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		getSelectionModel().selectedIndexProperty().addListener(
				(observable, oldValue, newValue) -> selection = getSelectionModel().getSelectedItems());
		getSortOrder().add(dateAddedCol);
		setRowFactory(tableView -> getTrackTableRowFactory());
		addEventHandler(KeyEvent.KEY_PRESSED, getKeyPressedEventHandler());

		trackTableContextMenu = new TrackTableViewContextMenu(getSelectionModel().getSelectedItems());
		setContextMenu(trackTableContextMenu);
		addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
			if (event.getButton() == MouseButton.SECONDARY)
				trackTableContextMenu.show(this, event.getScreenX(), event.getScreenY());
			else if (event.getButton() == MouseButton.PRIMARY && trackTableContextMenu.isShowing())
				trackTableContextMenu.hide();
		});
	}

	/**
	 * Returns a {@link TableRow} that fires the play of a {@link Track}
	 * when the user double-clicks a row.
	 *
	 * @return The <tt>TableRow</tt>
	 */
	private TableRow<Entry<Integer, Track>> getTrackTableRowFactory() {
		TableRow<Entry<Integer, Track>> row = new TableRow<>();
		row.setOnMouseClicked(event -> {
			if (event.getClickCount() == 2 && ! row.isEmpty()) {
				List<Integer> singleTrackIdList = Collections.singletonList(row.getItem().getKey());
				player.addTracksToPlayQueue(singleTrackIdList, true);
			}
		});
		return row;
	}

	/**
	 * Returns a {@link EventHandler} that fires the play of a {@link Track} when
	 * the user presses the <tt>Enter</tt> key, and pauses/resumes the player when the user
	 * presses the <tt>Space</tt> key.
	 *
	 * @return The <tt>EventHandler</tt>
	 */
	private EventHandler<KeyEvent> getKeyPressedEventHandler() {
		return event -> {
			if (event.getCode() == KeyCode.ENTER) {
				List<Integer> selectionIDs = selection.stream().map(Entry::getKey).collect(Collectors.toList());
				player.addTracksToPlayQueue(selectionIDs, true);
			}
			else if (event.getCode() == KeyCode.SPACE) {
				String playerStatus = player.getPlayerStatus();
				if ("PLAYING".equals(playerStatus))
					player.pause();
				else if ("PAUSED".equals(playerStatus))
					player.resume();
				else if ("STOPPED".equals(playerStatus))
					player.play(true);
			}
		};
	}

	private void initColumns() {
		Callback<TableColumn<Entry<Integer, Track>, Number>, TableCell<Entry<Integer, Track>, Number>>
				numericCellFactory = columns -> new NumericTableCell();

		Callback<TableColumn<Entry<Integer, Track>, LocalDateTime>, TableCell<Entry<Integer, Track>, LocalDateTime>>
				dateCellFactory = column -> new DateTimeTableCell();

		nameCol = new TableColumn<>("Name");
		nameCol.setPrefWidth(170);
		nameCol.setCellValueFactory(cellData -> cellData.getValue().getValue().nameProperty());

		artistCol = new TableColumn<>("Artist");
		artistCol.setPrefWidth(170);
		artistCol.setCellValueFactory(cellData -> cellData.getValue().getValue().artistProperty());

		albumCol = new TableColumn<>("Album");
		albumCol.setPrefWidth(170);
		albumCol.setCellValueFactory(cellData -> cellData.getValue().getValue().albumProperty());

		genreCol = new TableColumn<>("Genre");
		genreCol.setPrefWidth(120);
		genreCol.setCellValueFactory(cellData -> cellData.getValue().getValue().genreProperty());

		commentsCol = new TableColumn<>("Comments");
		commentsCol.setPrefWidth(150);
		commentsCol.setCellValueFactory(cellData -> cellData.getValue().getValue().commentsProperty());

		albumArtistCol = new TableColumn<>("Album Artist");
		albumArtistCol.setPrefWidth(100);
		albumArtistCol.setCellValueFactory(cellData -> cellData.getValue().getValue().albumArtistProperty());

		labelCol = new TableColumn<>("Label");
		labelCol.setPrefWidth(120);
		labelCol.setCellValueFactory(cellData -> cellData.getValue().getValue().labelProperty());

		dateModifiedCol = new TableColumn<>("Modified");
		dateModifiedCol.setPrefWidth(110);
		dateModifiedCol.setCellValueFactory(cellData -> cellData.getValue().getValue().lastDateModifiedProperty());
		dateModifiedCol.setStyle(CENTER_RIGHT_STYLE);
		dateModifiedCol.setCellFactory(dateCellFactory);

		dateAddedCol = new TableColumn<>("Added");
		dateAddedCol.setPrefWidth(110);
		dateAddedCol.setCellValueFactory(
				cellData -> new SimpleObjectProperty<>(cellData.getValue().getValue().getDateAdded()));
		dateAddedCol.setStyle(CENTER_RIGHT_STYLE);
		dateAddedCol.setSortType(TableColumn.SortType.DESCENDING);    // Default sort of the table
		dateAddedCol.setCellFactory(dateCellFactory);

		sizeCol = new TableColumn<>("Size");
		sizeCol.setPrefWidth(64);
		sizeCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getValue().getSize()));
		sizeCol.setStyle(CENTER_RIGHT_STYLE);
		sizeCol.setCellFactory(column -> new ByteSizeTableCell());

		totalTimeCol = new TableColumn<>("Duration");
		totalTimeCol.setPrefWidth(60);
		totalTimeCol.setCellValueFactory(
				cellData -> new SimpleObjectProperty<>(cellData.getValue().getValue().getTotalTime()));
		totalTimeCol.setStyle(CENTER_RIGHT_STYLE);
		totalTimeCol.setCellFactory(column -> new DurationTableCell());

		yearCol = new TableColumn<>("Year");
		yearCol.setPrefWidth(60);
		yearCol.setCellValueFactory(cellData -> cellData.getValue().getValue().yearProperty());
		yearCol.setCellFactory(numericCellFactory);
		yearCol.setStyle(CENTER_RIGHT_STYLE);

		playCountCol = new TableColumn<>("Plays");
		playCountCol.setPrefWidth(50);
		playCountCol.setCellValueFactory(cellData -> cellData.getValue().getValue().playCountProperty());
		playCountCol.setStyle(CENTER_RIGHT_STYLE);

		discNumberCol = new TableColumn<>("Disc Num");
		discNumberCol.setPrefWidth(45);
		discNumberCol.setCellValueFactory(cellData -> cellData.getValue().getValue().discNumberProperty());
		discNumberCol.setStyle(CENTER_RIGHT_STYLE);
		discNumberCol.setCellFactory(numericCellFactory);

		trackNumberCol = new TableColumn<>("Track Num");
		trackNumberCol.setPrefWidth(45);
		trackNumberCol.setCellValueFactory(cellData -> cellData.getValue().getValue().trackNumberProperty());
		trackNumberCol.setStyle(CENTER_RIGHT_STYLE);
		trackNumberCol.setCellFactory(numericCellFactory);

		bitRateCol = new TableColumn<>("BitRate");
		bitRateCol.setPrefWidth(60);
		bitRateCol.setCellValueFactory(
				cellData -> new SimpleIntegerProperty(cellData.getValue().getValue().getBitRate()));
		bitRateCol.setCellFactory(columns -> new BitRateTableCell());
		bitRateCol.setStyle(CENTER_RIGHT_STYLE);

		coverCol = new TableColumn<>("Cover");
		coverCol.setPrefWidth(50);
		coverCol.setCellValueFactory(cellData -> cellData.getValue().getValue().hasCoverProperty());
		coverCol.setCellFactory(CheckBoxTableCell.forTableColumn(coverCol));

		bpmCol = new TableColumn<>("BPM");
		bpmCol.setPrefWidth(60);
		bpmCol.setCellValueFactory(cellData -> cellData.getValue().getValue().bpmProperty());
		bpmCol.setStyle(CENTER_RIGHT_STYLE);
		bpmCol.setCellFactory(numericCellFactory);
	}
}

class NumericTableCell extends TableCell<Entry<Integer, Track>, Number> {

	@Override
	protected void updateItem(Number item, boolean empty) {
		super.updateItem(item, empty);
		if (empty || item == null)
			setText("");
		else if (((int) item) < 1)
			setText("");
		else
			setText("" + item);
	}
}

class DateTimeTableCell extends TableCell<Entry<Integer, Track>, LocalDateTime> {

	@Override
	protected void updateItem(LocalDateTime item, boolean empty) {
		super.updateItem(item, empty);
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
		if (item == null)
			setText("");
		else
			setText(item.format(dateFormatter));
	}
}

class ByteSizeTableCell extends TableCell<Entry<Integer, Track>, Number> {

	@Override
	protected void updateItem(Number item, boolean empty) {
		super.updateItem(item, empty);
		if (item == null)
			setText("");
		else
			setText(Utils.byteSizeString(item.longValue(), 1));
	}
}

class DurationTableCell extends TableCell<Entry<Integer, Track>, Duration> {

	@Override
	protected void updateItem(Duration item, boolean empty) {
		super.updateItem(item, empty);
		if (item == null)
			setText("");
		else {
			int hours = (int) item.toHours();
			int minutes = (int) item.subtract(Duration.hours(hours)).toMinutes();
			int seconds = (int) item.subtract(Duration.minutes(minutes)).subtract(Duration.hours(hours)).toSeconds();

			String text = "";
			if (hours > 0)
				text += Integer.toString(hours) + ":";
			if (minutes < 10)
				text += "0" + Integer.toString(minutes) + ":";
			else
				text += Integer.toString(minutes) + ":";
			if (seconds < 10)
				text += "0" + Integer.toString(seconds);
			else
				text += Integer.toString(seconds);
			setText(text);
		}
	}
}

class BitRateTableCell extends TableCell<Entry<Integer, Track>, Number> {

	@Override
	protected void updateItem(Number item, boolean empty) {
		super.updateItem(item, empty);
		if (empty || item == null)
			setText("");
		else if (((int) item) == 0)
			setText("");
		else
			setText("" + item);
	}
}
