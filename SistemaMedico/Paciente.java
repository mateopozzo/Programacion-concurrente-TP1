package SistemaMedico;

public class Paciente implements Runnable {
    
    private boolean esUrgente; // Define qué tipo de paciente es
    private Hospital hospital = null;

    public Paciente(boolean esUrgente) {
        this.esUrgente = esUrgente;
    }

    @Override
    public void run() {
        hospital = Hospital.obtenerInstancia();
        try {
            if (esUrgente) {
                // Impresión de estado
                hospital.logEstado("========== ARRIBO DE URGENCIA ========");

                // Notifica que hay un paciente en espera
                hospital.pacienteUrgente.release(); // Aviso específico
                hospital.pacientesPendientes.release(); // Despierta al enfermero

                // Espera que el enfermero lo llame a la Sala de Preparación
                hospital.permisoIngresoSdPUrgente.acquire();
            } else {

                // Contador de pacientes esperando en recepción
                hospital.mutexSdE.acquire();
                hospital.pacientesEnColaRecepcion++;
                hospital.mutexSdE.release();

                // Impresión de estado
                hospital.logEstado("========== ARRIBO TURNO COMUN ========");

                // Notifica a Recepción que está esperando
                hospital.pacienteEnRecepcion.release();

                // Espera que la recepcionista le dé entrada a la SdE
                hospital.permisoIngresoSdE.acquire();

                // Ya en la Sala de Espera, notifica al enfermero
                hospital.pacienteEnSdE.release();       // Aviso específico
                hospital.pacientesPendientes.release(); // Despierta al enfermero

                // Espera que el enfermero lo llame a la Sala de Preparación
                hospital.permisoIngresoSdPNormal.acquire();
            }
            


            // Espera a que el médico lo empiece a atender
            hospital.permisoAtencionMedica.acquire();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}