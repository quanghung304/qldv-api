package com.agribank.qldv_api.gateway;


import com.agribank.qldvutils.response.BaseResponse;
import com.agribank.qldvutils.response.DefaultListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "baseClient", url = "${qldv.database.url}", configuration = DatabaseFeignConfiguration.class)
public interface BaseClient<TEntity, TID> {

    @PostMapping("/save")
    BaseResponse<TEntity> save(@RequestBody TEntity entityObj);

    @PostMapping("/save-all")
    DefaultListResponse<TEntity> saveAll(@RequestBody List<TEntity> entityList);

    @GetMapping("/find/{id}")
    BaseResponse<Optional<TEntity>> findById( @PathVariable TID id);

    @GetMapping("/find-all")
    DefaultListResponse<TEntity> findAll();

    @GetMapping("/find-by-ids")
    DefaultListResponse<TEntity> findAllById(List<TID> ids);

    @DeleteMapping("/delete/{id}")
    BaseResponse<String> deleteById( @PathVariable("id") TID id);

    @DeleteMapping("/delete")
    BaseResponse<String> delete( @RequestBody TEntity entityObj);

    @DeleteMapping("/delete-by-ids")
    BaseResponse<String> deleteByIds(List<TID> ids);

    @DeleteMapping("/delete-all")
    BaseResponse<String> deleteAll( @RequestBody List<TEntity> entityList);
}
