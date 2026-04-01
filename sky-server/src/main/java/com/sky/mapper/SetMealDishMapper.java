package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMealDishMapper {

    /**
     * 根据菜品id查询对应的套餐id
     * @param ids
     * @return
     */

    List<Long> getSetMealIdsByDishIds(List<Long> ids);

    /**
     * 批量插入套餐菜品关系
     * @param setmealDishes
     */
    @Insert("<script>" +
            "insert into setmeal_dish (setmeal_id, dish_id, name, price, copies) values " +
            "<foreach collection='setmealDishes' item='item' separator=','>" +
            "(#{item.setmealId}, #{item.dishId}, #{item.name}, #{item.price}, #{item.copies})" +
            "</foreach>" +
            "</script>")
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id批量删除套餐菜品关系
     * @param setmealIds
     */
    @Delete("<script>delete from setmeal_dish where setmeal_id in <foreach collection='setmealIds' item='id' separator=',' open='(' close=')'>#{id}</foreach></script>")
    void deleteBySetmealIds(List<Long> setmealIds);

    /**
     * 根据套餐id查询套餐菜品关系
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getBySetmealId(Long setmealId);
}
