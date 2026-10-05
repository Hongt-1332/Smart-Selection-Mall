package edu.fafu.service.service.toolservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.response.common.ImageResponse;

import java.util.List;

public interface ImageService {

    Result<List<ImageResponse>> getImages(List<String> paths);
}