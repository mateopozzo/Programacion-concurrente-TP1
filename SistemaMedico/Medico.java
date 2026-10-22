package SistemaMedico;

import java.util.Random;

public class Medico implements Runnable {

    private static final Random generadorVA = new Random();

    private int tiempoAtencion() {
        return generadorVA.nextInt(100, 3000);
    }

    @Override
    public void run() {
        Hospital hospital = Hospital.obtenerInstancia();
        try {
            while (true) {
                // Espera a que algún paciente haya sido preparado por el enfermero
                hospital.pacienteEnSdP.acquireUninterruptibly();

                // Prioriza los pacientes urgentes
                hospital.mutexSdP.acquireUninterruptibly();
                boolean urgente = hospital.urgenciasPreparadas > 0;
                if (urgente) {
                    hospital.urgenciasPreparadas--;
                }
                hospital.pacientesEnSalaPreparacion--;
                hospital.pacientesEnAtencionMedica++;
                hospital.mutexSdP.release();

                // Libera el lugar después de actualizar el contador
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

                // Loguea antes de liberar al paciente, así el fin de la atención aparece antes de que se retire
                hospital.logEstado("FINALIZA LA ATENCION AL PACIENTE");

                // Avisa al hospital que un paciente terminó de atenderse
                hospital.siendoAtendido.release();
            }
        } catch (InterruptedException e) { // odio el trycatch de sleep
            e.printStackTrace();
        }
    }

}