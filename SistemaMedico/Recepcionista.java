package SistemaMedico;

public class Recepcionista implements Runnable {

    private Hospital hospital = null;

    public void run(){
        hospital = Hospital.obtenerInstancia();
        try {
            while(true){
                // Espera el arribo de algún paciente
                hospital.pacienteEnRecepcion.acquire();

                // Actualiza contador
                hospital.mutexSdE.acquire();
                hospital.pacientesEnColaRecepcion--;
                hospital.mutexSdE.release();

                // Si SdE está llena, espera a que el Enfermero libere algún lugar
                hospital.lugaresDisponiblesSdE.acquire();

                // Notifica al Paciente que ingresó a la SdE
                hospital.permisoIngresoSdE.release();

                // Actualiza contadores
                hospital.mutexSdE.acquire();
                hospital.pacientesEnSalaEspera++;
                hospital.mutexSdE.release();

                // Impresión de estado
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
