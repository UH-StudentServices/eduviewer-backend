package fi.helsinki.eduview.postgres;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: hpr
 * @date: 01/02/2019
 */

@Component
public class PGDao {

    private static Logger logger = LoggerFactory.getLogger(PGDao.class);

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<String> getAllActiveEducations() {
        long start = System.currentTimeMillis();
        List<String> ids = namedParameterJdbcTemplate.queryForList("select id from education where documentstate = 'ACTIVE'", new MapSqlParameterSource(), String.class);
        logger.info("active documentstate from education took " + (System.currentTimeMillis() - start));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ids", ids);
        start = System.currentTimeMillis();
        List<String> values = namedParameterJdbcTemplate.queryForList("select entity from sisu_exports where id in (:ids)", params, String.class);
        logger.info("allActiveEntities from ids took " + (System.currentTimeMillis() - start));
        return values;
    }

    public String getById(String id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        return namedParameterJdbcTemplate.queryForObject("select entity from sisu_exports where id = :id", params, String.class);
    }

    public void test() {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", "hy-opinto-103620132");
        String result = namedParameterJdbcTemplate.queryForObject("select entity from sisu_exports where id = :id", params, String.class);
        logger.info(result);
    }

    public String getByCode(String code) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("code", code);
        return namedParameterJdbcTemplate.queryForObject("select entity from sisu_exports where id = (select id from module where code = :code)", params, String.class);
    }

    public List<String> findByGroupId(String groupId) {
        List<String> ids = findEducationsByGroupId(groupId);
        if(ids != null && !ids.isEmpty()) {
            return ids;
        }
        return findModulesByGroupId(groupId);
    }

    public List<String> findEducationsByGroupId(String groupId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("groupid", groupId);
        return namedParameterJdbcTemplate.queryForList("select entity from sisu_exports where id in (select id from education where groupid = :groupid)", params, String.class);
    }

    public List<String> findModulesByGroupId(String groupId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("groupid", groupId);
        long start = System.currentTimeMillis();
        List<String> response = namedParameterJdbcTemplate.queryForList("select entity from sisu_exports where id in (select id from module where groupid = :groupid)", params, String.class);
        logger.info("findModulesByGroupId took " + (System.currentTimeMillis() - start));
        return response;
    }
}
