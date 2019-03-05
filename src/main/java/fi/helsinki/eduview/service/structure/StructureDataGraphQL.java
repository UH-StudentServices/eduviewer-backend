package fi.helsinki.eduview.service.structure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.List;

/**
 * @author: hpr
 * @date: 22/02/2019
 */

public class StructureDataGraphQL implements StructureData {


    @Override
    public JsonNode getById(String id) throws Exception {
        return null;
    }

    @Override
    public JsonNode find(String id) throws Exception {
        return null;
    }

    @Override
    public JsonNode getDegreeProgrammeNode(String code) throws IOException {
        return null;
    }

    @Override
    public JsonNode findByGroupId(String groupId) throws Exception {
        return null;
    }

    @Override
    public JsonNode findEducationById(String id) throws Exception {
        return null;
    }

    @Override
    public JsonNode findModulesById(String id) throws Exception {
        return null;
    }

    @Override
    public ArrayNode findModulesByGroupId(String groupId) throws IOException {
        return null;
    }

    @Override
    public List<JsonNode> getEducations() throws IOException {
        return null;
    }
}
