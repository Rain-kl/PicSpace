package io.ryan.picspace.controller;

import cn.hutool.core.util.ArrayUtil;
import io.ryan.picspace.auth.annotation.AuthSysAdmin;
import io.ryan.picspace.common.BaseResponse;
import io.ryan.picspace.exception.ErrorCode;
import io.ryan.picspace.exception.ThrowUtils;
import io.ryan.picspace.model.dto.data.DataOverview;
import io.ryan.picspace.service.DataAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/dataAnalysis")
@AuthSysAdmin
public class DataAnalysisController {

    private final DataAnalysis dataAnalysis;

    public DataAnalysisController(DataAnalysis dataAnalysis) {
        this.dataAnalysis = dataAnalysis;
    }

    @GetMapping("/overview")
    public BaseResponse<DataOverview> getDataOverview() {
        DataOverview dataOverview = dataAnalysis.getDataOverview();
        return BaseResponse.Results.success(dataOverview);
    }

    @GetMapping("/overview")
    public BaseResponse<DataTrend> getDataTrend(Integer days) {
        Integer[] filter = {7, 30, 90, 180, 365};
        ThrowUtils.throwIf(!ArrayUtil.contains(filter, days), ErrorCode.PARAMS_ERROR, "days 参数错误");

    }
}
