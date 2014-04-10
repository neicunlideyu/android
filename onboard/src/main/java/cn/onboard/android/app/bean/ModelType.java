package cn.onboard.android.app.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuchen on 14-4-9.
 */
public class ModelType {
    public static final String TODO = "todo";
    public static final String TODOLIST = "todolist";
    public static final String DISCUSSSION = "discussion";
    public static final String UPLOAD = "upload";
    public static final String DOCUMENT = "document";
    public static final String COMMENT = "comment";
    public static final String PROJECT = "project";
    public static final String ATTACHMENT = "attachment";
    public static final String PRIVILEGE = "privilege";
    public static final String EVENT = "event";
    public static final String NOTIFICATION = "notification";
    public static final String PULLREQUEST = "pull-request";
    public static final Map<Integer, String> FILTER_CATEGORY = new HashMap<Integer, String>() {
        private static final long serialVersionUID = 1L;

        {
            put(1, ModelType.TODO);
            put(2, ModelType.TODOLIST);
            put(3, ModelType.DISCUSSSION);
            put(4, ModelType.UPLOAD);
            put(5, ModelType.DOCUMENT);
            put(6, ModelType.COMMENT);
            put(7, ModelType.ATTACHMENT);
            put(8, ModelType.PROJECT);
            put(9, ModelType.EVENT);
        }
    };
}
