public class PID {
    double Kc;
    double Ti;
    double Td;
    String type;
    double pvHi;
    double pvLo;
    double opHi;
    double opLo;

    public PID(double Kc, double Ti, double Td, String type, double pvHi, double pvLo, double opHi, double opLo) {
        this.Kc = Kc;
        this.Ti = Ti;
        this.Td = Td;
        this.type = type;
        this.pvHi = pvHi;
        this.pvLo = pvLo;
        this.opHi = opHi;
        this.opLo = opLo;
    }


}