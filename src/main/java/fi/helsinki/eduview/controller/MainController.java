package fi.helsinki.eduview.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import fi.helsinki.eduview.service.CourseService;
import fi.helsinki.eduview.service.StudyStructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.util.List;

/**
 * @author: hpr
 * @date: 04/01/2018
 */
@Controller
public class MainController {

    @Autowired
    private StudyStructureService studyService;

    @Autowired
    private CourseService courseService;

    @RequestMapping(value = "/api/update_lv/{lv}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String updateLv(@PathVariable String lv) {
        RequestContextHolder.getRequestAttributes().setAttribute("lv", lv, RequestAttributes.SCOPE_SESSION);
        return "ok";
    }

    @RequestMapping(value = "/api/available_lvs", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String availbleLV() throws IOException {
        return studyService.getAvailableLVs();
    }

    @RequestMapping(value = "/api/educations", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getEducations() throws Exception {
        return studyService.getEducations();
    }

    @RequestMapping(value = "/api/by_group_id/{groupId}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getStructureByBothIds(@PathVariable String groupId) throws Exception {
        return studyService.getByGroupId(groupId);
    }

    @RequestMapping(value = "/api/by_id/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getStructureById(@PathVariable String id) throws Exception {
        return studyService.getById(id);
    }

    @RequestMapping(value = "/api/all_ids", produces = "application/json; charset=utf-8", method = RequestMethod.POST)
    @ResponseBody
    public String getByAllIds(@RequestBody String ids) throws IOException {
        List<String> idList = parseIds(ids);
        return studyService.getByAllIds(idList);
    }

    @RequestMapping(value = "/api/cu/names", produces = "application/json; charset=utf-8", method = RequestMethod.POST)
    @ResponseBody
    public String getCUNames(@RequestBody String ids) throws IOException {
        List<String> idList = parseIds(ids);
        return courseService.getCUNamesByIds(idList);
    }

    private List<String> parseIds(String ids) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<String> list = mapper.readValue(ids, TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
        return list;
    }

//    @RequestMapping(value = "/api/structure/tree/{id}", produces = "application/json; charset=utf-8")
//    @ResponseBody
//    public String getStructureTree(@PathVariable String id) throws Exception {
//        return studyService.traverseStructureTree(id);
//        return null;
//    }

    @RequestMapping(value = "/api/by_id_nodes/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getRuleById(@PathVariable String id) throws Exception {
        return studyService.getNodesById(id);
    }

    @RequestMapping(value = "/api/rule/tree/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getRuleTree(@PathVariable String id) throws Exception {
        return studyService.traverseTree(id);
    }
}
