package cn.onboard.android.app.core.Project;

import com.onboard.api.dto.Project;

import java.util.Arrays;
import java.util.List;

import cn.onboard.android.app.AppContext;
import cn.onboard.android.app.core.util.OnboardService;

/**
 * Created by XingLiang on 14-4-28.
 */
public class ProjectService extends OnboardService{

    private final static String GET_PROJECT_BY_COMPANY_ID_URI = "/%d";

    public ProjectService(AppContext appContext) {
        super(appContext);
    }
    /**
     * 通过companyID获取这个公司的所有项目
     * @param companyId
     * @return
     */
    public List<Project> getProjectByCompanyId(int companyId) {
        String uri = String.format(GET_PROJECT_BY_COMPANY_ID_URI, companyId);
        String url = super.getUrl(uri);

        return Arrays.asList(getForObjectWithCookie(url, Project[].class));
    }
}
