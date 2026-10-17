package SistemaMedico;

import java.util.Random;

import static java.lang.Thread.sleep;

public class Enfermero implements Runnable {

    private Hospital hospital = null;

    private static final Random generadorVA = new Random();

    private int tiempoAtencion() {
        return generadorVA.nextInt(100,1500);
    }

    public void run() {
        hospital = Hospital.obtenerInstancia();
        while (true) {
            try {
                //  Se bloquea hasta que llegue CUALQUIER paciente (urgente o normal)
                hospital.pacientesPendientes.acquire();

                //  Al despertar, verifica primero si la señal fue por una urgencia
                if (hospital.pacienteUrgente.tryAcquire()) {
                    prepararPacienteUrgente();
                } else if (hospital.pacienteEnSdE.tryAcquire()) {
                    // No pudo tomar una urgencia, entonces la señal fue de un paciente normal
                    prepararPacienteNormal();
                }else {
                    continue;
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

        // Notifica a recepción que queda un lugar vacío en la sala de espera
        hospital.lugaresDisponiblesSdE.release();

        // Actualiza contador
        hospital.mutexSdE.acquire();
        hospital.pacientesEnSalaPreparacion++;
        hospital.pacientesEnSalaEspera--;
        hospital.mutexSdE.release();

        // Impresión de estado
        hospital.logEstado("======== PACIENTE NO URGENTE PASA A ENFERMERIA ========");

        // Notifica al Paciente que pasa a la sala de Preparación
        hospital.permisoIngresoSdPNormal.release();

        // Simula tiempo de preparación
        sleep(tiempoAtencion());

        // Notifica al Médico que tiene un paciente por atender
        hospital.pacienteEnSdP.release();
    }

    private void prepararPacienteUrgente() throws InterruptedException {

        // Revisa si hay lugar en la sala de preparacion
        hospital.lugaresDisponiblesSdP.acquire();

        // Actualiza contador
        hospital.mutexSdP.acquire();
        hospital.pacientesEnSalaPreparacion++;
        hospital.mutexSdP.release();

        // Impresión de estado
        hospital.logEstado("======== URGENCIA PASA A ENFERMERIA ========");

        // Simula tiempo de preparación
        sleep(tiempoAtencion());

        // Notifica al paciente que terminó su preparación
        hospital.permisoIngresoSdPUrgente.release();

        // Notifica al médico que deja un paciente en la sala de preparación
        hospital.pacienteEnSdP.release();
    }
}