package cn.onboard.android.app.common;

import com.google.common.base.Strings;

import java.io.File;


public class FileUtils {

    /**
     * 根据文件绝对路径获取文件名
     *
     * @param filePath
     * @return
     */
    public static String getFileName(String filePath) {
        if (Strings.isNullOrEmpty(filePath)) return "";
        return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
    }

}