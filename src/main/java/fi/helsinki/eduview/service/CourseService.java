package fi.helsinki.eduview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Hannu-Pekka Rajaniemi (h-p@iki.fi)
 * @date: 14/01/2018
 */
@Service
public class CourseService extends AbstractService {

    private Logger logger = LogManager.getLogger(CourseService.class);

    private boolean init = false;
    private ObjectMapper mapper = new ObjectMapper();
    private List<JsonNode> cus = new ArrayList<>();


    @PostConstruct
    private void init() throws IOException {
//        if(init) {
//            return;
//        }
//        ObjectMapper mapper = new ObjectMapper();
//        List<JsonNode> root = cus;
//        String courseUnitsFileName = env.getProperty("course-units", "course-units");
//        for(File file : new File(env.getProperty("data-location", "backup/")).listFiles()) {
//            if(!file.getName().contains(courseUnitsFileName)) {
//                continue;
//            }
//            ArrayNode fileTree = (ArrayNode)mapper.readTree(Files.readAllBytes(file.toPath()));
//            for(JsonNode child : fileTree) {
//                root.add(child);
//            }
//        }
        init = true;
    }

    protected JsonNode find(String id) throws Exception {
        return openFile(id);
    }

    private JsonNode openFile(String id) throws IOException {
        File file = new File(env.getProperty("data-location") + env.getProperty("course-units-dir") + "/" + id + ".json");
        if(file.isFile()) {
            return mapper.readTree(Files.readAllBytes(file.toPath()));
        }
        return mapper.createArrayNode();
    }

    public JsonNode getCUNameById(String id, String lv) throws Exception {
        JsonNode root = openFile(id);
        return filterResults(id, root, lv);
    }
}
