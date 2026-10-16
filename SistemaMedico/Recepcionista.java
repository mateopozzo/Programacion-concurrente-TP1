package SistemaMedico;

public class Recepcionista implements Runnable {

    private Hospital hospital = null;

    public void run(){
        hospital = Hospital.obtenerInstancia();
        try {
            while(true){
                hospital.pacienteEnRecepcion.acquire();
                hospital.mutexSdE.acquire();
                hospital.pacientesEnColaRecepcion--;
                if(hospital.pacientesEnSalaEspera == hospital.MAX_SALA_ESPERA){
                    hospital.mutexSdE.release();
                    hospital.pacienteEnSdE.acquire();
                    // Queda esperando a que haya un lugar en Sala de Espera
                } else {
                    hospital.mutexSdE.release();
                }
                hospital.mutexSdE.acquire();
                hospital.permisoIngresoSdE.release();
                hospital.pacientesEnSalaEspera++;
                hospital.mutexSdE.release();

                hospital.permisoImpresion.acquire(1);
                System.out.println("======== INGRESO A SDE ========");
                hospital.logEstado();
                hospital.permisoImpresion.release(1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
