package SistemaMedico;

import java.util.Random;

import static java.lang.Thread.sleep;

public class Enfermero implements Runnable {

    private Hospital hospital = null;

    private static final Random generadorVA = new Random();

    private int tiempoAtencion() {
        return generadorVA.nextInt(100, 1500);
    }

    public void run() {
        hospital = Hospital.obtenerInstancia();
        while (true) {
            try {
                // Se bloquea hasta que llegue CUALQUIER paciente
                hospital.pacientesPendientes.acquireUninterruptibly();

                // Prioriza a las urgencias
                if (elegirSiguientePaciente()) {
                    prepararPacienteUrgente();
                } else {
                    prepararPacienteNormal();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private boolean elegirSiguientePaciente() {

        // Espera lugar en la sala de preparación antes de elegir
        hospital.lugaresDisponiblesSdP.acquireUninterruptibly();

        // De nuevo: Se priorizan los urgentes.
        // Ademas se toma mutex de ambas salas para que ningun otro hilo pueda cambiar el estado
        hospital.mutexSdE.acquireUninterruptibly();
        hospital.mutexSdP.acquireUninterruptibly();
        boolean urgente = hospital.pacientesUrgentesEsperando > 0;
        if (urgente) {
            hospital.pacientesUrgentesEsperando--;
        } else {
            hospital.pacientesEnSalaEspera--;
        }
        hospital.pacientesEnSalaPreparacion++;
        hospital.mutexSdP.release();
        hospital.mutexSdE.release();

        return urgente;
    }

    private void prepararPacienteNormal() throws InterruptedException {

        // Libera el lugar
        hospital.lugaresDisponiblesSdE.release();

        // Impresión de estado
        hospital.logEstado("PACIENTE NO URGENTE PASA A ENFERMERIA");

        // Simula tiempo de preparación
        sleep(tiempoAtencion());

        // Avisa al paciente recién al terminar la preparación
        hospital.permisoIngresoSdPNormal.release();

        // Notifica al médico que deja un paciente en la sala de preparación
        hospital.pacienteEnSdP.release();
    }

    private void prepararPacienteUrgente() throws InterruptedException {

        // Impresión de estado
        hospital.logEstado("URGENCIA PASA A ENFERMERIA");

        // Simula tiempo de preparación
        sleep(tiempoAtencion());

        // Notifica al paciente que terminó su preparación
        hospital.permisoIngresoSdPUrgente.release();

        // Registra la urgencia lista antes de avisar al médico
        hospital.mutexSdP.acquireUninterruptibly();
        hospital.urgenciasPreparadas++;
        hospital.mutexSdP.release();

        // Notifica al médico que deja un paciente en la sala de preparación
        hospital.pacienteEnSdP.release();
    }
}