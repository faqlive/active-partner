package org.example.activepartner.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.example.activepartner.domain.ActionPoint;

public class PointsTableController {
    @FXML
    private TableView<ActionPoint> pointsTable;
    @FXML
    private TableColumn<ActionPoint, String> nameColumn;
    @FXML
    private TableColumn<ActionPoint, Number> xColumn;
    @FXML
    private TableColumn<ActionPoint, Number> yColumn;
    @FXML
    private TableColumn<ActionPoint, String> actionColumn;
    @FXML
    private TableColumn<ActionPoint, Boolean> enabledColumn;
    @FXML
    private TableColumn<ActionPoint, String> clickColumn;
    @FXML
    private TableColumn<ActionPoint, Number> delayColumn;

    public TableView<ActionPoint> getPointsTable() {
        return pointsTable;
    }

    public TableColumn<ActionPoint, String> getNameColumn() {
        return nameColumn;
    }

    public TableColumn<ActionPoint, Number> getXColumn() {
        return xColumn;
    }

    public TableColumn<ActionPoint, Number> getYColumn() {
        return yColumn;
    }

    public TableColumn<ActionPoint, String> getActionColumn() {
        return actionColumn;
    }

    public TableColumn<ActionPoint, Boolean> getEnabledColumn() {
        return enabledColumn;
    }

    public TableColumn<ActionPoint, String> getClickColumn() {
        return clickColumn;
    }

    public TableColumn<ActionPoint, Number> getDelayColumn() {
        return delayColumn;
    }
}
