package edu.fafu.database.mapper.businessmapper.vipmapper;

import edu.fafu.database.entity.VipConfig;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VipConfigMapper extends BaseMapper<VipConfig> {

    List<VipConfig> selectAll();

    VipConfig selectByLevel(@Param("level") int level);
}