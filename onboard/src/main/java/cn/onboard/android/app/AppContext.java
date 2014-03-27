package cn.onboard.android.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import cn.onboard.android.app.api.ApiClient;
import cn.onboard.android.app.bean.URLs;
import cn.onboard.android.app.common.StringUtils;

/**
 * 全局应用程序类：用于保存和调用全局应用配置及访问网络数据
 *
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
@SuppressWarnings("deprecation")
@SuppressLint("DefaultLocale")
public class AppContext extends Application {

    public static final int NETTYPE_WIFI = 0x01;
    public static final int NETTYPE_CMWAP = 0x02;
    public static final int NETTYPE_CMNET = 0x03;

    public static final int PAGE_SIZE = 20;// 默认分页大小
    private static final int CACHE_TIME = 60 * 60000;// 缓存失效时间

    private boolean login = false; // 登录状态
    private int userId = 0; // 登录用户的id
    private Hashtable<String, Object> memCacheRegion = new Hashtable<String, Object>();

    /**
     * 判断当前版本是否兼容目标版本的方法
     *
     * @param VersionCode
     * @return
     */
    public static boolean isMethodsCompat(int VersionCode) {
        int currentVersion = android.os.Build.VERSION.SDK_INT;
        return currentVersion >= VersionCode;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 注册App异常崩溃处理器
        Thread.setDefaultUncaughtExceptionHandler(AppException
                .getAppExceptionHandler());
    }

    /**
     * 检测当前系统声音是否为正常模式
     *
     * @return
     */
    public boolean isAudioNormal() {
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
    }

    /**
     * 应用程序是否发出提示音
     *
     * @return
     */
    public boolean isAppSound() {
        return isAudioNormal() && isVoice();
    }

    /**
     * 检测网络是否可用
     *
     * @return
     */
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
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
            if (!StringUtils.isEmpty(extraInfo)) {
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
        if (StringUtils.isEmpty(uniqueID)) {
            uniqueID = UUID.randomUUID().toString();
            setProperty(AppConfig.CONF_APP_UNIQUEID, uniqueID);
        }
        return uniqueID;
    }

    /**
     * 用户是否登录
     *
     * @return
     */
    public boolean isLogin() {
        return login;
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
     * 用户注销
     */
    public void Logout() {
        ApiClient.cleanCookie();
        this.cleanCookie();
        this.login = false;
        this.userId = 0;
    }

    /**
     * 初始化用户登录信息
     */
    public void initLoginInfo() {
        User loginUser = getLoginInfo();
        if (loginUser != null && loginUser.getId() > 0) {
            this.userId = loginUser.getId();
            this.login = true;
        } else {
            this.Logout();
        }
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

    /**
     * 清除登录信息
     */
    public void cleanLoginInfo() {
        this.userId = 0;
        this.login = false;
        removeProperty("user.id", "user.name", "user.email", "user.password");
    }

    /**
     * 获取登录信息
     *
     * @return
     */
    public User getLoginInfo() {
        User user = new User();
        user.setId(StringUtils.toInt(getProperty("user.id"), 0));
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
        this.login = true;
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
     * 获取用户头像
     *
     * @param key
     * @return
     * @throws AppException
     */
    public Bitmap getUserFace(String key) throws AppException {
        FileInputStream fis = null;
        try {
            fis = openFileInput(key);
            return BitmapFactory.decodeStream(fis);
        } catch (Exception e) {
            throw AppException.run(e);
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 是否加载显示文章图片
     *
     * @return
     */
    public boolean isLoadImage() {
        String perf_loadimage = getProperty(AppConfig.CONF_LOAD_IMAGE);
        // 默认是加载的
        if (StringUtils.isEmpty(perf_loadimage))
            return true;
        else
            return StringUtils.toBool(perf_loadimage);
    }

    /**
     * 设置是否加载文章图片
     *
     * @param b
     */
    public void setConfigLoadimage(boolean b) {
        setProperty(AppConfig.CONF_LOAD_IMAGE, String.valueOf(b));
    }

    /**
     * 是否发出提示音
     *
     * @return
     */
    public boolean isVoice() {
        String perf_voice = getProperty(AppConfig.CONF_VOICE);
        // 默认是开启提示声音
        if (StringUtils.isEmpty(perf_voice))
            return true;
        else
            return StringUtils.toBool(perf_voice);
    }

    /**
     * 设置是否发出提示音
     *
     * @param b
     */
    public void setConfigVoice(boolean b) {
        setProperty(AppConfig.CONF_VOICE, String.valueOf(b));
    }

    /**
     * 是否启动检查更新
     *
     * @return
     */
    public boolean isCheckUp() {
        String perf_checkup = getProperty(AppConfig.CONF_CHECKUP);
        // 默认是开启
        if (StringUtils.isEmpty(perf_checkup))
            return true;
        else
            return StringUtils.toBool(perf_checkup);
    }

    /**
     * 设置启动检查更新
     *
     * @param b
     */
    public void setConfigCheckUp(boolean b) {
        setProperty(AppConfig.CONF_CHECKUP, String.valueOf(b));
    }

    /**
     * 是否左右滑动
     *
     * @return
     */
    public boolean isScroll() {
        String perf_scroll = getProperty(AppConfig.CONF_SCROLL);
        // 默认是关闭左右滑动
        if (StringUtils.isEmpty(perf_scroll))
            return false;
        else
            return StringUtils.toBool(perf_scroll);
    }

    /**
     * 设置是否左右滑动
     *
     * @param b
     */
    public void setConfigScroll(boolean b) {
        setProperty(AppConfig.CONF_SCROLL, String.valueOf(b));
    }

    /**
     * 是否Https登录
     *
     * @return
     */
    public boolean isHttpsLogin() {
        String perf_httpslogin = getProperty(AppConfig.CONF_HTTPS_LOGIN);
        // 默认是http
        if (StringUtils.isEmpty(perf_httpslogin))
            return false;
        else
            return StringUtils.toBool(perf_httpslogin);
    }

    /**
     * 设置是是否Https登录
     *
     * @param b
     */
    public void setConfigHttpsLogin(boolean b) {
        setProperty(AppConfig.CONF_HTTPS_LOGIN, String.valueOf(b));
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

    /**
     * 判断缓存是否失效
     *
     * @param cachefile
     * @return
     */
    public boolean isCacheDataFailure(String cachefile) {
        boolean failure = false;
        File data = getFileStreamPath(cachefile);
        if (data.exists()
                && (System.currentTimeMillis() - data.lastModified()) > CACHE_TIME)
            failure = true;
        else if (!data.exists())
            failure = true;
        return failure;
    }

    /**
     * 清除app缓存
     */
//	public void clearAppCache() {
//		// 清除webview缓存
//		File file = CacheManager.getCacheFileBaseDir();
//		if (file != null && file.exists() && file.isDirectory()) {
//			for (File item : file.listFiles()) {
//				item.delete();
//			}
//			file.delete();
//		}
//		deleteDatabase("webview.db");
//		deleteDatabase("webview.db-shm");
//		deleteDatabase("webview.db-wal");
//		deleteDatabase("webviewCache.db");
//		deleteDatabase("webviewCache.db-shm");
//		deleteDatabase("webviewCache.db-wal");
//		// 清除数据缓存
//		clearCacheFolder(getFilesDir(), System.currentTimeMillis());
//		clearCacheFolder(getCacheDir(), System.currentTimeMillis());
//		// 2.2版本才有将应用缓存转移到sd卡的功能
//		if (isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)) {
//			clearCacheFolder(MethodsCompat.getExternalCacheDir(this),
//					System.currentTimeMillis());
//		}
//		// 清除编辑器保存的临时内容
//		Properties props = getProperties();
//		for (Object key : props.keySet()) {
//			String _key = key.toString();
//			if (_key.startsWith("temp"))
//				removeProperty(_key);
//		}
//	}

    /**
     * 清除缓存目录
     *
     * @param dir
     * @param curTime
     * @return
     */
    private int clearCacheFolder(File dir, long curTime) {
        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, curTime);
                    }
                    if (child.lastModified() < curTime) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return deletedFiles;
    }

    /**
     * 将对象保存到内存缓存中
     *
     * @param key
     * @param value
     */
    public void setMemCache(String key, Object value) {
        memCacheRegion.put(key, value);
    }

    /**
     * 从内存缓存中获取对象
     *
     * @param key
     * @return
     */
    public Object getMemCache(String key) {
        return memCacheRegion.get(key);
    }

    /**
     * 保存磁盘缓存
     *
     * @param key
     * @param value
     * @throws IOException
     */
    public void setDiskCache(String key, String value) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput("cache_" + key + ".data", Context.MODE_PRIVATE);
            fos.write(value.getBytes());
            fos.flush();
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 获取磁盘缓存数据
     *
     * @param key
     * @return
     * @throws IOException
     */
    public String getDiskCache(String key) throws IOException {
        FileInputStream fis = null;
        try {
            fis = openFileInput("cache_" + key + ".data");
            byte[] datas = new byte[fis.available()];
            fis.read(datas);
            return new String(datas);
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 保存对象
     *
     * @param ser
     * @param file
     * @throws IOException
     */
    public boolean saveObject(Serializable ser, String file) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = openFileOutput(file, MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ser);
            oos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                oos.close();
            } catch (Exception e) {
            }
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 读取对象
     *
     * @param file
     * @return
     * @throws IOException
     */
    public Serializable readObject(String file) {
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

    public boolean containsProperty(String key) {
        Properties props = getProperties();
        return props.containsKey(key);
    }

    public Properties getProperties() {
        return AppConfig.getAppConfig(this).get();
    }

    public void setProperties(Properties ps) {
        AppConfig.getAppConfig(this).set(ps);
    }

    public void setProperty(String key, String value) {
        AppConfig.getAppConfig(this).set(key, value);
    }

    public String getProperty(String key) {
        return AppConfig.getAppConfig(this).get(key);
    }

    public void removeProperty(String... key) {
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

    public List<Topic> getTopicsByProjectId(int companyId, int projectId)
            throws AppException {
        List<Topic> topicList = new ArrayList<Topic>();
        topicList = ApiClient.getTopicsByProjectId(this, companyId, projectId);
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

    public List<Discussion> getDiscussionsByProjectId(int companyId,
                                                      int projectId) throws AppException {
        List<Discussion> discussionList = new ArrayList<Discussion>();
        discussionList = ApiClient.getDiscussionsByProjectId(this, companyId,
                projectId);
        return discussionList;

    }

    public List<Activity> getActivitiesByCompanyId(int companyId, int page) throws AppException {
        List<Activity> activities = new ArrayList<Activity>();
        activities = ApiClient.getActivitiesByCompanyId(this, companyId, page);
        return activities;
    }

    public List<Activity> getActivitiesByCompanyIdByUserId(int companyId,int userId, int page) throws AppException {
        List<Activity> activities = new ArrayList<Activity>();
        activities = ApiClient.getActivitiesByCompanyIdByUserId(this, companyId, userId, page);
        return activities;
    }



    public List<Upload> getUploadsByProjectId(int companyId, int projectId)
            throws AppException {
        List<Upload> uploadList = new ArrayList<Upload>();
        uploadList = ApiClient.getUploadByProjectId(this, companyId, projectId);
        return uploadList;

    }

    public Upload getUploadById(int companyId, int projectId, int uploadId) throws AppException {
        return ApiClient.getUploadById(this, companyId, projectId, uploadId);
    }

    public Todolist getTodolistById(int companyId, int projectId, int todolistId) throws AppException {
        return ApiClient.getTodolistById(this, companyId, projectId, todolistId);
    }

    public List<Attachment> getAttachmentsByProjectId(int companyId,
                                                      int projectId) throws AppException {
        List<Attachment> attachments = new ArrayList<Attachment>();
        attachments = ApiClient.getAttachmentsByProjectId(this, companyId, projectId);
        return attachments;

    }

    public List<Attachment> getAttachmentsByCompanyIdByUserId(int companyId,
                                                              int userId) throws AppException {
        List<Attachment> attachments = new ArrayList<Attachment>();
        attachments = ApiClient.getAttachmentsByCompanyIdByUserId(this, companyId, userId);
        return attachments;

    }

    public void downloadAttachmentByAttachmentId(int attachmentId, int companyId, int projectId) {
        DownloadManager downloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
        String newUrl = URLs.ATTACHMENT_DOWNLOWD_HTTP.replaceAll("attachmentId", attachmentId + "")
                .replaceAll("companyId", companyId + "").replaceAll("projectId", projectId + "");

        Uri uri = Uri.parse(newUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("onboard附件");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);
    }

    public List<User> getUsersByProjectId(int companyId, int projectId) throws AppException {
        List<User> users = new ArrayList<User>();
        users = ApiClient.getUsersByProjectId(this, companyId, projectId);
        return users;
    }

    public Comment publishComment(Comment comment) throws AppException {
        return ApiClient.publishComment(this, comment);
    }

    public Todo createTodo(Todo todo) throws  AppException{
        return ApiClient.createTodo(this,todo);
    }

    public Todo updateTodo(Todo todo) throws  AppException{
        return ApiClient.updateTodo(this,todo);
    }

    public Todolist createTodolist(Todolist todolist) throws  AppException {
        return ApiClient.createTodolist(this, todolist);
    }
    public Discussion createDiscussion(Discussion discussion) throws AppException {
        return ApiClient.createDiscussion(this, discussion);
    }

    public Document createDocument(Document document) throws AppException {
        return ApiClient.createDocument(this, document);
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

}
