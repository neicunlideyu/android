package cn.onboard.android.app.core.upload;

import com.onboard.api.dto.Upload;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import java.io.File;

import cn.onboard.android.app.core.util.OnboardService;

/**
 * Created by XingLiang on 14-4-23.
 */
public class UploadService extends OnboardService {

    private final static String GET_UPLOAD_BY_ID_URI = "/%d/projects/%d/uploads/%d";

    private final static String CREATE_UPLOAD_URI = "/%d/projects/%d/uploads";

    public Upload getUploadById(int companyId, int projectId, int uploadId) throws RestClientException{
        String uri = String.format(GET_UPLOAD_BY_ID_URI, companyId, projectId, uploadId);
        String url = super.getUrl(uri);

        return restTemplate.getForObject(url, Upload.class);
    }

    public Upload createUpload(Upload upload, File file, int companyId, int projectId) throws RestClientException{
        String uri = String.format(CREATE_UPLOAD_URI, companyId, projectId);
        String url = super.getUrl(uri);

        return restTemplate.postForObject(url, getUploadRequestForm(upload, file), Upload.class);
    }

    private HttpHeaders getUploadRequestHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }

    private MultiValueMap<String, Object> getUploadRequestForm(Upload upload, File file) {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<String, Object>();
        formData.add("upload", upload);
        formData.add("file", file);

        return formData;
    }



}
