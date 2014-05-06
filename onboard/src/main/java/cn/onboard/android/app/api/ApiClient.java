package cn.onboard.android.app.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.codehaus.jackson.type.TypeReference;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.AppException;
import cn.onboard.android.app.bean.URLs;
import cn.onboard.android.app.common.DataHandleUtil;

/**
 * API客户端接口：用于访问网络数据
 *
 * @author xuchen
 * @version 1.0
 * @created 2012-3-21
 */
public class ApiClient {

    private static final String UTF_8 = "UTF-8";

    private final static int TIMEOUT_CONNECTION = 20000;
    private final static int TIMEOUT_SOCKET = 20000;
    private final static int RETRY_TIME = 3;

    private static String appCookie;
    private static String appUserAgent;

    public static void cleanCookie() {
        appCookie = "";
    }

    public static String getCookie(AppContext appContext) {
        if (appCookie == null || appCookie == "") {
            appCookie = appContext.getProperty("cookie");
        }
        return appCookie;
    }

    private static String getUserAgent(AppContext appContext) {
        if (appUserAgent == null || appUserAgent == "") {
            StringBuilder ua = new StringBuilder("Onboard");
            ua.append('/' + appContext.getPackageInfo().versionName + '_'
                    + appContext.getPackageInfo().versionCode);// App版本
            ua.append("/Android");// 手机系统平台
            ua.append("/" + android.os.Build.VERSION.RELEASE);// 手机系统版本
            ua.append("/" + android.os.Build.MODEL); // 手机型号
            ua.append("/" + appContext.getAppId());// 客户端唯一标识
            appUserAgent = ua.toString();
        }
        return appUserAgent;
    }

    private static HttpClient getHttpClient() {
        HttpClient httpClient = new HttpClient();
        // 设置 HttpClient 接收 Cookie,用与浏览器一样的策略
        httpClient.getParams().setCookiePolicy(
                CookiePolicy.BROWSER_COMPATIBILITY);
        // 设置 默认的超时重试处理策略
        httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler());
        // 设置 连接超时时间
        httpClient.getHttpConnectionManager().getParams()
                .setConnectionTimeout(TIMEOUT_CONNECTION);
        // 设置 读数据超时时间
        httpClient.getHttpConnectionManager().getParams()
                .setSoTimeout(TIMEOUT_SOCKET);
        // 设置 字符集
        httpClient.getParams().setContentCharset(UTF_8);
        return httpClient;
    }

    private static GetMethod getHttpGet(String url, String cookie,
                                        String userAgent) {

        GetMethod httpGet = new GetMethod(url);
        // 设置 请求超时时间
        httpGet.getParams().setSoTimeout(TIMEOUT_SOCKET);
        httpGet.setRequestHeader("Host", URLs.HOST);
        httpGet.setRequestHeader("Connection", "Keep-Alive");
        httpGet.setRequestHeader("Cookie", cookie);
        httpGet.setRequestHeader("User-Agent", userAgent);
        return httpGet;
    }

    private static PostMethod getHttpPost(String url, String cookie,
                                          String userAgent) {
        PostMethod httpPost = new PostMethod(url);
        // 设置 请求超时时间
        httpPost.getParams().setSoTimeout(TIMEOUT_SOCKET);
        httpPost.setRequestHeader("Host", URLs.HOST);
        httpPost.setRequestHeader("Connection", "Keep-Alive");
        httpPost.setRequestHeader("Cookie", cookie);
        httpPost.setRequestHeader("User-Agent", userAgent);
        return httpPost;
    }


    /**
     * @param appContext
     * @param url
     * @return
     * @throws AppException
     */
    private static InputStream http_get(AppContext appContext, String url)
            throws AppException {
        // System.out.println("get_url==> "+url);
        String cookie = getCookie(appContext);
        String userAgent = getUserAgent(appContext);

        HttpClient httpClient = null;
        GetMethod httpGet = null;

        String responseBody = "";
        int time = 0;
        int statusCode;
        do {
            try {
                httpClient = getHttpClient();
                httpGet = getHttpGet(url, cookie, userAgent);
                statusCode = httpClient.executeMethod(httpGet);
                if (statusCode != HttpStatus.SC_OK) {
                    throw AppException.http(statusCode);
                }
                responseBody = httpGet.getResponseBodyAsString();
                // System.out.println("XMLDATA=====>"+responseBody);
                break;
            } catch (HttpException e) {
                time++;
                if (time < RETRY_TIME) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }
                    continue;
                }
                // 发生致命的异常，可能是协议不对或者返回的内容有问题
                e.printStackTrace();
                throw AppException.http(e);
            } catch (IOException e) {
                time++;
                if (time < RETRY_TIME) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }
                    continue;
                }
                // 发生网络异常
                e.printStackTrace();
                throw AppException.network(e);
            } finally {
                // 释放连接
                httpGet.releaseConnection();
                httpClient = null;
            }
        } while (time < RETRY_TIME);

        responseBody = responseBody.replaceAll("\\p{Cntrl}", "");
        return new ByteArrayInputStream(responseBody.getBytes());
    }

    /**
     * 公用post方法
     *
     * @param url
     * @param params
     * @param files
     * @throws AppException
     */
    private static InputStream _post(AppContext appContext, String url,
                                     Map<String, Object> params, Map<String, File> files)
            throws AppException {
        String cookie = getCookie(appContext);
        String userAgent = getUserAgent(appContext);

        HttpClient httpClient = null;
        PostMethod httpPost = null;

        // post表单参数处理
        int length = (files == null ? 0 : files.size());
        Part[] parts = new Part[length];
        int i = 0;
        if (files != null)
            for (String file : files.keySet()) {
                try {
                    parts[i++] = new FilePart(file, files.get(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                // System.out.println("post_key_file==> "+file);
            }

        String responseBody = "";
        int time = 0;
        do {
            try {
                httpClient = getHttpClient();
                //认证
                // httpClient.getParams().setAuthenticationPreemptive(true);
                // httpClient.getState().setCredentials(AuthScope.ANY, new
                // UsernamePasswordCredentials("xuchen109@gmail.com",
                // "12345678"));
                httpPost = getHttpPost(url, cookie, userAgent);
                for (String name : params.keySet()) {
                    httpPost.setParameter(name,
                            String.valueOf(params.get(name)));
                }
                if (length != 0) {
                    httpPost.setRequestEntity(new
                            MultipartRequestEntity(parts, httpPost.getParams()));
                }
                int statusCode = httpClient.executeMethod(httpPost);
                if (statusCode != HttpStatus.SC_OK) {
                    throw AppException.http(statusCode);
                } else if (statusCode == HttpStatus.SC_OK) {
                    Cookie[] cookies = httpClient.getState().getCookies();
                    String tmpcookies = "";
                    for (Cookie ck : cookies) {
                        tmpcookies += ck.toString() + ";";
                    }
                    // 保存cookie
                    if (appContext != null && tmpcookies != "") {
                        appContext.setProperty("cookie", tmpcookies);
                        appCookie = tmpcookies;
                    }
                }
                responseBody = httpPost.getResponseBodyAsString();
                // System.out.println("XMLDATA=====>"+responseBody);
                break;
            } catch (HttpException e) {
                time++;
                if (time < RETRY_TIME) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }
                    continue;
                }
                // 发生致命的异常，可能是协议不对或者返回的内容有问题
                e.printStackTrace();
                throw AppException.http(e);
            } catch (IOException e) {
                time++;
                if (time < RETRY_TIME) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }
                    continue;
                }
                // 发生网络异常
                e.printStackTrace();
                throw AppException.network(e);
            } finally {
                // 释放连接
                httpPost.releaseConnection();
                httpClient = null;
            }
        } while (time < RETRY_TIME);

        responseBody = responseBody.replaceAll("\\p{Cntrl}", "");
        return new ByteArrayInputStream(responseBody.getBytes());
    }


    /**
     * 获取网络图片
     *
     * @param url
     * @return
     */
    public static Bitmap getNetBitmap(String url) throws AppException {
        // System.out.println("image_url==> "+url);
        URI uri = null;
        try {
            uri = new URI(url, false, "UTF-8");
        } catch (URIException e) {
            e.printStackTrace();
        }
        if (uri != null)
            url = uri.toString();
        HttpClient httpClient = null;
        GetMethod httpGet = null;
        Bitmap bitmap = null;
        int time = 0;
        do {
            try {
                httpClient = getHttpClient();
                httpGet = getHttpGet(url, null, null);
                int statusCode = httpClient.executeMethod(httpGet);
                if (statusCode != HttpStatus.SC_OK) {
                    throw AppException.http(statusCode);
                }
                InputStream inStream = httpGet.getResponseBodyAsStream();
                bitmap = BitmapFactory.decodeStream(inStream);
                inStream.close();
                break;
            } catch (HttpException e) {
                time++;
                if (time < RETRY_TIME) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }
                    continue;
                }
                // 发生致命的异常，可能是协议不对或者返回的内容有问题
                e.printStackTrace();
                throw AppException.http(e);
            } catch (IOException e) {
                time++;
                if (time < RETRY_TIME) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                    }
                    continue;
                }
                // 发生网络异常
                e.printStackTrace();
                throw AppException.network(e);
            } finally {
                // 释放连接
                httpGet.releaseConnection();
                httpClient = null;
            }
        } while (time < RETRY_TIME);
        return bitmap;
    }


    public static User login(AppContext appContext, String username, String pwd)
            throws AppException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("email", username);
        params.put("password", pwd);
        params.put("remember", 1);

        String loginurl = URLs.LOGIN_VALIDATE_HTTP;
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<User>() {
                    }, _post(appContext, loginurl, params, null));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static User logout(AppContext appContext) throws AppException{
        String url = URLs.LOGOUT;
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<User>() {
                    }, http_get(appContext, url));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }

    }

    /**
     * 获得公司列表
     *
     * @return
     * @throws AppException
     */
    public static List<Company> getCompanyList(AppContext appContext)
            throws AppException {
        String newUrl = URLs.COMPANY_LIST_HTTP;
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<List<Company>>() {
                    }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static List<Project> getProjectByCompanyId(AppContext appContext,
                                                      int companyId) throws AppException {
        String newUrl = URLs.PROJECT_LIST_HTTP.replaceAll("companyId",
                companyId + "");
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<List<Project>>() {
                    }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }

    }

    public static List<Todolist> getTodoListByProjectId(AppContext appContext,
                                                        Integer companyId, Integer projectId) throws AppException {
        String url = URLs.TODOLIST_LIST_BY_PROJECT_HTTP.replaceAll("companyId",
                companyId + "").replaceAll("projectId", projectId + "");
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<List<Todolist>>() {
                    }, http_get(appContext, url));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }

    }

    public static List<Todolist> getTodoListByCompanyIdByUserId(AppContext appContext,
                                                                Integer companyId, Integer userId) throws AppException {
        String url = URLs.TODOLIST_LIST_BY_USER_HTTP.replaceAll("companyId",
                companyId + "").replaceAll("userId", userId + "");
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<List<Todolist>>() {
                    }, http_get(appContext, url));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }

    }

    public static List<Todolist> getTodoListByCompanyIdByDate(AppContext appContext,
                                                              Integer companyId, Date date) throws AppException {
        String url = URLs.TODOLIST_LIST_BY_DATE_HTTP.replaceAll("companyId",
                companyId + "").replaceAll("duedatetime", date.getTime() + "");
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<List<Todolist>>() {
                    }, http_get(appContext, url));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }

    }

    public static List<Comment> getCommentsByCommentable(AppContext appContext, int companyId, int projectId, String attachType, int attachId) throws AppException {
        String url = URLs.COMMENT_HTTP.replaceAll("companyId", companyId + "").replaceAll("projectId", projectId + "").replaceAll("attachType", attachType).replaceAll("attachId", attachId + "");
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<List<Comment>>() {
                    }, http_get(appContext, url));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static List<Topic> getTopicsByProjectId(AppContext appContext,
                                                   int companyId, int projectId, int page) throws AppException {
        String newUrl = URLs.TOPIC_LIST_HTTP.replaceAll("companyId",
                companyId + "").replaceAll("projectId", projectId + "") + "?page=" + page;
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<List<Topic>>() {
                    }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }

    }

    public static List<Document> getDocumentsByProjectId(AppContext appContext,
                                                         int companyId, int projectId) throws AppException {
        String newUrl = URLs.DOCUMENT_LIST_HTTP.replaceAll("companyId",
                companyId + "").replaceAll("projectId", projectId + "");
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<List<Document>>() {
                    }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static Discussion getDiscussionById(AppContext appContext,
                                               int companyId, int projectId, int discussionId) throws AppException {
        String newUrl = URLs.DISCUSSION_HTTP_STRING
                .replaceAll("companyId", companyId + "")
                .replaceAll("projectId", projectId + "")
                .replaceAll("discussionId", discussionId + "");
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<Discussion>() {
                    }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }

    }

    public static Document getDocumentById(AppContext appContext,
                                           int companyId, int projectId, int documentId) throws AppException {
        String newUrl = URLs.DOCUMENT_HTTP_STRING
                .replaceAll("companyId", companyId + "")
                .replaceAll("projectId", projectId + "")
                .replaceAll("documentId", documentId + "");
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<Document>() {
                    }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }

    }

    public static List<Activity> getActivitiesByCompanyId(
            AppContext appContext, int companyId, int page)
            throws AppException {
        String url = URLs.ACTIVITY_BY_COMPANY_LIST_HTTP.replaceAll("companyId",
                companyId + "") + "?page=" + page;
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<List<Activity>>() {
                    }, http_get(appContext, url));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static List<Activity> getActivitiesByCompanyIdByUserId(
            AppContext appContext, int companyId, int userId, int page)
            throws AppException {
        String url = URLs.ACTIVITY_BY_USER_LIST_HTTP.replaceAll("companyId", companyId + "").replaceAll("userId",
                userId + "") + "?page=" + page;
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<List<Activity>>() {
                    }, http_get(appContext, url));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static Upload getUploadById(AppContext appContext, int companyId, int projectId, int uploadId) throws AppException {
        String newUrl = URLs.UPLOAD_HTTP.replaceAll("companyId", companyId + "").replaceAll("projectId", projectId + "")
                .replaceAll("uploadId", uploadId + "");
        try {
            return DataHandleUtil.inputStreamToObject(new TypeReference<Upload>() {
            }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static Todolist getTodolistById(AppContext appContext, int companyId, int projectId, int todolistId) throws AppException {
        String newUrl = URLs.TODOLIST_LIST_BY_TODOLISTID_HTTP.replaceAll("companyId", companyId + "").replaceAll("projectId", projectId + "")
                .replaceAll("todolistId", todolistId + "");
        try {
            return DataHandleUtil.inputStreamToObject(new TypeReference<Todolist>() {
            }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static List<Attachment> getAttachmentsByProjectId(AppContext appContext,
                                                             int companyId, int projectId, int page) throws AppException {
        String newUrl = URLs.ATTACHMENT_LIST_BY_PROJECT_HTTP.replaceAll("companyId",
                companyId + "").replaceAll("projectId", projectId + "") + "?page=" + page;
        ;
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<List<Attachment>>() {
                    }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static List<Attachment> getAttachmentsByCompanyIdByUserId(AppContext appContext,
                                                                     int companyId, int userId, int page) throws AppException {
        String newUrl = URLs.ATTACHMENT_LIST_BY_USER_HTTP.replaceAll("companyId",
                companyId + "").replaceAll("userId", userId + "") + "?page=" + page;
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<List<Attachment>>() {
                    }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static List<User> getUsersByProjectId(AppContext appContext,
                                                 int companyId, int projectId) throws AppException {
        String newUrl = URLs.USERS__LIST_HTTP.replaceAll("companyId",
                companyId + "").replaceAll("projectId", projectId + "");
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<List<User>>() {
                    }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static User getUserById(AppContext appContext, int userId) throws AppException {
        String newUrl = URLs.USER_HTTP.replaceAll("userId", userId + "");
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<User>() {
                    }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static Map<String, List<User>> getDepartmentNameUserMapByCompanyId(AppContext appContext, int companyId) throws AppException {
        String newUrl = URLs.DEPARTMENT_NAME_USER_MAP_HTTP.replaceAll("companyId", companyId + "");
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<Map<String, List<User>>>() {
                    }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static Comment publishComment(AppContext appContext, Comment comment)
            throws AppException {
        String url = URLs.COMMENT_HTTP.replaceAll("companyId", comment.getCompanyId() + "").replaceAll("projectId", comment.getProjectId() + "").replaceAll("attachType", comment.getAttachType()).replaceAll("attachId", comment.getAttachId() + "");
        Map<String, Object> params = DataHandleUtil.objectToMap(comment);
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<Comment>() {
                    }, _post(appContext, url, params, null));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static Todo createTodo(AppContext appContext, Todo todo) throws AppException {
        String url = URLs.TODO_HTTP.replaceAll("companyId", todo.getCompanyId() + "").replaceAll("projectId", todo.getProjectId() + "");
        Map<String, Object> params = DataHandleUtil.objectToMap(todo);
        Object dueTime = params.get("dueDate");
        params.remove("dueDate");
        params.put("date", dueTime);
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<Todo>() {
                    }, _post(appContext, url, params, null));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }

    }


    public static Upload createUpload(AppContext appContext, Upload upload, File file) throws AppException {
        String url = URLs.UPLOAD_LIST_BY_PROJECT_HTTP.replaceAll("companyId", upload.getCompanyId() + "").replaceAll("projectId", upload.getProjectId() + "");
        Map<String, Object> params = DataHandleUtil.objectToMap(upload);
        Map<String, File> files = new HashMap<String, File>();
        files.put("file", file);

        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<Upload>() {
                    }, _post(appContext, url, params, files));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }

    }

    public static Todo updateTodo(AppContext appContext, Todo todo) throws AppException {
        String url = URLs.TODO_HTTP.replaceAll("companyId", todo.getCompanyId() + "").replaceAll("projectId", todo.getProjectId() + "") + "/" + todo.getId();
        Map<String, Object> params = DataHandleUtil.objectToMap(todo);
        Object dueTime = params.get("dueDate");
        params.remove("dueDate");
        params.put("date", dueTime);
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<Todo>() {
                    }, _post(appContext, url, params, null));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static Todolist updateTodolist(AppContext appContext, Todolist todolist) throws AppException {
        String url = URLs.TODOLIST_UPDATE_HTTP.replaceAll("companyId", todolist.getCompanyId() + "").replaceAll("projectId", todolist.getProjectId() + "")
                .replaceAll("todolistId", todolist.getId() + "");

        Map<String, Object> params = DataHandleUtil.objectToMap(todolist);
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<Todolist>() {
                    }, _post(appContext, url, params, null));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static Todolist createTodolist(AppContext appContext, Todolist todolist)
            throws AppException {
        String url = URLs.TODOLIST_LIST_BY_PROJECT_HTTP.replaceAll("companyId", todolist.getCompanyId() + "").replaceAll("projectId", todolist.getProjectId() + "");
        Map<String, Object> params = DataHandleUtil.objectToMap(todolist);
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<Todolist>() {
                    }, _post(appContext, url, params, null));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static Discussion createDiscussion(AppContext appContext, Discussion discussion)
            throws AppException {
        String url = URLs.DISCUSSION_LIST_HTTP.replaceAll("companyId", discussion.getCompanyId() + "").replaceAll("projectId", discussion.getProjectId() + "");
        List<User> subscribers = discussion.getSubscribers();
        discussion.setSubscribers(null);
        Map<String, Object> params = DataHandleUtil.objectToMap(discussion);
        StringBuilder builder = new StringBuilder();
        if (subscribers != null) {
            for (User user : subscribers) {
                builder.append(user.getId());
                builder.append(',');
            }
        }

        params.put("subscriberIds", builder.toString());
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<Discussion>() {
                    }, _post(appContext, url, params, null));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static Document createDocument(AppContext appContext, Document document)
            throws AppException {
        String url = URLs.DOCUMENT_LIST_HTTP.replaceAll("companyId", document.getCompanyId() + "").replaceAll("projectId", document.getProjectId() + "");
        Map<String, Object> params = DataHandleUtil.objectToMap(document);
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<Document>() {
                    }, _post(appContext, url, params, null));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static List<Todo> getCalendarTodos(AppContext appContext, int companyId, long startTime, long endTime) throws AppException {
        String newUrl = URLs.CALENDAR_TODO_HTTP.replaceAll("companyId",
                companyId + "").replaceAll("startTime", startTime + "").replaceAll("endTime", endTime + "");
        try {
            return DataHandleUtil.inputStreamToObject(
                    new TypeReference<List<Todo>>() {
                    }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }

    }

    public static Todo getTodoById(AppContext appContext, int companyId, int projectId, int todoId) throws AppException {
        String newUrl = URLs.TODO_HTTP.replaceAll("companyId", companyId + "").replaceAll("projectId", projectId + "") + "/" + todoId;
        try {
            return DataHandleUtil.inputStreamToObject(new TypeReference<Todo>() {
            }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }

    public static Integer getLatestVersionCode(AppContext appContext) throws AppException {
        String newUrl = URLs.VERSION_HTTP;
        try {
            return DataHandleUtil.inputStreamToObject(new TypeReference<Integer>() {
            }, http_get(appContext, newUrl));
        } catch (Exception e) {
            if (e instanceof AppException)
                throw (AppException) e;
            throw AppException.network(e);
        }
    }
}
