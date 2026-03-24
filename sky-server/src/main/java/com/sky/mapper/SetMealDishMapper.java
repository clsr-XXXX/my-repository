package com.sky.mapper;

import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    /**
     * 根据菜品id查询对应的套餐id
     * @param ids
     * @return
     */

    List<Long> getSetMealIdsByDishIds(List<Long> ids);
}
