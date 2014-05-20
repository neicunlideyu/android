package cn.onboard.android.app.core.attachment;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;

import com.onboard.api.dto.Attachment;

import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.core.util.OnboardService;


/**
 * Created by XingLiang on 14-4-21.
 */
public class AttachmentService extends OnboardService {

    private final static String GET_ATTACHMENT_BY_PROJECTID_URI = "/%d/projects/%d/attachments?page=%d";

    private final static String GET_ATTACHMENT_BY_USERID_URI = "/%d/users/%d/attachments?page=%d";

    private final static String GET_ATTACHMENT_COMMENT_COUNT_BY_ID_URI = "/%d/projects/%d/attachments/comment/count/%d";

    private final static String DOWNLOAD_ATTACHMENT_URI = "/{companyId}/projects/{projectId}/attachments/{attachmentId}/download";

    public AttachmentService(AppContext appContext) {
        super(appContext);
    }

    public List<Attachment> getAttachmentsByCompanyIdAndProjectId(int companyId, int projectId, int page) throws RestClientException {
        String uri = String.format(GET_ATTACHMENT_BY_PROJECTID_URI, companyId, projectId, page);

        return Arrays.asList(getForObjectWithCookie(uri, Attachment[].class));
    }

    public List<Attachment> getAttachmentsByCompanyByUser(int companyId, int userId, int page) throws RestClientException {
        String uri = String.format(GET_ATTACHMENT_BY_USERID_URI, companyId, userId, page);

        return Arrays.asList(getForObjectWithCookie(uri, Attachment[].class));
    }

    public Integer getAttachmentCommentCountsById(int companyId, int projectId, int attachmentId) throws RestClientException {
        String uri = String.format(GET_ATTACHMENT_COMMENT_COUNT_BY_ID_URI, companyId, projectId, attachmentId);

        return getForObjectWithCookie(uri, Integer.class);
    }

    public void downloadAttachment(int attachmentId, String attachmentName, int companyId, int projectId) {
        DownloadManager downloadManager = (DownloadManager) appContext.getSystemService(Context.DOWNLOAD_SERVICE);

        String uri = String.format(GET_ATTACHMENT_COMMENT_COUNT_BY_ID_URI, companyId, projectId, attachmentId);
        String url = super.getUrl(uri);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(attachmentName);
        request.setDestinationInExternalFilesDir(appContext, null, attachmentName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);
    }

}
