package com.net128.oss.web.lib.jpa.csv;

import com.net128.oss.web.lib.jpa.csv.util.Attribute;
import com.net128.oss.web.lib.jpa.csv.util.JpaMapper;
import com.net128.oss.web.lib.jpa.csv.util.Props;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JpaService {
    private final JpaMapper jpaMapper;
    private final JpaCsvConfiguration jpaCsvConfiguration;

    public JpaService(JpaMapper jpaMapper, JpaCsvConfiguration jpaCsvConfiguration) {
        this.jpaMapper = jpaMapper;
        this.jpaCsvConfiguration = jpaCsvConfiguration;
    }

    public List<String> getEntities() {
        return jpaMapper.getEntities();
    }

    public Map<String, Attribute> getAttributes(String entity) {
        return jpaMapper.getAttributes(entity);
    }

    @SuppressWarnings("unchecked")
    public <T> int deleteIds(String entityName, List<Long> ids) {
        Class<T> entityClass = (Class<T>) jpaMapper.getEntityClass(entityName);
        JpaRepository<T, Long> jpaRepository =
                (JpaRepository<T, Long>) jpaMapper.getEntityRepository(entityClass);
        jpaRepository.deleteAllById(ids);
        return ids.size();
    }

    public JpaService.Configuration getConfiguration() {
        var configuration = new JpaService.Configuration();
        jpaMapper.getEntities().forEach(e -> {
            var idFieldName = jpaMapper.getIdFieldName(e);
           // .get(e.toLowerCase());
            var entity = jpaMapper.getEntityClass(e);
            var attributes = orderedAttributes(e);

            configuration.addEntity(
                entity.getName(), e,
                "/"+e+".csv",
                "?entity="+e,
                "?entity="+e+"&",
                attributes.get(idFieldName)!=null?idFieldName:null,
                Props.isSortable(entity),
                attributes
            );
        });
        return configuration;
    }

    private LinkedHashMap<String, Attribute> orderedAttributes(String entity) {
        var attributeOrderMap = jpaCsvConfiguration.getAttributeOrderOverrides();
        var attributeOrderOverrides0= attributeOrderMap==null?Collections.<String> emptyList():
            attributeOrderMap.get(entity.replace("_", "").toLowerCase());
        if(attributeOrderOverrides0==null) attributeOrderOverrides0 = Collections.emptyList();
        var attributeOrderOverrides = attributeOrderOverrides0.stream()
            .map(String::toLowerCase).collect(Collectors.toList());
        var attributeMap = jpaMapper.getAttributes(entity);
        var attributeOrders =
            attributeMap.values().stream().map(a -> a.getName().toLowerCase()).collect(Collectors.toList());

        Comparator<Attribute> attributeOrderComparator = (a1, a2) -> {
            var pos1 = attributeOrders.indexOf(a1.getName().toLowerCase());
            var pos2 = attributeOrders.indexOf(a2.getName().toLowerCase());
            if(pos1<0) return 1;
            if(pos2<0) return -1;
            return Integer.compare(pos1, pos2);
        };

        Comparator<Map.Entry<String, Attribute>> attributeOverrideComparator = (e1, e2) -> {
            var pos1 = attributeOrderOverrides.indexOf(e1.getKey().toLowerCase());
            var pos2 = attributeOrderOverrides.indexOf(e2.getKey().toLowerCase());
            if(pos1==pos2) return attributeOrderComparator.compare(e1.getValue(), e2.getValue());
            if(pos1<0) return 1;
            if(pos2<0) return -1;
            return Integer.compare(pos1, pos2);
        };

        return attributeMap.entrySet().stream().sorted(attributeOverrideComparator)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (x, y) -> y, LinkedHashMap::new));
    }

    @Data
    static class Configuration {
        TreeMap<String, Entity> entities = new TreeMap<>();
        @Data
        @AllArgsConstructor
        static class Entity{
            String id;
            String name;
            String getUri;
            String putUri;
            String deleteUri;
            String idField;
            boolean sortable;
            List<Attribute> attributes;
        }
        void addEntity(String id, String name, String getUri, String putUri, String deleteUri,
               String idField, boolean sortable, LinkedHashMap<String, Attribute> attributeMap) {
            entities.put(id, new Entity(id, name, getUri, putUri, deleteUri, idField, sortable, new ArrayList<>(attributeMap.values())));
        }
    }

}
