package cn.onboard.android.app.core.attachment;

import com.onboard.api.dto.Attachment;

import java.util.HashMap;
import java.util.Map;

import cn.onboard.android.app.R;

/**
 * Created by xingliang on 14-3-27.
 */
public class AttachmentIconType {
    private static final Map<String, String> SUFFIX_TYPE_MAP = new HashMap<String, String>() {

        {
            put("ppt", "ppt");
            put("pptx", "ppt");
            put("doc", "word");
            put("docx", "word");
            put("txt", "txt");
            put("excel", "excel");
            put("pdf", "pdf");
            put("css", "css");
            put("ai", "ai");
            put("zip", "zip");
            put("jar", "zip");
            put("rar", "zip");
            put("bat", "script");
            put("py", "script");
            put("sh", "script");
            put("html", "html");
            put("xml", "html");
        }
    };

    private static final Map<String, Integer> TYPE_RESOURCE_MAP = new HashMap<String, Integer>(){
        {
            put("ppt", R.drawable.powerpoint);
            put("word", R.drawable.word);
            put("txt", R.drawable.text);
            put("excel", R.drawable.excel);
            put("pdf", R.drawable.pdf);
            put("css", R.drawable.css);
            put("ai", R.drawable.illustrator);
            put("zip", R.drawable.keynote);
            put("script", R.drawable.html);
            put("html", R.drawable.html);
        }
    };

    private final static String IMAGE = "image";

    private static final String DEFAULT = "default";

    private static String getAttachmentType(Attachment attachment) {
        String attachmentType = attachment.getName().substring(attachment.getName().lastIndexOf(".") + 1).toLowerCase();

        if (attachment.getContentType().startsWith("image")) {
            return IMAGE;
        }
        else if (SUFFIX_TYPE_MAP.containsKey(attachmentType)) {
            return SUFFIX_TYPE_MAP.get(attachmentType);
        }
        return DEFAULT;
    }

    public static int getAttachmentTypeIconResourceId(String attachmentName, String attachmentContentType) {
        Attachment attachment = new Attachment();
        attachment.setName(attachmentName);
        attachment.setContentType(attachmentContentType);

        String attachmentType = getAttachmentType(attachment);

        if (TYPE_RESOURCE_MAP.containsKey(attachmentType)) {
            return TYPE_RESOURCE_MAP.get(attachmentType);
        }
        else {
            return R.drawable.folder;
        }

    }
}
