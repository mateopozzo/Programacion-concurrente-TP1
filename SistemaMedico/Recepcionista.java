package SistemaMedico;

public class Recepcionista implements Runnable {

    private Hospital hospital = null;

    public void run() {
        // Sin try/catch: acquireUninterruptibly no lanza InterruptedException
        hospital = Hospital.obtenerInstancia();
        while (true) {
            // Espera el arribo de algún paciente
            hospital.pacienteEnRecepcion.acquireUninterruptibly();

            // Si terminó la simulación, el hilo principal la despertó para que salga
            if (hospital.finSimulacion) break;

            // Si SdE está llena, espera a que el Enfermero libere algún lugar
            hospital.lugaresDisponiblesSdE.acquireUninterruptibly();

            // Actualiza contadores
            hospital.mutexSdE.acquireUninterruptibly();
            hospital.pacientesEnColaRecepcion--;
            hospital.pacientesEnSalaEspera++;
            hospital.mutexSdE.release();

            // Notifica al Paciente que ingresó a la SdE
            hospital.permisoIngresoSdE.release();

            // Impresión de estado (logEstado ya agrega los separadores)
            hospital.logEstado("INGRESO A SDE");
        }
    }
}