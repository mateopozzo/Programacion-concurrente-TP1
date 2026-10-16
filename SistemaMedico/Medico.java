package SistemaMedico;

public class Medico implements Runnable {

    private Hospital hospital = null;
    
    @Override
    public void run(){
        hospital = Hospital.obtenerInstancia();
        try {
            while(true){ 
                hospital.pacienteEnSdP.acquire();
                
                hospital.mutexSdP.acquire();
                hospital.pacientesEnSalaPreparacion--;
                hospital.pacientesEnAtencionMedica++;
                hospital.mutexSdP.release();

                hospital.permisoAtencionMedica.release(); // Avisa al paciente que empezó la consulta

                System.out.println("======== COMIENZA LA ATENCION AL CLIENTE ========");
                hospital.logEstado();

                Thread.sleep(3000); // Simula el tiempo de atención

                // Agarra mutex solo para poder hacer un buen log
                hospital.mutexSdP.acquire();
                hospital.pacientesEnAtencionMedica--;
                hospital.mutexSdP.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
}
