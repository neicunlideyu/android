package cn.onboard.android.app.bean;

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

    public final static String IMAGE = "image";

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
        if (attachmentType.contains("ppt")) {
            return R.drawable.powerpoint;
        }
        else if (attachmentType.contains("word")) {
            return R.drawable.word;
        }
        else if (attachmentType.contains("txt")) {
            return R.drawable.text;
        }
        else if (attachmentType.contains("excel")) {
            return R.drawable.excel;
        }
        else if (attachmentType.contains("pdf")) {
            return R.drawable.pdf;
        }
        else if (attachmentType.contains("css")) {
            return R.drawable.css;
        }
        else if (attachmentType.contains("ai")) {
            return R.drawable.illustrator;
        }
        else if (attachmentType.contains("zip")) {
            return R.drawable.keynote;
        }
        else if (attachmentType.contains("script")) {
            return R.drawable.html;
        }
        else if (attachmentType.contains("html")) {
            return R.drawable.html;
        }
        else {
            return R.drawable.folder;
        }

    }
}
