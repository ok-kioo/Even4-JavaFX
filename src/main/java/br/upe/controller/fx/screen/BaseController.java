package br.upe.controller.fx.screen;


import br.upe.controller.fx.screen.attendee.CertificateScreenController;
import br.upe.controller.fx.screen.event.UpdateEventScreenController;
import br.upe.controller.fx.screen.session.UpdateSessionScreenController;
import br.upe.controller.fx.screen.subevent.UpdateSubEventScreenController;
import br.upe.controller.fx.screen.submit.EventArticleScreenController;
import br.upe.controller.fx.screen.submit.UpdateSubmitScreenController;
import br.upe.facade.FacadeInterface;
import br.upe.persistence.repository.EventRepository;
import br.upe.persistence.repository.SubEventRepository;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public abstract class BaseController {

    public void genericButton(String path, AnchorPane pane, FacadeInterface facade, String eventId) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        AnchorPane screen = loader.load();

        if (facade != null) {
            FxController screenController = loader.getController();
            screenController.setFacade(facade);

            if (screenController instanceof UpdateEventScreenController updateEventScreenController) {
                updateEventScreenController.setEventId(UUID.fromString(eventId));
            }
            if (screenController instanceof UpdateSubEventScreenController updateSubEventScreenController) {
                updateSubEventScreenController.setEventId(UUID.fromString(eventId));
            }
            if (screenController instanceof UpdateSessionScreenController updateSessionScreenController) {
                updateSessionScreenController.setEventName(eventId);
            }
            if (screenController instanceof UpdateSubmitScreenController updateSubmitScreenController) {
                updateSubmitScreenController.setEventName(eventId);
            }
            if (screenController instanceof CertificateScreenController certificateScreenController) {
                certificateScreenController.setEventName(eventId);
            }
            if (screenController instanceof EventArticleScreenController eventArticleScreenController) {
                eventArticleScreenController.setEventId(UUID.fromString(eventId));
            }
        }

        Platform.runLater(() -> {
            Scene scene = new Scene(screen);
            Stage stage = (Stage) pane.getScene().getWindow();

            stage.getIcons().clear();
            stage.getIcons().add(new javafx.scene.image.Image("/images/logo/Logo.png"));
            stage.setScene(scene);
            stage.setTitle("Even4");
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

        });
    }

    public Pane createLoadPane(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14pt; -fx-text-fill: white;");
        label.setAlignment(Pos.CENTER);

        StackPane loadStack = new StackPane();
        loadStack.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(-1); // Indeterminate state for spinner
        progressIndicator.setStyle("-fx-progress-color: white;");

        VBox vbox = new VBox(20, label, progressIndicator);
        vbox.setAlignment(Pos.CENTER);

        loadStack.getChildren().add(vbox);

        return loadStack;
    }

    public void loadScreen(String text, Runnable taskBackend, Pane root) {
        Pane loadPane = createLoadPane(text);

        loadPane.prefWidthProperty().bind(root.widthProperty());
        loadPane.prefHeightProperty().bind(root.heightProperty());

        Platform.runLater(() -> root.getChildren().add(loadPane));

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                taskBackend.run();
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> root.getChildren().remove(loadPane));
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> root.getChildren().remove(loadPane));
            }
        };

        new Thread(task).start();
    }

    public boolean validateEventDate(String date, String searchId, String type) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        EventRepository eventRepository = new EventRepository();
        SubEventRepository subEventRepository = new SubEventRepository();
        String parentDateString = "";
        if (type.equals("evento")) {
            parentDateString = String.valueOf(eventRepository.getData(UUID.fromString(searchId), "date"));
        } else if (type.equals("subEvento")) {
            parentDateString = String.valueOf(subEventRepository.getData(UUID.fromString(searchId), "date"));
        }

        if (parentDateString.contains(" ")) {
            parentDateString = parentDateString.split(" ")[0];
        }
        if (date.contains(" ")) {
            date = date.split(" ")[0];
        }

        LocalDate eventDate = LocalDate.parse(parentDateString, formatter);
        LocalDate inputDate = LocalDate.parse(date, formatter);

        return !inputDate.isBefore(eventDate);
    }

    public abstract void setFacade(FacadeInterface facade) throws IOException;
}

