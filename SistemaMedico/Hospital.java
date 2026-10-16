package SistemaMedico;

import static java.lang.Thread.sleep;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Hospital {
    
    private static Hospital singleton = null;
    private Hospital(){}

    public static Hospital obtenerInstancia(){
        if(singleton == null){
            singleton = new Hospital();
        }
        return singleton;
    }

    public final int MAX_SALA_ESPERA = 10;

    // Muteces para secciones críticas (variables compartidas)
    public Semaphore mutexSdE = new Semaphore(1); // Protege contador de Sala de Espera
    public Semaphore mutexSdP = new Semaphore(1); // Protege contador de Sala de Preparación

    // Semáforos para flujo de pacientes (notificación)
    public Semaphore pacienteEnRecepcion = new Semaphore(0); // Aviva a la Recepcionista
    public Semaphore pacientesPendientes = new Semaphore(0); // Aviva al Enfermero
    public Semaphore pacienteEnSdE = new Semaphore(0);       // Aviva al Enfermero
    public Semaphore pacienteUrgente = new Semaphore(0);     // Aviva al Enfermero (Prioridad)
    public Semaphore pacienteEnSdP = new Semaphore(0);       // Aviva al Médico

    // Semáforos para sincronizar la transición individual del paciente
    public Semaphore permisoIngresoSdE = new Semaphore(0);
    public Semaphore permisoIngresoSdP = new Semaphore(0);
    public Semaphore permisoAtencionMedica = new Semaphore(0);
    public Semaphore permisoImpresion = new Semaphore(1);
    
    // Contadores
    public int pacientesEnSalaEspera = 0;
    public int pacientesEnSalaPreparacion = 0;
    public int pacientesEnColaRecepcion = 0;
    public int pacientesEnAtencionMedica = 0;

    public void logEstado(){
        System.out.println("Pacientes esperando en cola de recepcion: " + pacientesEnColaRecepcion);
        System.out.println("Pacientes esperando en hall: " + pacientesEnSalaEspera);
        System.out.println("En enfermeria: " + pacientesEnSalaPreparacion);
        System.out.println("En atencion médica: " + pacientesEnAtencionMedica);
        System.out.println("================");
        System.out.println("\n");
    }

    public void iniciar(){
        Random generadorVA = new Random();
        new Thread(new Medico()).start();
        System.out.println("medico");
        new Thread(new Enfermero()).start();
        System.out.println("enfermero");
        new Thread(new Recepcionista()).start();
        System.out.println("recepcionista");
        while(true){
            new Thread(new Paciente(generadorVA.nextBoolean())).start();
            System.out.println("======== PACIENTE ARRIBA A HOSPITAL ========");
            this.logEstado();
            try {
                sleep(generadorVA.nextInt(200,5000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
