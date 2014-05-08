package cn.onboard.android.app.core.discussion;

import com.onboard.api.dto.Discussion;
import com.onboard.api.dto.Topic;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.core.util.OnboardService;

/**
 * Created by XingLiang on 14-4-24.
 */
public class DiscussionService extends OnboardService {

    private final static String CREATE_DISCUSSION_URI = "/%d/projects/%d/discussions";

    private final static String GET_DISCUSSION_BY_ID_URI = "/%d/projects/%d/discussions/%d";

    private final static String GET_TOPIC_BY_PROJECT_URI = "/%d/projects/%d/topics?page=%d";

    public Discussion createDiscussion(Discussion discussion, AppContext appContext) throws RestClientException{
        String uri = String.format(CREATE_DISCUSSION_URI, discussion.getCompanyId(), discussion.getProjectId());
        String url = super.getUrl(uri);
        HttpEntity<Discussion> httpEntity = new HttpEntity(discussion, super.getHeaderWithCookie(appContext));

        return restTemplate.exchange(url, HttpMethod.POST, httpEntity, Discussion.class).getBody();
    }

    public Discussion getDiscussionById(int companyId, int projectId, int discussionId) throws RestClientException {
        String uri = String.format(GET_DISCUSSION_BY_ID_URI, companyId, projectId, discussionId);
        String url = super.getUrl(uri);

        return restTemplate.getForObject(url, Discussion.class);
    }

    public List<Topic> getTopicsByProjectId(int companyId, int projectId, int page) throws RestClientException {
        String uri = String.format(GET_TOPIC_BY_PROJECT_URI, companyId, projectId, page);
        String url = super.getUrl(uri);

        return Arrays.asList(restTemplate.getForObject(url, Topic[].class));
    }

}
