
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.jfree.chart.ChartPanel;

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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.embed.swing.SwingNode;

public class Main extends Application {
    private static final double DEFAULT_DT = 0.05;

    private TextField horizonField;
    private TextField kpField;
    private TextField tauField;
    private TextField deadtimeField;
    private Label openLoopValidationLabel;
    private Label controlValidationLabel;
    private LineChart<Number, Number> openLoopChart;
    private TextField controllerKcField;
    private TextField tiField;
    private TextField tdField;
    private TextField pvHiField;
    private TextField pvLoField;
    private TextField opHiField;
    private TextField opLoField;
    private ComboBox<String> equationTypeComboBox;
    private TextField dspField;
    private TextField distField;
    private LineChart<Number, Number> pvSpChart;
    private LineChart<Number, Number> opChart;
    private SwingNode openLoopSwingNode;
    private SwingNode controlSwingNode;
    private SwingNode disturbSwingNode;

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

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        

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


        Label kpLabel = new Label("Kp");
        Tooltip kpTooltip = new Tooltip("Change in PV per 1% OP change");
        kpTooltip.setShowDelay(javafx.util.Duration.millis(200));
        kpLabel.setTooltip(kpTooltip);

        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(12);
        inputGrid.addRow(0, new Label("Horizon"), horizonField);
        inputGrid.addRow(1, kpLabel, kpField);
        inputGrid.addRow(2, new Label("tau"), tauField);
        inputGrid.addRow(3, new Label("deadtime"), deadtimeField);

        VBox controls = new VBox(14,
                new Label("Open Loop Parameters"),
                inputGrid,
                // new Label("dt is fixed at 0.1 seconds."),
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
        controllerKcField = new TextField("0.35");
        tiField = new TextField("3.0");
        tdField = new TextField("0.0");
        pvHiField = new TextField("100");
        pvLoField = new TextField("0");
        opHiField = new TextField("100");
        opLoField = new TextField("0");

        dspField = new TextField("5.0");
        distField = new TextField("2.0");
        equationTypeComboBox = new ComboBox<>();
        equationTypeComboBox.getItems().addAll("A", "B", "C");
        equationTypeComboBox.setValue("B");

        controlValidationLabel = new Label();
        controlValidationLabel.setStyle("-fx-text-fill: #b42318;");

        Button replotButton = new Button("Replot");
        replotButton.setMaxWidth(Double.MAX_VALUE);
        replotButton.setOnAction(event -> replotControl());

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.NEVER);
        col1.setMinWidth(Region.USE_PREF_SIZE);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        col2.setMinWidth(50);
        
        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(12);
        inputGrid.addRow(0, new Label("Kc"), controllerKcField);
        inputGrid.addRow(1, new Label("Ti"), tiField);
        inputGrid.addRow(2, new Label("Td"), tdField);
        inputGrid.addRow(3, new Label("Equation"), equationTypeComboBox);
        inputGrid.getColumnConstraints().addAll(col1, col2);

        GridPane limitsGrid = new GridPane();
        limitsGrid.setHgap(10);
        limitsGrid.setVgap(12);
        limitsGrid.addRow(0, new Label("PV Hi"), pvHiField);
        limitsGrid.addRow(1, new Label("PV Lo"), pvLoField);
        limitsGrid.addRow(2, new Label("OP Hi"), opHiField);
        limitsGrid.addRow(3, new Label("OP Lo"), opLoField);
        limitsGrid.getColumnConstraints().addAll(col1, col2);

        GridPane spGrid = new GridPane();
        spGrid.setHgap(10);
        spGrid.setVgap(12);
        spGrid.addRow(0, new Label("dSP"), dspField);

        GridPane distGrid  = new GridPane();
        distGrid.setHgap(10);
        distGrid.setVgap(12);
        distGrid.addRow(0,new Label("dOP"), distField);

        Label infoLabel = new Label("Process values come from the Open Loop tab");
        infoLabel.setWrapText(true);
    

        VBox controls = new VBox(14,
                new Label("Controller Parameters"),
                inputGrid,
                infoLabel,
                limitsGrid,
                new Label("Setpoint Change"),
                spGrid,
                new Label("Disturbance Kick"),
                distGrid,
                replotButton,
                controlValidationLabel);

        infoLabel.prefWidthProperty().bind(controls.widthProperty().subtract(32));

        controls.setAlignment(Pos.TOP_LEFT);
        controls.setPadding(new Insets(16));
        controls.setMinWidth(150);
        controls.setMaxWidth(280);

        controlSwingNode = new SwingNode();
        controlSwingNode.prefWidth(800);
        controlSwingNode.prefHeight(600);

        disturbSwingNode = new SwingNode();
        disturbSwingNode.prefWidth(800);
        disturbSwingNode.prefHeight(600);

        VBox charts = new VBox(16,controlSwingNode, disturbSwingNode);
        charts.setPadding(new Insets(0, 0, 0, 16));

        BorderPane content = new BorderPane();
        content.setLeft(controls);
        content.setCenter(charts);

        controls.prefWidthProperty().bind(content.widthProperty().multiply(0.25));

        Tab controlTab = new Tab("Control", content);
        controlTab.setClosable(false);
        return controlTab;
    }

    private void replotOpenLoop() {
        try {
            Process process = buildProcessFromOpenLoopInputs();
            PID pid = new PID(0.0, 0.0, 0.0, "B", 100.0, 0.0, 100.0, 0.0);
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
            double controllerKc = parseDouble(controllerKcField.getText(), "Kc");
            double ti = parsePositiveDouble(tiField.getText(), "Ti");
            double td = parseNonNegativeDouble(tdField.getText(), "Td");
            double dSP = parseDouble(dspField.getText(), "dSP");
            String equationType = equationTypeComboBox.getValue();
            double dOP = parseDouble(distField.getText(), "dOP");

            double pvHi = parseDouble(pvHiField.getText(), "PV Hi");
            double pvLo = parseDouble(pvLoField.getText(), "PV Lo");
            double opHi = parseDouble(opHiField.getText(), "OP Hi");
            double opLo = parseDouble(opLoField.getText(), "OP Lo");

            PID pid = new PID(controllerKc, ti, td, equationType, pvHi, pvLo, opHi, opLo);
            Simulation simulation = new Simulation(pid, process);
            simulation.runSPChangeSim(dSP);

            // Use the Plotter to create the Swing panel
            Plotter plotter = new Plotter();
            ChartPanel spChart = plotter.createControlChart(
                simulation.getPVData(), 
                simulation.getSPData(), 
                simulation.getOPData()
            );

            simulation.runDisturbanceSim(dOP);
            ChartPanel distChart = plotter.createDisturbChart(
                simulation.getPVData(), 
                simulation.getSPData(), 
                simulation.getOPData()
            );

            javax.swing.SwingUtilities.invokeLater(() -> {
                controlSwingNode.setContent(spChart);
                disturbSwingNode.setContent(distChart);
            });

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

    private void addRunToLibrary(String loopName) {
        TuningRun run = new TuningRun(
            "Run " + System.currentTimeMillis(),
            parseDouble(horizonField.getText(), "Horizon"),
            parseDouble(kpField.getText(), "kp"),
            parseDouble(tauField.getText(), "tau"),
            parseDouble(deadtimeField.getText(), "deadtime"),
            parseDouble(controllerKcField.getText(), "Kc"),
            parseDouble(tiField.getText(), "Ti"),
            parseDouble(tdField.getText(), "Td"),
            parseDouble(pvHiField.getText(), "pvHi"),
            parseDouble(pvLoField.getText(), "pvLo"),
            parseDouble(opHiField.getText(), "opHi"),
            parseDouble(opLoField.getText(), "opLo"),
            equationTypeComboBox.getValue(),
            parseDouble(dspField.getText(), "dSP"),
            parseDouble(distField.getText(), "dOP")
            );

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(loopName + ".csv", true)))) {
            out.println(run.toString());
            controlValidationLabel.setText("Run saved to " + loopName + ".csv");
        } catch (IOException e) {
            controlValidationLabel.setText("Error Saving: " + e.getMessage());
        }
    }
}
