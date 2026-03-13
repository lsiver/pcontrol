
import java.util.LinkedList;
import java.util.Queue;



public class Simulation {
    Process process;
    PID pid;
    double[][] PVdata;
    double[][] SPdata;
    double[][] OPdata;

    int plotOption;

    public Simulation(PID pid) {
        this.pid = pid;
    }

    public Simulation(PID pid, Process process) {
        this.process = process;
        this.pid = pid;
    }

    public void runSPChangeSim(double dSP) {
        int length = (int) (process.horizon/process.dt);
        double[][] PVdata = new double[2][length+1];
        double[][] SPdata = new double[2][length+1];
        double[][] OPdata = new double[2][length+1];

        double currentPV = 0;
        double currentOP = 0;
        double integralSum = 0;
        double lastPV = 0;
        double lastError = 0;
        double SP = 0;

        // Buffer for Deadtime
        Queue<Double> deadtimeBuffer = new LinkedList<>();
        int delaySteps = (int) (process.deadtime / process.dt);

        int i = 0;
        for (double t = 0; t<= process.horizon; t+=process.dt) {
            SP = (t > 5) ? dSP : 0;

            double error = SP - currentPV;
            double dPV = (currentPV - lastPV) / process.dt;
            double dE = (error - lastError) / process.dt;

            double pTerm, iTerm, dTerm;

            integralSum += error * process.dt;
            iTerm = (pid.Kc / pid.Ti) * integralSum;

            if (pid.type.equals("A")) {
                pTerm = pid.Kc * error;
                dTerm = pid.Kc * pid.Td * dE;
            } else if (pid.type.equals("B")) {
                pTerm = pid.Kc * error;
                dTerm = -pid.Kc * pid.Td * dPV; // D on PV
            } else { // Type C
                pTerm = -pid.Kc * currentPV; // P on PV
                dTerm = -pid.Kc * pid.Td * dPV; // D on PV
            }

            currentOP = pTerm + iTerm + dTerm;
            
            // Clamp OP (0-100%)
            currentOP = Math.max(0, Math.min(100, currentOP));

            // 2. Handle Deadtime
            deadtimeBuffer.add(currentOP);
            double delayedOP = (deadtimeBuffer.size() > delaySteps) ? deadtimeBuffer.poll() : 0;

            // 3. Process Model (FOPDT)
            // PV_dot = (1/tau) * (Kp * OP_delayed - PV)
            double pvChange = (process.dt / process.tau) * (process.Kp * delayedOP - currentPV);
            currentPV += pvChange;

            // Update history
            lastPV = currentPV;
            lastError = error;

            PVdata[0][i] = (double) t;
            PVdata[1][i] = currentPV;

            SPdata[0][i] = (double) t;
            SPdata[1][i] = SP;

            OPdata[0][i] = (double) t;
            OPdata[1][i] = currentOP;

            i+=1;

        }

        this.PVdata = PVdata;
        this.SPdata = SPdata;
        this.OPdata = OPdata;

        this.plotOption = 1;
        
    }

    public void runOpenLoopSim() {
        int length = (int) (process.horizon/process.dt);
        double[][] PVdata = new double[2][length+1];

        // Buffer for Deadtime
        Queue<Double> openLoopBuffer = new LinkedList<>();
        int delaySteps = (int) (process.deadtime / process.dt);

        int i = 0;
        double openLoopPV = 0;
        for (double t = 0; t<= process.horizon; t+=process.dt) {
            double manualOP = (t > 5) ? 1.0 : 0;

            openLoopBuffer.add(manualOP);
            double delayedManualOP = (openLoopBuffer.size() > delaySteps) ? openLoopBuffer.poll() : 0;

            double dOpenLoopPV = (this.process.dt / this.process.tau) * (this.process.Kp * delayedManualOP - openLoopPV);

            openLoopPV += dOpenLoopPV;

            PVdata[0][i] = (double) t;
            PVdata[1][i] = openLoopPV;

            i+=1;

        }

        this.PVdata = PVdata;

        this.plotOption = 2;

    }

    public void plotter() {
        Plotter plotter = new Plotter();

        if (this.plotOption == 1) {
            plotter.plotPVSP(this.PVdata, this.SPdata, this.OPdata);
        } else if (this.plotOption == 2) {
            plotter.plotOpenLoop(this.PVdata);
        }
    }

}