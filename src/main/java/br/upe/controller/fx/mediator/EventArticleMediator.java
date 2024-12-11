package br.upe.controller.fx.mediator;

import br.upe.controller.fx.EventArticleScreenController;
import br.upe.facade.FacadeInterface;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.UUID;

public class EventArticleMediator extends Mediator {
    private final EventArticleScreenController eventArticleScreenController;
    private UUID articleId;

    public EventArticleMediator(EventArticleScreenController eventArticleScreenController, FacadeInterface facade, AnchorPane screenPane, Label errorUpdtLabel) {
        super(facade, screenPane, errorUpdtLabel, eventArticleScreenController);
        this.eventArticleScreenController = eventArticleScreenController;
    }

    public void setArticleId(UUID eventId) {
        this.articleId = eventId;
    }

    @Override
    public void registerComponents() {
        if (screenPane != null) {
            setupButtonAction("#handleSubEventButton", "handleSubEvent");
            setupButtonAction("#handleSubmitButton", "handleSubmit");
            setupButtonAction("#handleSessionButton", "handleSession");
            setupButtonAction("#handleInscriptionButton", "handleInscription");
            setupButtonAction("#handleEventButton", "handleEvent");
            setupButtonAction("#handleUserButton", "handleUser");
            setupButtonAction("#handleBackButton", "handleBack");
            setupButtonAction("#logoutButton", "logout");
        }
    }

    @Override
    public Object notify(String event) throws IOException {
        if (eventArticleScreenController != null) {
            switch (event) {
                case "handleUser"
                , "handleSubmit"
                , "handleBack"
                , "handleSession"
                , "handleInscription"
                , "handleSubEvent"
                , "handleEvent":
                    loadScreenForEvent(event);
                    break;

                case "handleDeleteArticle":
                    return deleteButtonAlert();

                case "logout":
                    facade = null;
                    loadScreenForEvent("loginScreen");
                    break;

                default:
                    throw new IllegalArgumentException("Ação não reconhecida: " + event);
            }
        }
        return null;
    }

    private void loadScreenForEvent(String event){
        String fxmlFile = getFxmlPathForEvent(event);

        if (!event.equals("handleUpdateArticle")) {
            this.articleId = null;
        }

        loadScreenWithTask(() -> {
            try {
                eventArticleScreenController.genericButton(fxmlFile, screenPane, facade, String.valueOf(articleId));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String getFxmlPathForEvent(String event) {
        return switch (event) {
            case "handleUser" -> "/fxml/userScreen.fxml";
            case "handleInscription" -> "/fxml/attendeeScreen.fxml";
            case "handleSubmit" -> "/fxml/submitScreen.fxml";
            case "handleSession" -> "/fxml/sessionScreen.fxml";
            case "handleSubEvent" -> "/fxml/subEventScreen.fxml";
            case "handleEvent", "handleBack" -> "/fxml/eventScreen.fxml";
            case "loginScreen" -> "/fxml/loginScreen.fxml";
            default -> throw new IllegalArgumentException("Unknown event: " + event);
        };
    }

    private void loadScreenWithTask(Runnable task) {
        assert screenPane != null;
        eventArticleScreenController.loadScreen("Carregando", () -> {
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, screenPane);
    }

}
