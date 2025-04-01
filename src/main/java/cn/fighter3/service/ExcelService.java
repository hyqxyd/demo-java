package cn.fighter3.service;

import cn.fighter3.entity.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelService {

    public List<User> importUsersFromExcel(MultipartFile file) throws Exception {
        List<User> users = new ArrayList<>();
        InputStream inputStream = file.getInputStream();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row
                User user = new User();

                Cell cell = row.getCell(0);
                double doubleValue = Double.parseDouble(cell.toString());  // 先转Double
                int value = (int) doubleValue;  // 显式类型转换（注意精度丢失）
                user.setId(value);
                cell=row.getCell(1);
                user.setUserName(getCellValueAsString(cell));

                cell = row.getCell(2);
                user.setPassword(getCellValueAsString(cell));

                cell = row.getCell(3);
                user.setEmail(getCellValueAsString(cell));

                cell = row.getCell(4);
                user.setRole(getCellValueAsString(cell));

                users.add(user);
            }
        }
        return users;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double value = cell.getNumericCellValue();
                    // 判断是否是整数
                    if (value == (long) value) {
                        return String.valueOf((long) value);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}