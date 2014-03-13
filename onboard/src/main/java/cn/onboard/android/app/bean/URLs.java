package cn.onboard.android.app.bean;

import java.io.Serializable;

/**
 * 接口URL实体类
 */
public class URLs implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static String HOST = "192.168.100.37:8080";

    public final static String HTTP = "http://";

    public final static String HTTPS = "https://";

    private final static String URL_SPLITTER = "/";

    private final static String URL_API_HOST = HTTP + HOST + URL_SPLITTER;

    public final static String LOGIN_VALIDATE_HTTP = URL_API_HOST + "api/signin";
    public final static String COMPANY_LIST_HTTP = URL_API_HOST + "api";
    public final static String PROJECT_LIST_HTTP = URL_API_HOST + "api/company/companyId";
    public final static String TODOLIST_LIST_HTTP = URL_API_HOST + "api/company/companyId/project/projectId/todoLists";
    public final static String TOPIC_LIST_HTTP = URL_API_HOST + "api/company/companyId/project/projectId/topics";
    public final static String UPLOAD__LIST_HTTP = URL_API_HOST + "api/company/companyId/project/projectId/uploads";
    public final static String DOCUMENT__LIST_HTTP = URL_API_HOST + "api/company/companyId/project/projectId/documents";
    public final static String DISCUSSION__LIST_HTTP = URL_API_HOST + "api/company/companyId/project/projectId/discussions";
    public final static String DISCUSSION_HTTP_STRING = URL_API_HOST + "api/company/companyId/project/projectId/discussions/discussionId";
    public final static String REPOSITY__LIST_HTTP = URL_API_HOST + "api/company/companyId/project/projectId/repositories";


    public final static String UPDATE_VERSION = URL_API_HOST + "MobileAppVersion.xml";

}
