package fi.helsinki.eduview.service.course;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import fi.helsinki.eduview.postgres.PGDao;
import fi.helsinki.eduview.service.AbstractDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: hpr
 * @date: 05/03/2019
 */
@Service
public class CourseDataPostgres extends AbstractDataService implements CourseData {

    private static Logger logger = LoggerFactory.getLogger(CourseDataPostgres.class);
    private static ObjectMapper mapper = new ObjectMapper();

    @Autowired private PGDao pgDao;

    @Override
    public JsonNode find(String id) throws Exception {
        return mapper.readTree(pgDao.getById(id));
    }

    @Override
    public JsonNode getCourseUnitByGroupId(String groupId, String lv) throws Exception {
        List<String> courseUnits = pgDao.findCourseUnitsByGroupId(groupId);
        ArrayNode root = mapper.createArrayNode();
        for(String cu : courseUnits) {
            root.add(mapper.readTree(cu));
        }
        return filterResults(groupId, root, lv);
    }
}
