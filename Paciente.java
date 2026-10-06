package concurrente.concurrente;

public class Paciente implements Runnable {
    
    private boolean esUrgente; // Define qué tipo de paciente es

    public Paciente(boolean esUrgente) {
        this.esUrgente = esUrgente;
    }

    @Override
    public void run() {
        
            if (esUrgente) {
                System.out.println("[Paciente Urgente] Llegó directamente a guardia.");
                Hospital.pacienteUrgente.release();     // Aviso específico
                Hospital.pacientesPendientes.release(); // Despierta al enfermero
            } else {
                System.out.println("[Paciente Normal] Llegó a la puerta del hospital.");
                Hospital.pacienteEnRecepcion.release(); // Avisa a la recepcionista

                // Espera que la recepcionista le dé entrada a la SdE
                Hospital.permisoIngresoSdE.acquire();
                System.out.println("[Paciente Normal] Ingresó a la Sala de Espera.");
                
                // Ya en la Sala de Espera, notifica al enfermero
                Hospital.pacienteEnSdE.release();       // Aviso específico
                Hospital.pacientesPendientes.release(); // Despierta al enfermero
            }

            // Espera que el enfermero lo llame a la Sala de Preparación
            Hospital.permisoIngresoSdP.acquire();

            // Espera a que el médico lo empiece a atender
            Hospital.permisoAtencionMedica.acquire();
            System.out.println("[Paciente " + (esUrgente ? "Urgente" : "Normal") + "] Siendo atendido por el médico..."); 

    }
}