package SistemaMedico;
public class Enfermero implements Runnable { // Asumiendo que implementa Runnable o hereda de Thread
    public void run() {
        while (true) {
            try {
                //  Se bloquea hasta que llegue CUALQUIER paciente (urgente o normal)
                Hospital.pacientesPendientes.acquire();
                
                //  Al despertar, verifica primero si la señal fue por una urgencia
                if (Hospital.pacienteUrgente.tryAcquire()) {
                    prepararPacienteUrgente();
                } else {
                    // 3. Si no pudo tomar una urgencia, entonces la señal fue de un paciente normal
                    Hospital.pacienteEnSdE.acquire();

                    // Decrementa de la Sala de Espera de forma segura
                    Hospital.mutexSdE.acquire();
                    Hospital.pacientesEnSalaEspera--;
                    Hospital.mutexSdE.release();

                    prepararPacienteNormal();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break; // Buena práctica para poder detener el hilo
            }
        }
    }

    private void prepararPacienteNormal() throws InterruptedException {
        Hospital.mutexSdP.acquire();
        Hospital.pacientesEnSalaPreparacion++;
        System.out.println("[Enfermero] Preparando paciente normal en SdP.");
        Hospital.mutexSdP.release();

        Hospital.permisoIngresoSdP.release();
        Hospital.pacienteEnSdP.release();
    }

    private void prepararPacienteUrgente() throws InterruptedException {
        Hospital.mutexSdP.acquire();
        Hospital.pacientesEnSalaPreparacion++;
        System.out.println("[Enfermero] ¡ATENCIÓN URGENTE! Preparando paciente en SdP.");
        Hospital.mutexSdP.release();
        Hospital.permisoIngresoSdP.release();
        Hospital.pacienteEnSdP.release();
    }
}