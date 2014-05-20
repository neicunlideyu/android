package cn.onboard.android.app.core.document;

import com.onboard.api.dto.Document;

import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.core.util.OnboardService;

/**
 * Created by XingLiang on 14-4-28.
 */
public class DocumentService extends OnboardService {

    private final static String GET_DOCUMENT_BY_PROJECT_URI = "/%d/projects/%d/documents";

    private final static String GET_DOCUMENT_BY_ID = "/%d/projects/%d/documents/%d";

    public DocumentService(AppContext appContext) {
        super(appContext);
    }
    /**
     * 获取一个项目的所有文档
     * @param companyId
     * @param projectId
     * @return
     * @throws RestClientException
     */
    public List<Document> getDocumentByProject(int companyId, int projectId) throws RestClientException {
        String uri = String.format(GET_DOCUMENT_BY_PROJECT_URI, companyId, projectId);

        return Arrays.asList(getForObjectWithCookie(uri, Document[].class));
    }

    /**
     * 根据id获取文章
     * @param companyId
     * @param projectId
     * @param documentId
     * @return
     * @throws RestClientException
     */
    public Document getDocumentById(int companyId, int projectId, int documentId) throws RestClientException {
        String uri = String.format(GET_DOCUMENT_BY_ID, companyId, projectId, documentId);

        return getForObjectWithCookie(uri, Document.class);
    }
}
