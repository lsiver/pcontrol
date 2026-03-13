public class PID {
    double Kc;
    double Ti;
    double Td;
    String type;

    public PID(double Kc, double Ti, double Td, String type) {
        this.Kc = Kc;
        this.Ti = Ti;
        this.Td = Td;
        this.type = type;
    }


}