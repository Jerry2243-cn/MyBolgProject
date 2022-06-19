package com.jerry.project.service;

import com.jerry.project.cache.CacheProvider;
import com.jerry.project.vo.Type;
import com.jerry.project.vo.Blog;
import com.jerry.project.NotFoundException;
import com.jerry.project.dao.TypeRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TypeServiceImpl implements TypeService{

    @Autowired
    private TypeRepository typeRepository;
    @Autowired
    private CacheProvider cacheProvider;

    @Transactional
    @Override
    public Type saveType(Type type) {
        flushCache();
        return typeRepository.save(type);
    }

    @Transactional
    @Override
    public Type getType(Long id) {
        return typeRepository.findById(id).get();
    }

    @Override
    public Type getTypeByName(String name) {
        return typeRepository.findByName(name);
    }

    @Transactional
    @Override
    public Page<Type> listType(Pageable pageable) {
        return typeRepository.findAll(pageable);
    }

    @Override
    public List<Type> listType() {
        return typeRepository.findAll();
    }

    @Override
    public List<Type> listTypeTop(Integer size) {
        if(cacheProvider.get("types") == null){
            Sort sort = Sort.by(Sort.Direction.DESC,"publishedCount");
            Pageable pageable = PageRequest.of(0,size,sort);
            List<Type> types =  typeRepository.findTop(pageable);
            cacheProvider.put("types" ,types);
        }
        return cacheProvider.get("types");
    }

    @Transactional
    @Override
    public Type updateType(Long id, Type type) {
        flushCache();
        Type t =typeRepository.findById(id).get();
        if(t == null){
            throw new NotFoundException("不存在该类型");
        }
        BeanUtils.copyProperties(type,t);
        return typeRepository.save(t);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        flushCache();
        typeRepository.deleteById(id);
    }

    @Override
    public void setPublishedCount() {
        for(Type t :typeRepository.findAll()){
            t.setPublishedCount(typeRepository.findPublishedBlogs(t.getId()));
            typeRepository.save(t);
        }
    }

    @Override
    public boolean noBlogs(Long id) {
        return typeRepository.hasBlogs(id).isEmpty();
    }

    private void flushCache(){
        cacheProvider.remove("types");
    }
}
