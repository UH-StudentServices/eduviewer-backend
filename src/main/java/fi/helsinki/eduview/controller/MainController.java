package fi.helsinki.eduview.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

/**
 * @author: hpr
 * @date: 04/01/2018
 */
@Controller
public class MainController {

    @Autowired
    private JsonService jsonService;

    @RequestMapping(value = "/ajax/educations", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getEducations() throws Exception {
        return jsonService.getEducations();
    }

    @RequestMapping(value = "/ajax/by_id/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getById(@PathVariable String id) throws Exception {
        return jsonService.getById(id);
    }

    @RequestMapping(value = "/ajax/tree/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getTree(@PathVariable String id) throws Exception {
        return jsonService.traverseTree(id);
    }
}
