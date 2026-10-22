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

                // Elige con el contador en vez de tryAcquire: si hay una urgencia preparada pasa primero
                hospital.mutexSdP.acquireUninterruptibly();
                boolean urgente = hospital.urgenciasPreparadas > 0;
                if (urgente) {
                    hospital.urgenciasPreparadas--;
                }
                hospital.pacientesEnSalaPreparacion--;
                hospital.pacientesEnAtencionMedica++;
                hospital.mutexSdP.release();

                // Libera el lugar después de actualizar el contador, así el log nunca muestra más de 5 en preparación
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

                // Recién acá libera al paciente: se queda en el consultorio durante toda la consulta
                hospital.siendoAtendido.release();
            }
        } catch (InterruptedException e) { // Solo puede venir del sleep
            e.printStackTrace();
        }
    }

}