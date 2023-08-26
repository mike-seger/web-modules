package otherpackage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(("/api/other"))
public class OtherController {
	@GetMapping
	public String index() { return "other"; }
}
