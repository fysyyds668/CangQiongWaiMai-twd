package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品及口味
     * @param dishDTO
     */
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();

        BeanUtils.copyProperties(dishDTO,dish);

        //向菜品表插入一条数据
        dishMapper.insert(dish);

        //获取insert语句生成的主键值
        Long dishId = dish.getId();

        //向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null ||flavors.size()>0){
            flavors.forEach(dishFlavor ->{
                dishFlavor.setDishId(dishId);
            });
            //批量插入n条口味数据
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
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 菜品批量删除
     * @param ids
     */
    @Override
    public void deleteBatch(List<Long> ids) {

        //是否存在起售中的菜品，不能删除
        for (Long id : ids) {
            Dish dish=dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断是否有套餐中的菜品，不能删除
        List<Long> setmealId = setmealDishMapper.getSetmealIdByDishId(ids);
        if (setmealId!=null && setmealId.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

//        //删除菜品
//        for (Long id : ids) {
//            dishMapper.deleteById(id);
//            //删除口味
//            dishFlavorMapper.deleteByDishId(id);
//        }

        dishMapper.deleteByIds(ids);

        dishFlavorMapper.deleteByDishIds(ids);

    }

    /**
     * 根据菜品id查询菜品和对应的口味
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品
        Dish dish=dishMapper.getById(id);
        //根据菜品id查询口味数据
        List<DishFlavor> dishFlavors=dishFlavorMapper.getByDishId(id);

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        //修改菜品基本信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        //删除口味信息
        dishFlavorMapper.deleteByDishIds(Collections.singletonList(dishDTO.getId()));

        //插入口味信息
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null &&flavors.size()>0){
            flavors.forEach(dishFlavor ->{
                dishFlavor.setDishId(dishDTO.getId());
            });
            //批量插入n条口味数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品起售停售
     * @param status
     * @param id
     */
    @Override
    public void status(Integer status, Long id) {
        dishMapper.status(status,id);
    }

    @Override
    public List<Dish> list(Long categoryId) {
        List<Dish> list=dishMapper.list(categoryId);

        return list;
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list1(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}






















