package fi.helsinki.eduview.service.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import fi.helsinki.eduview.postgres.PGDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: hpr
 * @date: 11/02/2019
 */
@Service
public class StudyDataPostgres implements StudyData {

    private static ObjectMapper mapper = new ObjectMapper();

    @Autowired private PGDao pgDao;

    public JsonNode getById(String id) throws Exception {
        String entity = pgDao.getById(id);
        return mapper.readTree(entity);
    }

    @Override
    public JsonNode find(String id) throws Exception {
        return mapper.readTree(pgDao.getById(id));
    }

    @Override
    public JsonNode getDegreeProgrammeNode(String code) throws IOException {
        return mapper.readTree(pgDao.getByCode(code));
    }

    @Override
    public JsonNode findByGroupId(String groupId) throws Exception {
        List<String> response = pgDao.findByGroupId(groupId);
        return createArray(response);
    }

    private ArrayNode createArray(List<String> response) throws IOException {
        if(response == null) {
            return null;
        }
        ArrayNode node = mapper.createArrayNode();
        for(String entity : response) {
            node.add(mapper.readTree(entity));
        }
        return node;
    }

    @Override
    public JsonNode findEducationById(String id) throws Exception {
        return mapper.readTree(pgDao.getById(id));
    }

    @Override
    public JsonNode findModulesById(String id) throws Exception {
        return mapper.readTree(pgDao.getById(id));
    }

    @Override
    public ArrayNode findModulesByGroupId(String groupId) throws IOException {
        return createArray(pgDao.findModulesByGroupId(groupId));
    }

    @Override
    public List<JsonNode> getEducations() throws IOException {
        List<JsonNode> nodes = new ArrayList<>();
        for(String entity : pgDao.getAllActiveEducations()) {
            nodes.add(mapper.readTree(entity));
        }
        return nodes;
    }
}
