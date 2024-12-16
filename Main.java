package com.example.filedatabaseapphse;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main extends Application {
    private TableView<ObservableList<String>> tableView;
    private Database database;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("File Database App");

        tableView = new TableView<>();
        Label statusLabel = new Label("Статус: База данных не выбрана.");

        TextField idField = new TextField();
        idField.setPromptText("ID");
        TextField nameField = new TextField();
        nameField.setPromptText("Имя");
        TextField subjectField = new TextField();
        subjectField.setPromptText("Предмет");
        TextField hoursField = new TextField();
        hoursField.setPromptText("Часы");
        TextField dateField = new TextField();
        dateField.setPromptText("Дата (ГГГГ-ММ-ДД)");

        Button createButton = new Button("Создать БД");
        Button loadButton = new Button("Открыть БД");
        Button addButton = new Button("Добавить запись");
        Button deleteButton = new Button("Удалить запись");
        Button searchButton = new Button("Поиск");
        Button backupButton = new Button("Создать Backup");
        Button restoreButton = new Button("Восстановить из Backup");
        Button exportButton = new Button("Экспорт в Excel");

        createButton.setOnAction(event -> createDatabase(primaryStage, statusLabel));
        loadButton.setOnAction(event -> loadDatabase(primaryStage, statusLabel));
        addButton.setOnAction(event -> addRecord(idField, nameField, subjectField, hoursField, dateField, statusLabel));
        deleteButton.setOnAction(event -> deleteRecord(statusLabel));
        searchButton.setOnAction(event -> searchRecord(statusLabel));
        backupButton.setOnAction(event -> {
            if (database == null) {
                statusLabel.setText("Ошибка: база данных не выбрана.");
                return;
            }
            try {
                BackupManager backupManager = new BackupManager(database.databaseFile);
                File backupFile = backupManager.createBackup();
                statusLabel.setText("Резервная копия создана: " + backupFile.getName());
            } catch (IOException e) {
                statusLabel.setText("Ошибка создания резервной копии: " + e.getMessage());
            }
        });

        restoreButton.setOnAction(event -> {
            if (database == null) {
                statusLabel.setText("Ошибка: база данных не выбрана.");
                return;
            }
            try {
                BackupManager backupManager = new BackupManager(database.databaseFile);
                backupManager.restoreFromBackup();
                statusLabel.setText("База данных восстановлена из резервной копии.");
                loadTableFromDatabase();
            } catch (IOException e) {
                statusLabel.setText("Ошибка восстановления базы данных: " + e.getMessage());
            }
        });

        exportButton.setOnAction(event -> {
            if (database == null) {
                statusLabel.setText("Ошибка: база данных не выбрана.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("database.xlsx");
            File excelFile = fileChooser.showSaveDialog(primaryStage);

            if (excelFile != null) {
                try {
                    database.exportToExcel(excelFile);
                    statusLabel.setText("База данных экспортирована в: " + excelFile.getName());
                } catch (IOException e) {
                    statusLabel.setText("Ошибка экспорта в Excel: " + e.getMessage());
                }
            }
        });

        HBox buttonBox = new HBox(10, createButton, loadButton, addButton, deleteButton, searchButton, backupButton, restoreButton, exportButton);
        VBox layout = new VBox(10, statusLabel, tableView, buttonBox);
        Scene scene = new Scene(layout, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createDatabase(Stage stage, Label statusLabel) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("new_database.csv");
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                database = new Database(file);
                database.create(new String[]{"ID", "Имя", "Предмет", "Часы", "Дата"});
                statusLabel.setText("База данных создана: " + file.getName());
                loadTableHeaders(new String[]{"ID", "Имя", "Предмет", "Часы", "Дата"});
            } catch (IOException e) {
                statusLabel.setText("Ошибка создания базы данных: " + e.getMessage());
            }
        }
    }

    private void loadDatabase(Stage stage, Label statusLabel) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            database = new Database(file);
            statusLabel.setText("База данных загружена: " + file.getName());
            loadTableFromDatabase();
        }
    }

    private void addRecord(TextField idField, TextField nameField, TextField subjectField, TextField hoursField, TextField dateField, Label statusLabel) {
        if (database == null) {
            statusLabel.setText("Ошибка: база данных не выбрана.");
            return;
        }

        String id = idField.getText();
        String name = nameField.getText();
        String subject = subjectField.getText();
        String hours = hoursField.getText();
        String date = dateField.getText();

        if (id.isEmpty() || name.isEmpty() || subject.isEmpty() || hours.isEmpty() || date.isEmpty()) {
            statusLabel.setText("Ошибка: заполните все поля.");
            return;
        }

        try {
            database.addRecord(new String[]{id, name, subject, hours, date});
            statusLabel.setText("Запись добавлена.");
            loadTableFromDatabase();
            idField.clear();
            nameField.clear();
            subjectField.clear();
            hoursField.clear();
            dateField.clear();
        } catch (IOException e) {
            statusLabel.setText("Ошибка добавления записи: " + e.getMessage());
        }
    }

    private void deleteRecord(Label statusLabel) {
        if (database == null) {
            statusLabel.setText("Ошибка: база данных не выбрана.");
            return;
        }

        try {
            database.deleteRecords("ID", "1");
            statusLabel.setText("Запись удалена.");
            loadTableFromDatabase();
        } catch (IOException e) {
            statusLabel.setText("Ошибка удаления записи: " + e.getMessage());
        }
    }



    private void searchRecord(Label statusLabel) {
        if (database == null) {
            statusLabel.setText("Ошибка: база данных не выбрана.");
            return;
        }

        try {
            List<String[]> results = database.search("Имя", "Иван Иванов");
            statusLabel.setText("Найдено записей: " + results.size());
            loadTableHeaders(new String[]{"ID", "Имя", "Предмет", "Часы", "Дата"});
            tableView.getItems().clear();
            for (String[] row : results) {
                tableView.getItems().add(FXCollections.observableArrayList(row));
            }
        } catch (IOException e) {
            statusLabel.setText("Ошибка поиска записи: " + e.getMessage());
        }
    }

    private void loadTableHeaders(String[] headers) {
        tableView.getColumns().clear();
        for (String header : headers) {
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(header);
            int columnIndex = tableView.getColumns().size();
            column.setCellValueFactory(param -> {
                if (param.getValue().size() > columnIndex) {
                    return new javafx.beans.property.SimpleStringProperty(param.getValue().get(columnIndex));
                } else {
                    return new javafx.beans.property.SimpleStringProperty("");
                }
            });
            tableView.getColumns().add(column);
        }
    }

    private void loadTableFromDatabase() {
        try {
            tableView.getItems().clear();
            try (var reader = new java.io.BufferedReader(new java.io.FileReader(database.databaseFile))) {
                String line;
                String[] headers = reader.readLine().split(",");
                loadTableHeaders(headers);

                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    tableView.getItems().add(FXCollections.observableArrayList(fields));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
