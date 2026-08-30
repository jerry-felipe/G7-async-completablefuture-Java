
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;

public class StructuredConcurrencySolution {

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

        // Concurrencia estructurada (API de JDK 25+, preview): las tareas viven
        // dentro del scope, como variables locales dentro de un bloque.
        // open() sin argumentos usa la política "todas deben tener éxito":
        // si una falla, las demás se cancelan y join() lanza la excepción.
        // Nada puede quedar "huérfano" ejecutándose fuera del try.
        try (var scope = StructuredTaskScope.<String>open()) {

            Subtask<String> report1 = scope.fork(() -> service.generateReport("Reporte-1"));
            Subtask<String> report2 = scope.fork(() -> service.generateReport("Reporte-2"));
            Subtask<String> report3 = scope.fork(() -> service.generateReport("Reporte-3"));

            // Espera a todas y propaga el fallo de la primera que falle.
            scope.join();

            System.out.println(report1.get());
            System.out.println(report2.get());
            System.out.println(report3.get());
        }

        long end = System.currentTimeMillis();
        System.out.println("Tiempo total aproximado: " + (end - start) + " ms");
    }

}
