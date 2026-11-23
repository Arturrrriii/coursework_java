package metallurgy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.zip.*;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;

/** Обмен CSV/XLSX и печать отчета и графика в PDF. */
public final class ExportService {
    private ExportService(){}
    public static final String[] HEAD = {"mass_t","depth_m","intensity_m3_t_min","nozzles","manganese_percent","angle_deg","slag_factor","diameter_m","bottom_m","free_height_m","height_m","mouth_m","cone_m","cylinder_m","volume_m3","specific_m3_t","height_diameter"};
    public static String[] row(ConverterCalculator.Input x) {
        var r=ConverterCalculator.calculate(x);
        return new String[]{s(x.mass()),s(x.depth()),s(x.intensity()),Integer.toString(x.nozzles()),s(x.manganese()),s(x.angle()),s(x.slagFactor()),s(r.diameter()),s(r.bottomDiameter()),s(r.freeHeight()),s(r.height()),s(r.mouthDiameter()),s(r.coneHeight()),s(r.cylinderHeight()),s(r.volume()),s(r.specificVolume()),s(r.heightRatio())};
    }
    private static String s(double x){return String.format(Locale.US,"%.6f",x);}
    public static void csv(Path file,List<Database.Entry> entries) throws IOException {
        try(BufferedWriter w=Files.newBufferedWriter(file,StandardCharsets.UTF_8)){w.write('\ufeff');w.write(String.join(";",HEAD));w.newLine();for(var e:entries){w.write(String.join(";",row(e.input())));w.newLine();}}
    }
    public static List<ConverterCalculator.Input> readCsv(Path file) throws IOException {
        List<String> lines=Files.readAllLines(file,StandardCharsets.UTF_8);List<ConverterCalculator.Input> out=new ArrayList<>();
        for(int i=0;i<lines.size();i++) {String line=lines.get(i).replace("\ufeff","").trim();if(line.isEmpty())continue;if(i==0&&line.startsWith("mass_t;"))continue;
            String[] p=line.split(";",-1);if(p.length<7)throw new IOException("Строка "+(i+1)+": нужно не менее семи столбцов");
            try {var x=new ConverterCalculator.Input(Double.parseDouble(p[0].replace(',','.')),Double.parseDouble(p[1].replace(',','.')),Double.parseDouble(p[2].replace(',','.')),Integer.parseInt(p[3]),Double.parseDouble(p[4].replace(',','.')),Double.parseDouble(p[5].replace(',','.')),Double.parseDouble(p[6].replace(',','.')));ConverterCalculator.calculate(x);out.add(x);}catch(RuntimeException e){throw new IOException("Строка "+(i+1)+": "+e.getMessage());}
        } return out;
    }
    private static void put(ZipOutputStream z,String name,String content)throws IOException{z.putNextEntry(new ZipEntry(name));z.write(content.getBytes(StandardCharsets.UTF_8));z.closeEntry();}
    public static void xlsx(Path path,List<Database.Entry> entries)throws IOException{
        try(ZipOutputStream z=new ZipOutputStream(Files.newOutputStream(path))){
            put(z,"[Content_Types].xml","<?xml version=\"1.0\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/></Types>");
            put(z,"_rels/.rels","<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>");
            put(z,"xl/workbook.xml","<?xml version=\"1.0\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets><sheet name=\"Расчеты\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
            put(z,"xl/_rels/workbook.xml.rels","<?xml version=\"1.0\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/></Relationships>");
            StringBuilder b=new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetData><row>");for(String h:HEAD)b.append("<c t=\"inlineStr\"><is><t>").append(h).append("</t></is></c>");b.append("</row>");for(var e:entries){b.append("<row>");for(String val:row(e.input()))b.append("<c><v>").append(val).append("</v></c>");b.append("</row>");}b.append("</sheetData></worksheet>");put(z,"xl/worksheets/sheet1.xml",b.toString());
        }
    }
    public static void pdf(Path path,List<Database.Entry> entries, boolean chart)throws Exception{
        try(PDDocument doc=new PDDocument()) {
            if(chart) {BufferedImage img=ChartPanel.image(entries,1100,700); addImage(doc,img);}
            else {
                int pages=Math.max(1,(entries.size()+24)/25);
                for(int page=0;page<pages;page++) {
                    BufferedImage im=new BufferedImage(1240,1754,BufferedImage.TYPE_INT_RGB);Graphics2D g=im.createGraphics();g.setColor(Color.WHITE);g.fillRect(0,0,1240,1754);g.setColor(Color.BLACK);g.setFont(new Font("SansSerif",Font.BOLD,25));g.drawString("Калькулятор металлурга — отчет",70,70);g.setFont(new Font("SansSerif",Font.PLAIN,18));g.drawString("Масса, т     Глубина, м     Диаметр, м     Высота, м     Объем, м³     Удельный, м³/т",70,115);
                    for(int i=page*25;i<Math.min(entries.size(),(page+1)*25);i++){var x=entries.get(i).input();var r=ConverterCalculator.calculate(x);String line=String.format(Locale.US,"%8.2f            %6.2f             %6.3f              %6.3f           %8.2f              %6.3f",x.mass(),x.depth(),r.diameter(),r.height(),r.volume(),r.specificVolume());g.drawString(line,70,155+(i-page*25)*55);}g.drawString("Страница "+(page+1)+" из "+pages,70,1690);g.dispose();addImage(doc,im);
                }
            } doc.save(path.toFile());
        }
    }
    private static void addImage(PDDocument doc,BufferedImage image)throws Exception{PDPage p=new PDPage(PDPage.PAGE_SIZE_A4);doc.addPage(p);var pic=new PDPixelMap(doc,image);try(PDPageContentStream content=new PDPageContentStream(doc,p)){float w=535,h=w*image.getHeight()/image.getWidth();if(h>760){h=760;w=h*image.getWidth()/image.getHeight();}content.drawXObject(pic,30,800-h,w,h);}}
}
