package fi.helsinki.eduview.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * @author: hpr
 * @date: 04/01/2018
 */
@Controller
public class MainController {

    @Autowired
    private JsonService jsonService;


    @RequestMapping(value = "/api/educations", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getEducations() throws Exception {
        return jsonService.getEducations();
    }

    @RequestMapping(value = "/api/by_group_id/{groupId}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getStructureByBothIds(@PathVariable String groupId) throws Exception {
        return jsonService.getByGroupId(groupId);
    }

    @RequestMapping(value = "/api/by_id/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getStructureById(@PathVariable String id) throws Exception {
        return jsonService.getById(id);
    }

    @RequestMapping(value = "/api/all_ids", produces = "application/json; charset=utf-8", method = RequestMethod.POST)
    @ResponseBody
    public String getByAllIds(@RequestBody String ids) throws IOException {
        List<String> idList = parseIds(ids);
        return jsonService.getByAllIds(idList);
    }

    private List<String> parseIds(String ids) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<String> list = mapper.readValue(ids, TypeFactory.defaultInstance().constructCollectionType(List.class, String.class));
        return list;
    }

//    @RequestMapping(value = "/api/structure/tree/{id}", produces = "application/json; charset=utf-8")
//    @ResponseBody
//    public String getStructureTree(@PathVariable String id) throws Exception {
//        return jsonService.traverseStructureTree(id);
//        return null;
//    }

    @RequestMapping(value = "/api/by_id_nodes/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getRuleById(@PathVariable String id) throws Exception {
        return jsonService.getNodesById(id);
    }

    @RequestMapping(value = "/api/rule/tree/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getRuleTree(@PathVariable String id) throws Exception {
        return jsonService.traverseTree(id);
    }
}
