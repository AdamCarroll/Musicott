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
 * along with Musicott library.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.musicott;

import java.io.IOException;

import com.musicott.error.ErrorHandler;
import com.musicott.task.LoadLibraryTask;
import com.musicott.view.EditController;
import com.musicott.view.ErrorDialogController;
import com.musicott.view.ImportCollectionController;
import com.musicott.view.ProgressImportController;
import com.musicott.view.RootLayoutController;
import com.musicott.model.ObservableTrack;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * @author Octavio Calleya
 *
 */
public class SceneManager {

	private Stage rootStage;
	private Stage importStage;
	private Stage progressStage;
	private Stage editStage;
	private Scene mainScene;

	private EditController editController;
	private RootLayoutController rootController;
	private ImportCollectionController importController;
	private ProgressImportController progressImportController;
	
	private static SceneManager instance;
	
	private SceneManager() {
	}
	
	public static SceneManager getInstance() {
		if(instance==null)
			instance = new SceneManager();
		return instance;
	}
	
	protected void setPrimaryStage(Stage primaryStage) {
		rootStage = primaryStage;
		initPrimaryStage();
	}

	
	public RootLayoutController getRootController() {
		return rootController;
	}
	
	public ImportCollectionController getImportController() {
		return importController;
	}
	
	public ProgressImportController getProgressImportController() {
		return progressImportController;
	}
	
	public void openEditScene(ObservableList<ObservableTrack> selection) {
		try {
			editStage = new Stage();
			editStage.setTitle("Edit");
			
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(SceneManager.class.getResource("view/EditInfoView.fxml"));
			AnchorPane editLayout = (AnchorPane) loader.load();
			editController = loader.getController();
			editController.setStage(editStage);
			editController.setSelection(selection);
			
			Scene editScene = new Scene(editLayout);
			editStage.initModality(Modality.APPLICATION_MODAL);
			editStage.initOwner(editScene.getWindow());
			editStage.setScene(editScene);
			editStage.setResizable(false);
			editStage.showAndWait();
		} catch (IOException e) {
			//TODO Show error dialog and closes edit scene
			e.printStackTrace();
		}
	}
	
	public void openImportScene() {
		try {
			importStage = new Stage();
			importStage.setTitle("Import");
			
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/ImportCollectionView.fxml"));

			AnchorPane importLayout = (AnchorPane) loader.load();
			importController = loader.getController();
			importController.setStage(importStage);
			
			Scene importMainScene = new Scene(importLayout);
			importStage.initModality(Modality.APPLICATION_MODAL);
			importStage.initOwner(importMainScene.getWindow());
			importStage.setScene(importMainScene);
			importStage.setResizable(false);
			importStage.showAndWait();
		} catch (IOException e) {
			//TODO Show error dialog and closes import scene
			e.printStackTrace();
		}
	}
	
	public void closeImportScene() {
		if(ErrorHandler.getInstance().hasErrors()) {
			ErrorDialogController edc = new ErrorDialogController(progressStage.getScene());
			edc.showDialog();
			ErrorHandler.getInstance().reset();
		}
		progressStage.close();
		if(importStage != null && importStage.isShowing())
			importStage.close();
	}
	
	public void showImportProgressScene(Task<?> task, boolean hideCancelButton) {
		try {
			progressStage = new Stage();
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/ProgressImportView.fxml"));
			AnchorPane progressLayout =  (AnchorPane) loader.load();
			progressImportController = loader.getController();
			if(hideCancelButton)
				progressImportController.hideCancelButton();
			progressImportController.setTask(task);
			progressImportController.runTask();
			
			Scene progressScene = new Scene(progressLayout);
			progressStage.initModality(Modality.APPLICATION_MODAL);
			progressStage.initOwner(progressScene.getWindow());
			progressStage.setScene(progressScene);
			progressStage.setResizable(false);
			progressStage.showAndWait();
		}
		catch(IOException e) {
			// TODO Show error dialog and closes import scene
			e.printStackTrace();
		}
	}
	
	private void initPrimaryStage() {
		try {
			FXMLLoader rootLoader = new FXMLLoader();
			rootLoader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
			BorderPane rootLayout = (BorderPane) rootLoader.load();
			rootController = rootLoader.getController();
			rootController.setStage(rootStage);

			LoadLibraryTask task = new LoadLibraryTask();
			showImportProgressScene(task, true);
			
			mainScene = new Scene(rootLayout);
			rootStage.setMinWidth(1200);
			rootStage.setMinHeight(775);
			rootStage.setScene(mainScene);
			rootStage.show();
		} catch (IOException e) {
			//TODO Show error dialog and crashes
			e.printStackTrace();
		}
	}
}