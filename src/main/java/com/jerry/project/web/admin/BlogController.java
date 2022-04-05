package com.jerry.project.web.admin;

import com.jerry.project.service.FileService;
import com.jerry.project.service.TagService;
import com.jerry.project.vo.BlogQuery;
import com.jerry.project.vo.User;
import com.jerry.project.service.BlogService;
import com.jerry.project.service.TypeService;
import com.jerry.project.vo.Blog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.*;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

@Controller
@RequestMapping("/admin")
public class BlogController {

    public static final String INPUT = "admin/blogs-input";
    public static final String LIST = "admin/blogs";
    public static final String REDIRECT_LIST = "redirect:/admin/blogs";


    @Autowired
    private BlogService blogService;
    @Autowired
    private TypeService typeService;
    @Autowired
    private TagService tagService;
    @Autowired
    private FileService fileService;

    @GetMapping("blogs")
    public String blogs(@PageableDefault(size = 10,sort ={"createDate"},direction = Sort.Direction.DESC) Pageable pageable,
                        BlogQuery bq, Model model){
        model.addAttribute("types", typeService.listType());
        model.addAttribute("page",blogService.listBlog(pageable,bq));
        return LIST;
    }

    @PostMapping("blogs/search")
    public String search(@PageableDefault(size = 10,sort ={"createDate"},direction = Sort.Direction.DESC) Pageable pageable,
                        BlogQuery bq, Model model){
        model.addAttribute("page",blogService.listBlog(pageable,bq));
        return "admin/blogs :: blogList";
    }

    @GetMapping("blogs/{id}/preview")
    public String preview(@PathVariable Long id,Model model){
        model.addAttribute("blog",blogService.getAndPreView(id));
        return "blog";
    }

    @GetMapping("/blogs/input")
    public String input(Model model){
        model.addAttribute("types", typeService.listType());
        model.addAttribute("tags", tagService.ListTag());
        model.addAttribute("blog",new Blog());
        return INPUT;
    }

    @GetMapping("blogs/{id}/publish")
    public String changePublishState(@PathVariable Long id,RedirectAttributes attributes) {
        blogService.changePublishState(id);
        attributes.addFlashAttribute("message", "操作成功");
        return REDIRECT_LIST;
    }

    @GetMapping("/blogs/{id}/input")
    public String editInput(@PathVariable Long id,Model model){
        model.addAttribute("types", typeService.listType());
        model.addAttribute("tags", tagService.ListTag());
        Blog blog = blogService.getBlog(id);
        blog.init();
        model.addAttribute("blog",blog);
        return INPUT;
    }

    @PostMapping("/blogs")
    public String post(@RequestParam(value = "file",required = false) MultipartFile file, Blog blog, RedirectAttributes attributes, HttpSession session){
        blog.setUser((User)session.getAttribute("user"));
        blog.setType(typeService.getType(blog.getType().getId()));
        blog.setTags(tagService.ListTag(blog.getTagIds()));
        Blog b;
        if(blog.getId()==null){
            if(!file.isEmpty())
                blog.setFirstPicture( fileService.saveFile(file));
            b = blogService.saveBlog(blog);
        }else{
            if(!file.isEmpty())
                blog.setFirstPicture(fileService.saveFile(file,blogService.getBlog(blog.getId()).getFirstPicture()));
            b = blogService.updateBlog(blog.getId(),blog);
        }
        if (b == null){
            attributes.addFlashAttribute("message","操作失败");
        }else{
            attributes.addFlashAttribute("message","操作成功");
        }
        return REDIRECT_LIST;
    }

    @GetMapping("/blogs/{id}/delete")
    public String delete(@PathVariable Long id,RedirectAttributes attributes) {
        blogService.deleteBlog(id);
        attributes.addFlashAttribute("message", "删除成功");
        return REDIRECT_LIST;
    }

}
