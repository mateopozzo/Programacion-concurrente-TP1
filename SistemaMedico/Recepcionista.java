package SistemaMedico;

public class Recepcionista implements Runnable {
    public void run(){
        try {
            while(true){
                Hospital.pacienteEnRecepcion.acquire();
                Hospital.mutexSdE.acquire();
                if(Hospital.pacientesEnSalaEspera == Hospital.MAX_SALA_ESPERA){
                    Hospital.mutexSdE.release();
                    Hospital.pacienteEnSdE.acquire(); s
                    // Queda esperando a que alla un lugar en Sala de Espera
                } else {
                    Hospital.mutexSdE.release();
                }
                Hospital.permisoIngresoSdE.release();
                Hospital.mutexSdE.acquire();
                Hospital.pacientesEnSalaEspera++;
                Hospital.mutexSdE.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
