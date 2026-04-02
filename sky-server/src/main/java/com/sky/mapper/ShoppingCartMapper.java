package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 动态条件查询
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 更新购物车中菜品或套餐的数量
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 插入购物车数据
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (user_id, dish_id, setmeal_id, name, image, amount, number) " +
            "values (#{userId}, #{dishId}, #{setmealId}, #{name}, #{image}, #{amount}, #{number})")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据用户id删除购物车数据
     * @param userId
     * @return
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

    /**
     * 删除购物车内某种商品
     * @param shoppingCart
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void subShoppingCart(ShoppingCart shoppingCart);
}
