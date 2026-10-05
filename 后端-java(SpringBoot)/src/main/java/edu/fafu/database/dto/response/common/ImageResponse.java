package edu.fafu.database.dto.response.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageResponse {
    private String imageUrl;
    private String imagePath;
}