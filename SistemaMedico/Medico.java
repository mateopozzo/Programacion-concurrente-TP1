package SistemaMedico;

import java.util.Random;

public class Medico implements Runnable {

    private static final Random generadorVA = new Random();

    private int tiempoAtencion() {
        return 100 + generadorVA.nextInt(2900);
    }

    @Override
    public void run() {
        Hospital hospital = Hospital.obtenerInstancia();
        try {
            while (true) {
                // Espera que el enfermero lo desbloquee
                hospital.pacienteEnSdP.acquireUninterruptibly();

                // checkeo Bandera de finalización
                if (hospital.finSimulacion) break;

                // Elige tipo de paciente con el contador
                hospital.mutexSdP.acquireUninterruptibly();
                boolean urgente = hospital.urgenciasPreparadas > 0;
                if (urgente) {
                    hospital.urgenciasPreparadas--;
                }
                hospital.pacientesEnSalaPreparacion--;
                hospital.pacientesEnAtencionMedica++;
                hospital.mutexSdP.release();

                // Libera el lugar
                hospital.lugaresDisponiblesSdP.release();

                // Llama al paciente del tipo elegido
                if (urgente) {
                    hospital.permisoAtencionUrgente.release();
                } else {
                    hospital.permisoAtencionNormal.release();
                }

                // Impresión de estado
                hospital.logEstado("COMIENZA LA ATENCION AL PACIENTE " + (urgente ? "URGENTE" : "NO URGENTE"));

                // Simula el tiempo de atención
                Thread.sleep(tiempoAtencion());

                // Actualiza contadores
                hospital.mutexSdP.acquireUninterruptibly();
                hospital.pacientesEnAtencionMedica--;
                hospital.mutexSdP.release();

                // Impresion de log antes de desbloquear otro hilo
                hospital.logEstado("FINALIZA LA ATENCION AL PACIENTE");

                // Libera al paciente
                hospital.siendoAtendido.release();
            }
        } catch (InterruptedException e) { // Solo puede venir del sleep
            e.printStackTrace();
        }
    }

}