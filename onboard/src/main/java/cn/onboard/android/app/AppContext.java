package cn.onboard.android.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.google.common.base.Strings;
import com.onboard.api.dto.Activity;
import com.onboard.api.dto.Attachment;
import com.onboard.api.dto.Comment;
import com.onboard.api.dto.Company;
import com.onboard.api.dto.Discussion;
import com.onboard.api.dto.Document;
import com.onboard.api.dto.Project;
import com.onboard.api.dto.Todo;
import com.onboard.api.dto.Todolist;
import com.onboard.api.dto.Topic;
import com.onboard.api.dto.Upload;
import com.onboard.api.dto.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import cn.onboard.android.app.api.ApiClient;
import cn.onboard.android.app.bean.URLs;

public class AppContext extends Application {

    public static final int NETTYPE_WIFI = 0x01;
    private static final int NETTYPE_CMWAP = 0x02;
    private static final int NETTYPE_CMNET = 0x03;

    public static final int PAGE_SIZE = 20;// 默认分页大小

    private int userId = 0; // 登录用户的id

    @Override
    public void onCreate() {
        super.onCreate();
        // 注册App异常崩溃处理器
        Thread.setDefaultUncaughtExceptionHandler(AppException
                .getAppExceptionHandler());
    }

    /**
     * 获取当前网络类型
     *
     * @return 0：没有网络 1：WIFI网络 2：WAP网络 3：NET网络
     */
    @SuppressLint("DefaultLocale")
    public int getNetworkType() {
        int netType = 0;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            String extraInfo = networkInfo.getExtraInfo();
            if (!Strings.isNullOrEmpty(extraInfo)) {
                if (extraInfo.toLowerCase().equals("cmnet")) {
                    netType = NETTYPE_CMNET;
                } else {
                    netType = NETTYPE_CMWAP;
                }
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = NETTYPE_WIFI;
        }
        return netType;
    }

    /**
     * 获取App安装包信息
     *
     * @return
     */
    public PackageInfo getPackageInfo() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace(System.err);
        }
        if (info == null)
            info = new PackageInfo();
        return info;
    }

    /**
     * 获取App唯一标识
     *
     * @return
     */
    public String getAppId() {
        String uniqueID = getProperty(AppConfig.CONF_APP_UNIQUEID);
        if (Strings.isNullOrEmpty(uniqueID)) {
            uniqueID = UUID.randomUUID().toString();
            setProperty(AppConfig.CONF_APP_UNIQUEID, uniqueID);
        }
        return uniqueID;
    }

    /**
     * 获取登录用户id
     *
     * @return
     */
    public int getLoginUid() {
        return this.userId;
    }

    /**
     * 用户登录验证
     *
     * @param account
     * @param pwd
     * @return
     * @throws AppException
     */
    public User loginVerify(String account, String pwd) throws AppException {
        return ApiClient.login(this, account, pwd);
    }

    public User logOut() throws  AppException{
        return ApiClient.logout(this);
    }
    /**
     * 获取登录信息
     *
     * @return
     */
    public User getLoginInfo() {
        User user = new User();
        String id = getProperty("user.id");
        if (id != null) {
            user.setId(Integer.parseInt(id));
        }
        user.setName(getProperty("user.name"));
        user.setEmail(getProperty("user.email"));
        user.setPassword(getProperty("user.password"));
        return user;
    }

    /**
     * 保存登录信息
     *
     * @param user
     */
    @SuppressWarnings("serial")
    public void saveLoginInfo(final User user) {
        this.userId = user.getId();
        setProperties(new Properties() {
            {
                setProperty("user.id", String.valueOf(user.getId()));
                setProperty("user.name", user.getName());
                setProperty("user.email", user.getEmail());
                setProperty("user.password", user.getPassword());
            }
        });
    }

    /**
     * 保存用户头像
     *
     * @param fileName
     * @param bitmap
     */
    // public void saveUserFace(String fileName, Bitmap bitmap) {
    // try {
    // ImageUtils.saveImage(this, fileName, bitmap);
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }

    /**
     * 是否加载显示文章图片
     *
     * @return
     */
    public boolean isLoadImage() {
        String perf_loadimage = getProperty(AppConfig.CONF_LOAD_IMAGE);
        // 默认是加载的
        if (Strings.isNullOrEmpty(perf_loadimage))
            return true;
        else
            return Boolean.parseBoolean(perf_loadimage);
    }

    /**
     * 清除保存的缓存
     */
    public void cleanCookie() {
        removeProperty(AppConfig.CONF_COOKIE);
    }

    /**
     * 判断缓存数据是否可读
     *
     * @param cachefile
     * @return
     */
    @SuppressWarnings("unused")
    private boolean isReadDataCache(String cachefile) {
        return readObject(cachefile) != null;
    }

    /**
     * 判断缓存是否存在
     *
     * @param cachefile
     * @return
     */
    private boolean isExistDataCache(String cachefile) {
        boolean exist = false;
        File data = getFileStreamPath(cachefile);
        if (data.exists())
            exist = true;
        return exist;
    }

//    /**
//     * 清除app缓存
//     */
//    public void clearAppCache() {
//        // 清除webview缓存
//        File file = CacheManager.getCacheFileBaseDir();
//        if (file != null && file.exists() && file.isDirectory()) {
//            for (File item : file.listFiles()) {
//                item.delete();
//            }
//            file.delete();
//        }
//        deleteDatabase("webview.db");
//        deleteDatabase("webview.db-shm");
//        deleteDatabase("webview.db-wal");
//        deleteDatabase("webviewCache.db");
//        deleteDatabase("webviewCache.db-shm");
//        deleteDatabase("webviewCache.db-wal");
//        // 清除数据缓存
//        clearCacheFolder(getFilesDir(), System.currentTimeMillis());
//        clearCacheFolder(getCacheDir(), System.currentTimeMillis());
//        // 2.2版本才有将应用缓存转移到sd卡的功能
//        if (isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)) {
//            clearCacheFolder(MethodsCompat.getExternalCacheDir(this),
//                    System.currentTimeMillis());
//        }
//        // 清除编辑器保存的临时内容
//        Properties props = getProperties();
//        for (Object key : props.keySet()) {
//            String _key = key.toString();
//            if (_key.startsWith("temp"))
//                removeProperty(_key);
//        }
//    }

    /**
     * 读取对象
     *
     * @param file
     * @return
     * @throws IOException
     */
    Serializable readObject(String file) {
        if (!isExistDataCache(file))
            return null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = openFileInput(file);
            ois = new ObjectInputStream(fis);
            return (Serializable) ois.readObject();
        } catch (FileNotFoundException e) {
        } catch (Exception e) {
            e.printStackTrace();
            // 反序列化失败 - 删除缓存文件
            if (e instanceof InvalidClassException) {
                File data = getFileStreamPath(file);
                data.delete();
            }
        } finally {
            try {
                ois.close();
            } catch (Exception e) {
            }
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    void setProperties(Properties ps) {
        AppConfig.getAppConfig(this).set(ps);
    }

    public void setProperty(String key, String value) {
        AppConfig.getAppConfig(this).set(key, value);
    }

    public String getProperty(String key) {
        return AppConfig.getAppConfig(this).get(key);
    }

    void removeProperty(String... key) {
        AppConfig.getAppConfig(this).remove(key);
    }

    public List<Company> getCompanyList() throws AppException {
        List<Company> companyList = new ArrayList<Company>();
        companyList = ApiClient.getCompanyList(this);
        return companyList;
    }

    public List<Project> getProjectListByCompanyId(int companyId)
            throws AppException {
        List<Project> projectList = new ArrayList<Project>();
        projectList = ApiClient.getProjectByCompanyId(this, companyId);
        return projectList;
    }

    public List<Todolist> getTodoListsByProjectId(int companyId, int projectId)
            throws AppException {
        List<Todolist> todolists = new ArrayList<Todolist>();
        todolists = ApiClient
                .getTodoListByProjectId(this, companyId, projectId);
        return todolists;

    }

    public List<Todolist> getTodoListsByUserId(int companyId, int userId)
            throws AppException {
        List<Todolist> todolists = new ArrayList<Todolist>();
        todolists = ApiClient
                .getTodoListByCompanyIdByUserId(this, companyId, userId);
        return todolists;

    }

    public List<Todolist> getTodoListsByDate(int companyId, Date date)
            throws AppException {
        List<Todolist> todolists = new ArrayList<Todolist>();
        todolists = ApiClient
                .getTodoListByCompanyIdByDate(this, companyId, date);
        return todolists;

    }

    public List<Topic> getTopicsByProjectId(int companyId, int projectId, int page)
            throws AppException {
        List<Topic> topicList = new ArrayList<Topic>();
        topicList = ApiClient.getTopicsByProjectId(this, companyId, projectId, page);
        return topicList;

    }

    public List<Document> getDocumentsByProjectId(int companyId, int projectId)
            throws AppException {
        List<Document> documentList = new ArrayList<Document>();
        documentList = ApiClient.getDocumentsByProjectId(this, companyId,
                projectId);
        return documentList;

    }


    public Discussion getDiscussionById(int companyId, int projectId,
                                        int discussionId) throws AppException {
        Discussion discussion = ApiClient.getDiscussionById(this, companyId,
                projectId, discussionId);
        return discussion;
    }

    public Document getDocumentById(int companyId, int projectId,
                                    int documentId) throws AppException {
        Document document = ApiClient.getDocumentById(this, companyId,
                projectId, documentId);
        return document;
    }

    public List<Activity> getActivitiesByCompanyId(int companyId, int page) throws AppException {
        List<Activity> activities = new ArrayList<Activity>();
        activities = ApiClient.getActivitiesByCompanyId(this, companyId, page);
        return activities;
    }

    public List<Activity> getActivitiesByCompanyIdByUserId(int companyId, int userId, int page) throws AppException {
        List<Activity> activities = new ArrayList<Activity>();
        activities = ApiClient.getActivitiesByCompanyIdByUserId(this, companyId, userId, page);
        return activities;
    }


    public Upload getUploadById(int companyId, int projectId, int uploadId) throws AppException {
        return ApiClient.getUploadById(this, companyId, projectId, uploadId);
    }

    public Todolist getTodolistById(int companyId, int projectId, int todolistId) throws AppException {
        return ApiClient.getTodolistById(this, companyId, projectId, todolistId);
    }

    public List<Attachment> getAttachmentsByProjectId(int companyId,
                                                      int projectId, int page) throws AppException {
        List<Attachment> attachments = new ArrayList<Attachment>();
        attachments = ApiClient.getAttachmentsByProjectId(this, companyId, projectId, page);
        return attachments;

    }

    public List<Attachment> getAttachmentsByCompanyIdByUserId(int companyId,
                                                              int userId, int page) throws AppException {
        List<Attachment> attachments = new ArrayList<Attachment>();
        attachments = ApiClient.getAttachmentsByCompanyIdByUserId(this, companyId, userId, page);
        return attachments;

    }

    public void downloadAttachmentByAttachmentId(int attachmentId, String attachmentName, int companyId, int projectId) {
        DownloadManager downloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
        String newUrl = URLs.ATTACHMENT_DOWNLOWD_HTTP.replaceAll("attachmentId", attachmentId + "")
                .replaceAll("companyId", companyId + "").replaceAll("projectId", projectId + "");

        Uri uri = Uri.parse(newUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(attachmentName);
        request.setDestinationInExternalFilesDir(this, null, attachmentName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);
    }

    public List<User> getUsersByProjectId(int companyId, int projectId) throws AppException {
        List<User> users = new ArrayList<User>();
        users = ApiClient.getUsersByProjectId(this, companyId, projectId);
        return users;
    }

    public User getUserById(int userId) throws AppException {
        User user = ApiClient.getUserById(this, userId);
        return user;
    }

    public Comment publishComment(Comment comment) throws AppException {
        return ApiClient.publishComment(this, comment);
    }

    public Todo createTodo(Todo todo) throws AppException {
        return ApiClient.createTodo(this, todo);
    }

    public Upload createUpload(Upload upload, File file) throws AppException {
        return ApiClient.createUpload(this, upload, file);
    }

    public Todo updateTodo(Todo todo) throws AppException {
        return ApiClient.updateTodo(this, todo);
    }

    public Todolist createTodolist(Todolist todolist) throws AppException {
        return ApiClient.createTodolist(this, todolist);
    }

    public Todolist updateTodolist(Todolist todolist) throws AppException {
        return ApiClient.updateTodolist(this, todolist);
    }

    public Discussion createDiscussion(Discussion discussion) throws AppException {
        return ApiClient.createDiscussion(this, discussion);
    }


    public List<Todo> getCalendarTodos(int companyId, long startTime, long endTime) throws AppException {
        return ApiClient.getCalendarTodos(this, companyId, startTime, endTime);
    }

    public Todo getTodoById(int companyId, int projectId, int todoId) throws AppException {
        return ApiClient.getTodoById(this, companyId, projectId, todoId);
    }

    public Map<String, List<User>> getDepartmentNameUserMapByCompanyId(int companyId) throws AppException {
        Map<String, List<User>> departmentNameUserMap = ApiClient.getDepartmentNameUserMapByCompanyId(this, companyId);

        return departmentNameUserMap;
    }

    public int getLatestVersionCode() throws AppException {
        return ApiClient.getLatestVersionCode(this);
    }

}
