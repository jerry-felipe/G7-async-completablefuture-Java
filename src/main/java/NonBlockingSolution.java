
import java.util.concurrent.CompletableFuture;

public class NonBlockingSolution {

    static class ReportService {

        public String generateReport(String reportName) {
            System.out.println("Iniciando " + reportName
                    + " en " + Thread.currentThread().getName());
            try {
                // Simula una operación lenta: llamada externa, archivo, espera, etc.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return reportName + " completado";
        }

    }

    public static void main(String[] args) {
        ReportService service = new ReportService();
        long start = System.currentTimeMillis();

        CompletableFuture<String> report1 = CompletableFuture.supplyAsync(() -> service.generateReport("Reporte-1"));
        CompletableFuture<String> report2 = CompletableFuture.supplyAsync(() -> service.generateReport("Reporte-2"));
        CompletableFuture<String> report3 = CompletableFuture.supplyAsync(() -> service.generateReport("Reporte-3"));

        CompletableFuture<Void> allReports = CompletableFuture.allOf(report1, report2, report3);
        allReports.join();

        System.out.println(report1.join());
        System.out.println(report2.join());
        System.out.println(report3.join());

        long end = System.currentTimeMillis();
        System.out.println("Tiempo total aproximado: " + (end - start) + " ms");
    }

}
