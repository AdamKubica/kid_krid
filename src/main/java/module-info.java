module lab01 {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires org.apache.logging.log4j.core;
    requires static lombok;
    opens lab to javafx.fxml;
    exports lab;
}
