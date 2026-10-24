package SistemaMedico;

public class Recepcionista implements Runnable {

    private Hospital hospital = null;

    public void run() {
        hospital = Hospital.obtenerInstancia();
        while (true) {
            // Espera el arribo de algún paciente
            hospital.pacienteEnRecepcion.acquireUninterruptibly();

            // checkeo Bandera de finalización
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

            // Impresión de estado
            hospital.logEstado("INGRESO A SDE");
        }
    }
}