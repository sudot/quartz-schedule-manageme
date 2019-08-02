package net.sudot.quartzschedulemanage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 主页控制器
 *
 * @author tangjialin on 2019-08-02.
 */
@Controller
@RequestMapping
public class IndexController {

    /**
     * 返回首页
     *
     * @return 返回首页
     */
    @GetMapping
    public String index() {
        return "index.html";
    }
}
