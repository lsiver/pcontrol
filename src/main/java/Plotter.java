import java.awt.GridLayout;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultXYDataset;

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

}