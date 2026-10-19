package SistemaMedico;

public class Recepcionista implements Runnable {

    private Hospital hospital = null;

    public void run(){
        hospital = Hospital.obtenerInstancia();
        try {
            while(true){
                // Espera el arribo de algún paciente
                hospital.pacienteEnRecepcion.acquire();

                // Si SdE está llena, espera a que el Enfermero libere algún lugar
                hospital.lugaresDisponiblesSdE.acquire();

                // Actualiza contadores
                hospital.mutexSdE.acquire();
                hospital.pacientesEnColaRecepcion--;
                hospital.pacientesEnSalaEspera++;
                hospital.mutexSdE.release();

                // Notifica al Paciente que ingresó a la SdE
                hospital.permisoIngresoSdE.release();

                // Impresión de estado
                hospital.logEstado("======== INGRESO A SDE ========");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

