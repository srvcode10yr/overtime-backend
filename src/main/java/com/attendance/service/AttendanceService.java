package com.attendance.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AttendanceService {

    public byte[] processFile(MultipartFile file, double minDurationP, double maxDurationPA,
                              double ot1Min, double ot1Max, double ot2Min, String excludedPaycodes) throws IOException {

        Set<String> excluded = new HashSet<>(Arrays.asList(excludedPaycodes.split(",")));

        InputStream inputStream = file.getInputStream();
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);


        Row header = sheet.getRow(0);
        int inCol = findColumn(header, "In");
        int outCol = findColumn(header, "Out");
        int paycodeCol = findColumn(header, "Pay Code");

        // Add new
        CellStyle headerStyle = header.getCell(0) != null ? header.getCell(0).getCellStyle() : workbook.createCellStyle();



        // Add new header cells with copied style
        int durationCol = header.getLastCellNum();
        Cell durationHeader = header.createCell(durationCol);
        durationHeader.setCellValue("Duration");
        durationHeader.setCellStyle(headerStyle);

        Cell shiftHeader = header.createCell(durationCol + 1);
        shiftHeader.setCellValue("Shift");
        shiftHeader.setCellStyle(headerStyle);

        Cell statusHeader = header.createCell(durationCol + 2);
        statusHeader.setCellValue("Status");
        statusHeader.setCellStyle(headerStyle);

        Cell otHeader = header.createCell(durationCol + 3);
        otHeader.setCellValue("OT Hours");
        otHeader.setCellStyle(headerStyle);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            Cell inCell = row.getCell(inCol);
            Cell outCell = row.getCell(outCol);
            Cell payCell = row.getCell(paycodeCol);
            String paycode = payCell != null ? payCell.getStringCellValue().trim() : "";

            CellStyle baseStyle = row.getCell(0) != null ? row.getCell(0).getCellStyle() : workbook.createCellStyle();



            LocalTime inTime = parseTime(inCell);
            LocalTime outTime = parseTime(outCell);

            double duration = 0.0;
            String shift = "";
            String status = "A";
            int otHours = 0;

            if (inTime != null && outTime != null) {
                Duration dur = Duration.between(inTime, outTime);
                if (dur.isNegative()) dur = dur.plusHours(24);
                duration = dur.toMinutes() / 60.0;

                // Determine shift
                if (inTime.isAfter(LocalTime.of(18, 0)) || inTime.isBefore(LocalTime.of(6, 0))) {
                    shift = "NIGHT";
                    if (duration >= ot2Min) otHours = 2;
                    else if (duration >= ot1Min && duration <= ot1Max) otHours = 1;
                } else {
                    shift = "DAY";
                    if (duration >= ot2Min) otHours = 2;
                    else if (duration >= ot1Min && duration <= ot1Max) otHours = 1;
                }

                // Determine status
                if(duration<minDurationP){
                    status = "A";
                }else if (duration >= minDurationP && duration < maxDurationPA) {
                    status = "P/A";
                }else{
                    status = "P";
                }

            }

            if (excluded.contains(paycode)) {
                otHours = 0;
            }
            CellStyle style = row.getCell(0) != null ? row.getCell(0).getCellStyle() : workbook.createCellStyle();
            DataFormat df = workbook.createDataFormat();
            CellStyle numericStyle = workbook.createCellStyle();
            numericStyle.cloneStyleFrom(style);
            numericStyle.setDataFormat(df.getFormat("0.00"));

            CellStyle intStyle = workbook.createCellStyle();
            intStyle.cloneStyleFrom(style);
            intStyle.setDataFormat(df.getFormat("0"));


            Cell srlCell = row.getCell(0);
            if (srlCell != null) {
                srlCell.setCellValue(i);  // Use numeric format
                srlCell.setCellStyle(intStyle);
            }


            Cell durationCell = row.createCell(durationCol);
            durationCell.setCellValue(duration);
            durationCell.setCellStyle(numericStyle);


            Cell shiftCell = row.createCell(durationCol + 1);
            shiftCell.setCellValue(shift);
            shiftCell.setCellStyle(style);

            Cell statusCell = row.createCell(durationCol + 2);
            statusCell.setCellValue(status);
            statusCell.setCellStyle(style);

            Cell otCell = row.createCell(durationCol + 3);
            otCell.setCellValue(otHours);
            otCell.setCellStyle(style);

        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();
        return bos.toByteArray();
    }

    private int findColumn(Row header, String name) {
        for (Cell cell : header) {
            if (cell.getStringCellValue().trim().equalsIgnoreCase(name)) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }

    private LocalTime parseTime(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                Date date = cell.getDateCellValue();
                return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).toLocalTime();
            } else if (cell.getCellType() == CellType.STRING) {
                return LocalTime.parse(cell.getStringCellValue().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
