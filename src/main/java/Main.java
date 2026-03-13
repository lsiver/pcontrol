
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
    private static final double DEFAULT_DT = 0.1;

    private TextField horizonField;
    private TextField kpField;
    private TextField tauField;
    private TextField deadtimeField;
    private Label validationLabel;
    private LineChart<Number, Number> openLoopChart;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Process Control");
        stage.setScene(new Scene(buildRoot(), 1100, 700));
        stage.show();
        replotOpenLoop();
    }

    private BorderPane buildRoot() {
        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(createOpenLoopTab());

        BorderPane root = new BorderPane(tabPane);
        root.setPadding(new Insets(16));
        return root;
    }

    private Tab createOpenLoopTab() {
        horizonField = new TextField("300.0");
        kpField = new TextField("1.5");
        tauField = new TextField("30.0");
        deadtimeField = new TextField("5.0");
        validationLabel = new Label();
        validationLabel.setStyle("-fx-text-fill: #b42318;");

        Button replotButton = new Button("Replot");
        replotButton.setMaxWidth(Double.MAX_VALUE);
        replotButton.setOnAction(event -> replotOpenLoop());

        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(12);
        inputGrid.addRow(0, new Label("Horizon"), horizonField);
        inputGrid.addRow(1, new Label("Kp"), kpField);
        inputGrid.addRow(2, new Label("tau"), tauField);
        inputGrid.addRow(3, new Label("deadtime"), deadtimeField);

        VBox controls = new VBox(14,
                new Label("Open Loop Parameters"),
                inputGrid,
                new Label("dt is fixed at 0.1 seconds."),
                replotButton,
                validationLabel);
        controls.setAlignment(Pos.TOP_LEFT);
        controls.setPadding(new Insets(16));
        controls.setPrefWidth(280);
        controls.setMinWidth(280);

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");

        openLoopChart = new LineChart<>(xAxis, yAxis);
        openLoopChart.setTitle("Process Response");
        openLoopChart.setCreateSymbols(false);
        openLoopChart.setAnimated(false);
        openLoopChart.setLegendVisible(true);

        BorderPane content = new BorderPane();
        content.setLeft(controls);
        content.setCenter(openLoopChart);
        BorderPane.setMargin(openLoopChart, new Insets(0, 0, 0, 16));
        BorderPane.setAlignment(controls, Pos.TOP_LEFT);
        VBox.setVgrow(replotButton, Priority.NEVER);

        Tab openLoopTab = new Tab("Open Loop", content);
        openLoopTab.setClosable(false);
        return openLoopTab;
    }

    private void replotOpenLoop() {
        try {
            double horizon = parsePositiveDouble(horizonField.getText(), "Horizon");
            double kp = parseDouble(kpField.getText(), "Kp");
            double tau = parsePositiveDouble(tauField.getText(), "tau");
            double deadtime = parseNonNegativeDouble(deadtimeField.getText(), "deadtime");

            PID pid = new PID(0.0, 0.0, 0.0, "B");
            Process process = new Process(DEFAULT_DT, horizon, kp, tau, deadtime);
            Simulation simulation = new Simulation(pid, process);
            simulation.runOpenLoopSim();

            openLoopChart.getData().setAll(toSeries(simulation.getPVData()));
            validationLabel.setText("");
        } catch (IllegalArgumentException exception) {
            validationLabel.setText(exception.getMessage());
        }
    }

    private XYChart.Series<Number, Number> toSeries(double[][] data) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Open Loop");

        if (data == null) {
            return series;
        }

        for (int i = 0; i < data[0].length; i++) {
            series.getData().add(new XYChart.Data<>(data[0][i], data[1][i]));
        }

        return series;
    }

    private double parsePositiveDouble(String rawValue, String fieldName) {
        double value = parseDouble(rawValue, fieldName);
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0.");
        }
        return value;
    }

    private double parseNonNegativeDouble(String rawValue, String fieldName) {
        double value = parseDouble(rawValue, fieldName);
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must be 0 or greater.");
        }
        return value;
    }

    private double parseDouble(String rawValue, String fieldName) {
        try {
            return Double.parseDouble(rawValue.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
    }
}
