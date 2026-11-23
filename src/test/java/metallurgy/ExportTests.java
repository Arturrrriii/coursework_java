package metallurgy;
import java.nio.file.*;
import java.util.*;
public class ExportTests {
 public static void main(String[] args)throws Exception{
  Path dir=Path.of(args[0]);Files.createDirectories(dir);var input=new ConverterCalculator.Input(200,1.6,3.2,4,.6,30,1.15);var data=List.of(new Database.Entry(1,"2026",input));
  ExportService.csv(dir.resolve("test.csv"),data);if(ExportService.readCsv(dir.resolve("test.csv")).size()!=1)throw new AssertionError("CSV");
  ExportService.xlsx(dir.resolve("test.xlsx"),data);ExportService.pdf(dir.resolve("test.pdf"),data,false);ExportService.pdf(dir.resolve("chart.pdf"),data,true);System.out.println("Экспорт выполнен");
 }
}
