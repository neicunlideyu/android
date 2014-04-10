package cn.onboard.android.app.bean;

import java.io.Serializable;

/**
 * 接口URL实体类
 */
public class URLs implements Serializable {
    private static final long serialVersionUID = 1L;

    public final static String HOST = "192.168.100.31:8080";

//    public final static String HOST = "onboard.cn";

    public final static String IMAGE_HOST = "teamforge.b0.upaiyun.com";

    public final static String HTTP = "http://";

    public final static String HTTPS = "https://";

    private final static String URL_SPLITTER = "/";

    public final static String URL_API_HOST = HTTP + HOST + URL_SPLITTER;

    public final static String LOGIN_VALIDATE_HTTP = URL_API_HOST + "api/signin";
    public final static String COMPANY_LIST_HTTP = URL_API_HOST + "api";
    public final static String PROJECT_LIST_HTTP = URL_API_HOST + "api/companyId";

    public final static String TODOLIST_LIST_BY_PROJECT_HTTP = URL_API_HOST + "api/companyId/projects/projectId/todoLists";
    public final static String TODOLIST_LIST_BY_USER_HTTP = URL_API_HOST + "api/companyId/users/userId/todoLists";
    public final static String TODOLIST_LIST_BY_DATE_HTTP = URL_API_HOST + "api/companyId/duedate/duedatetime/todoLists";
    public final static String TODOLIST_UPDATE_HTTP = URL_API_HOST + "api/companyId/projects/projectId/todoLists/todolistId";

    public final static String TODOLIST_LIST_BY_TODOLISTID_HTTP = URL_API_HOST + "api/companyId/projects/projectId/todoLists/todolistId";

    public final static String UPLOAD_LIST_BY_PROJECT_HTTP = URL_API_HOST + "api/companyId/projects/projectId/uploads";
    public final static String UPLOAD_LIST_BY_USER_HTTP = URL_API_HOST + "api/companyId/users/userId/uploads";

    public final static String ACTIVITY_BY_COMPANY_LIST_HTTP = URL_API_HOST + "api/companyId/activities";
    public final static String ACTIVITY_BY_USER_LIST_HTTP = URL_API_HOST + "api/companyId/users/userId/activities";

    public final static String TOPIC_LIST_HTTP = URL_API_HOST + "api/companyId/projects/projectId/topics";
    public final static String UPLOAD_HTTP = URL_API_HOST + "api/companyId/projects/projectId/uploads/uploadId";
    public final static String USERS__LIST_HTTP = URL_API_HOST + "api/companyId/projects/projectId/users";
    public final static String USER_HTTP = URL_API_HOST + "api/users/userId";

    public final static String ATTACHMENT_LIST_BY_PROJECT_HTTP = URL_API_HOST + "api/companyId/projects/projectId/attachments";
    public final static String ATTACHMENT_LIST_BY_USER_HTTP = URL_API_HOST + "api/companyId/users/userId/attachments";
    public final static String ATTACHMENT_IMAGE_HTTP = URL_API_HOST + "api/companyId/projects/projectId/attachments/image/attachmentId";
    public final static String DEPARTMENT_NAME_USER_MAP_HTTP = URL_API_HOST + "api/company/companyId/users";

    public final static String ATTACHMENT_COMMENT_COUNT_HTTP = URL_API_HOST
            + "api/companyId/projects/projectId/attachments/comment/count/attachmentId";
    public final static String ATTACHMENT_DOWNLOWD_HTTP = URL_API_HOST
            + "api/companyId/projects/projectId/attachments/attachmentId/download";
    public final static String DOCUMENT_LIST_HTTP = URL_API_HOST + "api/companyId/projects/projectId/documents";
    public final static String DISCUSSION_LIST_HTTP = URL_API_HOST + "api/companyId/projects/projectId/discussions";
    public final static String DISCUSSION_HTTP_STRING = URL_API_HOST + "api/companyId/projects/projectId/discussions/discussionId";
    public final static String DOCUMENT_HTTP_STRING = URL_API_HOST + "api/companyId/projects/projectId/documents/documentId";
    public final static String REPOSITY__LIST_HTTP = URL_API_HOST + "api/companyId/projects/projectId/repositories";
    public final static String COMMENT_HTTP = URLs.URL_API_HOST + "api/companyId/projects/projectId/attachType/attachId/comments";
    public final static String CALENDAR_TODO_HTTP = URLs.URL_API_HOST + "api/companyId/todos/from/startTime/to/endTime";
    public final static String CALENDAR_EVENT_HTTP = URLs.URL_API_HOST + "api/companyId/events/from/startTime/to/endTime";
    public final static String UPDATE_VERSION = URL_API_HOST + "MobileAppVersion.xml";
    public final static String TODO_HTTP = URL_API_HOST + "api/companyId/projects/projectId/todos";

    public final static String USER_FACE_HTTP = HTTP + IMAGE_HOST;

}
