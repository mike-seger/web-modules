package com.net128.oss.web.lib.openapi.groups.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(("/api/admin"))
public class AdminController {
	@GetMapping
	public String index() { return "admin"; }
}
