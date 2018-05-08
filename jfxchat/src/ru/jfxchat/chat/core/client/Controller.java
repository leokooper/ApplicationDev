package ru.jfxchat.chat.core.client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import static javafx.application.Application.setUserAgentStylesheet;

public class Controller implements Initializable {
    final String SERVER_IP = "localhost";
    final int SERVER_PORT = 8189;
    @FXML
    HBox mainPane;
    @FXML
    TextArea textArea;
    @FXML
    TextField msgField;
    @FXML
    HBox authPanel;
    @FXML
    HBox msgPanel;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passField;
    @FXML
    ListView<String> clientsListView;

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    private boolean authorized;
    private String myNick;

    private ObservableList<String> clientsList;

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
        if (authorized){
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
            authPanel.setVisible(false);
            authPanel.setManaged(false);
            clientsListView.setVisible(true);
            clientsListView.setManaged(true);
        } else{
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
            authPanel.setVisible(true);
            authPanel.setManaged(true);
            clientsListView.setVisible(false);
            clientsListView.setManaged(false);
            myNick="";
        }
    }

    public void setLightTheme(){
        mainPane.getStylesheets().clear();
        setUserAgentStylesheet(null);
        mainPane.getStylesheets().add(getClass().getResource("modena_light.css").toExternalForm());
    }

    public void setDarkTheme(){
        mainPane.getStylesheets().clear();
        setUserAgentStylesheet(null);
        mainPane.getStylesheets().add(getClass().getResource("modena_dark.css").toExternalForm());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(false) ;}

    public void connect() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            clientsList = FXCollections.observableArrayList();
            clientsListView.setItems(clientsList);
            clientsListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
                @Override
                public ListCell<String> call(ListView<String> param) {
                    return new ListCell<String>(){
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            if(!empty) {
                                setText(item);
                                if(item.equals(myNick)) {
                                    setStyle("-fx-font-weight: bold; -fx-background-color: blue");
                                }
                                    } else {
                                        setGraphic(null);
                                        setText(null);
                                    }
                        }
                    };
                }
            });


            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        String s = in.readUTF();
                        if (s.startsWith("/authok ")) {
                            setAuthorized(true);
                            myNick = s.split("\\s")[1];
                            break;
                        }
                        textArea.appendText(s + "\n");
                    }
                    while (true) {
                        String s = in.readUTF();
                        if(s.startsWith("/")){
                            if(s.startsWith("/clientslist ")){
                                String[] data = s.split("\\s");{
                                    Platform.runLater(() -> {
                                        clientsList.clear();
                                        for (int i = 1; i < data.length; i++) {
                                            clientsList.addAll(data[i]);
                                        }
                                    });
                                }
                            }
                        } else {
                            textArea.appendText(s + "\n");
                        }
                    }
                } catch (IOException e) {
                    showAlert("Сервер перестал отвечать");
                } finally {
                    setAuthorized(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            showAlert("Не удалось подсоеденится, проверьте сетевое соединенение!");
        }
    }

    public void sendAuthMsg(){
            if(loginField.getText().isEmpty() || passField.getText().isEmpty()) {
                showAlert("Заполнены не все данные для авторизации");
                return;
        }

        if(socket == null || socket.isClosed()) {
            connect();
        }

        try {// /auth login pass
            out.writeUTF("/auth " + loginField.getText() + " " + passField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            showAlert("Неь удалось подсоеденится, проверте сетевое соединенение!");
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            showAlert("Не удалось подсоеденится, проверьте сетевое соединенение!");
        }
    }

    public void showAlert(String msg){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    public void clientsListClicked(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount() == 2) {
            msgField.setText("/w " + clientsListView.getSelectionModel().getSelectedItem() + " ");
            msgField.requestFocus();
            msgField.selectEnd();
        }
    }

}


