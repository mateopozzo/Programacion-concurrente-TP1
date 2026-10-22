package SistemaMedico;

import static java.lang.Thread.sleep;

import java.util.Random;
import java.util.concurrent.Semaphore;

public class Hospital {

    public static final int MAX_SALA_ESPERA = 10;
    public static final int MAX_SALA_PREPARACION = 5;
    public static final int CANTIDAD_PACIENTES = 30;     // Cantidad finita para que la simulación termine
    public static final int CANTIDAD_RECEPCIONISTAS = 1; // Según la consigna; la lógica soporta más de una
    public static final int CANTIDAD_ENFERMEROS = 1;     // Según la consigna; la lógica soporta más de uno

    // Instancia creada al cargar la clase: es segura aunque varios hilos llamen a obtenerInstancia
    private static final Hospital INSTANCIA = new Hospital();

    private Hospital() {}

    public static Hospital obtenerInstancia() {
        return INSTANCIA;
    }

    // Muteces para secciones críticas. Orden: mutexImpresion -> mutexSdE -> mutexSdP
    public final Semaphore mutexSdE = new Semaphore(1); // Protege contadores de recepción y Sala de Espera
    public final Semaphore mutexSdP = new Semaphore(1); // Protege contadores de urgencias, Sala de Preparación y consultorio
    public final Semaphore mutexImpresion = new Semaphore(1);

    // Semáforos para flujo de pacientes (notificación)
    public final Semaphore pacienteEnRecepcion = new Semaphore(0); // Despierta a la Recepcionista
    public final Semaphore pacientesPendientes = new Semaphore(0); // Despierta al Enfermero; el tipo de paciente lo decide con los contadores
    public final Semaphore pacienteEnSdP = new Semaphore(0);       // Despierta al Médico (paciente preparado de cualquier tipo)
    public final Semaphore lugaresDisponiblesSdE = new Semaphore(MAX_SALA_ESPERA);      // Usado por la Recepcionista para enviar pacientes a SdE
    public final Semaphore lugaresDisponiblesSdP = new Semaphore(MAX_SALA_PREPARACION); // Usado por el Enfermero para enviar pacientes a SdP
    public final Semaphore pacientesRetirados = new Semaphore(0);  // Cada paciente avisa al irse; iniciar espera a todos para terminar

    // Transición individual del paciente. Son justos (FIFO) para que los pacientes avancen en orden de llegada
    public final Semaphore permisoIngresoSdE = new Semaphore(0, true);
    public final Semaphore permisoIngresoSdPUrgente = new Semaphore(0, true);
    public final Semaphore permisoIngresoSdPNormal = new Semaphore(0, true);
    public final Semaphore permisoAtencionUrgente = new Semaphore(0, true); // Un permiso de atención por tipo: el médico llama primero a las urgencias
    public final Semaphore permisoAtencionNormal = new Semaphore(0, true);
    public final Semaphore siendoAtendido = new Semaphore(0, true);

    // Contadores (protegidos por mutexSdE)
    public int pacientesEnColaRecepcion = 0;
    public int pacientesEnSalaEspera = 0;

    // Contadores (protegidos por mutexSdP)
    public int pacientesUrgentesEsperando = 0; // Urgencias que esperan al enfermero; también le indica a quién atender primero
    public int pacientesEnSalaPreparacion = 0;
    public int urgenciasPreparadas = 0;        // Urgencias listas para el médico; le permite darles prioridad sin tryAcquire
    public int pacientesEnAtencionMedica = 0;

    public void logEstado(String tituloDeLog) {
        mutexImpresion.acquireUninterruptibly();
        mutexSdE.acquireUninterruptibly();
        mutexSdP.acquireUninterruptibly();
        System.out.println("======== " + tituloDeLog + " ========");
        System.out.println("Pacientes esperando en cola de recepcion: " + pacientesEnColaRecepcion);
        System.out.println("Urgencias esperando al enfermero: " + pacientesUrgentesEsperando);
        System.out.println("Pacientes esperando en hall: " + pacientesEnSalaEspera);
        System.out.println("En enfermeria: " + pacientesEnSalaPreparacion);
        System.out.println("En atencion médica: " + pacientesEnAtencionMedica);
        System.out.println("================");
        System.out.println("\n");
        mutexSdP.release();
        mutexSdE.release();
        mutexImpresion.release();
    }

    // Crea un hilo del personal como daemon: no impide que el programa termine cuando se van todos los pacientes
    private void iniciarPersonal(Runnable rol) {
        Thread hilo = new Thread(rol);
        hilo.start();
    }

    public void iniciar() throws InterruptedException {
        Random generadorVA = new Random();
        iniciarPersonal(new Medico());
        for (int i = 0; i < CANTIDAD_ENFERMEROS; i++) {
            iniciarPersonal(new Enfermero());
        }
        for (int i = 0; i < CANTIDAD_RECEPCIONISTAS; i++) {
            iniciarPersonal(new Recepcionista());
        }

        // Genera una cantidad fija de pacientes, cada uno con su id para poder seguirlo en el log
        for (int id = 1; id <= CANTIDAD_PACIENTES; id++) {
            new Thread(new Paciente(id, generadorVA.nextBoolean())).start();
            sleep(generadorVA.nextInt(200, 2000));
        }

        // Espera con un semáforo (en vez de join) a que se retiren todos los pacientes antes de cerrar
        for (int i = 0; i < CANTIDAD_PACIENTES; i++) {
            pacientesRetirados.acquireUninterruptibly();
        }

        logEstado("FIN DE LA SIMULACION");
    }
}