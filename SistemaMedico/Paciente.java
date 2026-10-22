package SistemaMedico;

public class Paciente implements Runnable {

    private final int id;            // Identifica al paciente en el log
    private final boolean esUrgente; // Define qué tipo de paciente es
    private Hospital hospital = null;

    public Paciente(int id, boolean esUrgente) {
        this.id = id;
        this.esUrgente = esUrgente;
    }

    @Override
    public void run() {
        hospital = Hospital.obtenerInstancia();
        if (esUrgente) {
            // Cuenta la urgencia antes de avisar, así el enfermero la ve al despertar y le da prioridad
            // Bypassea la recepcion directamente
            hospital.mutexSdP.acquireUninterruptibly();
            hospital.pacientesUrgentesEsperando++;
            hospital.mutexSdP.release();

            // Impresión de estado
            hospital.logEstado("ARRIBO DE URGENCIA (paciente " + id + ")");

            // Despierta al enfermero
            hospital.pacientesPendientes.release();

            // Espera a que el enfermero termine de prepararlo
            hospital.permisoIngresoSdPUrgente.acquireUninterruptibly();

            // Espera a que el médico lo llame
            hospital.permisoAtencionUrgente.acquireUninterruptibly();
        } else {

            // Contador de pacientes esperando en recepción
            hospital.mutexSdE.acquireUninterruptibly();
            hospital.pacientesEnColaRecepcion++;
            hospital.mutexSdE.release();

            // Impresión de estado
            hospital.logEstado("ARRIBO TURNO COMUN (paciente " + id + ")");

            // Notifica a Recepción que está esperando
            hospital.pacienteEnRecepcion.release();

            // Espera que la recepcionista le dé entrada a la SdE
            hospital.permisoIngresoSdE.acquireUninterruptibly();

            // Ya en la Sala de Espera, despierta al enfermero
            hospital.pacientesPendientes.release();

            // Espera a que el enfermero termine de prepararlo
            hospital.permisoIngresoSdPNormal.acquireUninterruptibly();

            // Espera a que el médico lo llame
            hospital.permisoAtencionNormal.acquireUninterruptibly();
        }

        // Se queda en el consultorio hasta que el médico termina la consulta
        hospital.siendoAtendido.acquireUninterruptibly();

        // Registra la salida para poder seguir a cada paciente de punta a punta
        hospital.logEstado("PACIENTE " + id + " SE RETIRA");

        // Avisa que salió del sistema
        hospital.pacientesRetirados.release();
    }
}