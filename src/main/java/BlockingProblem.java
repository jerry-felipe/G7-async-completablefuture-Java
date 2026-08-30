
public class BlockingProblem {

    static class ReportService {
        public String generateReport(String reportName) {
            System.out.println("Iniciando " + reportName);
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

        String report1 = service.generateReport("Reporte-1");
        String report2 = service.generateReport("Reporte-2");
        String report3 = service.generateReport("Reporte-3");

        System.out.println(report1);
        System.out.println(report2);
        System.out.println(report3);

        long end = System.currentTimeMillis();
        System.out.println("Tiempo total aproximado: " + (end - start) + " ms");
    }

}