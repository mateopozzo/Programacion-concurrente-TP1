import SistemaMedico.Hospital;

public class Main {
    public static void main(String args[]) throws InterruptedException {
        Hospital hospital = Hospital.obtenerInstancia();
        hospital.iniciar();
    }
}

