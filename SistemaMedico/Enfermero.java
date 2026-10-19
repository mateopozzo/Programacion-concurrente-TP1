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
                // Se bloquea hasta que llegue CUALQUIER paciente (urgente o normal)
                hospital.pacientesPendientes.acquire();

                // Al despertar, verifica primero si la señal fue por una urgencia
                if (hospital.pacienteUrgente.tryAcquire()) {
                    prepararPacienteUrgente();
                } else {
                    // Si no fue urgencia, fue un normal: acquire en vez de continue para no perder nunca al paciente
                    hospital.pacienteEnSdE.acquire();
                    prepararPacienteNormal();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void prepararPacienteNormal() throws InterruptedException {

        // Revisa si hay lugar en la sala de preparacion
        hospital.lugaresDisponiblesSdP.acquire();

        // Ambos mutex en el mismo orden que logEstado (SdE -> SdP): traspaso atómico y sin deadlock
        hospital.mutexSdE.acquire();
        hospital.mutexSdP.acquire();
        hospital.pacientesEnSalaEspera--;
        hospital.pacientesEnSalaPreparacion++;
        hospital.mutexSdP.release();
        hospital.mutexSdE.release();

        // Libera el lugar después de actualizar el contador, así el log nunca muestra más de 10 en espera
        hospital.lugaresDisponiblesSdE.release();

        // Impresión de estado
        hospital.logEstado("PACIENTE NO URGENTE PASA A ENFERMERIA");

        // Simula tiempo de preparación
        sleep(tiempoAtencion());

        // Avisa al paciente recién al terminar la preparación, igual que con las urgencias
        hospital.permisoIngresoSdPNormal.release();

        // Avisa el tipo antes que el aviso general, así el médico siempre lo encuentra
        hospital.pacienteNormalEnSdP.release();

        // Notifica al médico que deja un paciente en la sala de preparación
        hospital.pacienteEnSdP.release();
    }

    private void prepararPacienteUrgente() throws InterruptedException {

        // Revisa si hay lugar en la sala de preparacion
        hospital.lugaresDisponiblesSdP.acquire();

        // Actualiza contadores: la urgencia deja de esperar y entra a preparación
        hospital.mutexSdP.acquire();
        hospital.pacientesUrgentesEsperando--;
        hospital.pacientesEnSalaPreparacion++;
        hospital.mutexSdP.release();

        // Impresión de estado
        hospital.logEstado("URGENCIA PASA A ENFERMERIA");

        // Simula tiempo de preparación
        sleep(tiempoAtencion());

        // Notifica al paciente que terminó su preparación
        hospital.permisoIngresoSdPUrgente.release();

        // Avisa el tipo antes que el aviso general, así el médico siempre lo encuentra
        hospital.pacienteUrgenteEnSdP.release();

        // Notifica al médico que deja un paciente en la sala de preparación
        hospital.pacienteEnSdP.release();
    }
}