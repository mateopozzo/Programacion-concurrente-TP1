package SistemaMedico;

public class Paciente implements Runnable {

    private final int id;            // Identifica al paciente en el log
    private final boolean esUrgente; // Define qué tipo de paciente es
    private Hospital hospital = null;

    public Paciente(int id, boolean esUrgente) {
        this.id = id;
        this.esUrgente = esUrgente;
    }

    @Override
    public void run() {
        hospital = Hospital.obtenerInstancia();
        try {
            if (esUrgente) {
                // Cuenta la urgencia mientras espera al enfermero
                hospital.mutexSdP.acquire();
                hospital.pacientesUrgentesEsperando++;
                hospital.mutexSdP.release();

                // Impresión de estado
                hospital.logEstado("ARRIBO DE URGENCIA (paciente " + id + ")");

                // Notifica que hay un paciente en espera
                hospital.pacienteUrgente.release();     // Aviso específico
                hospital.pacientesPendientes.release(); // Despierta al enfermero

                // Espera a que el enfermero termine de prepararlo
                hospital.permisoIngresoSdPUrgente.acquire();

                // Espera a que el médico lo llame (las urgencias pasan antes que los normales)
                hospital.permisoAtencionUrgente.acquire();
            } else {

                // Contador de pacientes esperando en recepción
                hospital.mutexSdE.acquire();
                hospital.pacientesEnColaRecepcion++;
                hospital.mutexSdE.release();

                // Impresión de estado
                hospital.logEstado("ARRIBO TURNO COMUN (paciente " + id + ")");

                // Notifica a Recepción que está esperando
                hospital.pacienteEnRecepcion.release();

                // Espera que la recepcionista le dé entrada a la SdE
                hospital.permisoIngresoSdE.acquire();

                // Ya en la Sala de Espera, notifica al enfermero
                hospital.pacienteEnSdE.release();       // Aviso específico
                hospital.pacientesPendientes.release(); // Despierta al enfermero

                // Espera a que el enfermero termine de prepararlo
                hospital.permisoIngresoSdPNormal.acquire();

                // Espera a que el médico lo llame (después de las urgencias)
                hospital.permisoAtencionNormal.acquire();
            }

            // Espera a que termine de ser atendido
            hospital.siendoAtendido.acquire();

            // Registra la salida para poder seguir a cada paciente de punta a punta
            hospital.logEstado("PACIENTE " + id + " SE RETIRA");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}