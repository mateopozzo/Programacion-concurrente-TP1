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
    public final int MAX_SALA_PREPARACION = 5;

    // Muteces para secciones críticas (variables compartidas)
    public Semaphore mutexSdE = new Semaphore(1); // Protege contador de Sala de Espera
    public Semaphore mutexSdP = new Semaphore(1); // Protege contador de Sala de Preparación
    public Semaphore mutexImpresion = new Semaphore(1);

    // Semáforos para flujo de pacientes (notificación)
    public Semaphore pacienteEnRecepcion = new Semaphore(0); // Aviva a la Recepcionista
    public Semaphore pacientesPendientes = new Semaphore(0); // Aviva al Enfermero
    public Semaphore pacienteEnSdE = new Semaphore(0);       // Aviva al Enfermero
    public Semaphore pacienteUrgente = new Semaphore(0);     // Aviva al Enfermero (Prioridad)
    public Semaphore pacienteEnSdP = new Semaphore(0);       // Aviva al Médico
    public Semaphore lugaresDisponiblesSdE = new Semaphore(MAX_SALA_ESPERA); // Utilizado por recepcionista para enviar pacientes a SdE
    public Semaphore lugaresDisponiblesSdP = new Semaphore(MAX_SALA_PREPARACION); // Utilizado por Enfermero para enviar pacientes a SdP

    // Semáforos para sincronizar la transición individual del paciente
    public Semaphore permisoIngresoSdE = new Semaphore(0);
    public Semaphore permisoIngresoSdPUrgente = new Semaphore(0);
    public Semaphore permisoIngresoSdPNormal = new Semaphore(0);
    public Semaphore permisoAtencionMedica = new Semaphore(0);
    public Semaphore siendoAtendido = new Semaphore(0);
    // Contadores
    public int pacientesEnSalaEspera = 0;
    public int pacientesEnSalaPreparacion = 0;
    public int pacientesEnColaRecepcion = 0;
    public int pacientesEnAtencionMedica = 0;

    public void logEstado(String tituloDeLog) throws InterruptedException {
        mutexImpresion.acquire();
        mutexSdE.acquire();
        mutexSdP.acquire();
        System.out.println("======== " + tituloDeLog + " ========");
        System.out.println("Pacientes esperando en cola de recepcion: " + pacientesEnColaRecepcion);
        System.out.println("Pacientes esperando en hall: " + pacientesEnSalaEspera);
        System.out.println("En enfermeria: " + pacientesEnSalaPreparacion);
        System.out.println("En atencion médica: " + pacientesEnAtencionMedica);
        System.out.println("================");
        System.out.println("\n");
        mutexSdP.release();
        mutexSdE.release();
        mutexImpresion.release();
    }


    public void iniciar() throws InterruptedException {
        Random generadorVA = new Random();
        new Thread(new Medico()).start();
        System.out.println("medico");
        new Thread(new Enfermero()).start();
        System.out.println("enfermero");
        new Thread(new Recepcionista()).start();
        System.out.println("recepcionista");
        while(true){
            new Thread(new Paciente(generadorVA.nextBoolean())).start();
            this.logEstado("======== PACIENTE ARRIBA A HOSPITAL ========");
            try {
                sleep(generadorVA.nextInt(200,2000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
