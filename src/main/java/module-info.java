module cz.cvut.fel.pjv.guitar_survivors {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;


    opens cz.cvut.fel.pjv.guitar_survivors to javafx.fxml;
    exports cz.cvut.fel.pjv.guitar_survivors;
}