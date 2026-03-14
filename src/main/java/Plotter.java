import java.awt.GridLayout;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import javafx.scene.paint.Color;

public class Plotter extends JFrame {
    
    public Plotter() {
        this.setTitle("Process Control Simulator - Type ");
        // this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // this.setVisible(true);
    }

    public void plotPVSP(double[][] PVseries, double[][] SPseries, double[][] OPseries) {
        DefaultXYDataset PVSPdataset = new DefaultXYDataset();
        PVSPdataset.addSeries("Process Variable (PV)", PVseries);
        PVSPdataset.addSeries("Setpoint (SP)", SPseries);


        DefaultXYDataset OPdataset = new DefaultXYDataset();
        OPdataset.addSeries("OP Data", OPseries);

        JFreeChart pvChart = ChartFactory.createXYLineChart("PV vs SP", "Time", "Value", PVSPdataset);
        JFreeChart opChart = ChartFactory.createXYLineChart("Controller Output (OP)", "Time", "% Output", OPdataset);



        this.setLayout(new GridLayout(2, 1));
        this.add(new ChartPanel(pvChart));
        this.add(new ChartPanel(opChart));
        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

    }

    public void plotOpenLoop(double[][] PVSeries) {
        DefaultXYDataset PVdataset = new DefaultXYDataset();
        PVdataset.addSeries("OpenLoop", PVSeries);

        JFreeChart openLoopChart = ChartFactory.createXYLineChart("Process Response", "Time", "Value", PVdataset);

        this.setLayout(new GridLayout(1, 1));
        this.add(new ChartPanel(openLoopChart));
        this.pack();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    public ChartPanel createControlChart(double[][] pv, double[][] sp, double[][] op) {
        // 1. Create Datasets
        DefaultXYDataset pvSpDataset = new DefaultXYDataset();
        pvSpDataset.addSeries("PV", pv);
        pvSpDataset.addSeries("SP", sp);

        DefaultXYDataset opDataset = new DefaultXYDataset();
        opDataset.addSeries("OP", op);

        // 2. Setup Axes
        NumberAxis timeAxis = new NumberAxis("Time (min)");
        NumberAxis pvAxis = new NumberAxis("Process Variable / Setpoint");
        NumberAxis opAxis = new NumberAxis("Controller Output (%)");
        // opAxis.setRange(0.0, 100.0); // Fixed range for OP

        // 3. Create Plot and Map Datasets
        XYPlot plot = new XYPlot();
        plot.setDomainAxis(timeAxis);
        
        // Primary Axis (Left)
        plot.setRangeAxis(0, pvAxis);
        plot.setDataset(0, pvSpDataset);
        plot.setRenderer(0, new StandardXYItemRenderer());
        
        // Secondary Axis (Right)
        plot.setRangeAxis(1, opAxis);
        plot.setDataset(1, opDataset);
        plot.setRenderer(1, new StandardXYItemRenderer());
        
        // Map Dataset 1 (OP) to Axis 1 (Right)
        plot.mapDatasetToRangeAxis(1, 1);

        // Optional: Customizing colors
        // plot.getRenderer(1).setSeriesPaint(0, Color.GRAY); 

        JFreeChart chart = new JFreeChart("Setpoint Change Response", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        return new ChartPanel(chart);
    }

    public ChartPanel createDisturbChart(double[][] pv, double[][] sp, double[][] op) {
        // 1. Create Datasets
        DefaultXYDataset pvSpDataset = new DefaultXYDataset();
        pvSpDataset.addSeries("PV", pv);
        pvSpDataset.addSeries("SP", sp);

        DefaultXYDataset opDataset = new DefaultXYDataset();
        opDataset.addSeries("OP", op);

        // 2. Setup Axes
        NumberAxis timeAxis = new NumberAxis("Time (min)");
        NumberAxis pvAxis = new NumberAxis("Process Variable / Setpoint");
        NumberAxis opAxis = new NumberAxis("Controller Output (%)");
        // opAxis.setRange(0.0, 100.0); // Fixed range for OP

        // 3. Create Plot and Map Datasets
        XYPlot plot = new XYPlot();
        plot.setDomainAxis(timeAxis);
        
        // Primary Axis (Left)
        plot.setRangeAxis(0, pvAxis);
        plot.setDataset(0, pvSpDataset);
        plot.setRenderer(0, new StandardXYItemRenderer());
        
        // Secondary Axis (Right)
        plot.setRangeAxis(1, opAxis);
        plot.setDataset(1, opDataset);
        plot.setRenderer(1, new StandardXYItemRenderer());
        
        // Map Dataset 1 (OP) to Axis 1 (Right)
        plot.mapDatasetToRangeAxis(1, 1);

        // Optional: Customizing colors
        // plot.getRenderer(1).setSeriesPaint(0, Color.GRAY); 

        JFreeChart chart = new JFreeChart("Disturbance Response", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        return new ChartPanel(chart);
    }

}