
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

    //Probably no longer needed. Old Positional Form. Here for reference but probably will be removed in next major iteration.
    public void runSPChangeSim2(double dSP) {
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
        double pvRange = Math.abs(pid.pvHi - pid.pvLo);
        double opRange = Math.abs(pid.opHi - pid.opLo);
        double outputFrac = 0;


        // Buffer for Deadtime
        Queue<Double> deadtimeBuffer = new LinkedList<>();
        int delaySteps = (int) (process.deadtime / process.dt);

        int i = 0;
        for (double t = 0; t<= process.horizon; t+=process.dt) {
            // Apply the setpoint step immediately
            //  the process buffer models deadtime.
            SP = dSP;

            double error = (SP - currentPV) / pvRange ;
            double dPV = ((currentPV - lastPV) / process.dt) / pvRange ;
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
                pTerm = -pid.Kc * currentPV / pvRange; // P on PV
                dTerm = -pid.Kc * pid.Td * dPV; // D on PV
            }

            outputFrac = pTerm +iTerm + dTerm;
            currentOP = outputFrac * opRange;
            // currentOP = (pTerm + iTerm + dTerm);
            
            // Clamp OP (0-100%)
            currentOP = Math.max(pid.opLo, Math.min(pid.opHi, currentOP));

            //Was after FOPDT but this would cause error = 0 at all times;
            lastPV = currentPV;
            lastError = error;

            // 2. Handle Deadtime
            deadtimeBuffer.add(currentOP);
            double delayedOP = (deadtimeBuffer.size() > delaySteps) ? deadtimeBuffer.poll() : 0;

            // 3. Process Model (FOPDT)
            // PV_dot = (1/tau) * (Kp * OP_delayed - PV)
            double pvChange = (process.dt / process.tau) * (process.Kp * delayedOP - currentPV);
            currentPV += pvChange;

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

    public void runSPChangeSim(double dSP) {
        int length = (int) (process.horizon/process.dt);
        double[][] PVdata = new double[2][length+1];
        double[][] SPdata = new double[2][length+1];
        double[][] OPdata = new double[2][length+1];

        double currentPV = 0;
        double currentOP = 0;
        double lastPV = 0;
        double lastError = 0;
        double SP = 0;

        double pvRange = Math.abs(pid.pvHi - pid.pvLo);
        double opRange = Math.abs(pid.opHi - pid.opLo);


        // Buffer for Deadtime
        Queue<Double> deadtimeBuffer = new LinkedList<>();
        int delaySteps = (int) (process.deadtime / process.dt);

        for (int i = 0; i<= length; i++) {
            double t = i * process.dt;
            // Apply the setpoint step immediately
            //  the process buffer models deadtime.
            SP = dSP;

        // 1. Calculate Error and Changes
            double error = (SP - currentPV) / pvRange;
            double deltaPV = (currentPV - lastPV) / pvRange;
            double deltaError = error - lastError;

            double dP = 0;
            double dI = 0;
            double dD = 0;

            // 2. Select Term Logic based on Equation Type
            // Note: Constants are applied to the CHANGES in the signals
            
            // Integral is always on Error for A, B, and C
            dI = (pid.Kc / pid.Ti) * error * process.dt;

            if (pid.type.equals("A")) {
                dP = pid.Kc * deltaError;
                dD = pid.Kc * pid.Td * (deltaError / process.dt);
            } else if (pid.type.equals("B")) {
                dP = pid.Kc * deltaError; // P on Error
                dD = -pid.Kc * pid.Td * (deltaPV / process.dt); // D on PV
            } else { // Type C
                dP = -pid.Kc * deltaPV; // P on PV
                dD = -pid.Kc * pid.Td * (deltaPV / process.dt); // D on PV
            }

            // 3. Update the Output (The Incremental Step)
            double deltaOP_Frac = dP + dI + dD;
            currentOP += (deltaOP_Frac * opRange);

            // 4. Constraints (Clamp to Engineering Units)
            currentOP = Math.max(pid.opLo, Math.min(pid.opHi, currentOP));

            // 5. Update state for next iteration
            lastPV = currentPV;
            lastError = error;

            // 2. Handle Deadtime
            deadtimeBuffer.add(currentOP);
            double delayedOP = (deadtimeBuffer.size() > delaySteps) ? deadtimeBuffer.poll() : 0;

            // 3. Process Model (FOPDT)
            // PV_dot = (1/tau) * (Kp * OP_delayed - PV)
            double pvChange = (process.dt / process.tau) * (process.Kp * delayedOP - currentPV);
            currentPV += pvChange;

            PVdata[0][i] = (double) t;
            PVdata[1][i] = currentPV;

            SPdata[0][i] = (double) t;
            SPdata[1][i] = SP;

            OPdata[0][i] = (double) t;
            OPdata[1][i] = currentOP;

        }

        this.PVdata = PVdata;
        this.SPdata = SPdata;
        this.OPdata = OPdata;

        this.plotOption = 1;
        
    }

    public void runDisturbanceSim(double loadStep) {
        int length = (int) (process.horizon / process.dt);
        double[][] PVdata = new double[2][length + 1];
        double[][] SPdata = new double[2][length + 1];
        double[][] OPdata = new double[2][length + 1];

        // Initial Conditions
        double currentPV = 0;
        double lastPV = 0;
        double lastError = 0;
        double currentOP = loadStep; // The controller starts at 0 balance
        double SP = 0; 

        double pvRange = Math.abs(pid.pvHi - pid.pvLo);
        double opRange = Math.abs(pid.opHi - pid.opLo);

        Queue<Double> deadtimeBuffer = new LinkedList<>();
        int delaySteps = (int) (process.deadtime / process.dt);

        for (int i = 0; i <= length; i++) {
            double t = i * process.dt;

            // --- 1. PROCESS UPDATE (The "Plant" runs first) ---
            // We push the OP into the buffer
            deadtimeBuffer.add(currentOP);
            
            // We pull the delayed OP. If buffer isn't full, we assume the disturbance 'loadStep'
            // is what the process sees initially at t=0.
            double delayedOP = (deadtimeBuffer.size() > delaySteps) ? deadtimeBuffer.poll() : loadStep;

            double pvChange = (process.dt / process.tau) * (process.Kp * delayedOP - currentPV);
            currentPV += pvChange;

            // --- 2. CALCULATE DELTAS ---
            // Now currentPV has moved relative to lastPV, so deltaPV is non-zero
            double error = (SP - currentPV) / pvRange;
            double deltaPV = (currentPV - lastPV) / pvRange;
            double deltaError = error - lastError;

            // --- 3. PID CALCULATION ---
            double dP = 0;
            double dI = (pid.Kc / pid.Ti) * error * process.dt;
            double dD = 0;

            if (pid.type.equals("A")) {
                dP = pid.Kc * deltaError;
                dD = pid.Kc * pid.Td * (deltaError / process.dt);
            } else if (pid.type.equals("B")) {
                dP = pid.Kc * deltaError;
                dD = -pid.Kc * pid.Td * (deltaPV / process.dt);
            } else { // Type C
                dP = -pid.Kc * deltaPV;
                dD = -pid.Kc * pid.Td * (deltaPV / process.dt);
            }

            // --- 4. OUTPUT UPDATE ---
            currentOP += (dP + dI + dD) * opRange;
            currentOP = Math.max(pid.opLo, Math.min(pid.opHi, currentOP));

            // --- 5. DATA STORAGE ---
            PVdata[0][i] = t;
            PVdata[1][i] = currentPV;
            SPdata[0][i] = t;
            SPdata[1][i] = SP;
            OPdata[0][i] = t;
            OPdata[1][i] = currentOP;

            // Sync for next iteration
            lastPV = currentPV;
            lastError = error;
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
            // Apply the open-loop step immediately; the buffer below models deadtime.
            double manualOP = 1.0;

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

    public double[][] getPVData() {
        return this.PVdata;
    }

    public double[][] getSPData() {
        return this.SPdata;
    }

    public double[][] getOPData() {
        return this.OPdata;
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
