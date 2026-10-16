package SistemaMedico;
public class Enfermero implements Runnable { 

    private Hospital hospital = null;

    public void run() {
        hospital = Hospital.obtenerInstancia();
        while (true) {
            try {
                //  Se bloquea hasta que llegue CUALQUIER paciente (urgente o normal)
                hospital.pacientesPendientes.acquire();
                
                //  Al despertar, verifica primero si la señal fue por una urgencia
                if (hospital.pacienteUrgente.tryAcquire()) {
                    prepararPacienteUrgente();
                } else {
                    // si no pudo tomar una urgencia, entonces la señal fue de un paciente normal
                    hospital.pacienteEnSdE.acquire();

                    // Decrementa de la Sala de Espera de forma segura
                    hospital.mutexSdE.acquire();
                    hospital.pacientesEnSalaEspera--;
                    hospital.mutexSdE.release();

                    prepararPacienteNormal();

                    System.out.println("======== PACIENTE NO URGENTE PASA A ENFERMERIA ========");
                    hospital.logEstado();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break; // Buena práctica para poder detener el hilo
            }
        }
    }

    private void prepararPacienteNormal() throws InterruptedException {
        hospital.mutexSdP.acquire();
        hospital.pacientesEnSalaPreparacion++;
        System.out.println("======== PACIENTE NO URGENTE PASA A ENFERMERIA ========");
        hospital.logEstado();
        hospital.mutexSdP.release();

        hospital.permisoIngresoSdP.release();
        hospital.pacienteEnSdP.release();
    }

    private void prepararPacienteUrgente() throws InterruptedException {
        hospital.mutexSdP.acquire();
        hospital.pacientesEnSalaPreparacion++;
        System.out.println("======== URGENCIA PASA A ENFERMERIA ========");
        hospital.logEstado();
        hospital.mutexSdP.release();
        hospital.permisoIngresoSdP.release();
        hospital.pacienteEnSdP.release();
    }
}