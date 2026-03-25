package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetMealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class DishServceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetMealDishMapper setMealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    /**
     * 新增菜品和对应的口味
     * @param dishDTO
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
            //1. 保存菜品基本信息到菜品表 dish
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);


        long dishId = dish.getId(); //获取菜品id

            //2. 从dishDTO对象中获取口味信息，保存到菜品口味表 dish_flavor
        List<DishFlavor> flavors= dishDTO.getFlavors();
        if(flavors!=null&&flavors.size()>0){
            flavors.forEach(flavor -> {
                flavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 删除菜品和对应的口味
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {

          // 判断该菜品是否在售卖，如果在售卖，抛出业务异常
        ids.forEach(id -> {
           Dish dish = dishMapper.getById(id);
           if(dish.getStatus()== StatusConstant.ENABLE) {
               throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
           }
        });

            // 根据菜品id查询套餐id
        List<Long> setMealIds = setMealDishMapper.getSetMealIdsByDishIds(ids);
        if(setMealIds!=null&&setMealIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

//        ids.forEach(id -> {
//            //1. 删除菜品表中的数据
//            dishMapper.deleteById(id);
//            //2. 删除菜品口味表中的数据
//            dishFlavorMapper.deleteByDishId(id);
//        });

        // 批量删除
        dishMapper.deleteBatchByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
        return;
    }

    /**
     * 根据id查询菜品信息
     * @param id
     * @return
     */
    public DishVO getByIdWithFlavor(Long id) {
        // 根据id查询菜品基本信息
        Dish dish = dishMapper.getById(id);
        if(dish==null){

        }
        // 根据菜品id查询口味信息
        List<DishFlavor> flavor = dishFlavorMapper.getByDishId(id);

        // 封装成DishVO对象返回
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavor);
        return dishVO;
    }

    /**
     * 修改菜品信息和口味
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        //1. 修改菜品表中的数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        Long dishId = dish.getId();

        //2. 删除菜品口味表中的数据
        dishFlavorMapper.deleteByDishId(dishId);

        //3. 从dishDTO对象中获取口味信息，保存到菜品口味表 dish_flavor
        List<DishFlavor> flavors= dishDTO.getFlavors();
        if(flavors!=null&&flavors.size()>0){
            flavors.forEach(flavor -> {
                flavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 启用禁用菜品
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dishMapper.update(dish);

        // 根据菜品id查询包含该菜品的套餐id
        List<Long> setmealIds = setMealDishMapper.getSetMealIdsByDishIds(Arrays.asList(id));
        if (setmealIds != null && !setmealIds.isEmpty()) {
            // 更新套餐状态
            setmealIds.forEach(setmealId -> {
                Setmeal setmeal = new Setmeal();
                setmeal.setId(setmealId);
                setmeal.setStatus(status);
                setmealMapper.update(setmeal);
            });
        }
    }



    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        return dishMapper.list(dish);
    }


}
