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

    @RequestMapping(value = "/api/lvs/{degreeProgrammeCode}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String availbleLVByDPCode(@PathVariable String degreeProgrammeCode) throws Exception {
        return studyService.getAvailableLVsByDPCode(degreeProgrammeCode);
    }

    @RequestMapping(value = "/api/educations", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getEducations() throws Exception {
         return studyService.getEducations();
    }

    @RequestMapping(value = "/api/coded_educations", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getCodedEducations() throws Exception {
        return studyService.getEducationsWithDegreeProgrammeCodes();
    }

    @RequestMapping(value = "/api/by_id/{id}", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getStructureById(@PathVariable String id, @RequestParam(required = false) String lv) throws Exception {
        return studyService.getById(id, lv);
    }


    @RequestMapping(value = "/api/tree_by_code/{code}", produces = "application/json; charset=utf-8", method = RequestMethod.GET)
    @ResponseBody
    public String getTreeByCode(@PathVariable String code, @RequestParam(required = true) String lv) throws Exception {
        return studyService.getTreeByCode(code, lv);
    }

    @RequestMapping(value = "/api/tree/{id}", produces = "application/json; charset=utf-8", method = RequestMethod.GET)
    @ResponseBody
    public String getFullDataAsTree(@PathVariable String id, @RequestParam(required = true) String lv) throws Exception {
        return studyService.getTree(id, lv);
    }

    @RequestMapping(value = "/api/data_view/", produces = "text/plain; charset=utf-8")
    @ResponseBody
    public String dataView(@RequestParam(required = false) String salasana) {
        if(checkPassword(salasana)) {
            return studyService.getDataReport();
        }
        return "";
    }

    @RequestMapping(value = "/api/data_check/{lv}", produces = "text/plain; charset=utf-8")
    @ResponseBody
    public String dataCheck(@PathVariable String lv, @RequestParam(required = false) String salasana) throws Exception {
        if(checkPassword(salasana)) {
            return studyService.runDataCheckAsync(lv);
        }
        return "";
    }

    private boolean checkPassword(@RequestParam(required = false) String salasana) {
        return salasana != null && salasana.equals("monnitiskaamonnikuivaa");
    }

}
