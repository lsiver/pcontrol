public class TuningRun {
    public String runName;

    public double horizon, kp, tau, deadtime;

    public double kc, ti, td, pvHi, pvLo, opHi, opLo;

    public String eqType;

    public double dSP, dOP;

    public TuningRun(String name, double horizon, double kp, double tau, double deadtime, double kc, double ti, double td, double pvHi,
        double pvLo, double opHi, double opLo, String eqType, double dSP, double dOP) {
            this.runName = name;
            this.horizon = horizon;
            this.kp = kp;
            this.tau = tau;
            this.deadtime = deadtime;
            this.kc = kc;
            this.ti = ti;
            this.td = td;
            this.pvHi = pvHi;
            this.pvLo = pvLo;
            this.opHi = opHi;
            this.opLo = opLo;
            this.eqType = eqType;
            this.dSP = dSP;
            this.dOP = dOP;


        }

        @Override
        public String toString() {
            //create a csv
        return String.format("%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f",
            runName, horizon, kp, tau, deadtime, kc, ti, td, eqType, pvHi, pvLo, opHi, opLo, dSP, dOP);
    }
}
