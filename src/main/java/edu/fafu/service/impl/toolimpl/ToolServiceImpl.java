package edu.fafu.service.impl.toolimpl;

import edu.fafu.config.SystemConfig;
import edu.fafu.database.entity.Result;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.toolservice.ToolService;
import edu.fafu.tool.image.ImageUtil;
import edu.fafu.tool.oss.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ToolServiceImpl implements ToolService, BusinessExceptionInterface {

    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private OssUtil ossUtil;

    @Override
    public Result<String> uploadImage(MultipartFile file, Integer userId) {
        ensureTrue(file != null && !file.isEmpty(), "文件不能为空");

        String fileName = System.currentTimeMillis() + "_" + String.format("%04d", (int) (Math.random() * 10000)) + ".webp";
        String ossKey = "goods/" + userId + "/" + fileName;

        try {
            String url = ImageUtil.saveImageToOss(systemConfig, ossUtil, file, ossKey);
            return Result.success(url);
        } catch (IllegalArgumentException e) {
            fail(e.getMessage());
            return null;
        } catch (Exception e) {
            fail("图片上传失败");
            return null;
        }
    }
}