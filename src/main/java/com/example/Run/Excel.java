package com.example.Run;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class Excel {
    private final static Logger mylog = LoggerFactory.getLogger(Excel.class);

    private static Workbook workbook;
    private static Sheet sheet;
    private static Row row;

    private static Workbook createWorkBook(String... filename) throws IOException, InvalidFormatException {
        if (filename.length > 0) {
            workbook = new XSSFWorkbook(new File(filename[0]));
            return workbook;
        }
        workbook = new XSSFWorkbook();
        return workbook;
    }

    private static Sheet createSheet(String... name) {
        if (name.length == 0) {
            sheet = workbook.createSheet();
            return sheet;
        }
        sheet = workbook.createSheet(name[0]);
        return sheet;
    }

    private static Row createRow(int RowNum) {
        if (workbook != null && sheet != null){
            row = sheet.createRow(RowNum);
            return row;
        }
        return null;
    }

    private static int getRowNum(){
        return row.getRowNum();
    }

    private static void createCol(Row row, Object... value) {
        for (int i = 0; i < value.length; i++) {
            row.createCell(i).setCellValue((String) value[i]);
        }
    }

    /**
     * 给Excel创建一个header
     * @param headerName 第一行的列名，之后的写入的数据必须按照传入的列名，否则数据会错乱
     * @return
     */
    public static Workbook CreateHeader(String SheetName,String... headerName) {
        if (SheetName == null || SheetName.equals("")){
            SheetName = "";
        }
        try {
            workbook = createWorkBook();
            createSheet(SheetName);
            createCol(createRow(0), (Object) headerName);
            return workbook;
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 对某一行数据进行写入
     * @param data 写入对数据，因为一行数据不可能只有一列，所以不限制传入的数据长度
     * @return
     */
    public static Workbook CreateData(String SheetName,String... data){
        if (workbook == null){
            workbook = CreateHeader(SheetName);
        }
        Row row = createRow( getRowNum() + 1 );
        for (int i = 0; i < data.length; i++) {
            createCol(row, (Object) data);
        }
        return workbook;
    }




}