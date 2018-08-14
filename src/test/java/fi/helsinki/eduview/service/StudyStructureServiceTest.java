package fi.helsinki.eduview.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author: hpr
 * @date: 03/07/2018
 */
@RunWith(MockitoJUnitRunner.class)
public class StudyStructureServiceTest {

    private Logger logger = Logger.getLogger(StudyStructureServiceTest.class);

    @InjectMocks
    private StudyStructureService service;

    @Mock private CourseService courseService;
    @Mock private Environment environment;

    ObjectMapper mapper = new ObjectMapper();

    @Before
    public void init() throws Exception {
        ObjectNode node = mapper.createObjectNode();
        node.set("name", new TextNode("testnode"));
        when(courseService.getCUNameById(any(), any())).thenReturn(node);
        when(environment.getProperty(eq("data-location"), any(String.class))).thenReturn("../data/");
        when(environment.getProperty(eq("course-units-dir"), any(String.class))).thenReturn("kori-course-units");
        when(environment.getProperty(eq("modules-dir"), any(String.class))).thenReturn("modules");
        when(environment.getProperty(eq("degree-programmes-dir"), any(String.class))).thenReturn("modules");
        when(environment.getProperty(eq("educations-dir"), any(String.class))).thenReturn("educations");

        service.init();
    }

    @Test
    public void testTreeEducation() throws Exception {
        logger.info(service.getTree("hy-EDU-114256075", "hy-lv-68"));
    }

    @Test
    public void testTreeModule() throws Exception {
        logger.info(service.getTree("hy-DP-114257414", "hy-lv-68"));
    }

}