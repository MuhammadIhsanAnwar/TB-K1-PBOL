package warungaju;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelService {
    private static final String[] HEADERS={"Tanggal Transaksi","Nama Produk","Jumlah Terjual","Harga Jual/Unit","Total Penjualan","HPP/Unit","Total HPP","Keterangan"};
    public void buatTemplate(File file) throws IOException {
        Workbook book=new XSSFWorkbook(); Sheet sheet=book.createSheet("Penjualan");
        Row header=sheet.createRow(0); for(int i=0;i<HEADERS.length;i++)header.createCell(i).setCellValue(HEADERS[i]);
        Row sample=sheet.createRow(1); sample.createCell(0).setCellValue("2026-11-01"); sample.createCell(1).setCellValue("Nasi Goreng");
        sample.createCell(2).setCellValue(10); sample.createCell(3).setCellValue(18000); sample.createCell(4).setCellFormula("C2*D2"); sample.createCell(5).setCellValue(9000); sample.createCell(6).setCellFormula("C2*F2"); sample.createCell(7).setCellValue("Contoh data");
        Row hint=sheet.createRow(3); hint.createCell(0).setCellValue("Petunjuk: isi kolom tanggal (yyyy-MM-dd), produk, jumlah, harga/unit, HPP/unit, dan keterangan.");
        for(int i=0;i<HEADERS.length;i++)sheet.autoSizeColumn(i);
        OutputStream out=new FileOutputStream(file); book.write(out); out.close(); book.close();
    }
    public List<Transaksi> impor(File file, Set<String> produkTerdaftar) throws IOException {
        List<Transaksi> result=new ArrayList<Transaksi>(); Workbook book=WorkbookFactory.create(file); Sheet sheet=book.getSheetAt(0);
        for(int r=1;r<=sheet.getLastRowNum();r++){Row row=sheet.getRow(r); if(row==null||row.getCell(0)==null)continue;
            String tanggal=cell(row,0), nama=cell(row,1); int jumlah=(int)Double.parseDouble(cell(row,2)); double harga=Double.parseDouble(cell(row,3)), hpp=Double.parseDouble(cell(row,5));
            if(tanggal.isEmpty()||nama.isEmpty()||jumlah<=0||harga<0||hpp<0)throw new IOException("Data tidak valid pada baris "+(r+1));
            if(!produkTerdaftar.isEmpty()&&!produkTerdaftar.contains(nama))throw new IOException("Produk '"+nama+"' belum terdaftar pada baris "+(r+1));
            result.add(new Transaksi(LocalDate.parse(tanggal),"Pemasukan","Penjualan",jumlah*harga, jumlah*hpp,cell(row,7),nama));
        } book.close(); return result;
    }
    private String cell(Row row,int index){Cell c=row.getCell(index); return c==null?"":(c.getCellType()==CellType.NUMERIC?String.valueOf(c.getNumericCellValue()):c.getStringCellValue()).trim();}
}
