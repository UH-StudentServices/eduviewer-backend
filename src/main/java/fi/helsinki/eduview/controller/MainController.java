package fi.helsinki.eduview.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import fi.helsinki.eduview.service.CourseService;
import fi.helsinki.eduview.service.StudyStructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author: Hannu-Pekka Rajaniemi (h-p@iki.fi)
 * @date: 04/01/2018
 */
@Controller
public class MainController {

    @Autowired
    private StudyStructureService studyService;

    @Autowired
    private CourseService courseService;

    @RequestMapping(value = "/api/lv_names", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getLvNames() throws Exception {
        return studyService.getLvNames();
    }

    @RequestMapping(value = "/api/available_lvs/{educationId}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String availbleLV(@PathVariable String educationId) throws Exception {
        return studyService.getAvailableLVs(educationId);
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
    public String getStructureById(@PathVariable String id, @RequestParam(required = false) String lv) throws Exception {
        return studyService.getById(id, lv);
    }

    @RequestMapping(value = "/api/all_ids", produces = "application/json; charset=utf-8", method = RequestMethod.POST)
    @ResponseBody
    public String getByAllIds(@RequestBody String ids, @RequestParam(required = false) String lv) throws Exception {
        List<String> idList = parseIds(ids);
        return studyService.getByAllIds(idList, lv);
    }

    @RequestMapping(value = "/api/cu/names", produces = "application/json; charset=utf-8", method = RequestMethod.POST)
    @ResponseBody
    public String getCUNames(@RequestBody String ids, @RequestParam(required = false) String lv) throws IOException {
        List<String> idList = parseIds(ids);
        return courseService.getCUNamesByIds(idList, lv);
    }

    private List<String> parseIds(String ids) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<String> list = mapper.readValue(ids, TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
        return list;
    }

    // this is the preferred way of doing things, it will return a json tree of all the nodes under id & lv combination
    // where dataNode contains the next nodes in rule context
    @RequestMapping(value = "/api/tree/{id}", produces = "application/json; charset=utf-8", method = RequestMethod.GET)
    @ResponseBody
    public String getFullDataAsTree(@PathVariable String id, @RequestParam(required = true) String lv) throws Exception {
        return studyService.getTree(id, lv);
    }

    @RequestMapping(value = "/api/by_id_nodes/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getRuleById(@PathVariable String id) throws Exception {
        return studyService.getNodesById(id);
    }

    @RequestMapping(value = "/api/rule/tree/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getRuleTree(@PathVariable String id) throws Exception {
        return studyService.traverseTree(id, true);
    }
}
