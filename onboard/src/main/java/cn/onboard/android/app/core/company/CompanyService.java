package cn.onboard.android.app.core.company;

import com.onboard.api.dto.Company;

import java.util.Arrays;
import java.util.List;

import cn.onboard.android.app.core.util.OnboardService;

/**
 * Created by XingLiang on 14-4-28.
 */
public class CompanyService extends OnboardService {

    /**
     * 获得公司列表
     *
     * @return
     * @throws cn.onboard.android.app.AppException
     */
    public List<Company> getCompanyList() {
        String url = super.BASE_URL;

        return Arrays.asList(restTemplate.getForObject(url, Company[].class));
    }

}
