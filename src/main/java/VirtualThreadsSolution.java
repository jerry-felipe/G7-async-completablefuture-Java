
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class VirtualThreadsSolution {

    static class ReportService {

        public String generateReport(String reportName) {
            System.out.println("Iniciando " + reportName
                    + " en " + Thread.currentThread());
            try {
                // Simula una operación lenta: llamada externa, archivo, espera, etc.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return reportName + " completado";
        }

    }

    public static void main(String[] args) throws Exception {
        ReportService service = new ReportService();
        long start = System.currentTimeMillis();

        // Un hilo virtual por tarea: son tan baratos que no hace falta pool.
        // El try-with-resources cierra el executor y espera a que todas las
        // tareas terminen (concurrencia con ciclo de vida acotado).
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            List<Future<String>> reports = List.of("Reporte-1", "Reporte-2", "Reporte-3")
                    .stream()
                    .map(name -> executor.submit(() -> service.generateReport(name)))
                    .toList();

            // Código de estilo secuencial/bloqueante, pero la espera es barata:
            // bloquear un hilo virtual no ocupa un hilo del sistema operativo.
            for (Future<String> report : reports) {
                System.out.println(report.get());
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("Tiempo total aproximado: " + (end - start) + " ms");
    }

}
