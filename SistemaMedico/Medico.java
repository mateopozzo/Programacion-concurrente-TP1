package SistemaMedico;

import java.util.Random;

import static java.lang.Thread.sleep;

public class Medico implements Runnable {

    private static final Random generadorVA = new Random();

    private int tiempoAtencion() {
        return generadorVA.nextInt(100,3000);
    }

    @Override
    public void run(){
        Hospital hospital = Hospital.obtenerInstancia();
        try {
            while(true){
                // Espera a que algún paciente haya sido preparado por el enfermero
                hospital.pacienteEnSdP.acquire();

                // Actualización de contadores
                hospital.mutexSdP.acquire();
                hospital.pacientesEnSalaPreparacion--;
                hospital.pacientesEnAtencionMedica++;
                hospital.mutexSdP.release();

                // Impresión de estado
                hospital.logEstado("======== COMIENZA LA ATENCION AL CLIENTE ========");

                // Simula el tiempo de atención
                Thread.sleep(tiempoAtencion());

                // Actualiza contadores
                hospital.mutexSdP.acquire();
                hospital.pacientesEnAtencionMedica--;
                hospital.mutexSdP.release();

                // Avisa al paciente que finalizó su turno
                hospital.permisoAtencionMedica.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
}
