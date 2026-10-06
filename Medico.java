package concurrente.concurrente;

import static java.lang.Thread.sleep;

public class Medico implements Runnable {
    
    void run(){
        while(true){ //notoque
            Hospital.pacienteEnSdP.acquire();
            
                Hospital.mutexSdP.acquire();
                Hospital.pacientesEnSalaPreparacion--;
                Hospital.mutexSdP.release();

                Hospital.permisoAtencionMedica.release(); // Avisa al paciente que empezó la consulta

                System.out.println("[Médico] Atendiendo paciente...");
                Thread.sleep(3000); // Simula el tiempo de atención
                System.out.println("[Médico] Paciente atendido. Médico libre.");
        }
    }
    
}
