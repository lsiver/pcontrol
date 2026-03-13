
public class Main {
    public static void main(String[] args) {
        PID pid = new PID(0.7, 10.0, 0.0, "B");
        Process process = new Process(0.1, 300.0, 1.5, 30.0, 5.0);

        Simulation simulation = new Simulation(pid, process);
        simulation.runSPChangeSim(2);
        simulation.runOpenLoopSim();
        simulation.plotter();
        
    }
}