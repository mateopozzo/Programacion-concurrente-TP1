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

                // Elige a quién pasar: si hay urgencias esperando van primero
                if (elegirSiguientePaciente()) {
                    prepararPacienteUrgente();
                } else {
                    prepararPacienteNormal();
                }
            } catch (InterruptedException e) { // Solo puede venir del sleep
                e.printStackTrace();
                break;
            }
        }
    }

    // Elige al siguiente paciente con los contadores: primero las urgencias
    private boolean elegirSiguientePaciente() {

        // Espera lugar en la sala de preparación antes de elegir, así la prioridad se decide con lo que hay en ese momento
        hospital.lugaresDisponiblesSdP.acquireUninterruptibly();

        // Ambos mutex en el mismo orden que logEstado (SdE -> SdP): elección y traspaso atómicos, sin deadlock
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

        // Libera el lugar después de actualizar el contador, así el log nunca muestra más de 10 en espera
        hospital.lugaresDisponiblesSdE.release();

        // Impresión de estado
        hospital.logEstado("PACIENTE NO URGENTE PASA A ENFERMERIA");

        // Simula tiempo de preparación
        Thread.sleep(tiempoAtencion());

        // Avisa al paciente recién al terminar la preparación, igual que con las urgencias
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

        // Registra la urgencia lista antes de avisar al médico, así él la ve y le da prioridad
        hospital.mutexSdP.acquireUninterruptibly();
        hospital.urgenciasPreparadas++;
        hospital.mutexSdP.release();

        // Notifica al médico que deja un paciente en la sala de preparación
        hospital.pacienteEnSdP.release();
    }
}