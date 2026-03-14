public class Process {
    double dt;
    double horizon;
    double Kp;
    double tau;
    double deadtime;

    public Process(double dt, double horizon, double Kp, double tau, double deadtime) {
        this.dt = dt;
        this.horizon = horizon;
        this.Kp = Kp;
        this.tau = tau;
        this.deadtime = deadtime;
    }
}
