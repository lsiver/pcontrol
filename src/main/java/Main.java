
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {
    private static final double DEFAULT_DT = 0.1;

    private TextField horizonField;
    private TextField kpField;
    private TextField tauField;
    private TextField deadtimeField;
    private Label openLoopValidationLabel;
    private Label controlValidationLabel;
    private LineChart<Number, Number> openLoopChart;
    private TextField controllerKpField;
    private TextField tiField;
    private TextField tdField;
    private ComboBox<String> equationTypeComboBox;
    private TextField dspField;
    private LineChart<Number, Number> pvSpChart;
    private LineChart<Number, Number> opChart;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Process Control");
        stage.setScene(new Scene(buildRoot(), 1100, 700));
        stage.show();
        replotOpenLoop();
        replotControl();
    }

    private BorderPane buildRoot() {
        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(createOpenLoopTab());
        tabPane.getTabs().add(createControlTab());

        BorderPane root = new BorderPane(tabPane);
        root.setPadding(new Insets(16));
        return root;
    }

    private Tab createOpenLoopTab() {
        horizonField = new TextField("45.0");
        kpField = new TextField("1.5");
        tauField = new TextField("5");
        deadtimeField = new TextField("0");
        openLoopValidationLabel = new Label();
        openLoopValidationLabel.setStyle("-fx-text-fill: #b42318;");

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
                openLoopValidationLabel);
        controls.setAlignment(Pos.TOP_LEFT);
        controls.setPadding(new Insets(16));
        controls.setPrefWidth(280);
        controls.setMinWidth(280);

        openLoopChart = createLineChart("Process Response", "Time", "Value");

        BorderPane content = new BorderPane();
        content.setLeft(controls);
        content.setCenter(openLoopChart);
        BorderPane.setMargin(openLoopChart, new Insets(0, 0, 0, 16));
        BorderPane.setAlignment(controls, Pos.TOP_LEFT);

        Tab openLoopTab = new Tab("Open Loop", content);
        openLoopTab.setClosable(false);
        return openLoopTab;
    }

    private Tab createControlTab() {
        controllerKpField = new TextField("0.35");
        tiField = new TextField("3.0");
        tdField = new TextField("0.0");
        dspField = new TextField("5.0");
        equationTypeComboBox = new ComboBox<>();
        equationTypeComboBox.getItems().addAll("A", "B", "C");
        equationTypeComboBox.setValue("B");

        controlValidationLabel = new Label();
        controlValidationLabel.setStyle("-fx-text-fill: #b42318;");

        Button replotButton = new Button("Replot");
        replotButton.setMaxWidth(Double.MAX_VALUE);
        replotButton.setOnAction(event -> replotControl());

        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(12);
        inputGrid.addRow(0, new Label("Kp"), controllerKpField);
        inputGrid.addRow(1, new Label("Ti"), tiField);
        inputGrid.addRow(2, new Label("Td"), tdField);
        inputGrid.addRow(3, new Label("Equation"), equationTypeComboBox);

        GridPane spGrid = new GridPane();
        spGrid.setHgap(10);
        spGrid.setVgap(12);
        spGrid.addRow(0, new Label("dSP"), dspField);

        VBox controls = new VBox(14,
                new Label("Controller Parameters"),
                inputGrid,
                new Label("Process values come from the Open Loop tab."),
                new Label("Setpoint Change"),
                spGrid,
                replotButton,
                controlValidationLabel);
        controls.setAlignment(Pos.TOP_LEFT);
        controls.setPadding(new Insets(16));
        controls.setPrefWidth(280);
        controls.setMinWidth(280);

        pvSpChart = createLineChart("PV vs SP", "Time", "Value");
        opChart = createLineChart("Controller Output (OP)", "Time", "% Output");

        VBox charts = new VBox(16, pvSpChart, opChart);
        charts.setPadding(new Insets(0, 0, 0, 16));
        pvSpChart.setMinHeight(280);
        opChart.setMinHeight(220);

        BorderPane content = new BorderPane();
        content.setLeft(controls);
        content.setCenter(charts);

        Tab controlTab = new Tab("Control", content);
        controlTab.setClosable(false);
        return controlTab;
    }

    private void replotOpenLoop() {
        try {
            Process process = buildProcessFromOpenLoopInputs();
            PID pid = new PID(0.0, 0.0, 0.0, "B");
            Simulation simulation = new Simulation(pid, process);
            simulation.runOpenLoopSim();

            openLoopChart.getData().setAll(toSeries("Open Loop", simulation.getPVData()));
            openLoopValidationLabel.setText("");
        } catch (IllegalArgumentException exception) {
            openLoopValidationLabel.setText(exception.getMessage());
        }
    }

    private void replotControl() {
        try {
            Process process = buildProcessFromOpenLoopInputs();
            double controllerKp = parseDouble(controllerKpField.getText(), "Kp");
            double ti = parsePositiveDouble(tiField.getText(), "Ti");
            double td = parseNonNegativeDouble(tdField.getText(), "Td");
            double dSP = parseDouble(dspField.getText(), "dSP");
            String equationType = equationTypeComboBox.getValue();

            PID pid = new PID(controllerKp, ti, td, equationType);
            Simulation simulation = new Simulation(pid, process);
            simulation.runSPChangeSim(dSP);

            pvSpChart.getData().setAll(
                    toSeries("Process Variable (PV)", simulation.getPVData()),
                    toSeries("Setpoint (SP)", simulation.getSPData()));
            opChart.getData().setAll(toSeries("OP Data", simulation.getOPData()));
            controlValidationLabel.setText("");
        } catch (IllegalArgumentException exception) {
            controlValidationLabel.setText(exception.getMessage());
        }
    }

    private Process buildProcessFromOpenLoopInputs() {
        double horizon = parsePositiveDouble(horizonField.getText(), "Horizon");
        double kp = parseDouble(kpField.getText(), "Kp");
        double tau = parsePositiveDouble(tauField.getText(), "tau");
        double deadtime = parseNonNegativeDouble(deadtimeField.getText(), "deadtime");
        return new Process(DEFAULT_DT, horizon, kp, tau, deadtime);
    }

    private LineChart<Number, Number> createLineChart(String title, String xLabel, String yLabel) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel(xLabel);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);

        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        chart.setLegendVisible(true);
        return chart;
    }

    private XYChart.Series<Number, Number> toSeries(String seriesName, double[][] data) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(seriesName);

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
