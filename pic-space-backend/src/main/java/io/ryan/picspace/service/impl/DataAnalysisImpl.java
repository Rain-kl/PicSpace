package io.ryan.picspace.service.impl;

import groovy.util.logging.Slf4j;
import io.ryan.picspace.model.dto.data.DataOverview;
import io.ryan.picspace.service.DataAnalysis;
import io.ryan.picspace.service.PictureService;
import io.ryan.picspace.service.SpaceService;
import io.ryan.picspace.service.UserService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DataAnalysisImpl implements DataAnalysis {

    private final PictureService pictureService;
    private final UserService userService;
    private final SpaceService spaceService;

    public DataAnalysisImpl(PictureService pictureService, UserService userService, SpaceService spaceService) {
        this.pictureService = pictureService;
        this.userService = userService;
        this.spaceService = spaceService;
    }

    public DataOverview getDataOverview() {
        long pictureCount = pictureService.count();
        long userCount = userService.count();
        long spaceCount = spaceService.count();
        return new DataOverview(pictureCount, userCount, spaceCount);
    }
}
