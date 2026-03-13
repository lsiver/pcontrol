import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class ProcessSimulator extends JFrame {

    // Simulation Parameters
    double dt = 0.1;          // Time step (seconds)
    double horizon = 100.0;   // Total simulation time
    
    // Process Parameters (FOPDT)
    double Kp = 1.5;          // Process Gain
    double tau = 10.0;        // Time Constant
    double deadtime = 5.0;    // Deadtime
    
    // Controller Parameters (Honeywell Standard)
    double Kc = .7;          // Controller Gain
    double Ti = 10;          // Integral Time (min or sec)
    double Td = 0.0;          // Derivative Time
    String type = "C";        // A, B, or C

    public ProcessSimulator() {
        super();
        this.setTitle("Process Control Simulator - Type "+type);
        runSimulation();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    private void runSimulation() {
        XYSeries pvSeries = new XYSeries("Process Variable (PV)");
        XYSeries spSeries = new XYSeries("Set Point (SP)");
        XYSeries opSeries = new XYSeries("Controller Output (OP)");

        double currentPV = 0;
        double currentOP = 0;
        double integralSum = 0;
        double lastPV = 0;
        double lastError = 0;
        double SP = 0;

        // Buffer for Deadtime
        Queue<Double> deadtimeBuffer = new LinkedList<>();
        int delaySteps = (int) (deadtime / dt);

        for (double t = 0; t <= horizon; t += dt) {
            // Step change at t = 5
            SP = (t > 5) ? 50.0 : 0;

            double error = SP - currentPV;
            double dPV = (currentPV - lastPV) / dt;
            double dE = (error - lastError) / dt;
            
            // 1. PID Logic (Honeywell Equations)
            double pTerm, iTerm, dTerm;
            
            // Integral is almost always on Error
            integralSum += error * dt;
            iTerm = (Kc / Ti) * integralSum;

            if (type.equals("A")) {
                pTerm = Kc * error;
                dTerm = Kc * Td * dE;
            } else if (type.equals("B")) {
                pTerm = Kc * error;
                dTerm = -Kc * Td * dPV; // D on PV
            } else { // Type C
                pTerm = -Kc * currentPV; // P on PV
                dTerm = -Kc * Td * dPV; // D on PV
            }

            currentOP = pTerm + iTerm + dTerm;
            
            // Clamp OP (0-100%)
            currentOP = Math.max(0, Math.min(100, currentOP));

            // 2. Handle Deadtime
            deadtimeBuffer.add(currentOP);
            double delayedOP = (deadtimeBuffer.size() > delaySteps) ? deadtimeBuffer.poll() : 0;

            // 3. Process Model (FOPDT)
            // PV_dot = (1/tau) * (Kp * OP_delayed - PV)
            double pvChange = (dt / tau) * (Kp * delayedOP - currentPV);
            currentPV += pvChange;

            // Update history
            lastPV = currentPV;
            lastError = error;

            // Data collection
            pvSeries.add(t, currentPV);
            spSeries.add(t, SP);
            opSeries.add(t, currentOP);
        }

        displayCharts(pvSeries, spSeries, opSeries);
    }

    private void displayCharts(XYSeries pv, XYSeries sp, XYSeries op) {
        XYSeriesCollection pvDataset = new XYSeriesCollection();
        pvDataset.addSeries(pv);
        pvDataset.addSeries(sp);
        
        XYSeriesCollection opDataset = new XYSeriesCollection();
        opDataset.addSeries(op);

        JFreeChart pvChart = ChartFactory.createXYLineChart("PV vs SP", "Time", "Value", pvDataset);
        JFreeChart opChart = ChartFactory.createXYLineChart("Controller Output (OP)", "Time", "% Output", opDataset);

        this.setLayout(new GridLayout(2, 1));
        this.add(new ChartPanel(pvChart));
        this.add(new ChartPanel(opChart));
        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        new ProcessSimulator();
    }
}