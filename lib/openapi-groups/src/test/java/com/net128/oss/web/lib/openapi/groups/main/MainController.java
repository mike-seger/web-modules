package com.net128.oss.web.lib.openapi.groups.main;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(("/api"))
public class MainController {
	@GetMapping
	public String index() { return "main"; }
}
