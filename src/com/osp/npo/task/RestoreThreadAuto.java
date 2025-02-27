package com.osp.npo.task;

import com.osp.npo.common.util.Util;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by QUYENLC on 12/07/2021.
 */
public class RestoreThreadAuto extends Thread {
    private static final Logger logger = Logger.getLogger(RemoveFileBackUpThreadAuto.class.getName());
    private String database;
    private String user;
    private String pass;
    private String host;
    private String port;
    private String system_backup_os;
    private String system_backup_database_folder;
    private String system_mysql_mysqldump_folder;
    private String status;
//    private String timeRestore;

    public static void main(String[] arg) {
        new RestoreThreadAuto().start();
    }

    public void init() {
        try {
            Properties prop = new Properties();

            prop.loadFromXML(new FileInputStream(System.getProperty("user.dir") + "/config.xml"));
            database = prop.getProperty("database");
            user = prop.getProperty("userName");
            pass = prop.getProperty("password");
            host = prop.getProperty("host");
            port = prop.getProperty("port");
            system_backup_os = prop.getProperty("system_backup_os");
            system_backup_database_folder = prop.getProperty("system_backup_database_folder");
            system_mysql_mysqldump_folder = prop.getProperty("system_mysql_mysqldump_folder");
            status = prop.getProperty("status");
//            timeRestore = prop.getProperty("timeRestore");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        init();
//        while (true) {
//            Calendar cal = Calendar.getInstance();
//            Util util = new Util();
//            List<String> listDateBackup = util.convertListBooleanToListString();
//            String dayOfWeek = util.getDayOfWeek(cal.get(Calendar.DAY_OF_WEEK));
//            if (listDateBackup.size() > 0) {
//                for (int i = 0; i < listDateBackup.size(); i++) {
//                    if (dayOfWeek.equals(listDateBackup.get(i))) {
//                        try {
//                            if (cal.get(Calendar.HOUR_OF_DAY) == Integer.parseInt(timeRestore.split(":")[0])) {
//                                if (cal.get(Calendar.MINUTE) == Integer.parseInt(timeRestore.split(":")[1])) {
//                                    if (status.equals("true")) {
//                                        logger.info("Tiáº¿n hÃ nh khÃ´i phá»¥c dá»¯ liá»‡u-----------------------------------");
//                                        this.restoreThread();
//                                    } else {
//                                        logger.info("NgÃ y khÃ´ng tiáº¿n hÃ nh khÃ´i phá»¥c dá»¯ liá»‡u-----------------------------------");
//                                    }
//                                } else {
//                                    logger.info("Chá»�-----------------------------------");
//                                }
//                            } else {
//                                logger.info("Chá»�-----------------------------------");
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                    }
//                }
//            }
//            try {
//                sleep(60000);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
    }

    public void restoreThread() {
        List<String> lstFileSQL = findFileSqlBackUp();
        if (lstFileSQL.size() == 0) {
            logger.warning("Không có file backup để khôi phục lại dữ liệu");
        }
        String fileBackUp = lstFileSQL.get(0);
        String filePath = system_backup_database_folder;
        String[] cmd = new String[10];
        int i = 0;
        cmd[i++] = system_mysql_mysqldump_folder.substring(0, 2);
        cmd[i++] = "cd \"" + system_mysql_mysqldump_folder + "\"";
        cmd[i++] = "mysql -u" + user + " -p" + pass + " -h" + host + " -P" + port + " " + database + " < \"" + filePath + fileBackUp + "\"";

        Runtime c = Runtime.getRuntime();
        String fileretore = "restore.bat";
        if (system_backup_os.equals("1")) {
            fileretore = "restore.sh";
        }
        createFileBackUpOrRetore(cmd, fileretore, i);
        String cmdStr = "cmd /c start " + system_backup_database_folder + "restore.bat";
        try {
            Process pro;
            if (system_backup_os.equals("0")) {
                pro = c.exec(cmdStr, null, new File(system_backup_database_folder));
            } else {
                pro = c.exec(system_backup_database_folder + "restore.sh",
                        null, new File(system_backup_database_folder));
            }
            if (pro.waitFor() == 0) {
                logger.info("Khôi phục dữ liệu thành công");
            } else {
                logger.warning("Khôi phục dữ liệu thất bại.Vui lòng thử lại");
            }
        } catch (Exception e) {
            logger.warning("Bạn đã gặp lỗi trong quá trình khôi phục dữ liệu:" + e);
        }
    }

    public void createFileBackUpOrRetore(String[] fileContent, String fileName, int length) {
        try {
            File file = new File(system_backup_database_folder + fileName);
            File folder = new File(system_backup_database_folder);
            if (file.exists()) {
                file.delete();
            } else {
                if (!folder.exists())
                    folder.mkdirs();
            }
            file.createNewFile();
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            for (int i = 0; i < length; i++) {
                writer.println(fileContent[i]);
            }
            writer.println("Exit");
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<String> findFileSqlBackUp() {
        File folder = new File(system_backup_database_folder);
        File[] files = folder.listFiles();
        List<String> fileBackUp = new ArrayList<>();
        for (int i = files.length - 1; i >= 0; i--) {
            String fileName = files[i].getName();
            if (fileName.substring(fileName.lastIndexOf(".")).equals(".sql")) {
                fileBackUp.add(fileName);
            }
        }
        return fileBackUp;
    }
}
