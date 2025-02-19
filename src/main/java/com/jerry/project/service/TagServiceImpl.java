package com.jerry.project.service;

import com.jerry.project.cache.CacheProvider;
import com.jerry.project.vo.Tag;
import com.jerry.project.NotFoundException;
import com.jerry.project.vo.Blog;
import com.jerry.project.dao.TagRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TagServiceImpl implements TagService{

    @Autowired
    private TagRepository tagRepository;
//    @Autowired
//    private CacheProvider cacheProvider;

    @Override
    public Tag saveTag(Tag tag) {
//        flushCache();
        return tagRepository.save(tag);
    }

    @Override
    public Tag getTag(Long id) {
        return tagRepository.findById(id).get();
    }

    @Override
    public Tag getTagByName(String name) {
        return tagRepository.findByName(name);
    }

    @Override
    public Page<Tag> listTag(Pageable pageable) {
        return tagRepository.findAll(pageable);
    }

    @Override
    public List<Tag> ListTag() {
        return tagRepository.findAll();
    }

    @Override
    public List<Tag> ListTag(String ids) {
        return tagRepository.findAllById(convertToList(ids));
    }

    @Override
    public List<Tag> ListTagTop(Integer size) {
            Sort sort = Sort.by(Sort.Direction.DESC,"publishedCount");
            Pageable pageable = PageRequest.of(0,size,sort);
            return tagRepository.findTop(pageable);
    }

    private List<Long> convertToList(String ids) {
        List<Long> list = new ArrayList<>();
        if (!"".equals(ids) && ids != null) {
            String[] idarray = ids.split(",");
            for (int i=0; i < idarray.length;i++) {
                list.add(new Long(idarray[i]));
            }
        }
        return list;
    }

    @Override
    public Tag updateTag(Long id, Tag tag) {
//        flushCache();
        Tag t =tagRepository.findById(id).get();
        if(t == null){
            throw new NotFoundException("不存在该类型");
        }
        BeanUtils.copyProperties(tag,t);
        return tagRepository.save(t);
    }

    @Override
    public void delete(Long id) {
//        flushCache();
        tagRepository.deleteById(id);
    }

    @Override
    public void setPublishedCount(Long id) {
//        flushCache();
        List<Tag> tags = tagRepository.findTagsByBlog(id);
        for(Tag t :tags){
            t.setPublishedCount(tagRepository.findPublishedBlogsCount(t.getId()));
            tagRepository.save(t);
        }
    }

    @Override
    public int hasBlogs(Long id) {
        return tagRepository.hasBlogs(id).size();
    }

//    private void flushCache(){
//        cacheProvider.remove("tags");
//    }
}
