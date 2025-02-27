package com.osp.npo.task;

import com.osp.npo.common.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by QUYENLC on 12/07/2021.
 */

public class RemoveFileBackUpThreadAuto extends Thread {
    private static final Logger logger = Logger.getLogger(RemoveFileBackUpThreadAuto.class.getName());
    private String system_backupAll_database_folder;
    private String status;
    private String timeRemoveFileBU;
    private int dateEffect;

    public static void main(String[] arg) {
        new RemoveFileBackUpThreadAuto().start();
    }

    public void init() {
        try {
            Properties prop = new Properties();
            prop.loadFromXML(new FileInputStream(System.getProperty("user.dir") + "/config.xml"));
            system_backupAll_database_folder = prop.getProperty("system_backupAll_database_folder");
            status = prop.getProperty("status");
            timeRemoveFileBU = prop.getProperty("timeRemoveFileBU");
            dateEffect = Integer.parseInt(prop.getProperty("dateEffect"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        init();
        Util uTil = new Util();
        uTil.createFolderBU();
        while (true) {
            Calendar cal = Calendar.getInstance();
            Util util = new Util();
            List<String> listDateBackup = util.convertListBooleanToListString();
            String dayOfWeek = util.getDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));
            if (listDateBackup.size() > 0) {
                for (int i = 0; i < listDateBackup.size(); i++) {
                    if (dayOfWeek.equals(listDateBackup.get(i))) {
                        try {
                            if (cal.get(Calendar.HOUR_OF_DAY) == Integer.parseInt(timeRemoveFileBU.split(":")[0])) {
                                if (cal.get(Calendar.MINUTE) == Integer.parseInt(timeRemoveFileBU.split(":")[1])) {
                                    if (status.equals("true")) {
//                                      logger.info("Tiến hành xóa file backup cũ-----------------------------------");
                                        this.removeFileBackUpThread();
                                    } else {
//                                      logger.info("Ngày không tiến hành xóa file backup cũ-----------------------------------");
                                    }
                                } else {
                                }
                            }
                            else {
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
            try {
                sleep(60000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void removeFileBackUpThread() {
        init();
        List<String> allFileBackUp = new ArrayList<>();
        File folder = new File(system_backupAll_database_folder);
        File[] files = folder.listFiles();
        if(files.length > 0) {
            for (int i = files.length - 1; i >= 0; i--) {
                String fileName = files[i].getName();
                if (fileName.substring(fileName.lastIndexOf(".")).equals(".sql")) {
                    allFileBackUp.add(fileName);
                }
            }
            Date date = new Date();
            Format formatter = new SimpleDateFormat("yyyy-MM-dd");
            String dateNow = formatter.format(date);

            for (String dateBU : allFileBackUp) {
                String dateBackUp = dateBU.substring(7, 17);
                dateNow = dateNow.replaceAll("-", "/");
                dateBackUp = dateBackUp.replaceAll("-", "/");
                Date now = new Date(dateNow);
                Date dateFile = new Date(dateBackUp);
                Long millisBetween = now.getTime() - dateFile.getTime();
                Long days = millisBetween / (1000 * 3600 * 24);
                int result = Math.round(Math.abs(days));
                if (dateFile.getTime() < now.getTime()) {
                    // Giữ lại file back up của số ngày gần nhất theo file cấu hình
                    if (result > dateEffect) {
                        try {
                            boolean success = Files.deleteIfExists(Paths.get(system_backupAll_database_folder + dateBU));
                            if (success) {
                                logger.info("Xoá file back up " + dateBU + " thành công");
                            } else {
                                logger.warning("Xoá file back up " + dateBU + " thất bại");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        else {
            logger.info("Không có file nào tồn tại trong thư mục");
        }
    }
}