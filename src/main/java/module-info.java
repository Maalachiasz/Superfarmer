module com.example.superfarmer {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.superfarmer to javafx.fxml;
    exports com.example.superfarmer;
}