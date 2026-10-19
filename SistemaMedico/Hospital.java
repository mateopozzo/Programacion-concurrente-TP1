package SistemaMedico;

import static java.lang.Thread.sleep;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Hospital {

    public static final int MAX_SALA_ESPERA = 10;
    public static final int MAX_SALA_PREPARACION = 5;
    public static final int CANTIDAD_PACIENTES = 30; // Cantidad finita para que la simulación termine

    // Instancia creada al cargar la clase: es segura aunque varios hilos llamen a obtenerInstancia
    private static final Hospital INSTANCIA = new Hospital();

    private Hospital() {}

    public static Hospital obtenerInstancia() {
        return INSTANCIA;
    }

    // Muteces para secciones críticas. Si se necesita más de uno: mutexImpresion -> mutexSdE -> mutexSdP
    public final Semaphore mutexSdE = new Semaphore(1); // Protege contadores de recepción y Sala de Espera
    public final Semaphore mutexSdP = new Semaphore(1); // Protege contadores de urgencias, Sala de Preparación y consultorio
    public final Semaphore mutexImpresion = new Semaphore(1);

    // Semáforos para flujo de pacientes (notificación)
    public final Semaphore pacienteEnRecepcion = new Semaphore(0);  // Despierta a la Recepcionista
    public final Semaphore pacientesPendientes = new Semaphore(0);  // Despierta al Enfermero (paciente de cualquier tipo)
    public final Semaphore pacienteEnSdE = new Semaphore(0);        // Avisa al Enfermero que hay un paciente normal
    public final Semaphore pacienteUrgente = new Semaphore(0);      // Avisa al Enfermero que hay una urgencia (prioridad)
    public final Semaphore pacienteEnSdP = new Semaphore(0);        // Despierta al Médico (paciente preparado de cualquier tipo)
    public final Semaphore pacienteUrgenteEnSdP = new Semaphore(0); // Avisa al Médico que hay una urgencia preparada (prioridad)
    public final Semaphore pacienteNormalEnSdP = new Semaphore(0);  // Avisa al Médico que hay un paciente normal preparado
    public final Semaphore lugaresDisponiblesSdE = new Semaphore(MAX_SALA_ESPERA);      // Usado por la Recepcionista para enviar pacientes a SdE
    public final Semaphore lugaresDisponiblesSdP = new Semaphore(MAX_SALA_PREPARACION); // Usado por el Enfermero para enviar pacientes a SdP

    // Transición individual del paciente. Son justos (FIFO) para que los pacientes avancen en orden de llegada
    public final Semaphore permisoIngresoSdE = new Semaphore(0, true);
    public final Semaphore permisoIngresoSdPUrgente = new Semaphore(0, true);
    public final Semaphore permisoIngresoSdPNormal = new Semaphore(0, true);
    public final Semaphore permisoAtencionUrgente = new Semaphore(0, true); // Un permiso de atención por tipo: el médico llama primero a las urgencias
    public final Semaphore permisoAtencionNormal = new Semaphore(0, true);
    public final Semaphore siendoAtendido = new Semaphore(0, true);

    // Contadores
    public int pacientesEnColaRecepcion = 0;
    public int pacientesUrgentesEsperando = 0; // Urgencias que esperan al enfermero, antes no figuraban en ningún contador
    public int pacientesEnSalaEspera = 0;
    public int pacientesEnSalaPreparacion = 0;
    public int pacientesEnAtencionMedica = 0;

    public void logEstado(String tituloDeLog) throws InterruptedException {
        mutexImpresion.acquire();
        mutexSdE.acquire();
        mutexSdP.acquire();
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
        hilo.setDaemon(true);
        hilo.start();
    }

    public void iniciar() throws InterruptedException {
        Random generadorVA = new Random();
        iniciarPersonal(new Medico());
        iniciarPersonal(new Enfermero());
        iniciarPersonal(new Recepcionista());

        // Genera una cantidad fija de pacientes, cada uno con su id para poder seguirlo en el log
        List<Thread> pacientes = new ArrayList<>();
        for (int id = 1; id <= CANTIDAD_PACIENTES; id++) {
            Thread paciente = new Thread(new Paciente(id, generadorVA.nextBoolean()));
            pacientes.add(paciente);
            paciente.start();
            sleep(generadorVA.nextInt(200, 2000));
        }

        // Espera a que todos los pacientes terminen su atención antes de cerrar
        for (Thread paciente : pacientes) {
            paciente.join();
        }

        logEstado("FIN DE LA SIMULACION");
    }
}