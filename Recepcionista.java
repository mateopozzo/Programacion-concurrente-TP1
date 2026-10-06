package concurrente.concurrente;

public class Recepcionista implements Runnable {
    public void run(){
        while(true){
            Hospital.pacienteEnRecepcion.acquire();     
            Hospital.mutexSdE.acquire();
            if(pacientesEnSalaEspera == MaxPersonasSdE){
            Hospital.mutexSdE.release();
             Hospital.pacienteEnSdE.acquire();// Queda esperando a que alla un lugar en Sala de Espera 
            } else Hospital.mutexSdE.release();
            Hospital.permisoIngresoSdE.release();
            Hospital.mutexSdE.acquire();
             Hospital.pacientesEnSalaEspera++;
            Hospital.mutexSdE.release();
            
        }
    }
    
}
