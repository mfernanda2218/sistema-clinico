module clinica.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    // javafx.graphics instancia a Application via reflexão
    exports clinica.view to javafx.graphics;

    // RefleXão de terceiros: Gson desserializa o model; PropertyValueFactory
    // (TableView) lê os getters via reflexão em javafx.base
    opens clinica.model to com.google.gson, javafx.base;
}
