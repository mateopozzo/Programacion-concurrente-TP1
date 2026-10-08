package SistemaMedico;

import static java.lang.Thread.sleep;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Hospital {
    
    public Hospital(){}

    public static final int MAX_SALA_ESPERA = 10;

    // Muteces para secciones críticas (variables compartidas)
    public static Semaphore mutexSdE = new Semaphore(1); // Protege contador de Sala de Espera
    public static Semaphore mutexSdP = new Semaphore(1); // Protege contador de Sala de Preparación

    // Semáforos para flujo de pacientes (notificación)
    public static Semaphore pacienteEnRecepcion = new Semaphore(0); // Aviva a la Recepcionista
    public static Semaphore pacientesPendientes = new Semaphore(0); // Aviva al Enfermero
    public static Semaphore pacienteEnSdE = new Semaphore(0);       // Aviva al Enfermero
    public static Semaphore pacienteUrgente = new Semaphore(0);     // Aviva al Enfermero (Prioridad)
    public static Semaphore pacienteEnSdP = new Semaphore(0);       // Aviva al Médico

    // Semáforos para sincronizar la transición individual del paciente
    public static Semaphore permisoIngresoSdE = new Semaphore(0);
    public static Semaphore permisoIngresoSdP = new Semaphore(0);
    public static Semaphore permisoAtencionMedica = new Semaphore(0);
    
    // Contadores
    public static int pacientesEnSalaEspera = 0;
    public static int pacientesEnSalaPreparacion = 0;

    public static void iniciar(){
        Random generadorVA = new Random();
        new Thread(new Medico()).start();
        System.out.println("medico");
        new Thread(new Enfermero()).start();
        System.out.println("enfermero");
        new Thread(new Recepcionista()).start();
        System.out.println("recepcionista");
        while(true){
            new Thread(new Paciente(generadorVA.nextBoolean())).start();
            System.out.println("creo paciente");
            try {
                sleep(generadorVA.nextInt(200,5000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
