package cn.onboard.android.app.bean;

import java.io.Serializable;

/**
 * 接口URL实体类
 */
public class URLs implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public final static String HOST = "10.202.11.2:8080";
    
    public final static String HTTP = "http://";
    
    public final static String HTTPS = "https://";

    private final static String URL_SPLITTER = "/";

    public final static String URL_API_HOST = HTTP + HOST + URL_SPLITTER;

    public final static String LOGIN_VALIDATE_HTTP = URL_API_HOST + "api/signin";
    public final static String COMPANY_LIST_HTTP = URL_API_HOST + "api";
    public final static String PROJECT_LIST_HTTP = URL_API_HOST + "api/company/companyId";
    public final static String TODOLIST_LIST_HTTP = URL_API_HOST + "api/company/companyId/project/projectId/todoLists";
    public final static String TOPIC_LIST_HTTP = URL_API_HOST + "api/company/companyId/project/projectId/topics";
    public final static String UPLOAD__LIST_HTTP = URL_API_HOST + "api/company/companyId/project/projectId/uploads";
    public final static String UPLOAD_HTTP = URL_API_HOST + "api/company/companyId/project/projectId/uploads/uploadId";
    public final static String USERS__LIST_HTTP = URL_API_HOST + "api/company/companyId/project/projectId/users";
    public final static String ATTACHMENT_LIST_HTTP = URL_API_HOST + "api/companyId/projects/projectId/attachments";
    public final static String ATTACHMENT_COMMENT_COUNT_HTTP = URL_API_HOST
            + "api/companyId/projects/projectId/attachments/comment/count/attachmentId";
    public final static String ATTACHMENT_DOWNLOWD_HTTP = URL_API_HOST
            + "api/companyId/projects/projectId/attachments/attachmentId/download";
    public final static String DOCUMENT__LIST_HTTP = URL_API_HOST + "api/company/companyId/project/projectId/documents";
    public final static String TODOLIST__LIST_HTTP = URL_API_HOST + "api/company/companyId/project/projectId/todolists";
    public final static String DISCUSSION__LIST_HTTP = URL_API_HOST + "api/company/companyId/project/projectId/discussions";
    public final static String ACTIVITY__LIST_HTTP = URL_API_HOST + "api/company/companyId/project/projectId/activities";
    public final static String DISCUSSION_HTTP_STRING = URL_API_HOST + "api/company/companyId/project/projectId/discussions/discussionId";
    public final static String DOCUMENT_HTTP_STRING = URL_API_HOST + "api/company/companyId/project/projectId/documents/documentId";
    public final static String REPOSITY__LIST_HTTP = URL_API_HOST + "api/company/companyId/project/projectId/repositories";
    public final static String PUBLISH_COMMENT_HTTP = URLs.URL_API_HOST +"api/company/companyId/project/projectId/attachType/attachId/comments";
    public final static String CALENDAR_TODO_HTTP = URLs.URL_API_HOST+"api/company/companyId/todos/from/startTime/to/endTime";
    public final static String CALENDAR_EVENT_HTTP = URLs.URL_API_HOST+"api/company/companyId/events/from/startTime/to/endTime";    
    public final static String UPDATE_VERSION = URL_API_HOST+"MobileAppVersion.xml";
    public final static String TODO_HTTP = URL_API_HOST + "api/{companyId}/projects/{projectId}/todos/{todoId}";


}
