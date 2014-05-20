package cn.onboard.android.app.core.comment;

import com.onboard.api.dto.Comment;

import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.core.util.OnboardService;

/**
 * Created by XingLiang on 14-4-25.
 */
public class CommentService extends OnboardService {

    private final static String COMMENTS_BY_COMMENTABLE_URI = "/%d/projects/%d/%s/%d/comments";

    public CommentService(AppContext appContext) {
        super(appContext);
    }

    public List<Comment> getCommentsByCommentable(int companyId, int projectId, String attachType, int attachId) throws RestClientException{
        String uri = String.format(COMMENTS_BY_COMMENTABLE_URI, companyId, projectId, attachType, attachId);

        return Arrays.asList(getForObjectWithCookie(uri, Comment[].class));
    }

    public Comment publishComment(Comment comment) {
        String uri = String.format(COMMENTS_BY_COMMENTABLE_URI, comment.getCompanyId(), comment.getProjectId(), comment.getAttachType(), comment.getAttachId());

        return postForObjectWithCookie(uri, comment, Comment.class);
    }
}
