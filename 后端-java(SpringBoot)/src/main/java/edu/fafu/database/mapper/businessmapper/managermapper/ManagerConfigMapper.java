package edu.fafu.database.mapper.businessmapper.managermapper;

import edu.fafu.database.entity.VipConfig;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ManagerConfigMapper extends BaseMapper<VipConfig> {

    List<VipConfig> selectAll();

    VipConfig selectByLevel(@Param("level") int level);

    int update(@Param("entity") VipConfig entity);
}