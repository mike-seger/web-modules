package com.net128.oss.web.app.jpa.csv.testdata;

import com.net128.oss.web.lib.jpa.csv.JpaCsvController;
import com.net128.oss.web.lib.jpa.csv.JpaCsvControllerEntityChangeListener;
import com.net128.oss.web.lib.jpa.csv.util.EntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class JpaCsvControllerEntityChangeLogger implements JpaCsvControllerEntityChangeListener {
	private final EntityMapper entityMapper;
	public JpaCsvControllerEntityChangeLogger(JpaCsvController jpaCsvController, EntityMapper entityMapper) {
		jpaCsvController.addJpaCsvChangeListener(this);
		this.entityMapper = entityMapper;
	}

	@Override
	public void changed(List<String> entities) {
		entities.forEach(e -> {
			var clazz = entityMapper.getEntityClass(e);
			var repo = entityMapper.getEntityRepository(clazz);
			log.info("Entity {}: {}", e, repo.findAll().size());
		});
	}
}
