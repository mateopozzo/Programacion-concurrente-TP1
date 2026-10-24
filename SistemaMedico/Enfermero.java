package SistemaMedico;

import java.util.Random;

public class Enfermero implements Runnable {

    private Hospital hospital = null;

    private static final Random generadorVA = new Random();

    private int tiempoAtencion() {
        return 100 + generadorVA.nextInt(1400);
    }

    public void run() {
        hospital = Hospital.obtenerInstancia();
        while (true) {
            try {
                // Se bloquea hasta que llegue CUALQUIER paciente (urgente o normal)
                hospital.pacientesPendientes.acquireUninterruptibly();

                // Si terminó la simulación, el hilo principal lo despertó para que salga
                if (hospital.finSimulacion) break;

                // Escoge que paciente prepara
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

        // Reserva lugar de la sala de preparacion
        hospital.lugaresDisponiblesSdP.acquireUninterruptibly();

        // Mutex en orden
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

        // Libera lugar en SdE
        hospital.lugaresDisponiblesSdE.release();

        // Impresión de estado
        hospital.logEstado("PACIENTE NO URGENTE PASA A ENFERMERIA");

        // Simula tiempo de preparación
        Thread.sleep(tiempoAtencion());

        // Avisa al paciente al terminar preparacion
        hospital.permisoIngresoSdPNormal.release();

        // Notifica al médico que deja un paciente en la sala de preparación
        hospital.pacienteEnSdP.release();
    }

    private void prepararPacienteUrgente() throws InterruptedException {

        // Impresión de estado
        hospital.logEstado("URGENCIA PASA A ENFERMERIA");

        // Simula tiempo de preparación
        Thread.sleep(tiempoAtencion());

        // Notifica al paciente que terminó su preparación
        hospital.permisoIngresoSdPUrgente.release();

        // Registra la urgencia
        hospital.mutexSdP.acquireUninterruptibly();
        hospital.urgenciasPreparadas++;
        hospital.mutexSdP.release();

        // Notifica al médico que deja un paciente en la sala de preparación
        hospital.pacienteEnSdP.release();
    }
}