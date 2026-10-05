package edu.fafu.service.impl.toolimpl;

import edu.fafu.config.SystemConfig;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.response.common.ImageResponse;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.toolservice.ImageService;
import edu.fafu.tool.oss.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ImageServiceImpl implements ImageService, BusinessExceptionInterface {

    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private OssUtil ossUtil;

    @Override
    public Result<List<ImageResponse>> getImages(List<String> paths) {
        ensureNotEmpty(paths, "路径列表不能为空");
        ensureTrue(paths.size() <= systemConfig.getImageBatchMax(), "单次最多查询" + systemConfig.getImageBatchMax() + "张图片");

        List<ImageResponse> result = new ArrayList<>();
        for (String path : paths) {
            if (path == null || path.isBlank()) continue;

            if (path.startsWith("http://") || path.startsWith("https://")) {
                result.add(new ImageResponse(path, path));
            } else {
                String url = ossUtil.getUrl(path);
                if (ossUtil.exists(path)) {
                    result.add(new ImageResponse(url, path));
                }
            }
        }

        return Result.success(result);
    }
}