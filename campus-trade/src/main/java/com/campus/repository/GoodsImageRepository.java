package com.campus.repository;
import com.campus.entity.GoodsImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
public interface GoodsImageRepository extends JpaRepository<GoodsImage, Long> {
    List<GoodsImage> findByGoodsIdOrderBySortOrderAsc(Long goodsId);
    @Modifying @Transactional
    void deleteByGoodsId(Long goodsId);
}
